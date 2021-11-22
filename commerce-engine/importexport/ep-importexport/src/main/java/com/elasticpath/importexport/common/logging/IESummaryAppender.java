/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.ObjectMessage;

import com.elasticpath.importexport.common.summary.SummaryLogger;
import com.elasticpath.importexport.common.util.Message;

/**
 * Implements summary appender for import export.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Plugin(name = IESummaryAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class IESummaryAppender extends AbstractAppender {

	/** Plugin Name. */
	public static final String PLUGIN_NAME = "IESummary";
	
	private SummaryLogger summary;

	/**
	 * Constructs summary appender.
	 * 
	 */
	private IESummaryAppender(final String name, final Filter filter, final Layout layout,
			final boolean ignoreExceptions, final Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	@Override
	public void append(final LogEvent event) {

		/*
		 * We are filtering for {@link
		 * com.elasticpath.importexport.common.util.Message}, so it is safe to expect
		 * all messages that make it this far to be EP Messages.
		 */
		ObjectMessage objectMessage = (ObjectMessage) event.getMessage();
		Message message = (Message) objectMessage.getParameter();
		
		if (Level.ERROR.equals(event.getLevel())) {
			summary.addFailure(message);
		} else if (Level.WARN.equals(event.getLevel())) {
			summary.addWarning(message);
		} else if (Level.INFO.equals(event.getLevel())) {
			summary.addComment(message);
		}
	}

	/**
	 * @return a new instance of Builder.
	 */
	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Builder for IESummaryAppender.
	 * 
	 * @param <B> The type to build
	 */
	@SuppressWarnings({ "squid:S2176", "squid:S2972" })
	public static class Builder<B extends Builder<B>> extends AbstractFilterable.Builder<B>
			implements org.apache.logging.log4j.core.util.Builder<IESummaryAppender> {

		@PluginBuilderAttribute
		@Required(message = "No name provided for IESummaryAppender")
		private String name;

		@PluginBuilderAttribute
		private boolean ignoreExceptions;

		/**
		 * Set the name.
		 * 
		 * @param name the name
		 * @return this Builder
		 */
		public Builder setName(final String name) {
			this.name = name;
			return this;
		}
		
		/**
		 * Set ignoreExceptions.
		 * 
		 * @param ignoreExceptions true if the appender should ignore exceptions
		 * @return this Builder
		 */
		public Builder setIgnoreExceptions(final boolean ignoreExceptions) {
			this.ignoreExceptions = ignoreExceptions;
			return this;
		}

		@SuppressWarnings("PMD.AccessorClassGeneration")
		@Override
		public IESummaryAppender build() {
			
			Filter ieSummaryFilter = new AbstractFilter() {
				@Override
				public Result filter(final LogEvent event) {
					Result result = Result.DENY;

					if (event.getMessage() instanceof ObjectMessage) {
						ObjectMessage objectMessage = (ObjectMessage) event.getMessage();

						if (objectMessage.getParameter() instanceof Message) {
							result = Result.ACCEPT;
						}
					}

					return result;
				}
			};
			Layout layout =  PatternLayout.createDefaultLayout();
			
			return new IESummaryAppender(name, ieSummaryFilter, layout, ignoreExceptions, getPropertyArray());
		}
	}

	public void setSummary(final SummaryLogger summary) {
		this.summary = summary;
	}
}