/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jms broker configurator.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JmsBrokerConfigurator {

	/**
	 * JMS broker URL.
	 *
	 * @return JMS broker URL.
	 */
	String url();

}
