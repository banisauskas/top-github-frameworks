package service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class CommonService {

	/** Creates a request with 'Authorization' header and no body */
	public HttpEntity<Void> createHttpEntity(String authorization) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authorization);
		return new HttpEntity<Void>(headers);
	}
}