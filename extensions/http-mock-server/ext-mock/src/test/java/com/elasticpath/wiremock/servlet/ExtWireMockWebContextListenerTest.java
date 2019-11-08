package com.elasticpath.wiremock.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for WireMock web context listener ExtWireMockWebContextListener.java.
 *
 * These tests verify that the web context listener properly initializes the WireMockApp instance
 * and the web.xml is properly loaded and the example transformers are properly parsed and loaded.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtWireMockWebContextListenerTest {

	private static final String APP_CONTEXT_KEY = "WireMockApp";

	private static final String TRANSFORMER_CLASSES_KEY = "transformerClasses";
	private static final String TRANSFORMER_CLASSES_VALUE = "com.elasticpath.wiremock.servlet.TestResponseDefinitionTransformer";

	@Mock
	private ServletContext servletContext;

	@InjectMocks
	private ServletContextEvent servletContextEvent;

	@Captor
	private ArgumentCaptor<WireMockApp> wireMockAppCaptor;

	private ExtWireMockWebContextListener classToTest;

	@Test
	public void contextInitialized() {
		when(servletContext.getInitParameter(TRANSFORMER_CLASSES_KEY)).thenReturn(TRANSFORMER_CLASSES_VALUE);
		when(servletContext.getInitParameter(ExtWarConfiguration.WIRE_MOCK_CLASSPATH_FILE_SOURCE_ROOT)).thenReturn("wiremock");

		classToTest = new ExtWireMockWebContextListener();
		classToTest.contextInitialized(servletContextEvent);

		Mockito.verify(servletContext).setAttribute(Matchers.eq(APP_CONTEXT_KEY), wireMockAppCaptor.capture());

		Options options = wireMockAppCaptor.getValue().getOptions();

		assertThat(options.extensionsOfType(TestResponseDefinitionTransformer.class)).isNotEmpty();
		assertThat(options.extensionsOfType(ResponseTemplateTransformer.class)).isNotEmpty();

	}
}