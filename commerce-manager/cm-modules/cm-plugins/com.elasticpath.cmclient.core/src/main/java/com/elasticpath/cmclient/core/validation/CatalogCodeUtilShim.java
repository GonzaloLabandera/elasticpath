/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.validation;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.IValidator;

import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.CatalogCodeUtil;
import com.elasticpath.commons.util.CatalogCodeFormat;


/**
 * Provides a static-based method access into the underlying CatalogCodeUtil allowing use from other static contexts while allowing
 * the CatalogCodeUtil implementation to be switched out if needed.
 */
public class CatalogCodeUtilShim {

	/** List of "atomic" validators. */
	private final List<IValidator> validators = new ArrayList<IValidator>();

	/** Checks that a String has no leading or trailing spaces (and tabs). */
	private static final IValidator NO_SPACES = new NoSpacesValidator();

	private final CatalogCodeUtil catalogCodeUtil;

	/**
	 * Constructor.
	 */
	public CatalogCodeUtilShim() {
        catalogCodeUtil = BeanLocator.getSingletonBean(ContextIdNames.CATALOG_CODE_UTIL, CatalogCodeUtil.class);
	}

	/**
	 * Constructor -- This convenience constructor accepts an array of the atomic validators that must all return OK_STATUS.
	 *
	 * @param validators an array of <code>IValidator</code>s
	 */
	public CatalogCodeUtilShim(final IValidator... validators) {
        catalogCodeUtil = BeanLocator.getSingletonBean(ContextIdNames.CATALOG_CODE_UTIL, CatalogCodeUtil.class);
		for (int i = 0; i < validators.length; i++) {
			this.validators.add(validators[i]);
		}
	}

	public CompoundValidator getCatalogCodeValidator() {
		return getCompoundValidator(catalogCodeUtil.getCatalogCodeFormat());
	}

	public CompoundValidator getCategoryCodeValidator() {
		return getCompoundValidator(catalogCodeUtil.getCategoryCodeFormat());
	}

	public CompoundValidator getProductCodeValidator() {
		return getCompoundValidator(catalogCodeUtil.getProductCodeFormat());
	}

	public CompoundValidator getSkuCodeValidator() {
		return getCompoundValidator(catalogCodeUtil.getSkuCodeFormat());
	}

	public CompoundValidator getBrandCodeValidator() {
		return getCompoundValidator(catalogCodeUtil.getBrandCodeFormat());
	}

	private CompoundValidator getCompoundValidator(final CatalogCodeFormat catalogCodeFormat) {
		CompoundValidator compoundValidator = new CompoundValidator();
		for (IValidator validator : validators) {
			compoundValidator.addValidator(validator);
		}
		if (!catalogCodeFormat.isSpacesAllowed()) {
			compoundValidator.addValidator(NO_SPACES);
		}
		compoundValidator.addValidator(new MaxStringLengthValidator(catalogCodeFormat.getMaxLength()));
		compoundValidator.addValidator(new RegularExpressionValidator(catalogCodeFormat.getRegex(), getErrorMessage(catalogCodeFormat)));
		return compoundValidator;
	}

	private String getErrorMessage(final CatalogCodeFormat catalogCodeFormat) {
		return CoreMessages.get().getMessage(catalogCodeFormat.getInvalidCatalogCodeMessage().getMessageCode());
	}

}