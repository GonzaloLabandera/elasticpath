/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.rules.impl;


import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.rules.AppliedRule;
import com.elasticpath.test.TestLog4jLoggingAppender;


/** Test cases for <code>ChecksumAndLogVisitor</code>. */
public class ChecksumAndLogVisitorTest {
	
	private static final String RULE_CODE = "Rule Code";
	private static final String RULE_CODE_WITH_NEWLINES = "Rule\nCode\rWith\r\nNewlines";
	private final ChecksumAndLogVisitor visitor = new ChecksumAndLogVisitor();

	private final AppliedRule appliedRule = new AppliedRuleImpl();
	private final TestLog4jLoggingAppender appender = TestLog4jLoggingAppender.newBuilder().build();
	
	private static final Logger LOG = (Logger) LogManager.getLogger(PromotionLogSupport.RULECODE.getLoggerName());
	
	/**
	 * Add appender to loggers under test.
	 */
	@Before
	public void setUp() {
		appender.start();
		LOG.addAppender(appender);
	}

	@After
	public void tearDown() {
		appender.stop();
		LOG.removeAppender(appender);
	}
	
	/**
	 * Test that the visit method logs appropriately.
	 */
	@Test
	public void testBasicLoggingAndChecksumming() {
		
		appliedRule.setRuleCode(RULE_CODE);
		
		// add expectations
		appender.addMessageToVerify(Level.TRACE, "CHECKSUM: c38bddc59cff110525939cc537f06e0f RULECODE: Rule Code");
		

		visitor.visit(appliedRule);
		
		// ensure all the expectations are met
		appender.verify();
	}

	
	/**
	 * We squish the log messages so they are easier to read (and sed/grep) in the log files.
	 */
	@Test
	public void logMessagesShouldAppearOnOneLine() {
		
		LOG.addAppender(appender);
		
		appliedRule.setRuleCode(RULE_CODE_WITH_NEWLINES);

		// add expectations
		appender.addMessageToVerify(Level.TRACE, "CHECKSUM: c588f7f3e6ceeff5848c6eb707ad4975 RULECODE: Rule\\nCode\\nWith\\nNewlines");

		visitor.visit(appliedRule);
		
		appender.verify();
	}
	
	/**
	 * 
	 */
	@Test
	public void appliedRuleShouldHaveRuleCodeReplacedWithChecksumForPersisting() {
		appliedRule.setRuleCode(RULE_CODE);
		
		visitor.visit(appliedRule);
		
		assertEquals("c38bddc59cff110525939cc537f06e0f", appliedRule.getRuleCode());
	}	
}
