/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration.importjobs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test import job for categories.
 */
public class ImportCategoriesTest extends ImportJobTestCase {

	@Autowired
	@Qualifier("categoryLookup")
	private CategoryLookup categoryLookup;

	/**
	 * Test import categories insert.
	 */
	@DirtiesDatabase
	@Test
	public void testImportCategoriesInsert() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());

		Category category = categoryLookup.findByCategoryCodeAndCatalog("100001", scenario.getCatalog());
		assertThat(category)
			.as("Category should have been found")
			.isNotNull();

		Locale locale = Locale.ENGLISH;
		LocaleDependantFields localeDependantFields = category.getLocaleDependantFields(locale);
		assertThat(category.getCode()).isEqualTo("100001");
		assertThat(category.getParentGuid()).isNull();
		assertThat(localeDependantFields.getDisplayName()).isEqualTo("Office Desks");
		assertThat(category.getStartDate()).isEqualTo("2007-03-01T12:00:00");
		assertThat(category.getEndDate()).isEqualTo("2008-03-01T12:00:00");
		assertThat(category.isHidden()).isFalse();
		assertThat(category.getOrdering()).isEqualTo(1);
		assertThat(localeDependantFields.getUrl()).isNull();
		assertThat(localeDependantFields.getTitle()).isEqualTo("Office Desks");
		assertThat(localeDependantFields.getKeyWords()).isEqualTo("Office desks for your office.");
		assertThat(localeDependantFields.getDescription()).isNull();
	}

	/**
	 * Test import categories insert/update.
	 */
	@DirtiesDatabase
	@Test
	public void testImportCategoriesInsertUpdate() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createInsertUpdateCategoriesImportJob());

		Category category1 = categoryLookup.findByCategoryCodeAndCatalog("100001", scenario.getCatalog());

		// assert existing category was not changed during update
		Locale locale = Locale.ENGLISH;
		LocaleDependantFields localeDependantFields1 = category1.getLocaleDependantFields(locale);
		assertThat(category1.getCode()).isEqualTo("100001");
		assertThat(category1.getParentGuid()).isNull();
		assertThat(localeDependantFields1.getDisplayName()).isEqualTo("Office Desks");
		assertThat(category1.getStartDate()).isEqualTo("2007-03-01T12:00:00");
		assertThat(category1.getEndDate()).isEqualTo("2008-03-01T12:00:00");
		assertThat(category1.isHidden()).isFalse();
		assertThat(category1.getOrdering()).isEqualTo(1);
		assertThat(localeDependantFields1.getUrl()).isNull();
		assertThat(localeDependantFields1.getTitle()).isEqualTo("Office Desks");
		assertThat(localeDependantFields1.getKeyWords()).isEqualTo("Office desks for your office.");
		assertThat(localeDependantFields1.getDescription()).isNull();

		// assert existing category has been updated during import
		Category category2 = categoryLookup.findByCategoryCodeAndCatalog("100002", scenario.getCatalog());
		LocaleDependantFields localeDependantFields2 = category2.getLocaleDependantFields(locale);
		assertThat(category2.getCode()).isEqualTo("100002");
		assertThat(category2.getParentGuid()).isNull();
		assertThat(localeDependantFields2.getDisplayName()).isEqualTo("Contemporary Dining Tables");
		assertThat(localeDependantFields2.getTitle()).isEqualTo("Contemporary Dining Tables");
		assertThat(localeDependantFields2.getKeyWords()).isEqualTo("Contemporary Dining tables for your dining room.");
		assertThat(localeDependantFields2.getDescription()).isNull();

		// assert new category has been created during import
		Category category3 = categoryLookup.findByCategoryCodeAndCatalog("100004", scenario.getCatalog());
		LocaleDependantFields localeDependantFields3 = category3.getLocaleDependantFields(locale);
		assertThat(category3.getCode()).isEqualTo("100004");
		assertThat(category3.getParentGuid()).isNull();
		assertThat(localeDependantFields3.getDisplayName()).isEqualTo("Dining Chairs");
		assertThat(category3.isHidden()).isTrue();
		assertThat(category3.getOrdering()).isEqualTo(3);
		assertThat(localeDependantFields3.getUrl()).isNull();
		assertThat(localeDependantFields3.getTitle()).isEqualTo("Dining Chairs");
		assertThat(localeDependantFields3.getKeyWords()).isEqualTo("Dining chairs for your dining room.");
		assertThat(localeDependantFields3.getDescription()).isNull();
	}

	/**
	 * Test import categories update.
	 */
	@DirtiesDatabase
	@Test
	public void testImportCategoriesUpdate() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createUpdateCategoriesImportJob());

		Category category = categoryLookup.findByCategoryCodeAndCatalog("100001", scenario.getCatalog());
		assertThat(category).isNotNull();

		Locale locale = Locale.ENGLISH;
		LocaleDependantFields localeDependantFields = category.getLocaleDependantFields(locale);
		assertThat(category.getCode()).isEqualTo("100001");
		assertThat(category.getParentGuid()).isNull();
		assertThat(localeDependantFields.getDisplayName()).isEqualTo("Large Office Desks");
		assertThat(category.getStartDate()).isEqualTo("2007-03-01T12:00:00");
		assertThat(category.getEndDate()).isEqualTo("2008-03-01T12:00:00");
		assertThat(category.isHidden()).isFalse();
		assertThat(category.getOrdering()).isEqualTo(1);
		assertThat(localeDependantFields.getUrl()).isNull();
		assertThat(localeDependantFields.getTitle()).isEqualTo("Office Desks");
		assertThat(localeDependantFields.getKeyWords()).isEqualTo("Office desks for your office.");
		assertThat(localeDependantFields.getDescription()).isNull();
	}

	/**
	 * Test input categories delete.
	 */
	@DirtiesDatabase
	@Test
	public void testImportCategoriesDelete() throws Exception {
		executeImportJob(createInsertCategoriesImportJob());
		executeImportJob(createDeleteCategoriesImportJob());

		assertThat(categoryLookup.<Category>findByCategoryCodeAndCatalog("100001", scenario.getCatalog())).isNotNull();
		assertThat(categoryLookup.<Category>findByCategoryCodeAndCatalog("100002", scenario.getCatalog())).isNull();
		assertThat(categoryLookup.<Category>findByCategoryCodeAndCatalog("100003", scenario.getCatalog())).isNotNull();
	}
}
