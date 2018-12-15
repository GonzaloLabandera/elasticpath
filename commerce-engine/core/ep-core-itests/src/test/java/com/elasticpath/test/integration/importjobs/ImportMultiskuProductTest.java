/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration.importjobs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.dataimport.ImportBadRow;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test import job for Product.
 */
public class ImportMultiskuProductTest extends ImportJobTestCase {

	private void assertProductOne() {
		ProductLookup productLookup = getBeanFactory().getBean(ContextIdNames.PRODUCT_LOOKUP);
		Product product = productLookup.findByGuid("101");
		assertThat(product).isNotNull();
		final AttributeValueGroup attributes = product.getAttributeValueGroup();

		assertThat(attributes.getAttributeValue("description", Locale.ENGLISH).getValue()).isEqualTo("Desc en 1");
		assertThat(attributes.getAttributeValue("description", Locale.FRENCH).getValue()).isEqualTo("Desc fr 1");

		assertThat(product.getCode()).isEqualTo("101");
		assertThat(product.getAvailabilityCriteria()).isEqualTo(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		assertThat(product.getBrand().getCode()).isEqualTo("F00004");
		assertThat(product.getDisplayName(Locale.ENGLISH)).isEqualTo("Displ Name en 1");
		assertThat(product.getDisplayName(Locale.FRENCH)).isEqualTo("Displ Name fr 1");
		assertThat(product.getEndDate()).isNull();
		assertThat(product.getExpectedReleaseDate()).isNull();
		assertThat(product.getImage()).isEqualTo("Image 1.jpg");
		assertThat(product.getMinOrderQty()).isEqualTo(1);
		assertThat(product.getPreOrBackOrderLimit()).isEqualTo(0);

		{ // assert locale dependent fields: For English
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.ENGLISH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc en 1");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name en 1");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title en 1");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords en 1");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

		{ // assert locale dependent fields: For French
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.FRENCH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc fr 1");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name fr 1");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title fr 1");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords fr 1");
			assertThat(localeDependantFields.getUrl()).isNull();
		}
	}

	private void assertProductTwo() {
		ProductLookup productLookup = getBeanFactory().getBean(ContextIdNames.PRODUCT_LOOKUP);
		Product product = productLookup.findByGuid("102");
		final AttributeValueGroup attributes = product.getAttributeValueGroup();

		assertThat(attributes.getAttributeValue("description", Locale.ENGLISH).getValue()).isEqualTo("Desc en 2");
		assertThat(attributes.getAttributeValue("description", Locale.FRENCH).getValue()).isEqualTo("Desc fr 2");

		assertThat(product.getCode()).isEqualTo("102");
		assertThat(product.getAvailabilityCriteria()).isEqualTo(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		assertThat(product.getBrand().getCode()).isEqualTo("F00004");
		assertThat(product.getDisplayName(Locale.ENGLISH)).isEqualTo("Displ Name en 2");
		assertThat(product.getDisplayName(Locale.FRENCH)).isEqualTo("Displ Name fr 2");
		assertThat(product.getEndDate()).isNull();
		assertThat(product.getExpectedReleaseDate()).isNull();
		assertThat(product.getImage()).isEqualTo("Image 2.jpg");
		assertThat(product.getMinOrderQty()).isEqualTo(1);
		assertThat(product.getPreOrBackOrderLimit()).isEqualTo(0);

		{ // assert locale dependent fields: For English
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.ENGLISH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc en 2");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name en 2");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title en 2");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords en 2");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

		{ // assert locale dependent fields: For French
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.FRENCH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc fr 2");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name fr 2");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title fr 2");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords fr 2");
			assertThat(localeDependantFields.getUrl()).isNull();
		}
	}

	private void assertProductTwoUpdated() {
		ProductLookup productLookup = getBeanFactory().getBean(ContextIdNames.PRODUCT_LOOKUP);
		Product product = productLookup.findByGuid("102");
		final AttributeValueGroup attributes = product.getAttributeValueGroup();

		assertThat(attributes.getAttributeValue("description", Locale.ENGLISH).getValue()).isEqualTo("Desc en 2 upd");
		assertThat(attributes.getAttributeValue("description", Locale.FRENCH).getValue()).isEqualTo("Desc fr 2 upd");

		assertThat(product.getCode()).isEqualTo("102");
		assertThat(product.getAvailabilityCriteria()).isEqualTo(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		assertThat(product.getBrand().getCode()).isEqualTo("F00004");
		assertThat(product.getDisplayName(Locale.ENGLISH)).isEqualTo("Displ Name en 2 upd");
		assertThat(product.getDisplayName(Locale.FRENCH)).isEqualTo("Displ Name fr 2 upd");
		assertThat(product.getEndDate()).isNull();
		assertThat(product.getExpectedReleaseDate()).isNull();
		assertThat(product.getImage()).isEqualTo("Image 2.jpg");
		assertThat(product.getMinOrderQty()).isEqualTo(1);
		assertThat(product.getPreOrBackOrderLimit()).isEqualTo(0);

		{ // assert locale dependent fields: For English
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.ENGLISH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc en 2 upd");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name en 2 upd");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title en 2");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords en 2");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

		{ // assert locale dependent fields: For French
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.FRENCH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc fr 2 upd");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name fr 2 upd");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title fr 2");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords fr 2");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

	}

	/**
	 * Test import Product insert.
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductInsert() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());

		List<ImportBadRow> badRows = executeImportJob(createInsertMultiskuProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOne();
		assertProductTwo();
	}

	/**
	 * Test import Product update.
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductUpdate() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createInsertMultiskuProductImportJob());
		List<ImportBadRow> badRows = executeImportJob(createUpdateMultiskuProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOne();
		assertProductTwoUpdated();

	}

}
