/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

import com.elasticpath.commons.util.Pair;

/**
 * A utility class to help the testing of Log4J output.
 */
@Plugin(name = TestLog4jLoggingAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class TestLog4jLoggingAppender extends AbstractAppender {

	/** Plugin Name. */
	public static final String PLUGIN_NAME = "TestLog4jLogging";

	private TestLog4jLoggingAppender(final String name, final Filter filter,
			final Layout<? extends Serializable> layout, final boolean ignoreExceptions, final Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	private final Map<EventKey, State> called = new HashMap<>();

	/**
	 * Typedef for may key to keep the rest of the code cleaner.
	 */
	class EventKey extends Pair<Level, String> {
		private static final long serialVersionUID = 107774455892787679L;

		/**
		 * Creates an instance.
		 * 
		 * @param level   the logging level.
		 * @param message the logging message.
		 */
		EventKey(final Level level, final String message) {
			super(level, message);
		}
	}

	/**
	 * Verify that the expected log messages were received and that no unexpected
	 * messages were received. Calls Assert.fail with details of the failure, if
	 * any.
	 */
	public void verify() {
		boolean result = true;
		for (final Map.Entry<EventKey, State> callEntry : called.entrySet()) {
			State state = callEntry.getValue();
			if (!state.shouldCall() && state.wasCalled() || state.shouldCall() && !state.wasCalled()) {
				result = false;
			}
		}
		if (!result) {
			fail(getResults());
		}
	}

	private String getResults() {
		StringBuilder builder = new StringBuilder();
		for (final Map.Entry<EventKey, State> callEntry : called.entrySet()) {
			State state = callEntry.getValue();
			builder.append("Logging message '").append(state.getLevel()).append(": ").append(state.getMsg())
					.append("' was ");
			if (state.shouldCall()) {
				builder.append("expected ");
			} else {
				builder.append("not expected ");
			}
			if (state.wasCalled()) {
				builder.append("and called");
			} else {
				builder.append("and was not called");
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	/**
	 * Adds an logging expectation.
	 * 
	 * @param level the level at which the message should be logged.
	 * @param msg   the message that is expected to be logged.
	 */
	public void addMessageToVerify(final Level level, final String msg) {
		called.put(new EventKey(level, msg), new State(msg, true, false, level));
	}

	/**
	 * The tie into log4j - listen for log events and add them for later
	 * verification.
	 * 
	 * @param loggingEvent the log event that the code under test has emitted.
	 */
	@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
	@Override
	public synchronized void append(final LogEvent loggingEvent) {
		EventKey key = new EventKey(loggingEvent.getLevel(), loggingEvent.getMessage().toString());
		if (called.containsKey(key)) {
			called.get(key).setCalled(true);
		} else {
			called.put(key, new State(loggingEvent.getMessage().toString(), false, true, loggingEvent.getLevel()));
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
	 * The details of a single log message.
	 */
	@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
	private class State {

		private final String msg;
		private final boolean shouldCall;
		private boolean called;
		private final Level level;

		State(final String msg, final boolean shouldCall, final boolean wasCalled, final Level level) {
			this.msg = msg;
			this.shouldCall = shouldCall;
			this.called = wasCalled;
			this.level = level;
		}

		public String getMsg() {
			return msg;
		}

		public boolean shouldCall() {
			return shouldCall;
		}

		public boolean wasCalled() {
			return called;
		}

		public void setCalled(final boolean called) {
			this.called = called;
		}

		public Level getLevel() {
			return level;
		}
	}

	/**
	 * Builder for IESummaryAppender.
	 * 
	 * @param <B> The type to build
	 */
	@SuppressWarnings({ "squid:S2176", "squid:S2972" })
	public static class Builder<B extends Builder<B>> extends AbstractFilterable.Builder<B>
			implements org.apache.logging.log4j.core.util.Builder<TestLog4jLoggingAppender> {

		@PluginBuilderAttribute
		@Required(message = "No name provided for TestLog4jLoggingAppender")
		private String name = PLUGIN_NAME;

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
		public TestLog4jLoggingAppender build() {

			Filter testLog4jLoggingFilter = new AbstractFilter() {
				@Override
				public Result filter(final LogEvent event) {
					return Result.ACCEPT;
				}
			};
			Layout layout = PatternLayout.createDefaultLayout();

			return new TestLog4jLoggingAppender(name, testLog4jLoggingFilter, layout, ignoreExceptions,
					getPropertyArray());
		}
	}

}
