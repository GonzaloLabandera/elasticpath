/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.admin.configuration;

import java.lang.reflect.Field;

import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;

/**
 * Messages class for the configuration plug-in.
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.VariableNamingConventions" })
public final class AdminConfigurationMessages {
	private static final String BUNDLE_NAME = "com.elasticpath.cmclient.admin.configuration.AdminConfigurationPluginResources"; //$NON-NLS-1$
	private AdminConfigurationMessages() {
	}




	public String ConfigurationAdminSection_TagConfiguration;
	public String ConfigurationAdminSection_SystemConfiguration;
	public String ConfigurationAdminSection_SearchIndexes;

	// Global
	public String settingDefinitionPath; //Table Header
	public String settingValueContext; //Table header
	public String settingValueValue; //Table header
	public String settingValueLastModified; //Table header
	public String settingDefType;
	public String settingDefDefaultValue;
	public String settingDefMaxOverrides;
	public String settingDefDescription;
	public String settingDefMetadata;
	public String settingDefMetadataKey;
	public String settingDefMetadataValue;
	public String RebuildIndex;
	public String IndexName;
	public String LastBuildTime;
	public String Status;
	public String RebuildConfirmTitle;
	public String RebuildConfirmMessage;

	public String definedValues; //Grouping
	public String editButton;
	public String removeButton;
	public String addButton;
	public String filterLabel;

	// Dialogs
	public String newDialogTitle;
	public String editDialogTitle;
	public String newMetadataTitle;
	public String editMetadataTitle;
	public String contextLabel;
	public String valueLabel;

	// Errors
	public String errorDialogTitle;
	public String errorDialogText;
	public String metadataKeyExists;

	// Search index names
	public String category;
	public String customer;
	public String order;
	public String orderreturn;
	public String product;
	public String promotion;
	public String cmuser;
	public String shippingservicelevel;
	public String sku;

	// Search index statuses
	public String REBUILD_SCHEDULED;		// NOPMD
	public String MISSING;				// NOPMD
	public String COMPLETE;				// NOPMD
	public String REBUILD_IN_PROGRESS;	// NOPMD
	public String UPDATE_IN_PROGRESS;	// NOPMD

	public String progressColumn;
	public String remainingTimeColumn;

	public String tagGroupGuid;
	public String tagGroupName;
	public String languageLabel;
	public String editGroupButton;
	public String addGroupButton;
	public String removeGroupButton;
	public String addGroupDialogTitle;
	public String editGroupDialogTitle;
	public String guidLabel;
	public String nameLabel;

	public String TagDefinition_Label_Guid;
	public String TagDefinition_Label_Name;
	public String TagDefinition_Label_Type;
	public String TagDefinition_Edit_Button;
	public String TagDefinition_Add_Button;
	public String TagDefinition_Remove_Button;
	public String TagDefinition_Label_TagCode;
	public String TagDefinition_Label_TagName;
	public String TagDefinition_Label_Description;
	public String TagDefinition_Label_FieldType;
	public String TagDictionary_ColumnHeader_Code;
	public String TagDictionary_ColumnHeader_Name;
	public String TagDictionary_ColumnHeader_Purpose;
	public String editTagDefinitionDialogTitle;
	public String TagDefinition_Label_SampleHttpHeaderPrefix;
	public String TagGroupCodeExists;
	public String TagDefinitionCodeExists;
	public String TagDefinitionNameExists;
	public String addTagDefinitionDialogTitle;
	public String TagGroup_TableName;
	public String TagDefinition_TableName;

	/**
	 * Returns a message String by the given message key.
	 *
	 * @param messageKey the key string associated with the message String
	 * @return the message String
	 */
	public String getMessage(final String messageKey) {
		try {
			final Field field = AdminConfigurationMessages.class.getField(messageKey);
			return (String) field.get(this);
		} catch (final Exception e) {
			return messageKey;
		}
	}

	/**
	 * Gets the NLS localize message class.
	 * @return the localized message class.
	 */
	public static AdminConfigurationMessages get() {
		return LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, AdminConfigurationMessages.class);
	}

}
