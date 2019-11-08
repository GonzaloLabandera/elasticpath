/**
 * Copyright (c) 2009. ElasticPath Software Inc. All rights reserved.
 */
package com.elasticpath.tags.builder;

import com.elasticpath.tags.domain.TagDefinition;

/**
 * A custom string class that's metaclass will be changing in the run-time.
 */
public class BuilderString {
	private TagDefinition key;
	private String keyString;
	
	/**
	 * Default constructor which sets the active key name.
	 */
	public BuilderString(TagDefinition key, String keyString) {
		this.key = key;
		this.keyString = keyString;
	}
	
	/**
	 * Delegates any method invocation to the LogicalTreeBuilder#addCondition.
	 */
	def methodMissing(String name, args) {
		LogicalTreeBuilder.getInstance().addCondition(this.key, this.keyString, args, name);
	}
}
