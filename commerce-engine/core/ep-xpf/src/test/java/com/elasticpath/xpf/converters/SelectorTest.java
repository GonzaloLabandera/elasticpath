package com.elasticpath.xpf.converters;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

import com.elasticpath.xpf.json.Selector;
import com.elasticpath.xpf.exception.InvalidConfigurationException;

public class SelectorTest {

	@Test
	public void testLoadExtensionConfigurationWithEmptySelector() {
		assertThatThrownBy(() -> new Selector("XPFExtensionSelectorByStoreCode", null))
				.isInstanceOf(InvalidConfigurationException.class)
				.hasMessage("Value for extension selector is absent. Selector type is XPFExtensionSelectorByStoreCode");
	}

	@Test
	public void testLoadExtensionConfigurationWithWrongSelectorType() {
		assertThatThrownBy(() -> new Selector("XPFExtensionSelectorWrongName", "storeCode"))
				.isInstanceOf(InvalidConfigurationException.class)
				.hasMessage("Wrong extension selector type: XPFExtensionSelectorWrongName");
	}
}
