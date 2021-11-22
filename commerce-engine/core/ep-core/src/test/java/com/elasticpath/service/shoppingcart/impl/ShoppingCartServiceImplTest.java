/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.shoppingcart.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.shopper.impl.ShopperImpl;
import com.elasticpath.domain.shopper.impl.ShopperMementoImpl;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartMementoImpl;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.store.StoreService;

/**
 * Test suite for <code>ShoppingCartServiceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartServiceImplTest {

	public static final String GUID = "guid";

	@Spy
	@InjectMocks
	private ShoppingCartServiceImpl shoppingCartServiceImpl;

	@Mock
	private StoreService storeService;

	@Mock
	private ShopperService shopperService;

	@Mock
	private TimeService timeService;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private ElasticPath elasticpath;

	@Mock
	private FetchPlanHelper fetchPlanHelper;

	@Mock
	private BeanFactory beanFactory;


	private StoreImpl store;

	private ShopperImpl shopper;

	@Before
	public void setUp() throws Exception {
		store = new StoreImpl();
		store.setCode("SNAPITUP");

		shopper = new ShopperImpl();
		shopper.setShopperMemento(new ShopperMementoImpl());
		shopper.setUidPk(1L);
		shopper.setGuid("shopper-1");
		shopper.setStoreCode(store.getCode());

		@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
		ElasticPathImpl elasticPath = (ElasticPathImpl) ElasticPathImpl.getInstance();
		elasticPath.setBeanFactory(beanFactory);

		// Since AbstractEpDomainImpl does not support set method on Elasticpath object, we have to directly mock beanFactory in ElasticpathImpl.
		when(beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_CART_MEMENTO, ShoppingCartMemento.class)).thenReturn(new ShoppingCartMementoImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELDS_MAP_WRAPPER, ModifierFieldsMapWrapper.class))
				.thenReturn(new ModifierFieldsMapWrapper());
		when(elasticpath.getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class)).thenReturn(new ShoppingCartImpl());
		when(timeService.getCurrentTime()).thenReturn(new Date());
	}

	/**
	 * Test method for 'com.elasticpath.service.impl.ShoppingCartServiceImpl.add(ShoppingCart)'.
	 */
	@Test
	public void testSaveOrUpdateHappyPath() {
		final ShoppingCartImpl cart = new ShoppingCartImpl();
		cart.setStore(store);
		final ShoppingCartMementoImpl cartMemento = new ShoppingCartMementoImpl();
		cart.setShoppingCartMemento(cartMemento);

		final ShoppingCartMemento updatedShoppingCartMemento = new ShoppingCartMementoImpl();
		updatedShoppingCartMemento.setStoreCode(store.getCode());

		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);
		when(persistenceEngine.saveOrUpdate(cart.getShoppingCartMemento())).thenReturn(updatedShoppingCartMemento);

		ShoppingCart saved = shoppingCartServiceImpl.saveOrUpdate(cart);
		verify(persistenceEngine).saveOrUpdate(cart.getShoppingCartMemento());

		assertEquals("ShoppingCartMemento saved with new storeCode", store, saved.getStore());
		assertEquals("Old memento should have store code set before save", store.getCode(), cartMemento.getStoreCode());
		assertEquals("New cart has store", store, saved.getStore());
		assertEquals("New memento has store code", store.getCode(), updatedShoppingCartMemento.getStoreCode());
	}

	/**
	 * Test method for 'com.elasticpath.service.impl.ShoppingCartServiceImpl.saveOrUpdate(ShoppingCart)'.
	 */
	@Test
	public void testUpdate2() {
		final ShoppingCartImpl cart = new ShoppingCartImpl();
		cart.setStore(store);
		cart.setShoppingCartMemento(new ShoppingCartMementoImpl());
		cart.getShoppingCartMemento().setUidPk(1L);

		// expectations
		final ShoppingCartMemento updatedShoppingCartMemento = new ShoppingCartMementoImpl();
		updatedShoppingCartMemento.setStoreCode(store.getCode());
		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);
		when(persistenceEngine.saveOrUpdate(cart.getShoppingCartMemento())).thenReturn(updatedShoppingCartMemento);

		shoppingCartServiceImpl.saveOrUpdate(cart);
	}

	@Test
	public void testFindByGuid() {
		final ShoppingCartMemento memento = new ShoppingCartMementoImpl();
		memento.setGuid(GUID);
		memento.setStoreCode(store.getCode());
		memento.setShopper(shopper);

		when(persistenceEngine.retrieveByNamedQuery(ShoppingCartServiceImpl.SHOPPING_CART_FIND_BY_GUID_EAGER, GUID))
				.thenReturn(Collections.singletonList(memento));

		when(shopperService.get(shopper.getUidPk())).thenReturn(shopper);
		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);

		final ShoppingCart loadedCart = shoppingCartServiceImpl.findByGuid(GUID);

		verify(fetchPlanHelper).setLoadTuners((LoadTuner[]) null);
		assertSame("Shopping Cart should have its store loaded", store, loadedCart.getStore());
	}

	/**
	 * Test method for 'com.elasticpath.service.impl.ShoppingCartServiceImpl.getObject(long)'.
	 */
	@Test
	public void testGetObject() {
		final long uid = 1234L;
		final ShoppingCartMementoImpl cartMemento = new ShoppingCartMementoImpl();
		cartMemento.setGuid(GUID);
		cartMemento.setStoreCode(store.getCode());
		cartMemento.setShopper(shopper);

		final ShoppingCartImpl cart = new ShoppingCartImpl();
		cart.setShoppingCartMemento(cartMemento);

		Mockito.<Class<ShoppingCartMementoImpl>>when(elasticpath.getBeanImplClass(ContextIdNames.SHOPPING_CART_MEMENTO))
				.thenReturn(ShoppingCartMementoImpl.class);

		when(persistenceEngine.load(ShoppingCartMementoImpl.class, uid)).thenReturn(cartMemento);
		when(shopperService.get(shopper.getUidPk())).thenReturn(shopper);
		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);

		final ShoppingCart loadedCart = (ShoppingCart) shoppingCartServiceImpl.getObject(uid);
		verify(fetchPlanHelper).setLoadTuners((LoadTuner[]) null);

		assertSame(cart.getShoppingCartMemento(), ((ShoppingCartMementoHolder) loadedCart).getShoppingCartMemento());
		assertSame("Shopping Cart should have its store loaded", store, loadedCart.getStore());
	}

	@Test
	public void testFindOrCreateByShopperHappyFindPath() {
		final ShoppingCartMemento memento = new ShoppingCartMementoImpl();
		memento.setGuid(GUID);
		memento.setStoreCode(store.getCode());
		memento.setShopper(shopper);

		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);
		when(persistenceEngine.retrieveByNamedQuery(
					ShoppingCartServiceImpl.DEFAULT_SHOPPING_CART_FIND_BY_SHOPPER_UID, new Object[]{shopper.getUidPk()}, 0, 1))
				.thenReturn(Collections.singletonList(memento));

		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);

		final ShoppingCart loadedCart = shoppingCartServiceImpl.findOrCreateDefaultCartByShopper(shopper);
		assertSame("Shopping Cart should have its store loaded", store, loadedCart.getStore());
	}

	@Test
	public void testTouch() {
		String cartGuid = "memento-guid";
		Date currenDate = new Date();

		when(timeService.getCurrentTime()).thenReturn(currenDate);

		shoppingCartServiceImpl.touch(cartGuid);
		verify(persistenceEngine).executeNamedQuery("TOUCH_THE_CART", currenDate, cartGuid);
	}

	@Test
	public void testSaveIfNotPersistedWithUnpersistedCart() {
		final ShoppingCartImpl cart = new ShoppingCartImpl();
		cart.setStore(store);
		cart.setShoppingCartMemento(new ShoppingCartMementoImpl());

		final ShoppingCartMemento updatedShoppingCartMemento = new ShoppingCartMementoImpl();
		updatedShoppingCartMemento.setStoreCode(store.getCode());

		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);
		when(persistenceEngine.saveOrUpdate(cart.getShoppingCartMemento())).thenReturn(updatedShoppingCartMemento);

		ShoppingCart returned = shoppingCartServiceImpl.saveIfNotPersisted(cart);
		assertNotNull(returned);
	}

	@Test
	public void testSaveIfNotPersistedWithPreviouslyPersistedCart() {
		final ShoppingCartImpl cart = new ShoppingCartImpl();
		cart.setShoppingCartMemento(new ShoppingCartMementoImpl());
		cart.getShoppingCartMemento().setUidPk(1L);

		// When
		ShoppingCart returned = shoppingCartServiceImpl.saveIfNotPersisted(cart);
		assertSame(cart, returned);
	}

	@Test
	public void testGetCartDescriptors() {
		ModifierField modifierField1 = new ModifierFieldImpl();
		modifierField1.setCode("code1");
		modifierField1.setOrdering(1);

		ModifierField modifierField2 = new ModifierFieldImpl();
		modifierField2.setCode("code2");
		modifierField2.setOrdering(2);

		ModifierField modifierFieldWithDefaultValue = new ModifierFieldImpl();
		modifierFieldWithDefaultValue.setCode("code3");
		modifierFieldWithDefaultValue.setDefaultCartValue("defaultValue");

		ModifierGroup modifierGroup = new ModifierGroupImpl();
		modifierGroup.addModifierField(modifierField1);
		modifierGroup.addModifierField(modifierField2);
		modifierGroup.addModifierField(modifierFieldWithDefaultValue);

		final CartType cartType = new CartType();
		cartType.setModifiers(Collections.singletonList(modifierGroup));

		store.setShoppingCartTypes(Collections.singletonList(cartType));

		final ShoppingCartMemento memento = new ShoppingCartMementoImpl();
		memento.setGuid(GUID);
		memento.setStoreCode(store.getCode());
		memento.setShopper(shopper);
		memento.getModifierFields().put("code1", "value1");

		when(persistenceEngine.retrieveByNamedQuery(ShoppingCartServiceImpl.SHOPPING_CART_FIND_BY_GUID_EAGER, GUID))
				.thenReturn(Collections.singletonList(memento));

		when(shopperService.get(shopper.getUidPk())).thenReturn(shopper);
		when(storeService.findStoreWithCode(store.getCode())).thenReturn(store);

		Map<String, String> cartDataMap = shoppingCartServiceImpl.getCartDescriptors(memento.getGuid());

		assertEquals("value1", cartDataMap.get("code1"));
		assertEquals(null, cartDataMap.get("code2"));
		assertEquals("defaultValue", cartDataMap.get("code3"));
	}
}
