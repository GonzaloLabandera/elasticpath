/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.elasticpath.cmclient.admin.configuration.listener.TagGroupUpdateListener;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.service.TagDefinitionService;
import com.elasticpath.tags.service.TagDictionaryService;
import com.elasticpath.tags.service.TagGroupService;
import com.elasticpath.tags.service.TagValueTypeService;

/**
 * This class provides functionality to query and update the tag group model for the various Tag composites.
 */
public class TagGroupModel {

	private final List<TagGroupUpdateListener> tagGroupUpdateListeners = new LinkedList<>();

	private final TagGroupService tagGroupService = BeanLocator.getSingletonBean(ContextIdNames.TAG_GROUP_SERVICE, TagGroupService.class);

	private final TagDictionaryService tagDictionaryService = BeanLocator.getSingletonBean(ContextIdNames.TAG_DICTIONARY_SERVICE,
			TagDictionaryService.class);

	private final TagValueTypeService tagValueTypeService = BeanLocator.getSingletonBean(ContextIdNames.TAG_VALUE_TYPE_SERVICE,
			TagValueTypeService.class);

	private final TagDefinitionService tagDefinitionService = BeanLocator.getSingletonBean(ContextIdNames.TAG_DEFINITION_SERVICE,
			TagDefinitionService.class);

	private List<TagGroup> currentTagGroups;
	private List<TagDictionary> allTagDictionaries;
	private List<TagValueType> allValueTypes;

	/**
	 * Get all tag groups. The list of tag groups is looked up once for the life of this object.
	 *
	 * @return all settings definitions held by the model
	 */
	public List<TagGroup> getAllTagGroups() {
		if (currentTagGroups == null) {
			currentTagGroups = tagGroupService.getTagGroups();
		}
		return currentTagGroups;
	}

	/**
	 * Inserts or updates the tag group and makes sure all contained TagDefinitions are properly associated.
	 *
	 * @param tagGroup the TagGroup object to insert or update.
	 */
	public void updateTagGroup(final TagGroup tagGroup) {
		tagGroupService.saveOrUpdate(tagGroup);

		//update the tag group in the field of all tag groups
		int indexOfTagGroup = currentTagGroups.indexOf(tagGroup);
		if (indexOfTagGroup == -1) {
			currentTagGroups.add(tagGroup);

		} else {
			currentTagGroups.set(indexOfTagGroup, tagGroup);
		}

		updateTagGroupListeners(tagGroup);
	}

	/**
	 * Deletes that tag group.
	 *
	 * @param tagGroup the object representation of the TagGroup to delete
	 */
	public void removeTagGroup(final TagGroup tagGroup) {
		tagGroupService.delete(tagGroup);
		//update the local field list of all tag groups
		currentTagGroups.remove(tagGroup);
		updateTagGroupListeners(tagGroup);
	}

	/**
	 * Deletes the tag definition.
	 *
	 * @param tagDefinitionToDelete the tag definition instance to delete
	 */
	public void removeTagDefinition(final TagDefinition tagDefinitionToDelete) {
		TagGroup tagGroup = tagDefinitionToDelete.getGroup();
		tagDefinitionService.delete(tagDefinitionToDelete);
		tagGroup.removeTagDefinition(tagDefinitionToDelete);
		tagGroupService.saveOrUpdate(tagGroup);
		updateTagGroupListeners(tagGroup);
	}

	/**
	 * Query a list of all tag dictionaries.
	 *
	 * @return a List of all TagDictionary objects
	 */
	public List<TagDictionary> getAllTagDictionaries() {
		if (allTagDictionaries == null) {
			allTagDictionaries = tagDictionaryService.getTagDictionaries();
		}
		return allTagDictionaries;
	}

	/**
	 * Returns all tag dictionaries that contain a tag definition.
	 *
	 * @param tagDefinition the tag definition to find matching dictionaries for
	 * @return the list of tag dictionaries that contain the tag definition
	 */
	public List<TagDictionary> getTagDictionariesForTag(final TagDefinition tagDefinition) {
		if (tagDefinition == null) {
			return getAllTagDictionaries();
		} else {
			List<TagDictionary> dictionariesForTag = new ArrayList<>();
			for (TagDictionary dict : getAllTagDictionaries()) {
				if (dict.getTagDefinitions().contains(tagDefinition)) {
					dictionariesForTag.add(dict);
				}
			}
			return dictionariesForTag;
		}
	}

	/**
	 * Query all available tag value types.
	 *
	 * @return a list of all available TagValueType
	 */
	public List<TagValueType> getAllTagValueTypes() {
		if (allValueTypes == null) {
			allValueTypes = tagValueTypeService.getTagValueTypes();
		}
		return allValueTypes;
	}

	/**
	 * Update a tag definition.  Note that the tag group should not be associated to the tag definition yet as it may not be persisted.
	 *
	 * @param tagDefinition          the TagDefinition to update
	 * @param updatedTagDictionaries the set of TagDictionary instances to associate to the TagDefinition
	 * @param tagGroup               the TagGroup that the TagDefinition will belong to
	 */
	public void updateTagDefinition(final TagDefinition tagDefinition, final List<TagDictionary> updatedTagDictionaries, final TagGroup tagGroup) {
		if (tagDefinition.getGroup() == null) {
			tagDefinition.setGroup(tagGroup);
			tagGroup.getTagDefinitions().add(tagDefinition);
		}
		tagDefinitionService.saveOrUpdate(tagDefinition);
		for (TagDictionary dictionary : getAllTagDictionaries()) {
			//if the database copy of the dictionary contains this tag,but the list of update dictionaries does not- then remove the association
			if (dictionary.getTagDefinitions().contains(tagDefinition) && !updatedTagDictionaries.contains(dictionary)) {
				dictionary.removeTagDefinition(tagDefinition);
				tagDictionaryService.saveOrUpdate(dictionary);
			} else if (updatedTagDictionaries.contains(dictionary)) {
				dictionary.addTagDefinition(tagDefinition);
				tagDictionaryService.saveOrUpdate(dictionary);
			}
		}
		updateTagGroup(tagGroup);
	}

	/**
	 * Find a single TagGroup by its guid.
	 *
	 * @param guid the guid which identifies the unique TagGroup
	 * @return the TagGroup, or null if not found
	 */
	public TagGroup getTagGroupByGuid(final String guid) {
		return tagGroupService.findByGuid(guid);
	}

	/**
	 * Creates a new, unpersisted TagGroup.
	 *
	 * @return the empty TagGroup
	 */
	public TagGroup createTagGroup() {
		return tagGroupService.create();
	}

	/**
	 * Creates a new, unpersisted TagDefinition.
	 *
	 * @return the empty TagDefinition
	 */
	public TagDefinition createTagDefinition() {
		return tagDefinitionService.create();
	}

	/**
	 * Registers a tag group listener that will be notified whenever a TagGroup is altered.
	 *
	 * @param tagGroupUpdateListener the TagGroupUpdateListener to register and notify.
	 */
	public void registerTagGroupUpdateListener(final TagGroupUpdateListener tagGroupUpdateListener) {
		tagGroupUpdateListeners.add(tagGroupUpdateListener);
	}

	/**
	 * Unregisters a listener to remove it from notification of tag group changes.
	 *
	 * @param listener the listener to remove from the notification list
	 */
	public void unregisterTagGroupUpdateListener(final TagGroupUpdateListener listener) {
		if (tagGroupUpdateListeners.contains(listener)) {
			tagGroupUpdateListeners.remove(listener);
		}

	}

	private void updateTagGroupListeners(final TagGroup tagGroup) {
		for (TagGroupUpdateListener listener : tagGroupUpdateListeners) {
			listener.tagGroupUpdated(tagGroup);
		}
	}

	/**
	 * Checks if the guid of the tag group already exists in the local TagGroup List.
	 *
	 * @param tagGroup the tag group to get the guid/code from
	 * @return true if the tag group guid is already in use within the list, false otherwise
	 */
	public boolean tagGroupCodeExists(final TagGroup tagGroup) {
		for (TagGroup persistedTagGroup : currentTagGroups) {
			if (persistedTagGroup.getGuid().equals(tagGroup.getGuid())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the guid/code of the tag definition already exists by querying the tag definition service.
	 *
	 * @param tagDefinition the tag definition to get the guid/code from
	 * @return true if the tag definition guid/code is already present in the system, false otherwise
	 */
	public boolean tagDefinitionCodeExists(final TagDefinition tagDefinition) {
		return tagDefinitionService.findByGuid(tagDefinition.getGuid()) != null;
	}

	/**
	 * Checks if the name of the tag definition already exists by querying the tag definition service.
	 *
	 * @param tagDefinition the tag definition to get the name from
	 * @return true if the tag definition name is already present in the system, false otherwise
	 */
	public boolean tagDefinitionNameExists(final TagDefinition tagDefinition) {
		return tagDefinitionService.findByName(tagDefinition.getName()) != null;
	}
}
