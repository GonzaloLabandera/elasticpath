package com.elasticpath.definitions.stateobjects.content;

/**
 * Object to pass the state of generated option projection content.
 */
public class BrandProjectionContent extends ProjectionContent {

	/**
	 * Initializes content values.
	 *
	 * @param language    content language locale
	 * @param displayName content display name
	 */
	public void setContent(final String language, final String displayName) {
		setLanguage(language);
		setDisplayName(displayName);
		setContentBody("{\"translations\":[{\"language\":\"" + language + "\",\"displayName\":\"" + displayName + "\"}]}");
	}
}
