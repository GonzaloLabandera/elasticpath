/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.tag;

/**
 * Constants for sort attribute importexport.
 */
public final class TagGroupDTOConstants {

	/**
	 * TagGroup import file.
	 */
	public static final String TAG_GROUP_FILE = "tag_groups.xml";

	/**
	 * Manifest file.
	 */
	public static final String MANIFEST_IMPORT_FILE = "manifest.xml";

	/**
	 * TagGroup guid key.
	 */
	public static final String TAG_GROUP_GUID = "guid";

	/**
	 * Display name key.
	 */
	public static final String TAG_GROUP_DISPLAY_NAME = "display_name";

	/**
	 * Language identifier key for the display name.
	 */
	public static final String TAG_GROUP_DISPLAY_NAME_LANGUAGE = "language";

	/**
	 * Tag Definition guid/code key.
	 */
	public static final String TAG_DEFINITION_GUID = "code";

	/**
	 * Tag Definition name key.
	 */
	public static final String TAG_DEFINITION_NAME = "name";

	/**
	 * Tag Definition description key.
	 */
	public static final String TAG_DEFINITION_DESCRIPTION = "description";

	/**
	 * Tag Definition display name language key.
	 */
	public static final String TAG_DEFINITION_DISPLAY_NAME_LANGUAGE = "language";

	/**
	 * Tag Definition display name key.
	 */
	public static final String TAG_DEFINITION_DISPLAY_NAME = "displayName";

	/**
	 * Tag Definition value/field type key.
	 */
	public static final String TAG_DEFINITION_VALUE_TYPE = "fieldType";

	/**
	 * Tag Definition dictionary guid association key.
	 */
	public static final String TAG_DEFINITION_DICTIONARIES = "dictionaries";

	/**
	 * Tag Definition associated tag group code key.
	 */
	public static final String TAG_DEFINITION_GROUP_CODE = "groupCode";

	private TagGroupDTOConstants() {
		// prevent instantiation
	}
}
