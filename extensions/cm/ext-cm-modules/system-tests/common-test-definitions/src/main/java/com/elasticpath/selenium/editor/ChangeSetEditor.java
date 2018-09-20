package com.elasticpath.selenium.editor;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Change Set Editor.
 */
public class ChangeSetEditor extends AbstractPageObject {

	private static final String OBJECT_TABLE_PARENT_CSS = "div[widget-id='Object Table'][widget-type='Table'][seeable='true'] ";
	private static final String OBJECT_COLUMN_CSS = OBJECT_TABLE_PARENT_CSS + "div[column-id='%s']";
	private static final String CHANGE_SET_TAB_CSS =
			"div[automation-id='com.elasticpath.cmclient.changeset.ChangeSetMessages.ChangeSetEditor_%s'][seeable='true']";
	private static final String CHANGE_SET_EDITOR = "div[widget-id='%s'][appearance-id='ctab-item']";
	private static final String CHANGE_SET_SUMMARY_PAGE = "div[pane-location='editor-pane'] div[automation-id='SummaryPage'] ";
	private static final String CHANGE_SET_OBJECTS_TITLE = "div[automation-id='com.elasticpath.cmclient.changeset.ChangeSetMessages"
			+ ".ChangeSetEditor_ObjectsFormTitle'][seeable='true'][appearance-id='label-wrapper']";
	private static final String CHANGE_SET_NAME_INPUT_CSS = CHANGE_SET_SUMMARY_PAGE + "div[automation-id='com.elasticpath.cmclient.changeset"
			+ ".ChangeSetMessages.ChangeSetEditor_ChangeSet_Name'] input";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ChangeSetEditor(final WebDriver driver) {
		super(driver);
	}


	/**
	 * Verifies object exists.
	 *
	 * @param objectColumnValue the column value
	 * @param columnName        the column name
	 */
	public void verifyObjectExists(final String objectColumnValue, final String columnName) {
		assertThat(selectItemInEditorPaneWithScrollBar(OBJECT_TABLE_PARENT_CSS, OBJECT_COLUMN_CSS, objectColumnValue, columnName))
				.as("Unable to find object - " + objectColumnValue)
				.isTrue();
	}

	/**
	 * Selects Objects tab.
	 */
	public void selectObjectsTab() {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(CHANGE_SET_TAB_CSS, "ObjectsPageTitle"))));
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(CHANGE_SET_OBJECTS_TITLE));
	}

	/**
	 * Selects change set's editor.
	 *
	 * @param changeSetName the change set name
	 */
	public void selectChangeSetEditor(final String changeSetName) {
		click(getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(CHANGE_SET_EDITOR, changeSetName))));
	}

	/**
	 * Changes changeset Name.
	 *
	 * @param newName the new name to change to
	 */
	public void changeName(final String newName) {
		clearAndType(CHANGE_SET_NAME_INPUT_CSS, newName);
	}
}
