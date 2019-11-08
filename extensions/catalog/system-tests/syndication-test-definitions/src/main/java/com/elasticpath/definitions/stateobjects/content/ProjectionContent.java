package com.elasticpath.definitions.stateobjects.content;

/**
 * Object to pass the state of generated option projection content.
 */
public class ProjectionContent {

	/**
	 * One language content locale.
	 */
	private String language;

	/**
	 * Domain object display name.
	 */
	private String displayName;

	/**
	 * Content JSON.
	 */
	private String contentBody;

	/**
	 * @return projection language.
	 */
	public String getLanguage() {
		return language;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	protected void setContentBody(final String contentBody) {
		this.contentBody = contentBody;
	}

	/**
	 * @return projection display name.
	 */
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return projection content.
	 */
	public String getContent() {
		return contentBody;
	}

	/**
	 * Initializes content as empty.
	 */
	public void setEmptyContent() {
		this.contentBody = "";
	}
}
