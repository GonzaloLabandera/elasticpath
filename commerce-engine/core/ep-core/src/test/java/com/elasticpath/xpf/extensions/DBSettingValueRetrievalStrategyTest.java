package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.service.setting.DBSettingValueRetrievalStrategy;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.xpf.connectivity.context.XPFSettingValueRetrievalContext;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;

@RunWith(MockitoJUnitRunner.class)
public class DBSettingValueRetrievalStrategyTest {

	private static final String SETTING_PATH = "settingPath";
	private static final String SETTING_CONTEXT = "settingContext";

	@Mock
	private SettingsReader settingsReader;
	@InjectMocks
	private DBSettingValueRetrievalStrategy strategy;

	@Test
	public void getSettingValueEmptyTest() {
		when(settingsReader.getSettingValue(any(), any())).thenThrow(new EpServiceException("Test error message."));

		Optional<XPFSettingValue> settingValue = strategy.getSettingValue(new XPFSettingValueRetrievalContext(SETTING_PATH, SETTING_CONTEXT));
		assertFalse(settingValue.isPresent());
		verify(settingsReader).getSettingValue(any(), any());
	}
}
