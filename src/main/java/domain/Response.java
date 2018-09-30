package domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

	/** True when request timed out */
	@JsonProperty("incomplete_results")
	private boolean incompleteResults;

	/** GitHub repositories */
	private Repository[] items;

	public boolean getIncompleteResults() {
		return incompleteResults;
	}

	public void setIncompleteResults(boolean incompleteResults) {
		this.incompleteResults = incompleteResults;
	}

	public Repository[] getItems() {
		return items;
	}

	public void setItems(Repository[] items) {
		this.items = items;
	}
}