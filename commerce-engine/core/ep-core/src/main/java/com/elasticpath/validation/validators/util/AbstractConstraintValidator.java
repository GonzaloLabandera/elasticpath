/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.validators.util;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;

/**
 * Class to support Hibernate based constraint validation.
 *
 * @param <T> type of object dealing with specific validator classes.
 *
 */
public abstract class AbstractConstraintValidator<T extends DynamicValue> {
	private static final ProviderSpecificBootstrap<HibernateValidatorConfiguration> HIBERNATE_BOOTSTRAP =
			Validation.byProvider(HibernateValidator.class);
	private static final String VALIDATION_MESSAGES_PREFIX = "ValidationMessages";

	private final Class<T> validationClass;
	private final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache;
	private final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache;

	private HibernateValidatorConfiguration configuration;
	private ConstraintMapping constraintMapping;
	private PropertyConstraintMappingContext constraintMappingContext;

	/**
	 * Constructor.
	 * @param validationClass the typed class
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 */
	public AbstractConstraintValidator(final Class<T> validationClass,
									   final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
									   final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		this.validationClass = validationClass;
		this.requiredValidatorCache = requiredValidatorCache;
		this.completeValidatorCache = completeValidatorCache;
	}

	/**
	 * Initialize the Hibernate configuration, constraint mapping, and constraint mapping context.
	 */
	protected void initHibernateValidator() {
		this.configuration = HIBERNATE_BOOTSTRAP
				.providerResolver(createValidationProviderResolver(new HibernateValidator()))
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new PlatformResourceBundleLocator(VALIDATION_MESSAGES_PREFIX,
										AbstractConstraintValidator.this.getClass().getClassLoader())
						)
				);
		this.constraintMapping = this.configuration.createConstraintMapping();

		//tell validator what type and field need to be validated
		this.constraintMappingContext = this.constraintMapping
				.type(validationClass)
				.property("valueToValidate", ElementType.FIELD);
	}

	/**
	 * Validate dynamic value.
	 *
	 * @param dynamicValue the dynamic value to validate.
	 * @return a set of {@link ConstraintViolation} instances or empty set.
	 */
	public Set<ConstraintViolation<T>> validate(final T dynamicValue) {
		Pair<Object, Set<String>> cacheKey = new Pair<>(dynamicValue.getValueTypeDefinition(), dynamicValue.getValidOptions());
		Validator isRequiredValidator = requiredValidatorCache.get(cacheKey, thisCacheKey -> getIsRequiredValidator(dynamicValue));
		Set<ConstraintViolation<T>> violations = isRequiredValidator.validate(dynamicValue);
		if (!violations.isEmpty()) {
			return violations;
		}
		Validator completeValidator = completeValidatorCache.get(cacheKey, thisCacheKey -> getCompleteValidator(dynamicValue));
		return completeValidator.validate(dynamicValue);
	}

	/**
	 * Get a validator for the dynamic value that verifies if the value is required.
	 * @param dynamicValue the dynamic value to validate
	 * @return a validator created for the dynamic value
	 */
	public abstract Validator getIsRequiredValidator(T dynamicValue);

	/**
	 * Get a validator for the dynamic value that verifies all requirements.
	 * @param dynamicValue the dynamic value to validate
	 * @return a validator created for the dynamic value
	 */
	public abstract Validator getCompleteValidator(T dynamicValue);

	private ValidationProviderResolver createValidationProviderResolver(final ValidationProvider<?> provider) {
		return () -> Collections.singletonList(provider);
	}

	public HibernateValidatorConfiguration getConfiguration() {
		return configuration;
	}

	public ConstraintMapping getConstraintMapping() {
		return constraintMapping;
	}

	public PropertyConstraintMappingContext getConstraintMappingContext() {
		return constraintMappingContext;
	}
}
