package domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** GitHub repository */
public class Repository {

	/** Repository name */
	private String name;

	/** Repository description */
	private String description;

	/** Repository license name */
	private String licenseName;

	/** Repository URL */
	@JsonProperty("html_url")
	private String htmlUrl;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("license")
	private void unpackNameFromNestedObject(Map<String, String> license) {
		if (license != null) {
			licenseName = license.get("name");
		}
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}
}