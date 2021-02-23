/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.dataimport.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;
import com.elasticpath.commons.exception.EpBindException;
import com.elasticpath.commons.exception.EpInvalidCatalogCodeException;
import com.elasticpath.commons.exception.EpInvalidGuidBindException;
import com.elasticpath.commons.exception.EpNonNullBindException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.dataimport.ImportBadRow;
import com.elasticpath.domain.dataimport.ImportFault;
import com.elasticpath.domain.dataimport.ImportField;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeCategoryImpl;
import com.elasticpath.persistence.CsvFileReader;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.openjpa.PersistenceInterceptor;

/**
 * An import runner to import categories.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class ImportJobRunnerCategoryImpl extends AbstractImportJobRunnerImpl {

	/**
	 * Find the entity with the given guid.
	 *
	 * @param guid the guid
	 * @return the entity with the given guid if it exists, otherwise <code>null</code>.
	 */
	@Override
	protected Entity findEntityByGuid(final String guid) {
		return getImportGuidHelper().findCategoryByGuidAndCatalogGuid(guid, 
				getImportJob().getCatalog().getGuid());
	}

	/**
	 * Creates a new entity.
	 *
	 * @param baseObject the base object might be used to determine entity type, such as <code>CategoryType</code> etc.
	 * @return the newly created entity
	 */
	@Override
	protected Entity createNewEntity(final Object baseObject) {
		final Category category = getPrototypeBean(ContextIdNames.CATEGORY, Category.class);
		category.setCategoryType((CategoryType) baseObject);
		category.setCatalog(this.getImportJob().getCatalog());
		return category;
	}

	/**
	 * Validate the import job. This method overrides the parent one since category validation is more complex. In a category csv file, a child
	 * category may refer to a parent category that doesn't exist in db.
	 * 
	 * @param locale the locale of the results
	 * @return a list of <code>ImportBadRow</code>, or a empty list if there is no errors.
	 */
	@Override
	public List<ImportBadRow> validate(final Locale locale) {
		final CsvFileReader csvFileReader = getCsvFileReader();
		final List<ImportBadRow> importBadRows = new ArrayList<>();

		// skip the title line
		csvFileReader.readNext();

		int rowNumber = 0;
		String[] row;
		final SortedSet<String> guidsInCsvFile = new TreeSet<>();
		final String parentCategoryFieldName = ImportDataTypeCategoryImpl.PREFIX_OF_FIELD_NAME 
			+ ImportDataTypeCategoryImpl.PARENT_CATEGORY_CODE;
		
		while ((row = csvFileReader.readNext()) != null) {
			rowNumber++;
			final List<ImportFault> faults = new ArrayList<>();

			String guid = null;
			if (getGuidColNumber() != null) {
				guid = row[getGuidColNumber().intValue()];
				guidsInCsvFile.add(guid);
			}

			final Entity entity = createNewEntity(getBaseObject());

			for (Map.Entry<ImportField, Integer> entry : getMappings().entrySet()) {
				final ImportField importField = entry.getKey();
				final Integer colNum = entry.getValue();
				try {
					if (importField.getName().equals(parentCategoryFieldName)) {
						// Only if we can not find the parent category guid in the csv file,
						// we need to load it from database
						if (row[colNum] != null && !guidsInCsvFile.contains(row[colNum])) {
							checkField(row[colNum], entity, importField);
						}
					} else {
						checkField(row[colNum], entity, importField);
					}
				} catch (EpInvalidCatalogCodeException e) {
					for (InvalidCatalogCodeMessage message : e.getErrorReasonList()) {
						final ImportFault importFault = createImportFault(
								ImportFault.ERROR,
								message.getMessageCode(),
								createImportFaultArgs(importField, colNum, row, message));
						faults.add(importFault);
					}
				} catch (EpInvalidGuidBindException e) {
					final ImportFault importFault = createImportFault(
							ImportFault.WARNING,
							"import.csvFile.badRow.wrongGuid",
							createImportFaultArgs(importField, colNum, row));
					faults.add(importFault);
				} catch (EpNonNullBindException e) {
					final ImportFault importFault = createImportFault(
							ImportFault.ERROR,
							"import.csvFile.badRow.notNull",
							createImportFaultArgs(importField, colNum, row));
					faults.add(importFault);
				} catch (EpBindException e) {
					final ImportFault importFault = createImportFault(
							ImportFault.ERROR,
							"import.csvFile.badRow.bindError",
							createImportFaultArgs(importField, colNum, row));
					faults.add(importFault);
				}
			}
			
			validateChangeSetStatus(row, rowNumber, faults, entity);

			if (!faults.isEmpty()) {
				final ImportBadRow importBadRow = getPrototypeBean(ContextIdNames.IMPORT_BAD_ROW, ImportBadRow.class);
				importBadRow.setRowNumber(rowNumber);
				importBadRow.setRow(row[0]);
				importBadRow.addImportFaults(faults);
				importBadRows.add(importBadRow);
			}
		}
		csvFileReader.close();

		getImportJobStatusHandler().reportTotalRows(getImportJobProcessId(), rowNumber);
		return importBadRows;
	}

	/**
	 * Returns the commit unit.
	 *
	 * @return the commit unit.
	 */
	@Override
	protected int getCommitUnit() {
		// Category should be imported one by one because they are dependant to each other.
		return 1;
	}

	/**
	 * Update the entity before it get saved.
	 *
	 * @param entity the entity to save
	 */
	@Override
	protected void updateEntityBeforeSave(final Entity entity) {
		if (entity instanceof PersistenceInterceptor) {
			((PersistenceInterceptor) entity).executeBeforePersistAction();
		}
	}

	/**
	 * Save the entity (Category), setting the <code>Catalog</code> first.
	 * @param session the persistence session
	 * @param entity the entity (Category) to persist
	 * @param persistenceObject the persistable object
	 * @return the updated entity.
	 */
	@Override
	protected Entity saveEntity(final PersistenceSession session, final Entity entity, final Persistable persistenceObject) {
		Category category = (Category) entity;
		Catalog currentCatalog = category.getCatalog();
		Catalog updatedCatalog = loadCatalogObject(session);
		// making explicit check to see if current category's catalog is different than the catalog this import is associated with, and throw
		// exception if not
		if (currentCatalog == null || currentCatalog.getUidPk() == updatedCatalog.getUidPk()) {
			category.setCatalog(updatedCatalog);
			return super.saveEntity(session, entity, persistenceObject);
		} 
		throw new EpServiceException("Cannot save category because category code already exists in another catalog: " + category.getGuid());
	}
}
