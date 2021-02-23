/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.UseAdviceWith;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.test.integration.junit.DatabaseHandlingTestExecutionListener;
import com.elasticpath.test.integration.junit.LoggingMDCInitializerTestExecutionListener;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.support.junit.JmsRegistrationTestExecutionListener;

/**
 * Basic Spring Context Test.
 */
@RunWith(CamelSpringRunner.class)
@UseAdviceWith
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration("/integration-context.xml")
@SuppressWarnings("PMD.AbstractNaming")
@TestExecutionListeners({
		LoggingMDCInitializerTestExecutionListener.class,
		JmsRegistrationTestExecutionListener.class,
		DatabaseHandlingTestExecutionListener.class,
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class
})
@DirtiesContext
public abstract class BasicSpringContextTest {

	private static final Logger LOGGER = Logger.getLogger(BasicSpringContextTest.class);

	@Rule
	public CamelContextTestRule resource = new CamelContextTestRule();

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private TestApplicationContext tac;

	@Autowired(required = false)
	private List<CamelContext> camelContexts;

	/**
	 * @return the tac
	 */
	protected TestApplicationContext getTac() {
		return tac;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Rule class for start and stop camel context programmatically for each test case.
	 */
	final class CamelContextTestRule extends ExternalResource {

		CamelContextTestRule() {
			//empty default constructor
		}

		@Override
		protected void after() {
			if (camelContexts != null) {
				for (final CamelContext context : camelContexts) {
					try {
						context.stop();
					} catch (Exception ex) {
						LOGGER.error("Error occurred:", ex);
					}
				}
			}
		}

		@Override
		protected void before() throws Throwable {
			if (camelContexts != null) {
				for (final CamelContext context : camelContexts) {
					context.start();
				}
			}
		}
	}
}