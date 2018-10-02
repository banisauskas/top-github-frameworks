package domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** GitHub repository */
public class BackRepository {

	/** Name of repository */
	private String name;

	/** Name of repository owner */
	@JsonIgnore
	private String ownerLogin;

	/** Repository description */
	private String description;

	/** Repository license name */
	@JsonIgnore
	private String licenseName;

	/** Repository URL */
	@JsonProperty("html_url")
	private String htmlUrl;

	/** Number of stars */
	@JsonProperty("stargazers_count")
	private int stargazersCount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("owner")
	private void unpackOwner(Map<String, String> owner) {
		if (owner != null) {
			ownerLogin = owner.get("login");
		}
	}

	public String getOwnerLogin() {
		return ownerLogin;
	}

	public void setOwnerLogin(String ownerLogin) {
		this.ownerLogin = ownerLogin;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("license")
	private void unpackLicense(Map<String, String> license) {
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

	public int getStargazersCount() {
		return stargazersCount;
	}

	public void setStargazersCount(int stargazersCount) {
		this.stargazersCount = stargazersCount;
	}
}