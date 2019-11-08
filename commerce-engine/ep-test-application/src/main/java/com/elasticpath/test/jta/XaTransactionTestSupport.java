/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import org.springframework.test.context.TestExecutionListeners;

import com.elasticpath.test.db.DbTestCase;

/**
 * Represents base class for integration tests of JTA XA transactions.
 * Creates test infrastructure with DB and JMS sources.
 */
@TestExecutionListeners({ XaServicesTestExecutorListener.class })
public class XaTransactionTestSupport extends DbTestCase {

}
