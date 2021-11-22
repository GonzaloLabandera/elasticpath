package com.elasticpath.xpf.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.xpf.json.SettingValue;

@RunWith(MockitoJUnitRunner.class)
public class XPFSettingValueParserTest {

	@Mock
	private Environment environment;

	@InjectMocks
	private XPFSettingValueParser xpfSettingValueParser;

	@Before
	public void setUp() {
		xpfSettingValueParser.setPropertySources(new ArrayList<>());
	}

	@Test
	public void testResolvePlaceholder() {
		when(environment.getProperty("value")).thenReturn("placeholderValue");

		SettingValue settingValue = new SettingValue("key", "${value}");

		xpfSettingValueParser.resolvePlaceholder(settingValue);

		assertEquals("placeholderValue", settingValue.getValue());
	}

	@Test(expected = EpSystemException.class)
	public void testResolvePlaceholderException() {
		SettingValue settingValue = new SettingValue("key", "${value}");

		xpfSettingValueParser.resolvePlaceholder(settingValue);
	}
}
