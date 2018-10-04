package service;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import domain.FrontRepository;

@RestController
public class Controller {

	@Autowired
	private TopService topService;

	@Autowired
	private StarService starService;

	/**
	 * Responses:
	 * 200 OK - JSON array of repositories
	 * 400 Bad Request - Incorrect orderByContribs value
	 * 401 Unauthorized - Authorization header contains incorrect credentials
	 * 500 Internal Server Error
	 */
	@GetMapping(path="/top", produces=APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<FrontRepository[]> getTop(
		@RequestHeader(name="Authorization", required=false) String authorization,
		@RequestParam(name="orderByContribs", required=false) String orderByContribs) {

		return topService.processRepositories(orderByContribs, authorization);
	}

	/**
	 * Responses:
	 * 200 OK - Star added (or was already present)
	 * 401 Unauthorized - Authorization header is missing, or contains incorrect credentials
	 * 404 Not Found - Incorrect owner or repository
	 * 500 Internal Server Error
	 */
	@PutMapping(path="/star/{owner}/{repository}")
	public ResponseEntity<Void> putStar(
		@PathVariable("owner") String owner,
		@PathVariable("repository") String repository,
		@RequestHeader(name="Authorization", required=false) String authorization) {

		return starService.processStar(PUT, owner, repository, authorization);
	}

	/**
	 * Responses:
	 * 200 OK - Star removed (or was already absent)
	 * 401 Unauthorized - Authorization header is missing, or contains incorrect credentials
	 * 404 Not Found - Incorrect owner or repository
	 * 500 Internal Server Error
	 */
	@DeleteMapping(path="/star/{owner}/{repository}")
	public ResponseEntity<Void> deleteStar(
		@PathVariable("owner") String owner,
		@PathVariable("repository") String repository,
		@RequestHeader(name="Authorization", required=false) String authorization) {

		return starService.processStar(DELETE, owner, repository, authorization);
	}
}