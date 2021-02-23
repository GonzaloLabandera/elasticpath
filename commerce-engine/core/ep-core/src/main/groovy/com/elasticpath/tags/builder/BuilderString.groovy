/**
 * Copyright (c) 2009. ElasticPath Software Inc. All rights reserved.
 */
package com.elasticpath.tags.builder;

import com.elasticpath.tags.domain.TagDefinition;

/**
 * A custom string class that's metaclass will be changing in the run-time.
 */
class BuilderString {
	private LogicalTreeBuilder logicalTreeBuilder;
	private TagDefinition key;
	private String keyString;

	/**
	 * Default constructor which sets the active key name.
	 */
	BuilderString(final LogicalTreeBuilder logicalTreeBuilder, final TagDefinition key, final String keyString) {
		this.logicalTreeBuilder = logicalTreeBuilder;
		this.key = key;
		this.keyString = keyString;
	}
	
	/**
	 * Delegates any method invocation to the LogicalTreeBuilder#addCondition.
	 */
	def methodMissing(final String name, final args) {
		logicalTreeBuilder.addCondition(this.key, this.keyString, args, name);
	}
}
