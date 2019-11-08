package com.elasticpath.cmclient.admin.stores.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.cmclient.admin.stores.editors.facets.FacetModel;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.domain.search.FieldKeyType;

/**
 * This class handles the generation of the facet and search configurations.
 */
@SuppressWarnings("unchecked")
public class StoreConfigurationForSearchAndFacet {

	private final StoreEditorModel storeEditorModel;
	private final Locale defaultLocale;
	private final Collection<Locale> supportedLocales;

	/**
	 * Constructor.
	 *  @param storeEditorModel the editor model.
	 *
	 */
	public StoreConfigurationForSearchAndFacet(final StoreEditorModel storeEditorModel) {
		this.storeEditorModel = storeEditorModel;
		this.defaultLocale = storeEditorModel.getDefaultLocale();
		this.supportedLocales = storeEditorModel.getSupportedLocales();
	}

	/**
	 * Get the facet and search configuration.
	 *
	 * @return the list of facet and search configuration.
	 */
	public List<FacetModel> getFacetAndSearchableConfiguration() {

		List<FacetModel> facetModels = new ArrayList<>();

		SearchableConfiguration searchableConfiguration = new SearchableConfiguration(defaultLocale, supportedLocales);

		Map<String, FacetModel> defaultFacets = searchableConfiguration.getSearchableConfiguration();

		StoreFacets storeFacets = new StoreFacets(storeEditorModel);
		for (Facet facet : storeFacets.getStoreFacets()) {
			String fieldKey = facet.getFacetName();
			if (facet.getFacetGroup() == FacetGroup.FIELD.getOrdinal()) {
				defaultFacets.remove(fieldKey);
			}
			FacetModel facetModel = new FacetModel(fieldKey, FieldKeyType.STRING, true, FacetType.NO_FACET,
					supportedLocales, defaultLocale.toString(), facet.getBusinessObjectId());
			facetModel.setFacetType(FacetType.valueOfOrdinal(facet.getFacetType()));
			facetModel.setSearchable(facet.getSearchableOption());
			if (facet.getDisplayNameMap() != null) {
				facetModel.setDisplayNameMap(facet.getDisplayNameMap());
			}
			facetModel.setFieldKeyType(getFieldKeyTypeBasedOnOrdinal(facet.getFieldKeyType()));
			if (facet.getSortedRangeFacet() != null) {
				facetModel.setRangeFacets(facet.getSortedRangeFacet());
			}
			facetModel.setAttributeKey(facet.getBusinessObjectId());
			facetModel.setFacetGroup(FacetGroup.valueOfOrdinal(facet.getFacetGroup()));
			facetModel.setGuid(facet.getFacetGuid());
			facetModels.add(facetModel);
		}
		facetModels.addAll(defaultFacets.values());
		return facetModels;
	}

	private FieldKeyType getFieldKeyTypeBasedOnOrdinal(final Integer fieldKeyTypeOrdinal) {
		for (FieldKeyType runner : FieldKeyType.values()) {
			if (runner.getOrdinal() == fieldKeyTypeOrdinal) {
				return runner;
			}
		}

		// Default to String.
		return FieldKeyType.STRING;
	}

}