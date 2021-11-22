/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.modifier.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;

/**
 * Adapts an XPFModifierField to the ModifierField interface.
 */
public class ModifierFieldAdapter implements ModifierField {

	private final XPFModifierField xpfModifierField;

	/**
	 * Constructor.
	 *
	 * @param xpfModifierField the xpf modifier field.
	 */
	public ModifierFieldAdapter(final XPFModifierField xpfModifierField) {
		this.xpfModifierField = xpfModifierField;
	}

	@Override
	public String getCode() {
		return xpfModifierField.getCode();
	}

	@Override
	public  void setCode(final String code) {
		// empty method
	}

	@Override
	public boolean isRequired() {
		return xpfModifierField.isRequired();
	}

	@Override
	public void setRequired(final boolean required) {
		// empty method
	}

	@Override
	public int getOrdering() {
		return 0;
	}

	@Override
	public void setOrdering(final int ordering) {
		// empty method
	}

	@Override
	public Integer getMaxSize() {
		return xpfModifierField.getMaxSize();
	}

	@Override
	public void setMaxSize(final Integer maxSize) {
		// empty method
	}

	@Override
	public ModifierType getFieldType() {
		return ModifierType.valueOf(xpfModifierField.getModifierType());
	}

	@Override
	public void setFieldType(final ModifierType fieldType) {
		// empty method
	}

	@Override
	public Set<ModifierFieldLdf> getModifierFieldsLdf() {
		return null;
	}

	@Override
	public Set<ModifierFieldOption> getModifierFieldOptions() {
		return xpfModifierField.getModifierFieldOptions().stream()
				.map(xpfModifierFieldOption -> {
					ModifierFieldOptionImpl modifierFieldOption = new ModifierFieldOptionImpl();
					modifierFieldOption.setValue(xpfModifierFieldOption.getValue());

					return modifierFieldOption;
				})
				.collect(Collectors.toSet());
	}

	@Override
	public void addModifierFieldLdf(final ModifierFieldLdf modifierFieldLdf) {
		// empty method
	}

	@Override
	public void removeModifierFieldLdf(final ModifierFieldLdf modifierFieldLdf) {
		// empty method
	}

	@Override
	public void addModifierFieldOption(final ModifierFieldOption modifierFieldOption) {
		// empty method
	}

	@Override
	public void removeModifierFieldOption(final ModifierFieldOption modifierFieldOption) {
		// empty method
	}

	@Override
	public ModifierFieldLdf findModifierFieldLdfByLocale(final String language) {
		return null;
	}

	@Override
	public ModifierFieldOption findModifierFieldOptionByValue(final String value) {
		return null;
	}

	@Override
	public void setDefaultCartValue(final String defaultCartValue) {
		// empty method
	}

	@Override
	public String getDefaultCartValue() {
		return null;
	}

	@Override
	public String getGuid() {
		return null;
	}

	@Override
	public void setGuid(final String guid) {
		// empty method
	}

	@Override
	public void initialize() {
		// empty method
	}

	@Override
	public long getUidPk() {
		return 0;
	}

	@Override
	public void setUidPk(final long uidPk) {
		// empty method
	}

	@Override
	public boolean isPersisted() {
		return false;
	}

	@Override
	public int compareTo(final ModifierField modifierField) {
		return 0;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		ModifierFieldAdapter that = (ModifierFieldAdapter) other;

		return new EqualsBuilder()
				.append(xpfModifierField, that.xpfModifierField)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(xpfModifierField)
				.toHashCode();
	}
}
