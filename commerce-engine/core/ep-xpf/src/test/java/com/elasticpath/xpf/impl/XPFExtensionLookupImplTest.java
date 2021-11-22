/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;

import com.elasticpath.test.xpf.TestLog4jLoggingAppender;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;

@RunWith(MockitoJUnitRunner.class)
public class XPFExtensionLookupImplTest {

	private static final int EXECUTION_TIME_OVER_THRESHOLD = 120;

	@Mock
	private PluginManager pluginManager;

	@Mock
	private XPFExtensionResolver extensionPointResolver;

	@InjectMocks
	private XPFExtensionLookupImpl extensionLookup;

	private final TestLog4jLoggingAppender appender = TestLog4jLoggingAppender.newBuilder().build();

	private static final Logger LOGGER = (Logger) LogManager.getLogger(XPFTimingInvocationHandler.class);

	@Before
	public void init() {
		final List<StubbedExtensionPoint> extensions = new ArrayList<>(2);
		final StubbedExtension1 stubbedExtension1 = new StubbedExtension1();
		final StubbedExtension2 stubbedExtension2 = new StubbedExtension2();
		extensions.add(stubbedExtension1);
		extensions.add(stubbedExtension2);

		when(pluginManager.getExtensions(StubbedExtensionPoint.class))
				.thenReturn(extensions);

		List<StubbedExtensionPoint> proxiedExtensions = extensions.stream().map(ext ->
				(StubbedExtensionPoint)
						Proxy.newProxyInstance(ext.getClass().getClassLoader(),
								ext.getClass().getInterfaces(),
								new XPFTimingInvocationHandler<ExtensionPoint>(ext))
		).collect(Collectors.toList());

		when(extensionPointResolver.resolveExtensionPoints(ArgumentMatchers.<StubbedExtensionPoint>anyList(), eq(null), eq(null)))
				.thenReturn(proxiedExtensions);

		appender.start();
		LOGGER.addAppender(appender);
	}

	@Test
	public void testGetMultipleExtensions() {
		List<StubbedExtensionPoint> results = extensionLookup.getMultipleExtensions(StubbedExtensionPoint.class, null, null);
		assertEquals(2, results.size());
	}

	@Test
	public void testGetSingleExtension() {
		StubbedExtensionPoint stubbedExtension = extensionLookup.getSingleExtension(StubbedExtensionPoint.class, null, null);
		assertNotNull(stubbedExtension);
	}

	@Test
	public void testGetMultipleExtensionsTimeLogging() {
		List<StubbedExtensionPoint> results = extensionLookup.getMultipleExtensions(StubbedExtensionPoint.class, null, null);
		appender.addMessageToVerify(Level.TRACE, "ParameterizedMessage[messagePattern=Extension class {} method {} took {} ms to execute., "
						+ "stringArgs=[com.elasticpath.xpf.impl.XPFExtensionLookupImplTest.StubbedExtension1, testMethod, 0], throwable=null]");
		appender.addRawMessageToVerify(Level.WARN, "Extension class {} method {} took {} ms to execute.");

		results.get(0).testMethod();
		results.get(1).testMethod();

		appender.verify();
	}

	@After
	public void clearLoggers() {
		appender.stop();
		LOGGER.removeAppender(appender);
	}

	public interface StubbedExtensionPoint extends ExtensionPoint {
		void testMethod();
	}


	@XPFEmbedded
	@Extension
	public class StubbedExtension1 extends XPFExtensionPointImpl implements StubbedExtensionPoint  {
		@Override
		public void testMethod() {
			// Do nothing
		}
	}

	@Extension
	public class StubbedExtension2 extends XPFExtensionPointImpl implements StubbedExtensionPoint {
		@Override
		public void testMethod() {
			try {
				Thread.sleep(EXECUTION_TIME_OVER_THRESHOLD);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}
}
