package com.elasticpath.selenium.common;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.SignInDialog;

/**
 * CM Object. Blank page before each page loads.
 */
public class CM extends AbstractPageObject {
	private final WebDriver driver;

	/**
	 * constructor.
	 *
	 * @param driver WebDriver.
	 */
	public CM(final WebDriver driver) {
		super(driver);
		this.driver = driver;
	}

	/**
	 * Launch CM.
	 *
	 * @return SignInDialog.
	 */
	public SignInDialog openCM() {
		getDriver().get(getSiteURL());
		return new SignInDialog(driver);
	}

}
