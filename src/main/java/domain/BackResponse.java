package domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Response from GitHub */
public class BackResponse {

	/** True when request timed out */
	@JsonProperty("incomplete_results")
	private boolean incompleteResults;

	/** GitHub repositories */
	private BackRepository[] items;

	public boolean getIncompleteResults() {
		return incompleteResults;
	}

	public void setIncompleteResults(boolean incompleteResults) {
		this.incompleteResults = incompleteResults;
	}

	public BackRepository[] getItems() {
		return items;
	}

	public void setItems(BackRepository[] items) {
		this.items = items;
	}
}