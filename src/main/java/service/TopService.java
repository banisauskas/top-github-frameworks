package service;

import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import domain.BackRepository;
import domain.BackResponse;
import domain.FrontRepository;

public class TopService {

	/** Thread-safe */
	@Autowired
	private RestTemplate template;

	@Autowired
	private CommonService commonService;

	public FrontRepository[] processRepositories(String authorization) {
		HttpEntity<Void> httpEntity = null;
		if (authorization != null) {
			httpEntity = commonService.createHttpEntity(authorization);
		}

		ResponseEntity<BackResponse> backResponseEntity =
			template.exchange("https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10", GET, httpEntity, BackResponse.class);

		BackResponse backResponse = backResponseEntity.getBody();

		// TODO backResponse.getIncompleteResults()

		BackRepository[] backRepos = backResponse.getItems();
		FrontRepository[] frontRepos = new FrontRepository[backRepos.length];

		for (int i = 0; i < backRepos.length; i++) {
			frontRepos[i] = processRepository(backRepos[i], httpEntity);
		}

		return frontRepos;
	}

	private FrontRepository processRepository(BackRepository backRepo, HttpEntity<Void> httpEntity) {
		FrontRepository frontRepo = new FrontRepository();
		String repoOwner = backRepo.getOwnerLogin();
		String repoName = backRepo.getName();

		frontRepo.setName(backRepo.getName());

		frontRepo.setDescription(backRepo.getDescription());

		frontRepo.setStars(backRepo.getStargazersCount());

		frontRepo.setLicenseName(backRepo.getLicenseName());

		frontRepo.setLink(backRepo.getHtmlUrl());

		int contributors = retrieveContributors(repoOwner, repoName, httpEntity);
		frontRepo.setContributors(contributors);

		if (httpEntity != null) {
			boolean starred = determineIfStarredByMe(repoOwner, repoName, httpEntity);
			frontRepo.setStarredByMe(starred);
		}

		return frontRepo;
	}

	private int retrieveContributors(String owner, String repository, HttpEntity<Void> httpEntity) {
		ResponseEntity<Void> backResponseEntity =
			template.exchange("https://api.github.com/repos/{owner}/{repository}/contributors?per_page=1&anon=true", HEAD, httpEntity, Void.class, owner, repository);

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

	private boolean determineIfStarredByMe(String owner, String repository, HttpEntity<Void> httpEntity) {
		boolean starred = true;

		try {
			ResponseEntity<Void> response =
				template.exchange("https://api.github.com/user/starred/{owner}/{repository}", GET, httpEntity, Void.class, owner, repository);

			if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
				throw new RuntimeException("Incorrect starred response status code");
			}
		}
		catch (HttpClientErrorException e) {
			HttpStatus status = e.getStatusCode();

			if (status == HttpStatus.NOT_FOUND) {
				starred = false;
			}
			else {
				throw e;
			}
		}

		return starred;
	}
}