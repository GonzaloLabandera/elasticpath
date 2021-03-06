/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.dataimport.impl;

import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.ImportConstants;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductCategory;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.dataimport.ImportFault;
import com.elasticpath.domain.dataimport.ImportField;
import com.elasticpath.domain.dataimport.ImportType;
import com.elasticpath.domain.dataimport.impl.AbstractImportTypeImpl;
import com.elasticpath.domain.dataimport.impl.ImportDataTypeProductCategoryAssociationImpl;
import com.elasticpath.domain.dataimport.impl.ProductCategoryImportBean;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.openjpa.PersistenceInterceptor;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * An import runner to import products.
 */
public class ImportJobRunnerProductImpl extends AbstractImportJobRunnerImpl {
	private static final Logger LOG = LogManager.getLogger(ImportJobRunnerProductImpl.class);

	private IndexNotificationService indexNotificationService;
	
	/**
	 * Find the entity with the given guid.
	 * 
	 * @param guid the guid
	 * @return the entity with the given guid if it exists, otherwise <code>null</code>.
	 */
	@Override
	protected Entity findEntityByGuid(final String guid) {
		return getImportGuidHelper().findProductByGuid(guid, false, false, true);
	}

	/**
	 * Creates a new entity.
	 * 
	 * @param baseObject the base object might be used to determine entity type, such as <code>ProductType</code> etc.
	 * @return the newly created entity
	 */
	@Override
	protected Entity createNewEntity(final Object baseObject) {
		final Product product = getPrototypeBean(ContextIdNames.PRODUCT, Product.class);
		product.setProductType((ProductType) baseObject);
		return product;
	}

	/**
	 * Returns the commit unit.
	 * 
	 * @return the commit unit.
	 */
	@Override
	protected int getCommitUnit() {
		return ImportConstants.COMMIT_UNIT;
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
		((Product) entity).setLastModifiedDate(getTimeService().getCurrentTime());
	}

	/**
	 * Generate index notification to ensure the Product is re-indexed.
	 * @param session session.
	 * @param entity entity for save.
	 * @return the saved entity
	 */
	@Override
	protected Entity saveEntityHelper(final PersistenceSession session, final Entity entity) {
		Entity savedEntity = super.saveEntityHelper(session, entity);

		indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.PRODUCT, entity.getUidPk());

		return savedEntity;
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation, when the import type is {@link AbstractImportTypeImpl.DELETE}
	 * AND the datatype being imported is the value object {@link ImportDataTypeProductCategoryAssociationImpl},
	 * will remove the {@code Category} specified in the field array from the {@code Product} Entity.</p>
	 */
	@Override
	protected void importOneRow(final String[] nextLine, final PersistenceSession session) throws EpServiceException {

		ImportType importType = this.getRequest().getImportType();
		if (importType.equals(AbstractImportTypeImpl.CLEAR_INSERT_TYPE)) {
			throw new UnsupportedOperationException("This import does not support CLEAR THEN INSERT.");
		}

		final String guid = readGuid(nextLine);
		Entity entity = loadEntityByGuid(guid);
		if (importType.equals(AbstractImportTypeImpl.UPDATE_TYPE)) {
			entity = update(nextLine, session, guid, entity);
		}
		if (importType.equals(AbstractImportTypeImpl.INSERT_TYPE)) {
			entity = insert(nextLine, session, guid, entity);
		}
		if (importType.equals(AbstractImportTypeImpl.INSERT_UPDATE_TYPE)) {
			entity = insertAndUpdate(nextLine, session, guid, entity);
		}
		if (importType.equals(AbstractImportTypeImpl.DELETE_TYPE)) {
			if (getImportDataType() instanceof ImportDataTypeProductCategoryAssociationImpl) {
				removeCategoryFromProduct(session, (Product) entity, nextLine);
			} else {
				delete(session, guid, entity);
			}
		}
	}

	/**
	 * Removes a {@code Category} from a {@code Product} in the case that the current {@code ImportJob} is of type DELETE and
	 * generates a product {@code IndexNotification} for the Product.
	 * If the Category is the Product's default Category then no action will be taken and a warning will be logged.
	 * This method is marked as package protected to allow for more targeted unit testing.
	 * @param session the persistence session
	 * @param product the product
	 * @param nextLine the import line representing the category to be removed
	 */
	protected void removeCategoryFromProduct(final PersistenceSession session, final Product product, final String[] nextLine) {
		ProductCategoryImportBean productCategoryAssociation = (ProductCategoryImportBean) getImportDataType().createValueObject();
		updateContent(nextLine, productCategoryAssociation);
		Category categoryToRemove = productCategoryAssociation.getCategory();
		if (product.getDefaultCategory(product.getMasterCatalog()).equals(categoryToRemove)) {
			LOG.warn("Attempted to remove default (primary) category in master catalog code = " 
					+ product.getMasterCatalog().getCode() + " from product code=" + product.getCode() 
					+ " - not permitted.");
			
			return;
		}
		product.removeCategory(categoryToRemove);
		session.save(product);
		indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.PRODUCT, product.getUidPk());
	}
	
	/**
	 * {@inheritDoc}
	 * This implementation overrides the "required" parameter on the given {@code ImportField} (setting to false)
	 * in the case that the current {@code ImportJob} is of type DELETE, to allow flexibility of the CSV input
	 * for deletion of {@code ProductAssociation}s. Introduces a dependency on the {@link ImportDataTypeProductAssociationImpl}
	 * class so that the field names can be referenced accurately. Does not perform validation for Delete jobs.
	 * Calls {@link #getImportJob()} to get the type of import job.
	 */
	@Override
	protected void validateChangeSetStatus(final String[] nextLine, final int rowNumber, final List<ImportFault> faults,
			final Persistable persistenceObject) {
		super.validateChangeSetStatus(nextLine, rowNumber, faults, persistenceObject);

		// This code is for importing product category associations (either with or without featured products)
		// and ensuring that the products themselves
		// are not in another change set.
		
		// Identify that we're importing product category associations  
		if (persistenceObject instanceof ProductCategory) {
			for (Entry<ImportField, Integer> entry : getMappings().entrySet()) {
				final ImportField importField = entry.getKey();
				final Integer colNum = entry.getValue();
			
				if ("productCode".equals(importField.getName())) {
					Product product = new ProductImpl();
					product.setCode(nextLine[colNum]);
					if (!checkChangeSetStatus(product, getRequest().getChangeSetGuid())) {
						// report error
						final ImportFault importFault = createImportFault(
								ImportFault.ERROR,
								"import.csvFile.badRow.unavailableForChangeSet",
								new Object[] { rowNumber, getRequest().getChangeSetGuid() });
						faults.add(importFault);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param indexNotificationService the index notification service
	 */
	public void setIndexNotificationService(final IndexNotificationService indexNotificationService) {
		this.indexNotificationService = indexNotificationService;
	}

}
