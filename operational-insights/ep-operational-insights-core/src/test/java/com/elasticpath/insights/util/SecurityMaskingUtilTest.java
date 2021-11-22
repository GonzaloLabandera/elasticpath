/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests {@link SecurityMaskingUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityMaskingUtilTest {

	private final List<String> keywords = Arrays.asList("password", "secret");

	@Test
	public void testMaskValuesWithMatchingKeysWithValidInput() {
		List<String> propertyStrings = Arrays.asList("-Depdb.synctarget.password=stPassword", "-Dep.db.secret=", "-Dep.smtp.secret.password=test",
				"-Depdb.password=dbPassword", "-Depdb.password.secondary=dbPassword");

		assertEquals(
				Arrays.asList("-Depdb.synctarget.password=*MASKED*", "-Dep.db.secret=*MASKED*", "-Dep.smtp.secret.password=*MASKED*",
						"-Depdb.password=*MASKED*", "-Depdb.password.secondary=*MASKED*"),
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}

	@Test
	public void testMaskValuesWithMatchingKeysWithKeywordInMiddleOfKey() {
		List<String> propertyStrings = Collections.singletonList("-Depdb.password.secondary=dbPassword");

		assertEquals(
				Collections.singletonList("-Depdb.password.secondary=*MASKED*"),
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}

	@Test
	public void testMaskValuesWithMatchingKeysWithValidCaseDifferenceInput() {
		List<String> propertyStrings = Arrays.asList("-Depdb.synctarget.Password=stPassword", "-DEP.DB.SECRET=", "-Dep.smtp.SECRET.Password=test");
		assertEquals(Arrays.asList("-Depdb.synctarget.Password=*MASKED*", "-DEP.DB.SECRET=*MASKED*", "-Dep.smtp.SECRET.Password=*MASKED*"),
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}

	@Test
	public void testMaskValuesWithMatchingKeysWithSubstringMatchInput() {
		List<String> propertyStrings = Arrays.asList("-Depdb.synctarget.primaryPassword=stPassword", "-DEP.DB.SHASECRET=");
		assertEquals(Arrays.asList("-Depdb.synctarget.primaryPassword=*MASKED*", "-DEP.DB.SHASECRET=*MASKED*"),
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}

	@Test
	public void testMaskValuesWithMatchingKeysWithEmptyString() {
		List<String> propertyStrings = Arrays.asList("", "");
		assertEquals(propertyStrings,
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}

	@Test
	public void testMaskValuesWithMatchingKeysWithNoMatchingInput() {
		List<String> propertyStrings = Arrays.asList("-Djdk.tls.ephemeralDHKeySize=2048", "-Xms1536m");
		assertEquals(propertyStrings,
				SecurityMaskingUtil.maskValuesWithMatchingKeys(keywords, propertyStrings));
	}
}