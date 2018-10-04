package service;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class StarService {

	/** Thread-safe */
	@Autowired
	private RestTemplate template;

	@Autowired
	private CommonService commonService;

	public ResponseEntity<Void> processStar(HttpMethod method, String owner, String repository, String authorization) {
		if (authorization == null) {
			return new ResponseEntity<>(UNAUTHORIZED);
		}

		HttpEntity<Void> httpEntity = commonService.createHttpEntity(authorization);

		try {
			ResponseEntity<Void> response = template.exchange("https://api.github.com/user/starred/{owner}/{repository}", method, httpEntity, Void.class, owner, repository);

			boolean success = response.getStatusCode() == NO_CONTENT;
			return new ResponseEntity<>(success ? OK : INTERNAL_SERVER_ERROR);
		}
		catch (HttpClientErrorException e) {
			if (e.getStatusCode() == UNAUTHORIZED) {
				return new ResponseEntity<>(UNAUTHORIZED);
			}
			else if (e.getStatusCode() == NOT_FOUND) {
				return new ResponseEntity<>(NOT_FOUND);
			}
			else {
				throw e; // HTTP 500
			}
		}
	}
}