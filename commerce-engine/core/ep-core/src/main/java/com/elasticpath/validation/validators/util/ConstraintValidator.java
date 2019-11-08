/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.validators.util;

import java.lang.annotation.ElementType;
import java.util.Collections;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

/**
 * Class to support Hibernate based constraint validation.
 *
 * @param <T> type of object dealing with specific validator classes.
 *
 */
public class ConstraintValidator<T> {
	private static final ProviderSpecificBootstrap<HibernateValidatorConfiguration> HIBERNATE_BOOTSTRAP =
			Validation.byProvider(HibernateValidator.class);

	private static final String VALIDATION_MESSAGES_PREFIX = "ValidationMessages";

	private final HibernateValidatorConfiguration configuration;
	private final ConstraintMapping constraintMapping;
	private final PropertyConstraintMappingContext constraintMappingContext;

	/**
	 * Constructor which preps the constraint context with the typed class.
	 * @param validationClass the typed class
	 */
	public ConstraintValidator(final Class<T> validationClass) {
		this.configuration = HIBERNATE_BOOTSTRAP
				.providerResolver(createValidationProviderResolver(new HibernateValidator()))
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new PlatformResourceBundleLocator(VALIDATION_MESSAGES_PREFIX,
										ConstraintValidator.this.getClass().getClassLoader())
						)
				);
		this.constraintMapping = this.configuration.createConstraintMapping();

		//tell validator what type and field need to be validated
		this.constraintMappingContext = this.constraintMapping
				.type(validationClass)
				.property("valueToValidate", ElementType.FIELD);

	}

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
