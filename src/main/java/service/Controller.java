package service;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import domain.FrontRepository;

@RestController
public class Controller {

	@Autowired
	private TopService topService;

	@Autowired
	private StarService starService;

	@GetMapping(path="/top", produces=APPLICATION_JSON_UTF8_VALUE)
	public FrontRepository[] getTop(
		@RequestHeader(name="Authorization", required=false) String authorization) {

		return topService.processRepositories(authorization);
	}

	/** Returns statuses 200, 400, 401 */
	@PutMapping(path="/star/{owner}/{repository}")
	public ResponseEntity<Void> putStar(
		@PathVariable("owner") String owner,
		@PathVariable("repository") String repository,
		@RequestHeader(name="Authorization", required=false) String authorization) {

		if (authorization == null) {
			return new ResponseEntity<Void>((Void) null, HttpStatus.UNAUTHORIZED);
		}

		boolean added = starService.addStar(owner, repository, authorization);

		HttpStatus status = added ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return new ResponseEntity<Void>((Void) null, status);
	}

	/** Returns statuses 200, 400, 401 */
	@DeleteMapping(path="/star/{owner}/{repository}")
	public ResponseEntity<Void> deleteStar(
		@PathVariable("owner") String owner,
		@PathVariable("repository") String repository,
		@RequestHeader(name="Authorization", required=false) String authorization) {

		boolean removed = starService.removeStar(owner, repository, authorization);

		HttpStatus status = removed ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return new ResponseEntity<Void>((Void) null, status);
	}
}