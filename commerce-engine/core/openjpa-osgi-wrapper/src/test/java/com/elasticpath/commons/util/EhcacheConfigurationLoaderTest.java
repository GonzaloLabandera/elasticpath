/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.commons.util;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpSystemException;

@RunWith(MockitoJUnitRunner.class)
public class EhcacheConfigurationLoaderTest {

	private static final String RETURNED_RESOURCE_MUST_BE_SAME_AS_ORIGINAL_MSG = "Returned resource must be the same as original";
	private EhcacheConfigurationLoader fixture;
	private File expectedResource;

	@After
	public void cleanUp() {
		if (expectedResource != null && expectedResource.exists()) {
			expectedResource.delete();
		}
	}

	@Test
	public void shouldReturnNullWhenResourceIsNull() {
		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname(null);

		assertThat(fixture.getResource())
				.as("Returned resource must be null")
				.isNull();
	}

	@Test
	public void shouldReturnNullWhenResourceIsStringNull() {
		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("null");

		assertThat(fixture.getResource())
				.as("Returned resource must be null")
				.isNull();
	}

	@Test
	public void shouldThrowExceptionWhenResourceDoesNotExist() {
		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("file:///somewhere/ep.properties");

		assertThatThrownBy(() -> fixture.getResource())
				.isInstanceOf(EpSystemException.class);
	}

	@Test
	public void shouldReturnUrlResourceWhenResourceExists() throws Exception {
		expectedResource = File.createTempFile("tmp", "properties");

		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("file://" + expectedResource.getAbsolutePath());

		assertThat(fixture.getResource().getFile())
				.as(RETURNED_RESOURCE_MUST_BE_SAME_AS_ORIGINAL_MSG)
				.isEqualTo(expectedResource);
	}

	@Test
	public void shouldReturnResourceWhenResourceExists() throws Exception {
		expectedResource = File.createTempFile("tmp", "properties");

		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname(expectedResource.getAbsolutePath());

		assertThat(fixture.getResource().getFile())
				.as(RETURNED_RESOURCE_MUST_BE_SAME_AS_ORIGINAL_MSG)
				.isEqualTo(expectedResource);

	}

	@Test
	public void shouldReturnUserHomeResourceWhenResourceExists() throws Exception {
		String pathname = System.getProperty("user.home") + "/ep/conf/junit-test.temp";
		expectedResource = new File(pathname);
		expectedResource.getParentFile().mkdirs();
		expectedResource.createNewFile();

		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("${user.home}/ep/conf/junit-test.temp");

		assertThat(fixture.getResource().getFile())
				.as(RETURNED_RESOURCE_MUST_BE_SAME_AS_ORIGINAL_MSG)
				.isEqualTo(expectedResource);
	}

	@Test
	public void shouldReturnClasspathResourceWhenResourceExists() throws Exception {
		final String resourcePath = "test.txt";
		final URL expectedClasspathResource = getClass().getClassLoader().getResource(resourcePath);

		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("classpath:" + resourcePath);

		assertThat(fixture.getResource().getURL())
				.as(RETURNED_RESOURCE_MUST_BE_SAME_AS_ORIGINAL_MSG)
				.isEqualTo(expectedClasspathResource);
	}

	@Test
	public void shouldThrowExceptionWhenClasspathResourceDoesNotExist() {
		fixture = new EhcacheConfigurationLoader();
		fixture.setPathname("classpath:somewhere");

		assertThatThrownBy(() -> fixture.getResource())
				.isInstanceOf(EpSystemException.class);
	}
}
