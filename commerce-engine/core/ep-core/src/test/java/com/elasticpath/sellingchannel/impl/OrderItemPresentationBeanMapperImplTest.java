/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.sellingchannel.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.OrderItemDto;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.tree.impl.TreeNodeMemento;
import com.elasticpath.domain.catalog.DigitalAsset;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.money.Money;
import com.elasticpath.sellingchannel.presentation.OrderItemPresentationBean;
import com.elasticpath.sellingchannel.presentation.impl.OrderItemPresentationBeanImpl;
import com.elasticpath.sellingchannel.presentation.impl.OrderItemPresentationBeanMapperImpl;
import com.elasticpath.sellingchannel.presentation.impl.OrderItemPresentationBeanMapperImpl.CopyFunctor;

/**
 * Tests {@link OrderItemPresentationBeanMapperImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderItemPresentationBeanMapperImplTest {

	private static final int THIRTY = 30;
	private static final int TEN = 10;
	private static final int FIVE = 5;
	private static final int THREE = 3;

	private CopyFunctorTestable functor;
	private OrderItemPresentationBeanMapperTestable mapper;

	/**
	 * Prepare for the test.
	 */
	@Before
	public void init() {
		BeanFactory beanFactory = spy(new BeanFactory() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getBean(final String name) {
				return (T) new OrderItemPresentationBeanImpl();
			}

			@Override
			public <T> Class<T> getBeanImplClass(final String beanName) {
				return null;
			}
		});

		functor = new CopyFunctorTestable();
		functor.setBeanFactory(beanFactory);

		mapper = new OrderItemPresentationBeanMapperTestable();
		mapper.setBeanFactory(beanFactory);
	}

	/**
	 * Tests that an {@code OrderItemFormBean} can be created from an {@code OrderItemDto}.
	 */
	@Test
	public void testMapToDtoNoChild() {
		final OrderItemDto orderItemDto = new OrderItemDto();

		TreeNodeMemento<OrderItemPresentationBean> formBean = functor.processNode(orderItemDto, null, null, 0);

		assertThat(formBean).isNotNull();
	}
	
	/**
	 * Tests that a three level tree (parent->child->grandchildren) is mapped to a flattened structure with level information.
	 */
	@Test
	public void testMapThreeLevelTree() {
		final OrderItemDto orderItemDto = new OrderItemDto();
		orderItemDto.setQuantity(1);
		final OrderItemDto childDto = new OrderItemDto();
		childDto.setQuantity(1);
		final OrderItemDto grandchildDto = new OrderItemDto();
		grandchildDto.setQuantity(1);

		orderItemDto.addChild(childDto);
		childDto.addChild(grandchildDto);

		OrderItemPresentationBean presentationBean = mapper.mapFrom(orderItemDto);
		
		assertThat(presentationBean).isNotNull();
		assertThat(presentationBean.getChildren()).hasSize(2);
		assertThat(presentationBean.getLevel()).isEqualTo(0);

		OrderItemPresentationBean childFormBean = presentationBean.getChildren().get(0);
		assertThat(childFormBean.getLevel()).isEqualTo(1);

		OrderItemPresentationBean grandchildFormBean = presentationBean.getChildren().get(1);
		assertThat(grandchildFormBean.getLevel()).isEqualTo(2);
	}
	
	/**
	 * Tests that the functor copies the information required. 
	 */
	@SuppressWarnings("PMD.AvoidDuplicateLiterals")
	@Test
	public void testFunctor() {
		OrderItemDto dto = new OrderItemDto();
		
		dto.setDisplayName("display");
		dto.setImage("image.jpg");
		dto.setDisplaySkuOptions("skuoptions");
		dto.setSkuCode("sku-A");
		dto.setAllocated(true);
		ProductSku productSku = new ProductSkuImpl();
		dto.setProductSku(productSku);
		DigitalAsset digitalAsset = mock(DigitalAsset.class);
		dto.setDigitalAsset(digitalAsset);	
		dto.setListPrice(Money.valueOf("12.34", Currency.getInstance("CAD")));
		dto.setUnitPrice(Money.valueOf("23.45", Currency.getInstance("CAD")));
		dto.setUnitLessThanList(false);
		dto.setDollarSavings(Money.valueOf("11.11", Currency.getInstance("CAD")));
		dto.setQuantity(THREE);
		dto.setTotal(Money.valueOf("37.02", Currency.getInstance("CAD")));
				
		TreeNodeMemento<OrderItemPresentationBean> presentationBeanMemento = functor.processNode(dto, null, null, 0);
		OrderItemPresentationBean presentationBean = presentationBeanMemento.getTreeNode();
		
		assertThat(presentationBean.getDigitalAsset()).isEqualTo(digitalAsset);
		assertThat(presentationBean.getDisplayName()).isEqualTo("display");
		assertThat(presentationBean.getImage()).isEqualTo("image.jpg");
		assertThat(presentationBean.getDisplaySkuOptions()).isEqualTo("skuoptions");
		assertThat(presentationBean.isAllocated()).isTrue();
		assertThat(presentationBean.getListPriceMoney()).isEqualTo(Money.valueOf("12.34", Currency.getInstance("CAD")));
		assertThat(presentationBean.getUnitPriceMoney()).isEqualTo(Money.valueOf("23.45", Currency.getInstance("CAD")));
		assertThat(presentationBean.isUnitLessThanList()).isFalse();
		assertThat(presentationBean.getDollarSavingsMoney()).isEqualTo(Money.valueOf("11.11", Currency.getInstance("CAD")));
		assertThat(presentationBean.getSkuCode()).isEqualTo("sku-A");
		assertThat(presentationBean.getQuantity()).isEqualTo(THREE);
		assertThat(presentationBean.getTotalMoney().toString()).isEqualTo("37.02 CAD");
	}
	
	/** Tests that the quantity on the formBeans is the per bundle quantity instead of the shippable quantity. */
	@Test
	public void testMapFromHavingPerBundleQuantity() {
		OrderItemDto root = createFakeOrderItemDto();

		OrderItemPresentationBean actualPresentationBeanRoot = mapper.mapFrom(root);
		assertThat(actualPresentationBeanRoot.getQuantity()).isEqualTo(FIVE);
		assertThat(actualPresentationBeanRoot.getChildren())
			.as("Both descendants should be direct children of the root")
			.hasSize(2);

		OrderItemPresentationBean actualFormBeanChild1 = actualPresentationBeanRoot.getChildren().get(0);
		assertThat(actualFormBeanChild1.getQuantity()).isEqualTo(2);
		assertThat(actualFormBeanChild1.getChildren()).isEmpty();

		OrderItemPresentationBean actualFormBeanChild2 = actualPresentationBeanRoot.getChildren().get(1);
		assertThat(actualFormBeanChild2.getQuantity()).isEqualTo(THREE);
		assertThat(actualFormBeanChild2.getChildren()).isEmpty();
	}

	/** Required for testing. */
	private class CopyFunctorTestable extends CopyFunctor {
		@Override
		protected boolean isGC(final OrderItemDto sourceNode) {
			return true;
		}
	}

	/** Required for testing. */
	private class OrderItemPresentationBeanMapperTestable extends OrderItemPresentationBeanMapperImpl {
		OrderItemPresentationBeanMapperTestable() {
			super();
			setFunctor(new CopyFunctorTestable());
		}
	}

	/**
	 * The definition for the product bundle is:
	 * 
	 *  -root
	 *  	-child1 X 2
	 *  		-child11 X 3
	 *  
	 *  If the quantity of root is 5 then the order item has the following structure:
	 *  
	 *  -root X 5
	 *  	-child1 X 10
	 *  		-child11 X 30
	 *  .
	 */
	private OrderItemDto createFakeOrderItemDto() {
		OrderItemDto root = new OrderItemDto();
		root.setQuantity(FIVE);

		OrderItemDto child1 = new OrderItemDto();
		child1.setQuantity(TEN);
		root.addChild(child1);

		OrderItemDto child11 = new OrderItemDto();
		child11.setQuantity(THIRTY);
		child1.addChild(child11);
		
		return root;
	}
}
