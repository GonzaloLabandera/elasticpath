/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.adapters.catalogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.AttributeGroup;
import com.elasticpath.domain.attribute.AttributeGroupAttribute;
import com.elasticpath.domain.cartmodifier.CartItemModifierGroup;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.adapters.catalogs.helper.AttributeGroupHelper;
import com.elasticpath.importexport.common.dto.catalogs.MultiSkuDTO;
import com.elasticpath.importexport.common.dto.catalogs.ProductTypeDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.service.cartitemmodifier.CartItemModifierService;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>ProductType</code> and <code>ProductTypeDTO</code> objects.
 */
public class ProductTypeAdapter extends AbstractDomainAdapterImpl<ProductType, ProductTypeDTO> {

	private AttributeGroupHelper attributeGroupHelper;

	private CartItemModifierService cartItemModifierService;


	@Override
	public void populateDTO(final ProductType productType, final ProductTypeDTO productTypeDTO) {
		productTypeDTO.setGuid(productType.getGuid());
		productTypeDTO.setName(productType.getName());
		productTypeDTO.setAssignedAttributes(createAssignedAttributes(productType));
		productTypeDTO.setDefaultTaxCode(productType.getTaxCode().getCode());
		productTypeDTO.setNoDiscount(productType.isExcludedFromDiscount());
		
		if (productType.isMultiSku()) {
			productTypeDTO.setMultiSku(createMultiSkuDTO(productType));
		}

		if (productType.getCartItemModifierGroups() == null || productType.getCartItemModifierGroups().isEmpty()) {
			return;
		}

		productTypeDTO.setAssignedCartItemModifierGroups(new ArrayList<>());
		for (CartItemModifierGroup cartItemModifierGroup : productType.getCartItemModifierGroups()) {
			productTypeDTO.getAssignedCartItemModifierGroups().add(cartItemModifierGroup.getCode());
		}
		Collections.sort(productTypeDTO.getAssignedCartItemModifierGroups());
	}


	private MultiSkuDTO createMultiSkuDTO(final ProductType productType) {
		MultiSkuDTO multiSkuDTO = new MultiSkuDTO();
		
		multiSkuDTO.setAssignedSkuOptions(createAssignedSkuOptions(productType));
		multiSkuDTO.setAssignedAttributes(createAssignedSkuAttributes(productType));
		
		return multiSkuDTO;
	}

	/**
	 * Creates Assigned Attributes List.
	 * 
	 * @param productType the productType
	 * @return List of assigned attributes.
	 */
	List<String> createAssignedAttributes(final ProductType productType) {
		return attributeGroupHelper.createAssignedAttributes(productType.getProductAttributeGroupAttributes());
	}

	/**
	 * Creates Assigned SKU Attributes List.
	 * 
	 * @param productType the productType
	 * @return List of assigned SKU attributes.
	 */
	List<String> createAssignedSkuAttributes(final ProductType productType) {
		return attributeGroupHelper.createAssignedAttributes(productType.getSkuAttributeGroup().getAttributeGroupAttributes());
	}

	@Override
	public void populateDomain(final ProductTypeDTO productTypeDTO, final ProductType productType) {
		if (StringUtils.isNotBlank(productTypeDTO.getGuid())) {
			productType.setGuid(productTypeDTO.getGuid());
		}
		productType.setName(productTypeDTO.getName());
		productType.setTaxCode(findDefaultTaxCode(productTypeDTO.getDefaultTaxCode()));
		if (productTypeDTO.getNoDiscount() != null) {
			productType.setExcludedFromDiscount(productTypeDTO.getNoDiscount());
		}
		attributeGroupHelper.populateAttributeGroupAttributes(createAttributeGroupAttributes(productType), 
										 					  productTypeDTO.getAssignedAttributes(), 
										 					  ContextIdNames.PRODUCT_TYPE_PRODUCT_ATTRIBUTE);
		
		populateWithMultipleSkus(productType, productTypeDTO.getMultiSku());

		if (productTypeDTO.getAssignedCartItemModifierGroups() == null || productTypeDTO.getAssignedCartItemModifierGroups().isEmpty()) {
			return;
		}

		for (String cartItemModifierGroupCode : productTypeDTO.getAssignedCartItemModifierGroups()) {
			CartItemModifierGroup cartItemModifierGroup = cartItemModifierService.findCartItemModifierGroupByCode(cartItemModifierGroupCode);
			if (cartItemModifierGroup == null) {
				throw new IllegalStateException("Cannot find CartItemModifierGroup with code " + cartItemModifierGroupCode + " on the database");
			}
			productType.getCartItemModifierGroups().add(cartItemModifierGroup);
		}
	}

	private void populateWithMultipleSkus(final ProductType productType, final MultiSkuDTO multiSkuDTO) {
		if (multiSkuDTO != null) {
			productType.setMultiSku(true);
			for (String skuOptionCode : multiSkuDTO.getAssignedSkuOptions()) {
				productType.addOrUpdateSkuOption(findSkuOption(skuOptionCode));
			}
			
			attributeGroupHelper.populateAttributeGroupAttributes(createSkuAttributeGroupAttributes(productType),
													  		      multiSkuDTO.getAssignedAttributes(),
													  		      ContextIdNames.PRODUCT_TYPE_SKU_ATTRIBUTE);
		}
	}

	/**
	 * Creates SKU Attributes Group Attributes Set.
	 * 
	 * @param productType the productType
	 * @return Set of SKU attributes group attributes.
	 */
	Set<AttributeGroupAttribute> createSkuAttributeGroupAttributes(final ProductType productType) {
		final AttributeGroup skuAttributeGroup = productType.getSkuAttributeGroup();
		Set<AttributeGroupAttribute> skuAttributeGroupAttributes = skuAttributeGroup.getAttributeGroupAttributes();
		if (skuAttributeGroupAttributes == null) {
			skuAttributeGroupAttributes = new HashSet<>();
			skuAttributeGroup.setAttributeGroupAttributes(skuAttributeGroupAttributes);
			productType.setSkuAttributeGroup(skuAttributeGroup);
		}
		return skuAttributeGroupAttributes;
	}

	/**
	 * Creates Attributes Group Attributes Set.
	 * 
	 * @param productType the productType
	 * @return Set of attributes group attributes.
	 */
	Set<AttributeGroupAttribute> createAttributeGroupAttributes(final ProductType productType) {
		Set<AttributeGroupAttribute> productAttributeGroupAttributes = productType.getProductAttributeGroupAttributes();
		if (productAttributeGroupAttributes == null) {
			productAttributeGroupAttributes = new HashSet<>();
			productType.setProductAttributeGroupAttributes(productAttributeGroupAttributes);
		}
		return productAttributeGroupAttributes;
	}

	/**
	 * Creates Assigned SkuOptions List.
	 * 
	 * @param productType the skuOptions for creating list
	 * @return List for assigned skuOptions
	 */
	List<String> createAssignedSkuOptions(final ProductType productType) {
		final List<String> assignedSkuOptions = new ArrayList<>();
		for (SkuOption skuOption : productType.getSkuOptions()) {
			assignedSkuOptions.add(skuOption.getOptionKey());
		}
		Collections.sort(assignedSkuOptions);
		return assignedSkuOptions;
	}

	/**
	 * Finds for tax code and checks for inconstancy.
	 *
	 * @param defaultTaxCode the tax code string
	 * @return TaxCode instance or throws exception
	 */
	TaxCode findDefaultTaxCode(final String defaultTaxCode) {
		if (defaultTaxCode == null) {
			throw new PopulationRollbackException("IE-10004");
		}
		
		TaxCode taxCode = getCachingService().findTaxCodeByCode(defaultTaxCode);
		if (taxCode == null) {			
			throw new PopulationRollbackException("IE-10005", defaultTaxCode);
		}		
		return taxCode;
	}

	/**
	 * Finds skuOption by code.
	 * 
	 * @param skuOptionCode the code of the skuOption
	 * @throws PopulationRollbackException if skuOption with skuOptionCode does not exist 
	 * @return SkuOption instance if it was found
	 */
	SkuOption findSkuOption(final String skuOptionCode) {
		SkuOption skuOption = getCachingService().findSkuOptionByKey(skuOptionCode);
		if (skuOption == null) {
			throw new PopulationRollbackException("IE-10006", skuOptionCode);
		}
		return skuOption;
	}

	@Override
	public ProductType createDomainObject() {
		return getBeanFactory().getBean(ContextIdNames.PRODUCT_TYPE);
	}
	
	@Override
	public ProductTypeDTO createDtoObject() {
		return new ProductTypeDTO();
	}

	/**
	 * Gets AttributeGroupHelper.
	 * 
	 * @return AttributeGroupHelper
	 */
	protected AttributeGroupHelper getAttributeGroupHelper() {
		return attributeGroupHelper;
	}

	/**
	 * Sets AttributeGroupHelper.
	 * 
	 * @param attributeGroupHelper the AttributeGroupHelper instance
	 */
	public void setAttributeGroupHelper(final AttributeGroupHelper attributeGroupHelper) {
		this.attributeGroupHelper = attributeGroupHelper;
	}

	protected CartItemModifierService getCartItemModifierService() {
		return cartItemModifierService;
	}

	public void setCartItemModifierService(final CartItemModifierService cartItemModifierService) {
		this.cartItemModifierService = cartItemModifierService;
	}
}
