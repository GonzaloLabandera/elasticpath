package com.elasticpath.wiremock.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.extension.Extension;

import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for Wiremock Servlet configuration class ExtWarConfiguration.java.
 *
 * These tests verify that the feature to add transformer extensions are functioning correctly.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtWarConfigurationTest {

	private static final String TEST_EXTENSION = "com.elasticpath.wiremock.servlet.TestResponseDefinitionTransformer";

	private static final String INTERNAL_TEST_EXTENSION_KEY = "test-extension";

	@InjectMocks
	private ExtWarConfiguration classToTest;

	@Test
	public void extensionFromString() {
		classToTest.extensions(TEST_EXTENSION);

		assertThat(classToTest.extensionsOfType(Extension.class))
			.containsKey(TestResponseDefinitionTransformer.TEST_EXTENSION_KEY);
	}

	@Test
	public void extensionFromClass() {
		classToTest.extensions(() -> INTERNAL_TEST_EXTENSION_KEY);

		assertThat(classToTest.extensionsOfType(Extension.class))
				.containsKey(INTERNAL_TEST_EXTENSION_KEY);
	}


}