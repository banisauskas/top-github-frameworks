package service;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import domain.BackRepository;
import domain.BackResponse;
import domain.FrontRepository;

@RestController
public class Controller {

	@RequestMapping(method=RequestMethod.GET, path="/top")
	public FrontRepository[] top(
		@RequestParam(name="username", required=false) String username,
		@RequestParam(name="password", required=false) String password) {

		RestTemplate template = new RestTemplate();
		boolean authenticated = (username != null) && (password != null);

		if (authenticated) {
			template.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));
		}

		ResponseEntity<BackResponse> backResponseEntity =
			template.getForEntity("https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10", BackResponse.class);

		return processRepositories(backResponseEntity, authenticated, template);
	}

	private FrontRepository[] processRepositories(ResponseEntity<BackResponse> backResponseEntity, boolean authenticated, RestTemplate template) {
		BackResponse backResponse = backResponseEntity.getBody();

		// TODO
		// backResponseEntity.getStatusCodeValue()
		// backResponseEntity.getHeaders().getContentType()
		// backResponse.getIncompleteResults()

		BackRepository[] backRepos = backResponse.getItems();
		FrontRepository[] frontRepos = new FrontRepository[backRepos.length];

		for (int i = 0; i < backRepos.length; i++) {
			frontRepos[i] = processRepository(backRepos[i], authenticated, template);
		}

		return frontRepos;
	}

	private FrontRepository processRepository(BackRepository backRepo, boolean authenticated, RestTemplate template) {
		FrontRepository frontRepo = new FrontRepository();

		frontRepo.setName(backRepo.getName());

		frontRepo.setDescription(backRepo.getDescription());

		frontRepo.setStars(backRepo.getStargazersCount());

		frontRepo.setLicenseName(backRepo.getLicenseName());

		frontRepo.setLink(backRepo.getHtmlUrl());

		int contributors = retrieveContributors(backRepo, template);
		frontRepo.setContributors(contributors);

		if (authenticated) {
			boolean starred = determineIfStarredByMe(backRepo, template);
			frontRepo.setStarredByMe(starred);
		}

		return frontRepo;
	}

	private int retrieveContributors(BackRepository repository, RestTemplate template) {
		HttpHeaders headers =
			template.headForHeaders("https://api.github.com/repos/{owner}/{repo}/contributors?per_page=1&anon=true", repository.getOwnerLogin(), repository.getName());

		List<String> links = headers.get(HttpHeaders.LINK);
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

	private boolean determineIfStarredByMe(BackRepository repository, RestTemplate template) {
		boolean starred = true;

		try {
			// returns status 204 (starred) or 404 (not starred)
			ResponseEntity<String> response =
				template.getForEntity("https://api.github.com/user/starred/{owner}/{repo}", String.class, repository.getOwnerLogin(), repository.getName());

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