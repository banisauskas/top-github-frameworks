package service;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class StarService {

	/** Thread-safe */
	@Autowired
	private RestTemplate template;

	@Autowired
	private CommonService commonService;

	/** @return if star added */
	public boolean addStar(String owner, String repository, String authorization) {
		HttpEntity<Void> httpEntity = commonService.createHttpEntity(authorization);

		try {
			ResponseEntity<Void> response = template.exchange("https://api.github.com/user/starred/{owner}/{repository}", PUT, httpEntity, Void.class, owner, repository);
			return response.getStatusCode() == HttpStatus.NO_CONTENT;
		}
		catch (HttpClientErrorException e) {
			return false;
		}
	}

	/** @return if star removed */
	public boolean removeStar(String owner, String repository, String authorization) {
		HttpEntity<Void> httpEntity = commonService.createHttpEntity(authorization);

		try {
			ResponseEntity<Void> response = template.exchange("https://api.github.com/user/starred/{owner}/{repository}", DELETE, httpEntity, Void.class, owner, repository);
			return response.getStatusCode() == HttpStatus.NO_CONTENT;
		}
		catch (HttpClientErrorException e) {
			return false;
		}
	}
}