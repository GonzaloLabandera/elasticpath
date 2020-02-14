package com.elasticpath.commons.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.drools.core.util.StringUtils;
import org.junit.Test;

/**
 * Tests {@link EmailAddressUtil}.
 */
public class EmailAddressUtilTest {

	public static final String TEST_EMAIL_ADDRESS_1 = "test1@test.com";
	public static final String TEST_EMAIL_ADDRESS_2 = "test2@test.com";

	@Test
	public void testInline() {

		// given
		List<String> emailAddressList = Arrays.asList(TEST_EMAIL_ADDRESS_1, TEST_EMAIL_ADDRESS_2, null);

		// when
		final String inlineEmailAddress = EmailAddressUtil.inline(emailAddressList);

		// verify
		assertThat(inlineEmailAddress).isEqualTo("test1@test.com,test2@test.com");

	}

	@Test
	public void testInlineWithNullList() {

		// when
		final String inlineEmailAddress = EmailAddressUtil.inline(null);

		// verify
		assertThat(inlineEmailAddress).isEqualTo(StringUtils.EMPTY);

	}

	@Test
	public void testSplitWithSpace() {

		// given
		List<String> emailAddressList = Arrays.asList(TEST_EMAIL_ADDRESS_1, TEST_EMAIL_ADDRESS_2);

		// when
		List<String> result = EmailAddressUtil.split("test1@test.com, test2@test.com");

		// verify
		assertThat(emailAddressList).isEqualTo(result);

	}

	@Test
	public void testSplitWithNull() {

		// when
		List<String> result = EmailAddressUtil.split(null);

		// verify
		assertThat(result).isEmpty();

	}

}
