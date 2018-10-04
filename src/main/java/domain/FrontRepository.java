package domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/** GitHub repository */
public class FrontRepository implements Comparable<FrontRepository> {

	/** Repository name */
	private String name;

	/** Repository description */
	private String description;

	/** Repository license name */
	private String licenseName;

	/** URL link to repository */
	private String link;

	/** Number of contributors */
	private int contributors;

	/** Number of stars */
	private int stars;

	/** Whether current user has put a star on this repository */
	@JsonInclude(Include.NON_NULL)
	private Boolean starredByMe;

	/** Repository is considered "bigger" if it has more contributors */
	public int compareTo(FrontRepository other) {
		return contributors - other.contributors;
	}

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

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getContributors() {
		return contributors;
	}

	public void setContributors(int contributors) {
		this.contributors = contributors;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public Boolean getStarredByMe() {
		return starredByMe;
	}

	public void setStarredByMe(Boolean starredByMe) {
		this.starredByMe = starredByMe;
	}
}