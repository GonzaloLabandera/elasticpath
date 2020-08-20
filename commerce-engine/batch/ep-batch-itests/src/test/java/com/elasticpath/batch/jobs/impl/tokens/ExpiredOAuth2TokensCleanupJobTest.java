/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.tokens;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.TestPersistenceEngine;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.settings.provider.TestSettingValueProvider;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;


@DirtiesDatabase
public class ExpiredOAuth2TokensCleanupJobTest extends DbTestCase {

	private static final long ACTIVE_ROWS = 10;
	private static final long EXPIRED_ROWS = 20;

	@Autowired
	private TestPersistenceEngine testPersistenceEngine;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ExpiredOAuth2TokensCleanupJob expiredOAuth2TokensCleanupJob;

	private String storeCode;
	private long runner = 1;


	@Override
	public void setUpDb() {
		SimpleStoreScenario testScenario = getTac().useScenario(SimpleStoreScenario.class);
		Store store = testScenario.getStore();
		storeCode = store.getCode();

		expiredOAuth2TokensCleanupJob.setConfigBatchSize(new TestSettingValueProvider(1));

		Customer customer = customerBuilder
				.withCustomerType(CustomerType.SINGLE_SESSION_USER)
				.withFirstName("John")
				.withLastName("Doe")
				.withStoreCode(storeCode)
				.build();

		customerService.add(customer);

		generateTokens(customer.getGuid(), EXPIRED_ROWS, true);
		generateTokens(customer.getGuid(), ACTIVE_ROWS, false);
	}

	@Test
	public void shouldPurgeOnlyExpiredTokens() {
		String sqlQuery = "SELECT COUNT(*) FROM TOAUTHACCESSTOKEN";
		long totalRows = testPersistenceEngine.<Long>retrieveNative(sqlQuery).get(0);
		assertEquals("Total number of rows is invalid!", ACTIVE_ROWS + EXPIRED_ROWS, totalRows);

		expiredOAuth2TokensCleanupJob.execute();

		long numRows = testPersistenceEngine.<Long>retrieveNative(sqlQuery).get(0);
		assertEquals("Only active rows should be on the database!", ACTIVE_ROWS, numRows);
	}


	private void generateTokens(final String customerGuid, final long numItems, final Boolean expired) {

		String dateParam = expired ? "NOW()" : "'2050-01-01 00:00:00'";

		for (long index = runner; index < (runner + numItems); index++) {
			String oauthTokenInsertQuery = format("INSERT INTO TOAUTHACCESSTOKEN "
							+ "(UIDPK, TOKEN_ID, EXPIRY_DATE, TOKEN_TYPE, STORECODE, CLIENT_ID ,CUSTOMER_GUID, CUSTOMER_ROLE) "
							+ "VALUES (%d,'%s', %s,'bearer','MOBEE','ep_client_id','%s','PUBLIC')",
					index, UUID.randomUUID(), dateParam, customerGuid);

			doInTransaction(status -> testPersistenceEngine.executeNativeQuery(oauthTokenInsertQuery));
		}
		runner = runner + numItems;
	}
}
