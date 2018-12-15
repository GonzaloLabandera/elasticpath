/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */

package com.elasticpath.importexport.common.adapters.products.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.DigitalAsset;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.importexport.common.dto.products.DigitalAssetItemDTO;

/**
 * Verify that DigitalAssetItemAdapter populates category domain object from DTO properly and vice versa. 
 * <br>Nested adapters should be tested separately.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigitalAssetItemAdapterTest {
	
	private static final Integer EXPIRY_DAYS = 10;

	private static final Integer MAX_DOWNLOAD_TIMES = 15;

	private static final String FILE_NAME = "fileName";

	@Mock
	private ProductSku mockProductSku;

	@Mock
	private DigitalAsset mockDigitalAsset;

	@Mock
	private BeanFactory mockBeanFactory;

	private DigitalAssetItemAdapter digitalAssetItemAdapter;

	/**
	 * Setup test.
	 */
	@Before
	public void setUp() throws Exception {
		digitalAssetItemAdapter = new DigitalAssetItemAdapter();
		digitalAssetItemAdapter.setBeanFactory(mockBeanFactory);
	}

	/**
	 * Test method for {@link com.elasticpath.importexport.common.adapters.products.data.DigitalAssetItemAdapter#populateDomain}.
	 */
	@Test
	public void testPopulateDomain() {
		DigitalAssetItemAdapter adapter = new DigitalAssetItemAdapter() {
			@Override
			void checkDownloadExpiry(final Integer expiryDays) {
				// empty, not necessary
			}

			@Override
			void checkDownloadLimit(final Integer maxDownloadTimes) {
				// empty, not necessary
			}

			@Override
			DigitalAsset createDigitalAsset(final ProductSku productSku) {
				assertThat(productSku).isNotNull();
				return mockDigitalAsset;
			}
		};

		DigitalAssetItemDTO digitalAssetItemDTO = new DigitalAssetItemDTO();
		digitalAssetItemDTO.setEnabled(true);
		digitalAssetItemDTO.setExpiryDays(EXPIRY_DAYS);
		digitalAssetItemDTO.setMaxDownloadTimes(MAX_DOWNLOAD_TIMES);
		digitalAssetItemDTO.setFileName(FILE_NAME);

		adapter.populateDomain(digitalAssetItemDTO, mockProductSku);

		verify(mockProductSku).setDigital(true);
		verify(mockProductSku).setDigitalAsset(mockDigitalAsset);
		verify(mockDigitalAsset).setFileName(FILE_NAME);
		verify(mockDigitalAsset).setExpiryDays(EXPIRY_DAYS);
		verify(mockDigitalAsset).setMaxDownloadTimes(MAX_DOWNLOAD_TIMES);

	}

	/**
	 * Test method for {@link com.elasticpath.importexport.common.adapters.products.data.DigitalAssetItemAdapter#createDigitalAsset}.
	 */
	@Test
	public void testCreateDigitalAsset() {
		when(mockProductSku.getDigitalAsset()).thenReturn(null);
		when(mockBeanFactory.getBean(ContextIdNames.DIGITAL_ASSET)).thenReturn(mockDigitalAsset);

		DigitalAsset result = digitalAssetItemAdapter.createDigitalAsset(mockProductSku);

		assertThat(result).isEqualTo(mockDigitalAsset);
		verify(mockProductSku).getDigitalAsset();
		verify(mockBeanFactory).getBean(ContextIdNames.DIGITAL_ASSET);
	}

	/**
	 * Test method for {@link com.elasticpath.importexport.common.adapters.products.data.DigitalAssetItemAdapter#populateDTO}.
	 */
	@Test
	public void testPopulateDTO() {
		when(mockProductSku.isDigital()).thenReturn(true);
		when(mockProductSku.getDigitalAsset()).thenReturn(mockDigitalAsset);

		when(mockDigitalAsset.getFileName()).thenReturn(FILE_NAME);
		when(mockDigitalAsset.getExpiryDays()).thenReturn(EXPIRY_DAYS);
		when(mockDigitalAsset.getMaxDownloadTimes()).thenReturn(MAX_DOWNLOAD_TIMES);

		DigitalAssetItemDTO digitalAssetItemDTO = new DigitalAssetItemDTO();
		digitalAssetItemAdapter.populateDTO(mockProductSku, digitalAssetItemDTO);

		verify(mockProductSku).isDigital();
		verify(mockProductSku).getDigitalAsset();
		verify(mockDigitalAsset).getFileName();
		verify(mockDigitalAsset).getExpiryDays();
		verify(mockDigitalAsset).getMaxDownloadTimes();
		assertThat(digitalAssetItemDTO.isEnabled()).isTrue();
		assertThat(digitalAssetItemDTO.getFileName()).isEqualTo(FILE_NAME);
		assertThat(digitalAssetItemDTO.getExpiryDays()).isEqualTo(EXPIRY_DAYS);
		assertThat(digitalAssetItemDTO.getMaxDownloadTimes()).isEqualTo(MAX_DOWNLOAD_TIMES);
	}

}
