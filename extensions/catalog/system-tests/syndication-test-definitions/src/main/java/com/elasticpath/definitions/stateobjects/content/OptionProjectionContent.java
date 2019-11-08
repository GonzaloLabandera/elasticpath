package com.elasticpath.definitions.stateobjects.content;

/**
 * Object to pass the state of generated option projection content.
 */
public class OptionProjectionContent extends ProjectionContent {

	private String optionValue;
	private String displayValue;

	public String getOptionValue() {
		return optionValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	/**
	 * Initializes content values.
	 *
	 * @param language     content language locale
	 * @param displayName  content display name
	 * @param optionValue  content option value code
	 * @param displayValue content option display value
	 */
	public void setContent(final String language, final String displayName, final String optionValue, final String displayValue) {
		setLanguage(language);
		setDisplayName(displayName);
		this.optionValue = optionValue;
		this.displayValue = displayValue;
		setContentBody("{\"translations\":[{\"language\":\"" + language + "\",\"displayName\":\"" + displayName + "\","
				+ "\"optionValues\":[{\"value\":\"" + optionValue + "\",\"displayValue\":\"" + displayValue + "\"}]}]}");
	}
}
