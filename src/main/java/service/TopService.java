package service;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import domain.BackRepository;
import domain.BackResponse;
import domain.FrontRepository;

@Service
public class TopService {

	/** Thread-safe */
	@Autowired
	private RestTemplate template;

	@Autowired
	private CommonService commonService;

	public ResponseEntity<FrontRepository[]> processRepositories(String orderByContribs, String authorization) {
		// Order by
		int orderBy; // -1, 0, +1
		if (orderByContribs == null) {
			orderBy = 0;
		}
		else if (orderByContribs.equals("asc")) {
			orderBy = 1;
		}
		else if (orderByContribs.equals("desc")) {
			orderBy = -1;
		}
		else {
			return new ResponseEntity<>(BAD_REQUEST);
		}

		// Http entity
		HttpEntity<Void> httpEntity = null;
		if (authorization != null) {
			httpEntity = commonService.createHttpEntity(authorization);
		}

		// Retrieve repos from GitHub
		BackResponse backResponse;

		try {
			ResponseEntity<BackResponse> backResponseEntity =
				template.exchange("https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10", GET, httpEntity, BackResponse.class);

			backResponse = backResponseEntity.getBody();
		}
		catch (HttpClientErrorException e) {
			if (e.getStatusCode() == UNAUTHORIZED) {
				return new ResponseEntity<>(UNAUTHORIZED);
			}
			else {
				throw e;
			}
		}

		if (backResponse.getIncompleteResults()) {
			return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
		}

		// Fill additional fields to each retrieved repo
		BackRepository[] backRepos = backResponse.getItems();
		FrontRepository[] frontRepos = new FrontRepository[backRepos.length];

		for (int i = 0; i < backRepos.length; i++) {
			frontRepos[i] = processRepository(backRepos[i], httpEntity);
		}

		// Order by contributors
		if (orderBy == 1) {
			Arrays.sort(frontRepos);
		}
		else if (orderBy == -1) {
			Arrays.sort(frontRepos, Collections.reverseOrder());
		}

		// Success response
		return new ResponseEntity<>(frontRepos, OK);
	}

	private FrontRepository processRepository(BackRepository backRepo, HttpEntity<Void> httpEntity) {
		FrontRepository frontRepo = new FrontRepository();
		String repoOwner = backRepo.getOwnerLogin();
		String repoName = backRepo.getName();

		// Name
		frontRepo.setName(backRepo.getName());

		// Description
		frontRepo.setDescription(backRepo.getDescription());

		// Stars
		frontRepo.setStars(backRepo.getStargazersCount());

		// License name
		frontRepo.setLicenseName(backRepo.getLicenseName());

		// Link
		frontRepo.setLink(backRepo.getHtmlUrl());

		// Contributors
		int contributors = retrieveContributors(repoOwner, repoName, httpEntity);
		frontRepo.setContributors(contributors);

		// Starred by me
		if (httpEntity != null) {
			boolean starred = determineIfStarredByMe(repoOwner, repoName, httpEntity);
			frontRepo.setStarredByMe(starred);
		}

		return frontRepo;
	}

	/** GitHub returns 200 when the 1st page contains 1 result, or 204 when 0 results */
	private int retrieveContributors(String owner, String repository, HttpEntity<Void> httpEntity) {
		ResponseEntity<Void> backResponseEntity =
			template.exchange("https://api.github.com/repos/{owner}/{repository}/contributors?per_page=1&anon=true", HEAD, httpEntity, Void.class, owner, repository);

		if (backResponseEntity.getStatusCode() == NO_CONTENT) {
			return 0; // the 1st page is empty, so there are 0 contributors
		}

		List<String> links = backResponseEntity.getHeaders().get(HttpHeaders.LINK);
		if (links == null) {
			return 1; // 'Link' header is absent when there is only 1 page
		}

		if (links.size() != 1) {
			throw new RuntimeException("Incorrect Link header");
		}

		String link = links.get(0); // e.g. <https://api.github.com/repositories/82077268/contributors?per_page=1&anon=true&page=2>; rel="next", <https://api.github.com/repositories/82077268/contributors?per_page=1&anon=true&page=14>; rel="last"

		int start = link.lastIndexOf("page=");
		if (start == -1) {
			throw new RuntimeException("Incorrect Link header");
		}

		int end = link.indexOf('>', start);
		if (end == -1) {
			throw new RuntimeException("Incorrect Link header");
		}

		return Integer.parseInt(link.substring(start + 5, end));
	}

	/** GitHub returns 204 when starred, or 404 when not starred */
	private boolean determineIfStarredByMe(String owner, String repository, HttpEntity<Void> httpEntity) {
		try {
			ResponseEntity<Void> response =
				template.exchange("https://api.github.com/user/starred/{owner}/{repository}", GET, httpEntity, Void.class, owner, repository);

			return response.getStatusCode() == NO_CONTENT;
		}
		catch (HttpClientErrorException e) {
			if (e.getStatusCode() == NOT_FOUND) {
				return false;
			}
			else {
				throw e;
			}
		}
	}
}