/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.adapters.products.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.JpaAdaptorOfSkuOptionValueImpl;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.importexport.common.dto.products.AttributeGroupDTO;
import com.elasticpath.importexport.common.dto.products.AttributeValuesDTO;
import com.elasticpath.importexport.common.dto.products.DigitalAssetItemDTO;
import com.elasticpath.importexport.common.dto.products.ProductSkuAvailabilityDTO;
import com.elasticpath.importexport.common.dto.products.ProductSkuDTO;
import com.elasticpath.importexport.common.dto.products.ShippableItemDTO;
import com.elasticpath.importexport.common.dto.products.SkuOptionDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRuntimeException;

/**
 * Verify that ProductSkuAdapter populates category domain object from DTO properly and vice versa.
 * <br>Nested adapters should be tested separately.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductSkuAdapterTest {

	private static final String ATTRIBUTE_KEY = "attributeKey";

	private static final String PRODUCT_SKU_CODE = "productSkuCode";

	private static final Integer MAX_DOWNLOAD_TIME = 5;

	private static final String PRODUCT_SKU_IMAGE = "skuOptionImage";

	private static final Date START_DATE = new Date();

	private static final Date END_DATE = new Date();

	private static final String SKU_OPTION_VALUE = "skuOptionValue";

	private static final String SKU_OPTION_CODE = "skuOptionCode";

	private static final String PRODUCT_SKU_TAX_CODE_VALUE = "skuTaxCode";

	private ProductSkuAdapter productSkuAdapter;

	@Mock
	private ProductSku mockProductSku;

	@Mock
	private BeanFactory mockBeanFactory;

	@Mock
	private SkuOption mockSkuOption;

	@Mock
	private SkuOptionValue mockSkuOptionValue;

	@Mock
	private Product mockProduct;

	@Mock
	private AttributeValueGroup mockAttributeValueGroup;

	@Mock
	private TaxCode mockSkuTaxCode;

	@Before
	public void setUp() throws Exception {
		productSkuAdapter = new ProductSkuAdapter();
		productSkuAdapter.setBeanFactory(mockBeanFactory);
	}

	private SkuOptionDTO createSkuOptionDTO() {
		SkuOptionDTO skuOptionDTO = new SkuOptionDTO();
		skuOptionDTO.setCode(SKU_OPTION_CODE);
		skuOptionDTO.setSkuOptionValue(SKU_OPTION_VALUE);
		return skuOptionDTO;
	}

	/**
	 * Tests PopulateDomain.
	 *
	 */
	@Test
	public void testPopulateDomain() {
		when(mockProductSku.getAttributeValueGroup()).thenReturn(mockAttributeValueGroup);

		ProductSkuAdapter adapter = new ProductSkuAdapter() {
			@Override
			void populateDomainSkuOptions(final ProductSku productSku, final List<SkuOptionDTO> skuOptionDtoList) {
				// empty
			}

			@Override
			void populateProductSkuAvailability(final ProductSku productSku, final ProductSkuAvailabilityDTO productSkuAvailabilityDTO) {
				// empty
			}

			@Override
			void checkDigitalShippable(final ShippableItemDTO shippableItem, final DigitalAssetItemDTO digitalAssetItem) {
				// empty
			}
		};

		adapter.setShippableItemAdapter(new ShippableItemAdapter() {
			@Override
			public void populateDomain(final ShippableItemDTO source, final ProductSku target) {
				// empty
			}
		});

		adapter.setDigitalAssetItemAdapter(new DigitalAssetItemAdapter() {
			@Override
			public void populateDomain(final DigitalAssetItemDTO source, final ProductSku target) {
				// empty
			}
		});

		adapter.setAttributeGroupAdapter(new AttributeGroupAdapter() {
			@Override
			public void populateDomain(final AttributeGroupDTO attributeGroupDto, final AttributeValueGroup attributeValueGroup) {
				// empty
			}

		});

		ProductSkuDTO productSkuDTO = new ProductSkuDTO();
		productSkuDTO.setGuid(SKU_OPTION_CODE);
		productSkuDTO.setSkuCode(SKU_OPTION_CODE);
		productSkuDTO.setImage(PRODUCT_SKU_IMAGE);

		adapter.populateDomain(productSkuDTO, mockProductSku);

		verify(mockProductSku).setSkuCode(SKU_OPTION_CODE);
		verify(mockProductSku).setGuid(SKU_OPTION_CODE);
		verify(mockProductSku).setImage(PRODUCT_SKU_IMAGE);
		verify(mockProductSku).getAttributeValueGroup();
	}

	/**
	 * Tests PopulateProductSkuAvailability.
	 */
	@Test
	public void testPopulateProductSkuAvailability() {
		ProductSkuAvailabilityDTO productSkuAvailabilityDTO = new ProductSkuAvailabilityDTO();

		productSkuAvailabilityDTO.setStartDate(START_DATE);
		productSkuAvailabilityDTO.setEndDate(END_DATE);

		productSkuAdapter.populateProductSkuAvailability(mockProductSku, productSkuAvailabilityDTO);

		verify(mockProductSku).setStartDate(START_DATE);
		verify(mockProductSku).setEndDate(END_DATE);
	}

	/**
	 * Tests PopulateDomainSkuOptions.
	 */
	@Test
	public void testPopulateDomainSkuOptions() {
		final JpaAdaptorOfSkuOptionValueImpl expectedSkuOptionValue = new JpaAdaptorOfSkuOptionValueImpl();
		expectedSkuOptionValue.setSkuOptionValue(mockSkuOptionValue);

		final List<SkuOptionDTO> skuOptionDtoList = new ArrayList<>();
		skuOptionDtoList.add(createSkuOptionDTO());

		ProductSkuAdapter adapter = new ProductSkuAdapter() {
			@Override
			JpaAdaptorOfSkuOptionValueImpl createSkuOptionValue(final ProductSku productSku, final String skuOptionCode) {
				return expectedSkuOptionValue;
			}
		};
		adapter.setProductSkuOptionAdapter(new ProductSkuOptionAdapter() {
			@Override
			public void populateDomain(final SkuOptionDTO source, final JpaAdaptorOfSkuOptionValueImpl target) {
				assertThat(source.getCode()).isEqualTo(SKU_OPTION_CODE);
				assertThat(target).isEqualTo(expectedSkuOptionValue);
				target.setSkuOption(mockSkuOption);
			}
		});

		adapter.populateDomainSkuOptions(mockProductSku, skuOptionDtoList);
		verify(mockSkuOptionValue).setSkuOption(mockSkuOption);
	}

	/**
	 * Tests CreateSkuOptionValue.
	 */
	@Test
	public void testCreateSkuOptionValue() {
		final JpaAdaptorOfSkuOptionValueImpl expectedSkuOptionValue = new JpaAdaptorOfSkuOptionValueImpl();
		final Map<String, SkuOptionValue> optionValueMap = new HashMap<>();

		when(mockProductSku.getOptionValueMap()).thenReturn(optionValueMap);
		when(mockBeanFactory.getPrototypeBean(ContextIdNames.SKU_OPTION_VALUE_JPA_ADAPTOR, JpaAdaptorOfSkuOptionValueImpl.class))
			.thenReturn(expectedSkuOptionValue);

		assertThat(productSkuAdapter.createSkuOptionValue(mockProductSku, SKU_OPTION_CODE)).isEqualTo(expectedSkuOptionValue);
		assertThat(optionValueMap).containsOnly(entry(SKU_OPTION_CODE, expectedSkuOptionValue));
	}

	/**
	 * Tests PopulateDTO.
	 */
	@Test
	public void testPopulateDTO() {
		when(mockProduct.hasMultipleSkus()).thenReturn(Boolean.TRUE);

		when(mockProductSku.getSkuCode()).thenReturn(PRODUCT_SKU_CODE);
		when(mockProductSku.getGuid()).thenReturn(PRODUCT_SKU_CODE);
		when(mockProductSku.getProduct()).thenReturn(mockProduct);

		when(mockProductSku.getImage()).thenReturn(PRODUCT_SKU_IMAGE);
		when(mockProductSku.getAttributeValueGroup()).thenReturn(mockAttributeValueGroup);

		when(mockProductSku.getStartDate()).thenReturn(START_DATE);
		when(mockProductSku.getEndDate()).thenReturn(END_DATE);

		when(mockProductSku.getTaxCodeOverride()).thenReturn(mockSkuTaxCode);
		when(mockSkuTaxCode.getCode()).thenReturn(PRODUCT_SKU_TAX_CODE_VALUE);
		ProductSkuAdapter adapter = new ProductSkuAdapter() {
			@Override
			void populateDTOSkuOptions(final ProductSku productSku, final ProductSkuDTO productSkuDTO) {
				assertThat(productSku).isNotNull();
				SkuOptionDTO skuOptionDTO = new SkuOptionDTO();
				skuOptionDTO.setCode(SKU_OPTION_CODE);
				productSkuDTO.setSkuOptionList(ImmutableList.of(skuOptionDTO));
			}
		};

		ProductSkuDTO productSkuDTO = new ProductSkuDTO();

		adapter.setShippableItemAdapter(new ShippableItemAdapter() {
			@Override
			public void populateDTO(final ProductSku source, final ShippableItemDTO target) {
				assertThat(source).isNotNull();
				target.setEnabled(true);
			}
		});
		adapter.setDigitalAssetItemAdapter(new DigitalAssetItemAdapter() {
			@Override
			public void populateDTO(final ProductSku source, final DigitalAssetItemDTO target) {
				assertThat(source).isNotNull();
				target.setEnabled(true);
				target.setMaxDownloadTimes(MAX_DOWNLOAD_TIME);
			}
		});
		adapter.setAttributeGroupAdapter(new AttributeGroupAdapter() {
			@Override
			public void populateDTO(final AttributeValueGroup attributeValueGroup, final AttributeGroupDTO attributeGroupDto) {
				assertThat(attributeValueGroup).isNotNull();
				AttributeValuesDTO attributeValuesDTO = new AttributeValuesDTO();
				attributeValuesDTO.setKey(ATTRIBUTE_KEY);
				attributeGroupDto.setAttributeValues(ImmutableList.of(attributeValuesDTO));
			}
		});
		adapter.populateDTO(mockProductSku, productSkuDTO);

		verify(mockProduct).hasMultipleSkus();
		verify(mockProductSku).getSkuCode();
		verify(mockProductSku).getGuid();
		verify(mockProductSku).getProduct();
		verify(mockProductSku).getImage();
		verify(mockProductSku).getAttributeValueGroup();
		verify(mockProductSku).getStartDate();
		verify(mockProductSku).getEndDate();
		verify(mockProductSku).getTaxCodeOverride();
		verify(mockSkuTaxCode).getCode();

		assertThat(productSkuDTO.getSkuCode()).isEqualTo(PRODUCT_SKU_CODE);
		assertThat(productSkuDTO.getImage()).isEqualTo(PRODUCT_SKU_IMAGE);
		assertThat(productSkuDTO.getProductSkuAvailabilityDTO().getEndDate()).isEqualTo(END_DATE);
		assertThat(productSkuDTO.getProductSkuAvailabilityDTO().getStartDate()).isEqualTo(START_DATE);
		assertThat(productSkuDTO.getProductSkuAvailabilityDTO().getEndDate()).isEqualTo(END_DATE);
		assertThat(productSkuDTO.getDigitalAssetItem().getMaxDownloadTimes()).isEqualTo(MAX_DOWNLOAD_TIME);
		assertThat(productSkuDTO.getDigitalAssetItem().isEnabled()).isTrue();
		assertThat(productSkuDTO.getShippableItem().isEnabled()).isTrue();
		assertThat(productSkuDTO.getSkuOptionList().get(0).getCode()).isEqualTo(SKU_OPTION_CODE);
		assertThat(productSkuDTO.getSkuOptionList()).hasSize(1);
		assertThat(productSkuDTO.getAttributeGroupDTO().getAttributeValues()).hasSize(1);
		assertThat(productSkuDTO.getAttributeGroupDTO().getAttributeValues().get(0).getKey()).isEqualTo(ATTRIBUTE_KEY);
	}

	/**
	 * Tests PopulateDTOSkuOptions.
	 */
	@Test
	public void testPopulateDTOSkuOptions() {
		final JpaAdaptorOfSkuOptionValueImpl expectedJpaAdaptorOfSkuOptionValue = new JpaAdaptorOfSkuOptionValueImpl();
		final Map<String, SkuOptionValue> optionValueMap = new HashMap<>();
		optionValueMap.put(SKU_OPTION_CODE, expectedJpaAdaptorOfSkuOptionValue);
		when(mockProductSku.getOptionValueMap()).thenReturn(optionValueMap);

		ProductSkuDTO productSkuDTO = new ProductSkuDTO();
		productSkuAdapter.setProductSkuOptionAdapter(new ProductSkuOptionAdapter() {
			@Override
			public void populateDTO(final JpaAdaptorOfSkuOptionValueImpl source, final SkuOptionDTO target) {
				assertThat(source).isEqualTo(expectedJpaAdaptorOfSkuOptionValue);
				target.setCode(SKU_OPTION_CODE);
			}
		});
		productSkuAdapter.populateDTOSkuOptions(mockProductSku, productSkuDTO);

		assertThat(productSkuDTO.getSkuOptionList()).hasSize(1);
		assertThat(productSkuDTO.getSkuOptionList().get(0).getCode()).isEqualTo(SKU_OPTION_CODE);
		verify(mockProductSku).getOptionValueMap();
	}

	/**
	 * Tests CheckDigitalShippable.
	 */
	@Test(expected = PopulationRuntimeException.class)
	public void testCheckDigitalShippable() {
		final ShippableItemDTO shippableItem = new ShippableItemDTO();
		final DigitalAssetItemDTO digitalAssetItem = new DigitalAssetItemDTO();

		shippableItem.setEnabled(true);
		digitalAssetItem.setEnabled(true);

		productSkuAdapter.checkDigitalShippable(shippableItem, digitalAssetItem);
	}

}
