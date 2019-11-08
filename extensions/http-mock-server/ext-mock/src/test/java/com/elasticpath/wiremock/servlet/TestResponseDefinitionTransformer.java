package com.elasticpath.wiremock.servlet;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * This is a mock class that is used by unit tests to verify the proper load and configuration of Extensions by the EP WireMock framework.
 */
public class TestResponseDefinitionTransformer extends ResponseDefinitionTransformer {

	/**
	 * The key used in the extension map.  Test cases can use this key to validate that this extension is properly loaded.
	 */
	public static final String TEST_EXTENSION_KEY = "test-extension";

	@Override
	public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource files,
			final Parameters parameters) {
		return responseDefinition;
	}

	@Override
	public String getName() {
		return TEST_EXTENSION_KEY;
	}
}