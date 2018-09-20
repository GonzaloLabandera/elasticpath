/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.tools.sync.job.descriptor.impl;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.elasticpath.tools.sync.job.Command;
import com.elasticpath.tools.sync.job.descriptor.TransactionJobDescriptorEntry;

/**
 * Describes synchronization action on a single object.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class TransactionJobDescriptorEntryImpl implements TransactionJobDescriptorEntry {

	private static final long serialVersionUID = 8092438206905100783L;

	@XmlElement(name = "guid", required = false)
	private String guid;

	@XmlElement(name = "type", required = false)
	private Class<?> type;

	@XmlElement(name = "command", required = false)
	private Command command;

	/**
	 * @return guid of described object
	 */
	@Override
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid guid of described object
	 */
	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * @return type of described object
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	/**
	 * @param type type of described object
	 */
	@Override
	public void setType(final Class<?> type) {
		this.type = type;
	}

	/**
	 * @return operation on this object during synchronization
	 */
	@Override
	public Command getCommand() {
		return command;
	}

	/**
	 * @param command operation on this object during synchronization
	 */
	@Override
	public void setCommand(final Command command) {
		this.command = command;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TransactionJobDescriptorEntry)) {
			return false;
		}
		final TransactionJobDescriptorEntry jobEntry = (TransactionJobDescriptorEntry) obj;

		return Objects.equals(getGuid(), jobEntry.getGuid())
			&& Objects.equals(getType(), jobEntry.getType())
			&& Objects.equals(getCommand(), jobEntry.getCommand());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getGuid(), getType(), getCommand());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
		append("objectType", getType()).
		append("objectGuid", getGuid()).
		append("command", getCommand()).
		toString();
	}
}
