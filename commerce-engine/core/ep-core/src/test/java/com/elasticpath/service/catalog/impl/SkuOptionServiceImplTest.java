/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;
import com.elasticpath.service.catalog.SkuOptionKeyExistException;

/**
 * Test suite for <code>SkuOptionServiceImpl</code>.
 */
public class SkuOptionServiceImplTest {
	private static final String SKU_OPTION_FIND_UIDPK_BY_KEY = "SKU_OPTION_FIND_UIDPK_BY_KEY";

	private static final String SERVICE_EXCEPTION_EXPECTED = "SKU Option with the given key already exists";
	public static final String SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE = "SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE";

	private SkuOptionServiceImpl skuOptionService;

	private ProductDao mockProductDao;

	private JpaPersistenceEngine jpaPersistenceEngine;

	private ElasticPath elasticPath;

	@Before
	public void setUp() throws Exception {
		mockProductDao = mock(ProductDao.class);
		elasticPath = mock(ElasticPath.class);
		jpaPersistenceEngine = mock(JpaPersistenceEngine.class);

		skuOptionService = new SkuOptionServiceImpl();
		skuOptionService.setPersistenceEngine(jpaPersistenceEngine);
		skuOptionService.setProductDao(mockProductDao);
		skuOptionService.setElasticPath(elasticPath);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.setPersistenceEngine(PersistenceEngine)'.
	 */
	@Test
	public void testSetPersistenceEngine() {
		skuOptionService.setPersistenceEngine(null);
		assertThatThrownBy(() -> skuOptionService.add(new SkuOptionImpl()))
				.isInstanceOf(EpServiceException.class);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.getPersistenceEngine()'.
	 */
	@Test
	public void testGetPersistenceEngine() {
		assertNotNull(skuOptionService.getPersistenceEngine());
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.add(SkuOption)'.
	 */
	@Test
	public void testAdd() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionKeyAdd";
		skuOption.setOptionKey(key);

		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(new ArrayList<>());

		skuOptionService.add(skuOption);

		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
		verify(jpaPersistenceEngine).save(skuOption);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.add(SkuOption)'.
	 */
	@Test
	public void testAddKeyExists() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionKeyAdd";
		final long uidPk = 123456;
		skuOption.setUidPk(uidPk);
		skuOption.setOptionKey(key);
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(Collections.singletonList(uidPk));

		assertThatExceptionOfType(SkuOptionKeyExistException.class).isThrownBy(() -> skuOptionService.add(skuOption))
				.withMessage(SERVICE_EXCEPTION_EXPECTED)
				.withNoCause();

		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.load(Long)'.
	 */
	@Test
	public void testLoad() {
		final long uid = 1234L;
		final SkuOptionImpl skuOption = new SkuOptionImpl();
		skuOption.setOptionKey("skuOptionKeyLoad");
		skuOption.setUidPk(uid);
		// expectations
		Mockito.<Class<SkuOptionImpl>>when(elasticPath.getBeanImplClass(ContextIdNames.SKU_OPTION)).thenReturn(SkuOptionImpl.class);
		when(jpaPersistenceEngine.load(SkuOptionImpl.class, uid)).thenReturn(skuOption);

		final SkuOption loadedSkuOption = skuOptionService.load(uid);
		assertThat(skuOption).isEqualTo(loadedSkuOption);
		verify(elasticPath).getBeanImplClass(ContextIdNames.SKU_OPTION);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.update(SkuOption)'.
	 */
	@Test
	public void testUpdate() {
		final SkuOption skuOption = new SkuOptionImpl();
		final SkuOption updatedSkuOption = new SkuOptionImpl();
		final String key = "skuOptionUpdate";
		final long uidPk = 123456;
		skuOption.setUidPk(uidPk);
		skuOption.setOptionKey(key);
		updatedSkuOption.setUidPk(uidPk);
		updatedSkuOption.setOptionKey(key);

		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(new ArrayList<>());
		when(jpaPersistenceEngine.update(skuOption)).thenReturn(updatedSkuOption);

		final List<Long> productUids = Collections.singletonList(1L);
		when(mockProductDao.findUidsBySkuOption(updatedSkuOption)).thenReturn(productUids);

		SkuOption returnedSkuOption = skuOptionService.update(skuOption);

		assertThat(updatedSkuOption).isEqualTo(returnedSkuOption);

		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
		verify(jpaPersistenceEngine).update(skuOption);
		verify(mockProductDao).findUidsBySkuOption(updatedSkuOption);
		verify(mockProductDao).updateLastModifiedTimes(productUids);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.update(SkuOption)'.
	 */
	@Test
	public void testUpdateKeyExists() {
		final SkuOption skuOption1 = new SkuOptionImpl();
		final String key1 = "skuOptionKeyUpdate1";
		skuOption1.setOptionKey(key1);
		skuOption1.setUidPk(1);
		final SkuOption skuOption2 = new SkuOptionImpl();
		final String key2 = "skuOptionKeyUpdate2";
		skuOption2.setOptionKey(key2);
		skuOption2.setUidPk(2);

		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key2))
				.thenReturn(Collections.singletonList(skuOption1.getUidPk()));

		assertThatExceptionOfType(SkuOptionKeyExistException.class).isThrownBy(() -> skuOptionService.update(skuOption2))
				.withMessage(SERVICE_EXCEPTION_EXPECTED);

		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key2);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.remove(SkuOption)'.
	 */
	@Test
	public void testDelete() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionKeyAdd";
		skuOption.setOptionKey(key);

		skuOptionService.remove(skuOption);

		verify(jpaPersistenceEngine).delete(skuOption);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.list()'.
	 */
	@Test
	public void testList() {
		final List<Object> skuOptionList = Collections.singletonList(any());
		// expectations
		when(jpaPersistenceEngine.retrieveByNamedQuery("SKU_OPTION_SELECT_ALL_EAGER")).thenReturn(skuOptionList);

		final List<SkuOption> retrievedSkuOptionList = skuOptionService.list();

		assertThat(skuOptionList).isEqualTo(retrievedSkuOptionList);
		verify(jpaPersistenceEngine).retrieveByNamedQuery("SKU_OPTION_SELECT_ALL_EAGER");
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.findByKey(String)'.
	 */
	@Test
	public void testFindByKey() {
		final String key = "skuOptionKeyFind";
		final SkuOption skuOption = new SkuOptionImpl();
		skuOption.setOptionKey(key);

		when(jpaPersistenceEngine.retrieveByNamedQuery("SKU_OPTION_FIND_BY_KEY", key)).thenReturn(Collections.singletonList(skuOption));

		assertThat(skuOption).isEqualTo(skuOptionService.findByKey(key));
		verify(jpaPersistenceEngine).retrieveByNamedQuery("SKU_OPTION_FIND_BY_KEY", key);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.findByKey(String)'.
	 */
	@Test
	public void testFindUidPkByKey() {
		final String key = "skuOptionKeyFind";
		final long uidPk = 123456;

		final SkuOption skuOption = new SkuOptionImpl();
		skuOption.setUidPk(uidPk);
		skuOption.setOptionKey(key);
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key))
				.thenReturn(Collections.singletonList(skuOption.getUidPk()));

		assertThat(uidPk).isEqualTo(skuOptionService.findUidPkByKey(key));
		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.keyExists(String)'.
	 */
	@Test
	public void testKeyExists() {
		final String key = "skuOptionKey";
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(new ArrayList<>());
		assertThat(skuOptionService.keyExists(key)).isFalse();
		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);

		final long uidPk = 123456;
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(Collections.singletonList(uidPk));
		assertThat(skuOptionService.keyExists(key)).isTrue();
		verify(jpaPersistenceEngine, times(2)).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
	}

	/**
	 * Test method for 'com.elasticpath.service.SkuOptionServiceImpl.keyExists(SkuOption)'.
	 */
	@Test
	public void testKeyExistsSkuOption() {
		final List<Object> skuOptionUidPkList = new ArrayList<>();
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionKey";
		final long uidPk = 123456;
		final long uidPk2 = 234567;

		skuOption.setOptionKey(key);
		skuOption.setUidPk(uidPk);
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(skuOptionUidPkList);
		assertThat(skuOptionService.keyExists(skuOption)).isFalse();
		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);

		skuOptionUidPkList.add(uidPk2);
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key)).thenReturn(skuOptionUidPkList);
		assertThat(skuOptionService.keyExists(skuOption)).isTrue();
		verify(jpaPersistenceEngine, times(2)).retrieveByNamedQuery(SKU_OPTION_FIND_UIDPK_BY_KEY, key);
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#findAllSkuOptionFromCatalog(long)}.
	 */
	@Test
	public void testFindAllSkuOptionFromCatalog() {
		final List<SkuOption> skuOptionList = new ArrayList<>();
		// expectations
		final SkuOption skuOption = new SkuOptionImpl();
		final long skuOptionUid = 1234L;
		skuOption.setUidPk(skuOptionUid);
		skuOptionList.add(skuOption);
		// expectations
		when(jpaPersistenceEngine.retrieveByNamedQuery("SKU_OPTION_SELECT_CATALOG_ALL_EAGER", skuOptionUid))
				.thenReturn(Collections.singletonList(skuOption));

		assertThat(skuOptionList).isEqualTo(skuOptionService.findAllSkuOptionFromCatalog(skuOptionUid));
		verify(jpaPersistenceEngine).retrieveByNamedQuery("SKU_OPTION_SELECT_CATALOG_ALL_EAGER", skuOptionUid);
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#isSkuOptionInUse(long)}.
	 */
	@Test
	public void testIsSkuOptionInUse() {
		final long nonExistantSkuOptionUid = 123L;
		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE, nonExistantSkuOptionUid)).thenReturn(new ArrayList<>());

		assertThat(skuOptionService.isSkuOptionInUse(nonExistantSkuOptionUid)).isFalse();
		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE, nonExistantSkuOptionUid);

		final long existantSkuOptionUid = 1L;

		when(jpaPersistenceEngine.retrieveByNamedQuery(SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE, existantSkuOptionUid))
				.thenReturn(Collections.singletonList(existantSkuOptionUid));

		assertThat(skuOptionService.isSkuOptionInUse(existantSkuOptionUid)).isTrue();
		verify(jpaPersistenceEngine).retrieveByNamedQuery(SKU_OPTION_IN_USE_PRODUCT_TYPE_SINGLE, existantSkuOptionUid);
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#isSkuOptionValueInUse(long)}.
	 */
	@Test
	public void testIsSkuOptionValueInUse() {
		final long nonExistantSkuOptionValueUid = 123L;
		when(jpaPersistenceEngine.retrieveByNamedQuery("SKU_OPTIONVALUE_IN_USE_PRODUCT_TYPE_SINGLE", nonExistantSkuOptionValueUid))
				.thenReturn(Collections.emptyList());
		assertThat(skuOptionService.isSkuOptionValueInUse(nonExistantSkuOptionValueUid)).isFalse();

		final long existantSkuOptionValueUid = 1L;
		when(jpaPersistenceEngine.retrieveByNamedQuery("SKU_OPTIONVALUE_IN_USE_PRODUCT_TYPE_SINGLE", existantSkuOptionValueUid))
				.thenReturn(Collections.singletonList(existantSkuOptionValueUid));
		assertThat(skuOptionService.isSkuOptionValueInUse(existantSkuOptionValueUid)).isTrue();
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#notifySkuOptionUpdated(SkuOption)}.
	 */
	@Test
	public final void testNotifySkuOptionUpdated() {
		final SkuOption mockSkuOption = mock(SkuOption.class);
		final List<Long> productUids = new ArrayList<>();

		when(mockProductDao.findUidsBySkuOption(mockSkuOption)).thenReturn(productUids);
		doNothing().when(mockProductDao).updateLastModifiedTimes(productUids);

		skuOptionService.notifySkuOptionUpdated(mockSkuOption);

		verify(mockProductDao).findUidsBySkuOption(mockSkuOption);
		verify(mockProductDao).updateLastModifiedTimes(productUids);
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#add(SkuOptionValue)}.
	 */
	@Test
	public final void testAddOptionValue() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionValueKeyAdd";
		skuOption.setOptionKey(key);
		final SkuOptionValue skuOptionValue = new SkuOptionValueImpl();
		skuOptionValue.setOptionValueKey(key);
		skuOptionValue.setSkuOption(skuOption);

		doNothing().when(jpaPersistenceEngine).save(skuOptionValue);

		skuOptionService.add(skuOptionValue);

		verify(jpaPersistenceEngine).save(skuOptionValue);
	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#update(SkuOptionValue)}.
	 */
	@Test
	public final void testUpdateOptionValue() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionValueKeyUpdate";
		skuOption.setOptionKey(key);
		final SkuOptionValue skuOptionValue = new SkuOptionValueImpl();
		skuOptionValue.setOptionValueKey(key);
		skuOptionValue.setSkuOption(skuOption);

		when(jpaPersistenceEngine.update(skuOptionValue)).thenReturn(skuOptionValue);

		assertThat(skuOptionValue).isEqualTo(skuOptionService.update(skuOptionValue));

		verify(jpaPersistenceEngine).update(skuOptionValue);

	}

	/**
	 * Test method for {@link SkuOptionServiceImpl#remove(SkuOptionValue)}.
	 */
	@Test
	public final void testRemoveOptionValue() {
		final SkuOption skuOption = new SkuOptionImpl();
		final String key = "skuOptionValueKeyRemove";
		skuOption.setOptionKey(key);
		final SkuOptionValue skuOptionValue = new SkuOptionValueImpl();
		skuOptionValue.setOptionValueKey(key);
		skuOptionValue.setSkuOption(skuOption);

		doNothing().when(jpaPersistenceEngine).delete(skuOptionValue);

		skuOptionService.remove(skuOptionValue);

		verify(jpaPersistenceEngine).delete(skuOptionValue);
	}
}
