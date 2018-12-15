/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration.importjobs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.dataimport.ImportBadRow;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test import job for Product.
 */
public class ImportProductTest extends ImportJobTestCase {

	@Autowired
	@Qualifier("productLookup")
	private ProductLookup productLookup;

	private void assertProductOne() {
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
		final Map<String, ProductSku> skus = product.getProductSkus();
		assertThat(skus).hasSize(1); // this is a single sku import
		assertThat(skus.values().iterator().next().getSkuCode()).isEqualTo("SKU101");
	}

	private void assertProductOneAbsent() {
		Assertions.assertThat(productLookup.<Product>findByGuid("101")).isNull();
	}

	private void assertProductTwo() {
		Product product = productLookup.findByGuid("102");
		assertThat(product)
			.as("Product should have been found")
			.isNotNull();
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

		final Map<String, ProductSku> skus = product.getProductSkus();
		assertThat(skus).hasSize(1); // this is a single sku import
		assertThat(skus.values().iterator().next().getSkuCode()).isEqualTo("SKU102");
	}

	private void assertProductTwoUpdated() {
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

		final Map<String, ProductSku> skus = product.getProductSkus();
		assertThat(skus).hasSize(1); // this is a single sku import
		assertThat(skus.values().iterator().next().getSkuCode()).isEqualTo("SKU102");
	}

	private void assertProductThree() {
		Product product = productLookup.findByGuid("103");
		final AttributeValueGroup attributes = product.getAttributeValueGroup();

		assertThat(attributes.getAttributeValue("description", Locale.ENGLISH).getValue()).isEqualTo("Desc en 3");
		assertThat(attributes.getAttributeValue("description", Locale.FRENCH).getValue()).isEqualTo("Desc fr 3");

		assertThat(product.getCode()).isEqualTo("103");
		assertThat(product.getAvailabilityCriteria()).isEqualTo(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		assertThat(product.getBrand().getCode()).isEqualTo("F00004");
		assertThat(product.getDisplayName(Locale.ENGLISH)).isEqualTo("Displ Name en 3");
		assertThat(product.getDisplayName(Locale.FRENCH)).isEqualTo("Displ Name fr 3");
		assertThat(product.getEndDate()).isNull();
		assertThat(product.getExpectedReleaseDate()).isNull();
		assertThat(product.getImage()).isEqualTo("Image 3.jpg");
		assertThat(product.getMinOrderQty()).isEqualTo(1);
		assertThat(product.getPreOrBackOrderLimit()).isEqualTo(0);

		{ // assert locale dependent fields: For English
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.ENGLISH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc en 3");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name en 3");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title en 3");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords en 3");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

		{ // assert locale dependent fields: For French
			LocaleDependantFields localeDependantFields = product.getLocaleDependantFields(Locale.FRENCH);
			assertThat(localeDependantFields.getDescription()).isEqualTo("SEO desc fr 3");
			assertThat(localeDependantFields.getDisplayName()).isEqualTo("Displ Name fr 3");
			assertThat(localeDependantFields.getTitle()).isEqualTo("SEO title fr 3");
			assertThat(localeDependantFields.getKeyWords()).isEqualTo("SEO keywords fr 3");
			assertThat(localeDependantFields.getUrl()).isNull();
		}

		final Map<String, ProductSku> skus = product.getProductSkus();
		assertThat(skus).hasSize(1); // this is a single sku import
		assertThat(skus.values().iterator().next().getSkuCode()).isEqualTo("SKU103");
	}

	/**
	 * Test import Product insert.
	 *
	 * @throws Exception if any exception is thrown
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductInsert() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());

		List<ImportBadRow> badRows = executeImportJob(createInsertProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOne();
		assertProductTwo();
	}

	/**
	 * Test import Product insert/update.
	 *
	 * @throws Exception if any exception is thrown
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductInsertUpdate() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createInsertProductImportJob());
		List<ImportBadRow> badRows = executeImportJob(createInsertUpdateProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOne();
		assertProductTwoUpdated();
		assertProductThree();
	}

	/**
	 * Test import Product update.
	 *
	 * @throws Exception if any exception is thrown
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductUpdate() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createInsertProductImportJob());
		List<ImportBadRow> badRows = executeImportJob(createUpdateProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOne();
		assertProductTwoUpdated();

	}

	/**
	 * Test input Product delete.
	 *
	 * @throws Exception if any exception is thrown
	 */
	@DirtiesDatabase
	@Test
	public void testImportProductDelete() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createInsertProductImportJob());
		List<ImportBadRow> badRows = executeImportJob(createDeleteProductImportJob());
		assertThat(badRows).isEmpty();

		assertProductOneAbsent();
		assertProductTwo();
	}
}
