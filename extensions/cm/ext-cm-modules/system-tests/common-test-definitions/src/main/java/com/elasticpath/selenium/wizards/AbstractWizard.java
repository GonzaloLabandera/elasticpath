package com.elasticpath.selenium.wizards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Abstract Wizard class for common wizard actions. Wizard is a dialog that contains a series of configurations in sequential order.
 * Wizard classes should extend this class for common methods.
 */
public abstract class AbstractWizard extends AbstractPageObject {

	private static final String NEXT_BUTTON = "div[widget-id='Next >']";
	private static final String BACK_BUTTON = "div[widget-id='< Back']";
	private static final String CANCEL_BUTTON = "div[widget-id='Cancel']";
	private static final String FINISH_BUTTON = "div[widget-id='Finish']";
	private static final String DONE_BUTTON = "div[widget-id='Done']";
	private static final String OK_BUTTON = "div[widget-id='OK']";
	private static String wizardType;
	private static String wizardStep;

	public static String getWizardType() {
		return wizardType;
	}

	public static void setWizardType(final String wizardType) {
		AbstractWizard.wizardType = wizardType;
	}

	public static String getWizardStepCss() {
		return wizardStep;
	}

	public static void setWizardStepCss(final String upcomingStep) {
		AbstractWizard.wizardStep = upcomingStep;
	}

	/**
	 * Constructor.
	 *
	 * @param driver the driver.
	 */
	public AbstractWizard(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks Next.
	 */
	public void clickNextInDialog() {
		clickButton(NEXT_BUTTON, "Next");
	}

	/**
	 * Clicks Back.
	 */
	public void clickBackInDialog() {
		clickButton(BACK_BUTTON, "Back");
	}

	/**
	 * Clicks Cancel.
	 */
	public void clickCancel() {
		clickButton(CANCEL_BUTTON, "Cancel");
	}

	/**
	 * Clicks Finish.
	 */
	public void clickFinish() {
		clickButton(FINISH_BUTTON, "Finish");
		waitTillElementDisappears(By.cssSelector(FINISH_BUTTON));
	}

	/**
	 * Clicks Done.
	 */
	public void clickDone() {
		clickButton(DONE_BUTTON, "Done");
		waitTillElementDisappears(By.cssSelector(DONE_BUTTON));
	}

	/**
	 * Clicks OK.
	 */
	public void clickOk() {
		clickButton(OK_BUTTON, "OK");
		waitTillElementDisappears(By.cssSelector(OK_BUTTON));
	}

	/**
	 * Wait for wizard step to load.
	 *
	 * @param wizardCurrentStep the upcoming step.
	 */
	public void waitForWizardStep(final String wizardCurrentStep) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(String.format("div[widget-id='" + wizardType + " - " + wizardStep
				+ "'][seeable='true']", wizardCurrentStep)));
	}
}
