/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
/**
 * 
 */
package com.elasticpath.csvimport.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;
import com.elasticpath.commons.exception.EpBindException;
import com.elasticpath.commons.exception.EpInvalidCatalogCodeException;
import com.elasticpath.commons.exception.EpInvalidGuidBindException;
import com.elasticpath.commons.exception.EpNonNullBindException;
import com.elasticpath.commons.exception.EpRequiredDependentFieldsMissingBindException;
import com.elasticpath.commons.exception.EpSalePriceExceedListPriceException;
import com.elasticpath.commons.exception.EpTooLongBindException;
import com.elasticpath.csvimport.CsvImportFieldObjectMapper;
import com.elasticpath.csvimport.DtoImportDataType;
import com.elasticpath.domain.dataimport.ImportFault;
import com.elasticpath.domain.dataimport.ImportField;

/**
 * Maps CSV fields onto a system object for CSV imports. 
 * <p>This implementation uses an injected ImportDataType to retrieve ImportField objects that are
 * responsible for validating the fields and populating the system object.</p>
 * @param <T> the type of object being that will be mapped to the given row
 */
public class CsvImportFieldObjectMapperImpl<T> implements CsvImportFieldObjectMapper<T> {
	
	private DtoImportDataType<T> importDataType;
	private BeanFactory beanFactory;

	/**
	 * Gets the {@code ImportField} associated with the given field name,
	 * as mapped in the injected {@code ImportDataType}.
	 * Calls {@link #getDtoImportDataType()}.
	 * @param fieldName the name of the object's field
	 * @return the ImportField mapped to the field name
	 */
	ImportField getImportFieldForColumn(final String fieldName) {
		return getDtoImportDataType().getImportField(fieldName);
	}
	
	/**
	 * Maps the columns in the given row to the fields on the given object,
	 * using the given helper service if not null.
	 * @param row the row
	 * @param columnIndexFieldNameMap map of object field names to column indexes
	 * @param faults the collection of faults while setting the object's fields to the column index values
	 * @return the populated object
	 */
	@Override
	public T mapRow(
			final String[] row, final Map<String, Integer> columnIndexFieldNameMap, final Collection<ImportFault> faults) {
		T prototypeBean = getPrototypeBean();
		for (Map.Entry<String, Integer> mapping : columnIndexFieldNameMap.entrySet()) {
			ImportField importField = getImportFieldForColumn(mapping.getKey());
			int columnNumber = mapping.getValue();
			try {
				importField.setStringValue(prototypeBean, row[columnNumber], null);
			} catch (EpInvalidCatalogCodeException e) {
				for (InvalidCatalogCodeMessage message : e.getErrorReasonList()) {
					faults.add(createImportFault(
							ImportFault.ERROR,
							message.getMessageCode(),
							createImportFaultArgs(importField, columnNumber, row, message)));
				}
			} catch (EpInvalidGuidBindException e) {
				faults.add(createImportFault(
						ImportFault.WARNING, 
						"import.csvFile.badRow.wrongGuid",
						createImportFaultArgs(importField, columnNumber, row)));
			} catch (EpNonNullBindException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.notNull",
						createImportFaultArgs(importField, columnNumber, row)));
			} catch (EpTooLongBindException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.tooLong",
						createImportFaultArgs(importField, columnNumber, row)));
			} catch (EpSalePriceExceedListPriceException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.salePriceExceedListPrice",
						createImportFaultArgs(importField, columnNumber, row)));
			} catch (EpBindException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.bindError",
						createImportFaultArgs(importField, columnNumber, row)));
			} catch (IllegalArgumentException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.badValue",
						createImportFaultArgs(importField, columnNumber, row)));
			}
		}
		return validatePopulatedBean(prototypeBean, faults);
	}


	private Object[] createImportFaultArgs(final ImportField importField, final Integer columnNumber, final String[] row,
			final InvalidCatalogCodeMessage message) {

		Object[] args = createImportFaultArgs(importField, columnNumber, row);
		List<Object> list = Arrays.asList(args);
		list.addAll(message.getParameters());
		return list.toArray();
	}

	private Object[] createImportFaultArgs(final ImportField importField, final Integer columnNumber, final String[] row) {
		List<Object> args = new ArrayList<>();
		args.add(importField.getName());
		args.add(importField.getType());
		args.add(String.valueOf(columnNumber));
		args.add(row[columnNumber]);
		return args.toArray();
	}

	private T validatePopulatedBean(final T prototypeBean, final Collection<ImportFault> faults) {
			try {
				return getDtoImportDataType().validatePopulatedDtoBean(prototypeBean);
			} catch (EpRequiredDependentFieldsMissingBindException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.requiredDependentFieldsMissing", 
						new Object[] {e.getDependentField(), StringUtils.join(e.getRequiredFields()), "", e.getFieldValue()}));
			} catch (IllegalArgumentException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						"import.csvFile.badRow.badValue", 
						new Object[] {"row error", "", "", ""}));
			} catch (EpServiceException e) {
				faults.add(createImportFault(
						ImportFault.ERROR, 
						e.getMessage(), 
						new Object[] {"row error", "", "", ""}));
			}
			return prototypeBean;
	}

	/**
	 * Gets an instance of the prototype bean that is to be populated.
	 * This implementation retrieves the prototype bean from the ImportDataType
	 * because the bean cannot be injected via Spring due to the bean also being retrieved in 
	 * the {@code ImportServiceImpl} directly from the prototype bean factory.
	 * @return the prototype bean
	 */
	T getPrototypeBean() {
		return getDtoImportDataType().getPrototypeDtoBean();
	}
	
	
	/**
	 * Creates an {@code ImportFault} with the given data.
	 * @param faultLevel the fault level
	 * @param faultCode the fault code
	 * @param faultArgs the fault arguments
	 * @return the created ImportFault
	 */
	ImportFault createImportFault(final int faultLevel, final String faultCode, final Object[] faultArgs) {
		final ImportFault fault = getBeanFactory().getPrototypeBean(ContextIdNames.IMPORT_FAULT, ImportFault.class);
		fault.setLevel(faultLevel);
		fault.setCode(faultCode);
		fault.setArgs(faultArgs);
		return fault;
	}
	
	/**
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @return the importDataType
	 */
	public DtoImportDataType<T> getDtoImportDataType() {
		return importDataType;
	}

	/**
	 * @param dtoImportDataType the importDataType to set
	 */
	public void setDtoImportDataType(final DtoImportDataType<T> dtoImportDataType) {
		this.importDataType = dtoImportDataType;
	}
}
