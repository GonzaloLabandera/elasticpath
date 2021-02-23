/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.customer.UserAccountAssociationDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.UserAccountAssociationImpl;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Tests <code>UserAccountAssociationExporterImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAccountAssociationExporterImplTest {

	private static final String BUYER_ROLE = "BUYER";
	@InjectMocks
	private UserAccountAssociationExporterImpl userAccountAssociationExporter;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@Mock
	private CustomerService customerService;

	@Mock
	private DomainAdapter<UserAccountAssociation, UserAccountAssociationDTO> userAccountAssociationDomainAdapter;

	private ExportContext exportContext;

	private List<String> uidpkStringList;
	private List<Long> uidpkLongList;
	private List<UserAccountAssociation> userAccountAssociationList;
	private UserAccountAssociation association1;
	private UserAccountAssociation association2;

	private static final String USER_GUID_1 = "userGuid1";
	private static final String USER_GUID_2 = "userGuid2";
	private static final String ACCOUNT_GUID_1 = "accountGuid1";
	private static final String ACCOUNT_GUID_2 = "accountGuid2";
	private static final long UIDPK_1 = Long.MAX_VALUE;
	private static final long UIDPK_2 = Long.MIN_VALUE;

	/**
	 * All uidpks found during initialization should be exportable.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Before
	public void initialize() throws ConfigurationException {
		uidpkStringList = Arrays.asList(String.valueOf(UIDPK_1), String.valueOf(UIDPK_2));
		uidpkLongList = Arrays.asList(UIDPK_1, UIDPK_2);
		association1 = createUserAccountAssociation(UIDPK_1, USER_GUID_1, ACCOUNT_GUID_1, BUYER_ROLE);
		association2 = createUserAccountAssociation(UIDPK_2, USER_GUID_2, ACCOUNT_GUID_2, BUYER_ROLE);
		userAccountAssociationList = Arrays.asList(association1, association2);
		when(userAccountAssociationService.findAllUids()).thenReturn(uidpkLongList);
		when(userAccountAssociationService.findByIDs(uidpkLongList)).thenReturn(userAccountAssociationList);
		exportContext = new ExportContext(new ExportConfiguration(), new SearchConfiguration());
		userAccountAssociationExporter.initialize(exportContext);
	}

	/**
	 * All uidpks found during initialization should be exportable.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testGetListExportableIDs() throws ConfigurationException {
		List<String> results = userAccountAssociationExporter.getListExportableIDs();
		assertThat(CollectionUtils.isEqualCollection(results, uidpkStringList))
				.isTrue();
	}

	/**
	 * Searching for a uidpks should return all uidpks.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testFindByIDs() throws ConfigurationException {
		Customer registeredUser = new CustomerImpl();
		registeredUser.setCustomerType(CustomerType.REGISTERED_USER);

		when(customerService.findByGuid(association1.getUserGuid())).thenReturn(registeredUser);
		when(customerService.findByGuid(association2.getUserGuid())).thenReturn(registeredUser);
		List<UserAccountAssociation> results = userAccountAssociationExporter.findByIDs(uidpkStringList);

		assertThat(CollectionUtils.isEqualCollection(results, userAccountAssociationList))
				.as("Missing returned user account association")
				.isTrue();
	}

	/**
	 * Ensures the proper {@link JobType} is returned.
	 */
	@Test
	public void testJobType() {
		assertThat(userAccountAssociationExporter.getJobType())
				.as("Incorrect job type returned.")
				.isEqualTo(JobType.USERACCOUNTASSOCIATION);
	}

	/**
	 * Tests getDependentClasses.
	 */
	@Test
	public void testDependantClasses() {
		assertThat(userAccountAssociationExporter.getDependentClasses())
				.as("Incorrect dependent classes returned.")
				.isEqualTo(new Class<?>[]{UserAccountAssociation.class});
	}

	/**
	 * Tests getDomainAdapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(userAccountAssociationDomainAdapter)
				.as("Incorrect domain adapter returned.")
				.isEqualTo(userAccountAssociationExporter.getDomainAdapter());
	}

	private UserAccountAssociation createUserAccountAssociation(final long uidpk, final String userGuid, final String accountGuid,
																final String role) {
		UserAccountAssociation userAccountAssociation = new UserAccountAssociationImpl();
		userAccountAssociation.setUidPk(uidpk);
		userAccountAssociation.setUserGuid(userGuid);
		userAccountAssociation.setAccountGuid(accountGuid);
		userAccountAssociation.setAccountRole(role);
		return userAccountAssociation;
	}
}
