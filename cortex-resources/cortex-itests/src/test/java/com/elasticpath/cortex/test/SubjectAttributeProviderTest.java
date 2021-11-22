package com.elasticpath.cortex.test;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.rest.identity.attribute.CurrencySubjectAttribute;
import com.elasticpath.rest.identity.attribute.LocaleSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.attribute.UserTraitSubjectAttribute;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.impl.SubjectAttributeProviderImpl;
import com.elasticpath.rest.relos.rs.subject.attribute.SubjectAttributeProvider;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.context.builders.HttpTagSetContextBuilder;
import com.elasticpath.xpf.extensions.CurrencyTagSetPopulator;
import com.elasticpath.xpf.extensions.UserTraitsTagSetPopulator;
import com.elasticpath.xpf.extensions.LocaleTagSetPopulator;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFInMemoryExtensionResolverImpl;

public class SubjectAttributeProviderTest extends BasicSpringContextTest {

	@Autowired
	private XPFExtensionLookup extensionLookup;

	@Autowired
	private HttpTagSetContextBuilder httpTagSetContextBuilder;

	@Autowired
	private XPFInMemoryExtensionResolverImpl resolver;

	private SubjectAttributeProvider subjectAttributeProvider;
	private Store store;

	@Before
	public void setUp() {
		// must start with an order for this to work
		Catalog catalog = getTac().getPersistersFactory().getCatalogTestPersister().persistDefaultMasterCatalog();
		Warehouse warehouse = getTac().getPersistersFactory().getStoreTestPersister().persistDefaultWarehouse();
		store = getTac().getPersistersFactory().getStoreTestPersister().persistDefaultStore(catalog, warehouse);

		SubjectAttributeProviderImpl attributeProvider = new SubjectAttributeProviderImpl();
		attributeProvider.setExtensionLookup(extensionLookup);
		attributeProvider.setHttpTagSetContextBuilder(httpTagSetContextBuilder);
		subjectAttributeProvider = attributeProvider;
	}

	@Test
	public void testGetSubjectAttributesWithDefaultExtensions() {
		Collection<SubjectAttribute> attributes = subjectAttributeProvider.getSubjectAttributes(createRequest());

		Assertions.assertThat(attributes).hasOnlyElementsOfTypes(CurrencySubjectAttribute.class, UserTraitSubjectAttribute.class,
				LocaleSubjectAttribute.class);
	}

	@Test
	public void testGetSubjectAttributesWithMinimalExtensions() {
		resolver.removeExtensionFromSelector(CurrencyTagSetPopulator.class.getName(), null, XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR,
				new XPFExtensionSelectorAny());
		resolver.removeExtensionFromSelector(UserTraitsTagSetPopulator.class.getName(), null, XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR,
				new XPFExtensionSelectorAny());
		resolver.removeExtensionFromSelector(LocaleTagSetPopulator.class.getName(), null, XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR,
				new XPFExtensionSelectorAny());


		Collection<SubjectAttribute> attributes = subjectAttributeProvider.getSubjectAttributes(new MockHttpServletRequest());

		Assertions.assertThat(attributes).isEmpty();
	}

	private HttpServletRequest createRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("x-ep-user-scope", store.getCode());
		request.addHeader("x-ep-user-trait", "user-trait=value");
		return request;
	}
}
