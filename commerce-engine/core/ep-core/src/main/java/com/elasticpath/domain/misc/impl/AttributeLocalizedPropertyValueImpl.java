/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.domain.misc.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Implementation of LocalizedPropertyValue for Attribute.
 */
@Entity
@DiscriminatorValue("Attribute")
public class AttributeLocalizedPropertyValueImpl extends AbstractLocalizedPropertyValueImpl {

	private static final long serialVersionUID = -4316068132126082040L;

}
