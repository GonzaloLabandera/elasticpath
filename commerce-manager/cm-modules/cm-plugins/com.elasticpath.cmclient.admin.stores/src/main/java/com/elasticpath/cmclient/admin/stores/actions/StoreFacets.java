package com.elasticpath.cmclient.admin.stores.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.cmclient.admin.stores.editors.facets.FacetModel;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.domain.search.FieldKeyType;
import com.elasticpath.domain.search.impl.FacetImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.search.FacetService;

/**
 * Get/Set the store facets.
 */
public class StoreFacets {

	private static final Logger LOG = LoggerFactory.getLogger(StoreFacets.class);

	private final FacetService facetService;
	private final CatalogService catalogService;

	private final Locale defaultLocale;
	private final Catalog catalog;
	private final String storeCode;
	private final ObjectMapper objectMapper;

	/**
	 * Constructor.
	 *  @param storeEditorModel the editor model.
	 *
	 */
	public StoreFacets(final StoreEditorModel storeEditorModel) {
		facetService = BeanLocator.getSingletonBean(ContextIdNames.FACET_SERVICE, FacetService.class);
		catalogService = BeanLocator.getSingletonBean(ContextIdNames.CATALOG_SERVICE, CatalogService.class);

		this.defaultLocale = storeEditorModel.getDefaultLocale();
		this.catalog = storeEditorModel.getCatalog();
		this.storeCode = storeEditorModel.getStoreCode();
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * Generate the store facets.
	 *
	 * @return a list of store facets.
	 */
	public List<Facet> getStoreFacets() {

		List<Facet> facets = facetService.findAllFacetsForStore(storeCode, defaultLocale);

		List<Long> catalogUids = new ArrayList<>();

		if (catalog.isMaster()) {
			catalogUids.add(catalog.getUidPk());
		} else {
			catalogService.findMastersUsedByVirtualCatalog(catalog.getCode())
					.forEach(masterCatalog -> catalogUids.add(masterCatalog.getUidPk()));
		}

		List<Attribute> attributes = facetService.findByCatalogsAndUsageNotFacetable(AttributeUsage.PRODUCT, AttributeUsage.SKU,
				storeCode, catalogUids);
		for (Attribute attribute : attributes) {
			Integer fieldKeyType = getFieldKeyTypeOrdinalBasedOnAttributeType(attribute.getAttributeType());
			if (fieldKeyType == null) {
				continue;
			}
			Facet facet = new FacetImpl();
			facet.setFieldKeyType(fieldKeyType);
			facet.setFacetName(attribute.getDisplayName(defaultLocale));
			facet.setFacetGuid(UUID.randomUUID().toString());
			facet.setStoreCode(storeCode);
			facet.setFacetType(FacetType.NO_FACET.getOrdinal());
			facet.setSearchableOption(true);
			facet.setDisplayName(attribute.getDisplayName(defaultLocale));
			facet.setBusinessObjectId(attribute.getKey());
			facet.setFacetGroup(attribute.getAttributeUsage().getValue() == AttributeUsage.PRODUCT
					? FacetGroup.PRODUCT_ATTRIBUTE.getOrdinal() : FacetGroup.SKU_ATTRIBUTE.getOrdinal());
			facets.add(facet);
		}

		List<SkuOption> skuOptions = facetService.findAllNotFacetableSkuOptionFromCatalogs(storeCode, catalogUids);
		for (SkuOption skuOption : skuOptions) {
			String skuOptionsDisplayName = skuOption.getDisplayName(defaultLocale, true);
			Facet facet = new FacetImpl();
			facet.setFacetGuid(UUID.randomUUID().toString());
			facet.setFieldKeyType(FieldKeyType.STRING.getOrdinal());
			facet.setFacetName(skuOptionsDisplayName);
			facet.setStoreCode(storeCode);
			facet.setFacetType(FacetType.NO_FACET.getOrdinal());
			facet.setSearchableOption(true);
			facet.setDisplayName(skuOptionsDisplayName);
			facet.setFacetGroup(FacetGroup.SKU_OPTION.getOrdinal());
			facet.setBusinessObjectId(skuOption.getOptionKey());
			facets.add(facet);
		}

		return facets;

	}

	/**
	 * Save the facets to the database.
	 *
	 * @param facetModelList the facets to be saved.
	 */
	public void saveStoreFacets(final List<FacetModel> facetModelList) {
		for (FacetModel facetModel : facetModelList) {

			Facet facet = new FacetImpl();
			try {
				facet.setDisplayName(objectMapper.writeValueAsString(facetModel.getDisplayNameMap()));
				facet.setRangeFacetValues(objectMapper.writeValueAsString(facetModel.getRangeFacets()));
			} catch (JsonProcessingException e) {
				LOG.error("Failed to create JSON: ", e);
				continue;
			}
			facet.setFacetGuid(facetModel.getGuid());
			facet.setStoreCode(storeCode);
			facet.setFacetName(facetModel.getFacetName());
			facet.setFacetType(facetModel.getFacetType().getOrdinal());
			facet.setSearchableOption(facetModel.isSearchable());
			facet.setFieldKeyType(facetModel.getFieldKeyType().getOrdinal());
			facet.setFacetGroup(facetModel.getFacetGroup().getOrdinal());
			facet.setBusinessObjectId(facetModel.getAttributeKey());
			facetService.saveOrUpdate(facet);
		}
	}

	private Integer getFieldKeyTypeOrdinalBasedOnAttributeType(final AttributeType attributeType) {
		if (attributeType == AttributeType.SHORT_TEXT || attributeType == AttributeType.LONG_TEXT) {
			return FieldKeyType.STRING.getOrdinal();
		} else if (attributeType == AttributeType.DECIMAL) {
			return FieldKeyType.DECIMAL.getOrdinal();
		} else if (attributeType == AttributeType.INTEGER) {
			return FieldKeyType.INTEGER.getOrdinal();
		} else if (attributeType == AttributeType.BOOLEAN) {
			return FieldKeyType.BOOLEAN.getOrdinal();
		}

		return null;
	}
}