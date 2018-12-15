/*
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.commons.util.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.elasticpath.commons.util.AssetRepository;
import com.elasticpath.service.catalogview.StoreConfig;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Test for {@link StoreResourceManagerImpl}.
 */
public class StoreResourceManagerImplTest {
	private static final String THEME1 = "theme1";

	// this is the instance that spring creates with storeCode = ""
	private static final StoreResourceManagerImpl MAIN_INSTANCE_STORE_RESOURCE_MANAGER =
			(StoreResourceManagerImpl) StoreResourceManagerImpl.getInstance();
	private StoreResourceManagerImpl storeResourceManagerForTestStore;

	private static final String CMASSETS = "cmassets";

	private static final String TESTSTORE = "TESTSTORE";

	private static final String TEST_VM = "test.vm";

	private static final String GLOBAL_VM = "email" + File.separator + "global1.vm";

	private static final String RESOURCE_SHOULD_NOT_BE_NULL = "Resource should not be null";

	private static final String RESOURCE_SHOULD_BE_NULL = "Resource should be null";

	private static final String TESTSTORE_TEMPLATES_TEST_VM = THEME1 + File.separator + TESTSTORE + File.separator + "templates" + File.separator
			+ "velocity" + File.separator + TEST_VM;

	private static final String DEFAULT_TEMPLATES_TEST_VM = THEME1 + File.separator + "default" + File.separator + "templates" + File.separator
			+ "velocity" + File.separator + TEST_VM;

	private static final String GLOBAL_TEMPLATES_TEST_VM = CMASSETS + File.separator + "templates" + File.separator + "velocity" + File.separator
			+ GLOBAL_VM;

	private static final String UTF_8 = "UTF-8";

	private SettingValueProvider<String> themeProvider;

	private static final ResourceLoader STRING_RESOURCE_LOADER = new StringResourceLoader();

	private static final List<String> RESOURCE_NAMES = new ArrayList<>();

	static {
		RESOURCE_NAMES.add(DEFAULT_TEMPLATES_TEST_VM);
		RESOURCE_NAMES.add(TESTSTORE_TEMPLATES_TEST_VM);
		RESOURCE_NAMES.add(TEST_VM);
	}

	/**
	 * @throws Exception if something goes wrong
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		final List<String> resourceLoaderNames = new ArrayList<>();
		final String loaderName = "testResourceLoader";
		resourceLoaderNames.add(loaderName);

		final ExtendedProperties runtimeConfiguration = new ExtendedProperties();
		runtimeConfiguration.addProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoaderNames);

		final StringBuilder loaderID = new StringBuilder(loaderName);
		loaderID.append('.').append(RuntimeConstants.RESOURCE_LOADER);
		runtimeConfiguration.addProperty(loaderID + ".instance", STRING_RESOURCE_LOADER);

		themeProvider = mock(SettingValueProvider.class);
		StoreResourceManagerImpl.setStoreThemeProvider(themeProvider);
		
		final RuntimeServices rsvc = mock(RuntimeServices.class);
		// create a threadlocal
		final StoreConfig mockStoreConfig = mock(StoreConfig.class);

		when(rsvc.getConfiguration()).thenReturn(runtimeConfiguration);
		when(rsvc.getLog()).thenReturn(new Log());
		when(rsvc.getBoolean(any(String.class), any(Boolean.class))).thenReturn(true);
		when(rsvc.getString(any(String.class))).thenReturn(null);
		when(rsvc.getInt(any(String.class), any(Integer.class))).thenReturn(0);
		when(rsvc.parse(any(Reader.class), any(String.class))).thenReturn(new SimpleNode(0));
		when(mockStoreConfig.getStoreCode()).thenReturn(TESTSTORE);

		// attach a storeConfig to our instance
		MAIN_INSTANCE_STORE_RESOURCE_MANAGER.setStoreConfig(mockStoreConfig);
		//create an instance in our map for the store "TESTSTORE"
		storeResourceManagerForTestStore = (StoreResourceManagerImpl) StoreResourceManagerImpl.getInstance();
		storeResourceManagerForTestStore.initialize(rsvc);
	}

	/**
	 * Cleans up some of the objects that need to be static so that one test doesn't influence another.
	 */
	@After
	public void tearDown() {
		// Need to clean up resource listings in repository between tests since it is static and we don't want one test to affect another.
		final StringResourceRepository repo = StringResourceLoader.getRepository();
		for (final String resourceName : RESOURCE_NAMES) {
			repo.removeStringResource(resourceName);
		}
		MAIN_INSTANCE_STORE_RESOURCE_MANAGER.invalidate();

		StoreResourceManagerImpl.setStoreThemeProvider(null);
	}

	/**
	 * Tests that the resource is successfully retrieved when calling
	 * {@link StoreResourceManagerImpl#getResourceFromLastResolvedLocation(String, int, String)} for a resource that already exists in the resource
	 * lookup cache.
	 */
	@Test
	public void testGetResourceFromLastResolvedLocation() throws Exception {
		final Resource testStoreResource = mock(Resource.class);
		when(testStoreResource.getName()).thenReturn(TESTSTORE_TEMPLATES_TEST_VM);
		storeResourceManagerForTestStore.getResourceLookupCache().put(TEST_VM, testStoreResource);

		final StringResourceRepository repo = StringResourceLoader.getRepository();
		repo.putStringResource(TESTSTORE_TEMPLATES_TEST_VM, "");
		Resource resource = storeResourceManagerForTestStore
					.getResourceFromLastResolvedLocation(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		assertThat(resource).as(RESOURCE_SHOULD_NOT_BE_NULL).isNotNull();
		verify(testStoreResource).getName();
	}

	/**
	 * Tests that the resource is not retrieved when calling
	 * {@link StoreResourceManagerImpl#getResourceFromLastResolvedLocation(String, int, String)} for a resource that does not exist in the resource
	 * lookup cache.
	 */
	@Test
	public void testGetResourceFromLastResolvedLocationWhenNotYetLoaded() throws Exception {
		Resource resource = storeResourceManagerForTestStore.getResourceFromLastResolvedLocation(TEST_VM,
			ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		assertThat(resource).as(RESOURCE_SHOULD_BE_NULL).isNull();
	}

	/**
	 * Tests that the resource is not retrieved when calling
	 * {@link StoreResourceManagerImpl#getResourceFromLastResolvedLocation(String, int, String)} for a resource that exists in the resource lookup
	 * cache, but can no longer be found by the resource loaders. Also verify that the entry is then removed from the lookup cache.
	 */
	@Test
	public void testGetResourceFromLastResolvedLocationWhenNoLongerThere() throws Exception {
		final Resource testStoreResource = mock(Resource.class);
		when(testStoreResource.getName()).thenReturn(TESTSTORE_TEMPLATES_TEST_VM);
		storeResourceManagerForTestStore.getResourceLookupCache().put(TEST_VM, testStoreResource);

		Resource resource = storeResourceManagerForTestStore.getResourceFromLastResolvedLocation(TEST_VM,
			ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		assertThat(resource).as(RESOURCE_SHOULD_BE_NULL).isNull();
		assertThat(storeResourceManagerForTestStore.getResourceLookupCache().get(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.as("Resource should have been removed from the cache")
			.isNull();
		verify(testStoreResource).getName();
	}

	/**
	 * Tests that a store-specific resource is successfully retrieved when calling
	 * {@link StoreResourceManagerImpl#getResourceStoreSpecific(java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetResourceStoreSpecific() throws Exception {
		final StringResourceRepository repo = StringResourceLoader.getRepository();
		repo.putStringResource(TESTSTORE_TEMPLATES_TEST_VM, "");
		Resource resource = storeResourceManagerForTestStore.getResourceStoreSpecific(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1);
		assertThat(resource).as(RESOURCE_SHOULD_NOT_BE_NULL).isNotNull();
	}

	/**
	 * Tests that a resource is successfully retrieved from the theme's default directory when calling
	 * {@link StoreResourceManagerImpl#getResourceFromThemeDefault(java.lang.String, int, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetResourceFromThemeDefault() throws Exception {
		final StringResourceRepository repo = StringResourceLoader.getRepository();
		repo.putStringResource(DEFAULT_TEMPLATES_TEST_VM, "");
		Resource resource = storeResourceManagerForTestStore.getResourceFromThemeDefault(TEST_VM,
			ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1);
		assertThat(resource).as(RESOURCE_SHOULD_NOT_BE_NULL).isNotNull();
	}

	/**
	 * Tests that the various methods for resolving a store resource get called in the correct order when calling
	 * {@link StoreResourceManagerImpl#getResource(String, int, String)}.
	 */
	@Test
	public void testGetResourceFallbackLogicForStoreResource() throws Exception {
		final StoreResourceManagerImpl mockStoreResourceManager = mock(StoreResourceManagerImpl.class);
		final InOrder fallbackSequence = Mockito.inOrder(mockStoreResourceManager);
		final StoreConfig storeConfig = mock(StoreConfig.class, "willFallback");

		when(mockStoreResourceManager.getResourceFromLastResolvedLocation(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.thenReturn(null);
		when(mockStoreResourceManager.getResourceStoreSpecific(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1))
			.thenReturn(null);
		when(mockStoreResourceManager.getResourceFromThemeDefault(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1))
			.thenReturn(null);
		when(mockStoreResourceManager.getResourceFromParent(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.thenReturn(null);

		when(storeConfig.getStoreCode()).thenReturn(TESTSTORE);
		when(storeConfig.getSettingValue(themeProvider)).thenReturn(THEME1);
		
		final StoreResourceManagerImpl resourceManagerWithFallbackTracking = new TraceableStoreResourceManager(mockStoreResourceManager);
		resourceManagerWithFallbackTracking.setStoreConfig(storeConfig);

		resourceManagerWithFallbackTracking.getResource(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);

		fallbackSequence.verify(mockStoreResourceManager).getResourceFromLastResolvedLocation(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		fallbackSequence.verify(mockStoreResourceManager).getResourceStoreSpecific(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1);
		fallbackSequence.verify(mockStoreResourceManager).getResourceFromThemeDefault(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8, THEME1);
		fallbackSequence.verify(mockStoreResourceManager).getResourceFromParent(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
	}

	/**
	 * Tests that the various methods for resolving a CM resource get called in the correct order when calling
	 * {@link StoreResourceManagerImpl#getResource(String, int, String)}.
	 */
	@Test
	public void testGetResourceFallbackLogicForCmResource() throws Exception {
		final StoreResourceManagerImpl mockStoreResourceManager = mock(StoreResourceManagerImpl.class);
		final InOrder fallbackSequence = Mockito.inOrder(mockStoreResourceManager);
		final StoreConfig storeConfig = mock(StoreConfig.class, "willFallback");

		when(mockStoreResourceManager.getResourceFromLastResolvedLocation(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.thenReturn(null);
		when(mockStoreResourceManager.getResourceCMGlobal(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.thenReturn(null);
		when(mockStoreResourceManager.getResourceFromParent(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8))
			.thenReturn(null);
		when(storeConfig.getStoreCode()).thenReturn(null);

		final StoreResourceManagerImpl resourceManagerWithFallbackTracking = new TraceableStoreResourceManager(mockStoreResourceManager);
		resourceManagerWithFallbackTracking.setStoreConfig(storeConfig);

		resourceManagerWithFallbackTracking.getResource(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);

		fallbackSequence.verify(mockStoreResourceManager).getResourceFromLastResolvedLocation(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		fallbackSequence.verify(mockStoreResourceManager).getResourceCMGlobal(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		fallbackSequence.verify(mockStoreResourceManager).getResourceFromParent(TEST_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
	}

	/**
	 * Tests that a CM global resource is successfully retrieved when calling
	 * {@link StoreResourceManagerImpl#getResourceCMGlobal(java.lang.String, int, java.lang.String)}.
	 */
	@Test
	public void testGetResourceCMGlobal() throws Exception {
		final AssetRepository assetRepository = mock(AssetRepository.class);

		when(assetRepository.getCmAssetsSubfolder()).thenReturn(CMASSETS);

		storeResourceManagerForTestStore.setAssetRepository(assetRepository);
		final StringResourceRepository repo = StringResourceLoader.getRepository();
		repo.putStringResource(GLOBAL_TEMPLATES_TEST_VM, "");

		Resource resource = storeResourceManagerForTestStore.getResourceCMGlobal(GLOBAL_VM, ResourceManager.RESOURCE_TEMPLATE, UTF_8);
		assertThat(resource).as(RESOURCE_SHOULD_NOT_BE_NULL).isNotNull();
		verify(assetRepository).getCmAssetsSubfolder();
	}

	/**
	 * Tests that a resource loader is successfully found when {@link StoreResourceManagerImpl#getLoaderNameForResource(String)} is called for an
	 * existing resource.
	 */
	@Test
	public void testGetLoaderNameForStoreResource() {
		final StringResourceRepository repo = StringResourceLoader.getRepository();
		repo.putStringResource(TESTSTORE_TEMPLATES_TEST_VM, "");
		assertThat(storeResourceManagerForTestStore.getLoaderNameForResource(TESTSTORE_TEMPLATES_TEST_VM))
			.as("Resource Loader should have been found for this resource.")
			.isNotNull();
	}

	/**
	 * Tests that a resource loader is null when {@link StoreResourceManagerImpl#getLoaderNameForResource(String)} is called for a resource that does
	 * not exist.
	 */
	@Test
	public void testGetLoaderNameForNonExistantResource() {
		assertThat(storeResourceManagerForTestStore.getLoaderNameForResource(TESTSTORE_TEMPLATES_TEST_VM))
			.as("Resource Loader should be null for this resource.")
			.isNull();
	}

	/**
	 * Customization of {@link StoreResourceManagerImpl} that can have a mock object set as a delegate, allowing internal method calls to be traced.
	 */
	private class TraceableStoreResourceManager extends StoreResourceManagerImpl {
		private final StoreResourceManagerImpl delegate;

		TraceableStoreResourceManager(final StoreResourceManagerImpl delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		protected Resource getResourceCMGlobal(final String resourceName, final int resourceType, final String encoding) throws Exception {
			return delegate.getResourceCMGlobal(resourceName, resourceType, encoding);
		}

		@Override
		protected Resource getResourceFromLastResolvedLocation(final String resourceName, final int resourceType, final String encoding)
				throws Exception {
			return delegate.getResourceFromLastResolvedLocation(resourceName, resourceType, encoding);
		}

		@Override
		protected Resource getResourceFromParent(final String resourceName, final int resourceType, final String encoding) throws Exception {
			return delegate.getResourceFromParent(resourceName, resourceType, encoding);
		}

		@Override
		protected Resource getResourceFromThemeDefault(final String resourceName, final int resourceType, final String encoding, final String theme)
				throws Exception {
			return delegate.getResourceFromThemeDefault(resourceName, resourceType, encoding, theme);
		}

		@Override
		protected Resource getResourceStoreSpecific(final String resourceName, final int resourceType, final String encoding, final String theme)
				throws Exception {
			return delegate.getResourceStoreSpecific(resourceName, resourceType, encoding, theme);
		}
	}
}
