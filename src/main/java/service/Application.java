package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
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
		//template.getInterceptors().add(new BasicAuthorizationInterceptor("username", "password"));

		return args -> {
			ResponseEntity<Response> repositories =
				template.getForEntity("https://api.github.com/search/repositories?q=language:java+topic:framework&sort=stars&per_page=10", Response.class);
			print(repositories);
		};
	}

	private void print(ResponseEntity<Response> responseEntity) {
		Response response = responseEntity.getBody();

		System.out.println("STATUS " + responseEntity.getStatusCodeValue());
		System.out.println("CONTENT-TYPE " + responseEntity.getHeaders().getContentType());
		System.out.println("INCOMPLETE " + response.getIncompleteResults());
		System.out.println("LENGTH " + response.getItems().length);
		System.out.println("----------------");

		Repository[] repositories = response.getItems();

		for (Repository repository : repositories) {
			System.out.println("NAME " + repository.getName());
			System.out.println("DESCRIPTION " + repository.getDescription());
			System.out.println("LICENSE " + repository.getLicenseName());
			System.out.println("URL " + repository.getHtmlUrl());
			System.out.println("----");
		}
	}
}