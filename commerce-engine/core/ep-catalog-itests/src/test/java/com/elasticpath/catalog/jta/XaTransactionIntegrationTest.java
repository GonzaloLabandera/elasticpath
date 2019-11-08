/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.jta;

import static com.elasticpath.catalog.jta.XaTransactionIntegrationTest.JMS_BROKER_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionException;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;
import com.elasticpath.test.util.Utils;

@JmsBrokerConfigurator(url = JMS_BROKER_URL)
public class XaTransactionIntegrationTest extends XaTransactionTestSupport {

	public static final String JMS_BROKER_URL = "tcp://localhost:61620";

	private static final Logger LOGGER = Logger.getLogger(XaTransactionIntegrationTest.class);
	private static final String SKU_OPTION_KEY = "optionKey";
	private static final String SKU_DISPLAY_NAME = "displayName";

	@Autowired
	private SkuOptionService skuOptionService;

	@Autowired
	private BrokerService brokerService;

	@Test
	@DirtiesDatabase
	public void databaseShouldNotRolledBackWhenJmsBrokerOnline() {
		final SkuOption skuOption = createSkuOption();

		skuOptionService.saveOrUpdate(skuOption);

		assertThat(skuOptionService.findByKey(skuOption.getOptionKey())).isEqualTo(skuOption);
	}

	@Test
	public void databaseShouldRolledBackWhenJmsBrokerOffline() {
		final SkuOption skuOption = createSkuOption();

		assertThatThrownBy(() -> doInTransaction(status -> saveSkuOptionAndStopJmsBrokerService(skuOption)))
				.isInstanceOf(TransactionException.class);
		assertThat(skuOptionService.findByKey(skuOption.getOptionKey())).isNull();
	}

	private SkuOption saveSkuOptionAndStopJmsBrokerService(final SkuOption skuOption) {
		skuOptionService.saveOrUpdate(skuOption);

		try {
			brokerService.stop();
		} catch (Exception e) {
			LOGGER.error(e);
		}

		return skuOption;
	}

	private SkuOption createSkuOption() {
		final SkuOption skuOption = getBeanFactory().getBean(ContextIdNames.SKU_OPTION);
		skuOption.initialize();
		skuOption.setOptionKey(Utils.uniqueCode(SKU_OPTION_KEY));
		skuOption.setCatalog(createAndPersistStore().getCatalog());
		skuOption.setDisplayName(Utils.uniqueCode(SKU_DISPLAY_NAME), Locale.ENGLISH);

		return skuOption;
	}

	private Store createAndPersistStore() {
		final Store store = createStore();

		return doInTransaction(status -> persist(store));
	}

	private <T extends Persistable> T persist(final T entity) {
		getPersistenceEngine().save(entity);
		return entity;
	}

}
