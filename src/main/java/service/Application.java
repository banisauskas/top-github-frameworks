package service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import domain.Repository;
import domain.Response;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate template) throws Exception {
		return args -> {
			boolean authenticated = true; // change, recompile, rerun

			if (authenticated) {
				template.getInterceptors().add(new BasicAuthorizationInterceptor("username", "password"));
			}

			ResponseEntity<Response> repositories =
				template.getForEntity("https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10", Response.class);

			printRepositories(repositories, authenticated, template);
		};
	}

	private void printRepositories(ResponseEntity<Response> responseEntity, boolean authenticated, RestTemplate template) {
		Response response = responseEntity.getBody();

		System.out.println("STATUS: " + responseEntity.getStatusCodeValue());
		System.out.println("CONTENT-TYPE: " + responseEntity.getHeaders().getContentType());
		System.out.println("INCOMPLETE: " + response.getIncompleteResults());
		System.out.println("LENGTH: " + response.getItems().length);
		System.out.println("----------------");

		Repository[] repositories = response.getItems();

		for (Repository repository : repositories) {
			printRepository(repository, authenticated, template);
		}

		System.out.println();
	}

	private void printRepository(Repository repository, boolean authenticated, RestTemplate template) {
		System.out.println("NAME: " + repository.getName());
		System.out.println("DESCRIPTION: " + repository.getDescription());
		System.out.println("STARS: " + repository.getStargazersCount());
		System.out.println("LICENSE: " + repository.getLicenseName());
		System.out.println("URL: " + repository.getHtmlUrl());

		int contributors = retrieveContributors(repository, template);
		System.out.println("CONTRIBUTORS: " + contributors);

		if (authenticated) {
			boolean starred = determineIfStarredByMe(repository, template);
			System.out.print("STARRED BY ME: ");
			System.out.println(starred ? "YES" : "NO");
		}

		System.out.println("----");
	}

	private int retrieveContributors(Repository repository, RestTemplate template) {
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

	private boolean determineIfStarredByMe(Repository repository, RestTemplate template) {
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