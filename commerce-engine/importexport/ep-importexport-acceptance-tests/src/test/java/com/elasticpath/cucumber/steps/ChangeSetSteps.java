/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetMutator;
import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.service.changeset.ChangeSetLoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.test.persister.TestApplicationContext;

/**
 * Change Set Steps.
 */
public class ChangeSetSteps {

	@Autowired
	private TestApplicationContext tac;

	@Autowired
	private ChangeSetManagementService changeSetManagementService;

	@Autowired
	private BeanFactory beanFactory;

	/**
	 * Creates an empty change set with the given guid.
	 *
	 * @param changeSetGuid the change set guid
	 */
	@Given("^I create a change set (\\w+)$")
	public void createChangeSet(final String changeSetGuid) {
		ChangeSet changeSet = beanFactory.getPrototypeBean(ContextIdNames.CHANGE_SET, ChangeSet.class);
		changeSet.setName(changeSetGuid);
		changeSet.setDescription(changeSetGuid + " description");
		changeSet.setCreatedByUserGuid(tac.getPersistersFactory().getStoreTestPersister().getCmUser().getGuid());
		changeSet = changeSetManagementService.add(changeSet);

		((ChangeSetMutator) changeSet).setObjectGroupId(changeSetGuid);
		changeSetManagementService.update(changeSet, getChangeSetLoadTuner());
	}

	/**
	 * Verifies the change set has the expected member data.
	 *
	 * @param changeSetGuid   the change set guid
	 * @param memberDataTable the member data table
	 */
	@Given("^the change set (\\w+) has the following group member data")
	public void verifyChangeSetMemberData(final String changeSetGuid, final DataTable memberDataTable) {
		final ChangeSet changeSet = changeSetManagementService.get(changeSetGuid, getChangeSetLoadTuner());
		final List<Map<String, String>> changeSetMemberData = memberDataTable.asMaps(String.class, String.class);

		final Collection<BusinessObjectDescriptor> changeSetMembers = changeSet.getMemberObjects();
		assertThat(changeSetMembers)
				.hasSameSizeAs(changeSetMemberData);

		final Map<String, BusinessObjectDescriptor> businessObjectDescriptorMap = changeSetMembers.stream()
				.collect(Collectors.toMap(BusinessObjectDescriptor::getObjectIdentifier, Function.identity()));

		changeSetMemberData.forEach(expectedData -> verifyChangeSetMemberData(businessObjectDescriptorMap, expectedData));
	}

	private void verifyChangeSetMemberData(final Map<String, BusinessObjectDescriptor> businessObjectDescriptorMap,
										   final Map<String, String> expectedData) {
		final String objectType = expectedData.get("objectType");
		final String objectIdentifier = expectedData.get("objectIdentifier");
		final BusinessObjectDescriptor actualDescriptor = businessObjectDescriptorMap.get(objectIdentifier);

		assertThat(actualDescriptor)
				.isNotNull();
		assertThat(actualDescriptor.getObjectType())
				.isEqualTo(objectType);
	}

	private ChangeSetLoadTuner getChangeSetLoadTuner() {
		final ChangeSetLoadTuner loadTuner = beanFactory.getPrototypeBean(ContextIdNames.CHANGESET_LOAD_TUNER, ChangeSetLoadTuner.class);
		loadTuner.setLoadingMemberObjects(true);
		loadTuner.setLoadingMemberObjectsMetadata(true);
		return loadTuner;
	}
}
