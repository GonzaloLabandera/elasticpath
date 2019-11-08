/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.elasticpath.test.integration.junit.DatabaseHandlingTestExecutionListener;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/persistence-openjpa-itest-context.xml")
@TestExecutionListeners({
	DatabaseHandlingTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	TransactionalTestExecutionListener.class
})
public class DbTxIsolationValidatorTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void shouldThrowExceptionWhenTransactionIsolationIsNotReadCommitted() {
		assertThatThrownBy(() -> context.getBean("dbTxIsolationValidator"))
			.hasCauseExactlyInstanceOf(IllegalStateException.class)
			.hasMessageContaining("The database transaction isolation must be READ_COMMITTED");
	}
}
