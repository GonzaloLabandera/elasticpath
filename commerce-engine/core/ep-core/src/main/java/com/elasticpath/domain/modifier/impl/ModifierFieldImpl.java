/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

import com.elasticpath.domain.impl.AbstractLegacyEntityImpl;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierType;

/**
 * Implements ModifierField.
 */
@Entity
@Table(name = ModifierFieldImpl.TABLE_NAME)
@SuppressWarnings("PMD.GodClass")
public class ModifierFieldImpl extends AbstractLegacyEntityImpl implements ModifierField {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TMODIFIERFIELD";

	private long uidPk;

	private String code;

	private boolean required;

	private int ordering;

	private Integer maxSize;

	private int attributeTypeId;

	private Set<ModifierFieldLdf> modifierFieldLdfs = new HashSet<>();

	private Set<ModifierFieldOption> modifierFieldOptions = new HashSet<>();

	private String defaultCartValue;

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME, allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return this.uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Transient
	public String getGuid() {
		return getCode();
	}

	@Override
	public void setGuid(final String guid) {
		setCode(guid);
	}

	@Override
	@Basic
	@Column(name = "CODE")
	public String getCode() {
		return code;
	}

	@Override
	public void setCode(final String code) {
		this.code = code;
	}

	@Override
	@Basic
	@Column(name = "REQUIRED")
	public boolean isRequired() {
		return required;
	}

	@Override
	public void setRequired(final boolean required) {
		this.required = required;
	}

	@Override
	@Basic
	@Column(name = "ORDERING")
	public int getOrdering() {
		return ordering;
	}

	@Override
	public void setOrdering(final int ordering) {
		this.ordering = ordering;
	}

	@Override
	@Basic
	@Column(name = "MAX_SIZE")
	public Integer getMaxSize() {
		return maxSize;
	}

	@Override
	public void setMaxSize(final Integer maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	@Basic
	@Column(name = "DEFAULT_CART_VALUE")
	public String getDefaultCartValue() {
		return defaultCartValue;
	}

	@Override
	public void setDefaultCartValue(final String defaultCartValue) {
		this.defaultCartValue = defaultCartValue;
	}

	@Override
	@Transient
	public ModifierType getFieldType() {
		if (getAttributeTypeId() == 0) {
			return null;
		}
		return ModifierType.valueOf(getAttributeTypeId());
	}

	@Override
	public void setFieldType(final ModifierType fieldType) {
		if (fieldType == null) {
			setAttributeTypeId(0);
		} else {
			setAttributeTypeId(fieldType.getOrdinal());
		}
	}

	@Basic
	@Column(name = "ATTRIBUTE_TYPE")
	protected int getAttributeTypeId() {
		return attributeTypeId;
	}

	protected void setAttributeTypeId(final int attributeTypeId) {
		this.attributeTypeId = attributeTypeId;
	}

	@OneToMany(targetEntity = ModifierFieldLdfImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "MODIFIER_FIELD_UID", nullable = false)
	@ElementForeignKey(name = "TMODFIELDLDF_IBFK_1")
	@ElementDependent
	protected Set<ModifierFieldLdf> getModifierFieldsLdfInternal() {
		return modifierFieldLdfs;
	}

	protected void setModifierFieldsLdfInternal(final Set<ModifierFieldLdf> modifierFieldLdfs) {
		this.modifierFieldLdfs = modifierFieldLdfs;
	}

	@OneToMany(targetEntity = ModifierFieldOptionImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "MODIFIER_FIELD_UID", nullable = false)
	@ElementForeignKey(name = "TMODFIELDOPTION_IBFK_1")
	@ElementDependent
	protected Set<ModifierFieldOption> getModifierFieldOptionsInternal() {
		return modifierFieldOptions;
	}

	protected void setModifierFieldOptionsInternal(final Set<ModifierFieldOption> modifierFieldOptions) {
		this.modifierFieldOptions = modifierFieldOptions;
	}

	@Override
	public Set<ModifierFieldLdf> getModifierFieldsLdf() {
		return Collections.unmodifiableSet(getModifierFieldsLdfInternal());
	}

	@Override
	public Set<ModifierFieldOption> getModifierFieldOptions() {
		return Collections.unmodifiableSet(getModifierFieldOptionsInternal());
	}

	@Override
	public void addModifierFieldLdf(final ModifierFieldLdf modifierFieldLdf) {
		if (StringUtils.isEmpty(modifierFieldLdf.getDisplayName())) {
			throw new IllegalArgumentException("Cannot have a modifierFieldLdf with empty display name");
		}
		if (StringUtils.isEmpty(modifierFieldLdf.getLocale())) {
			throw new IllegalArgumentException("Cannot have a modifierFieldLdf with empty locale");
		}

		for (ModifierFieldLdf existentModifierFieldLdf : this.getModifierFieldsLdfInternal()) {
			if (modifierFieldLdf.getLocale().equals(existentModifierFieldLdf.getLocale())) {
				throw new IllegalArgumentException("Cannot have two modifierFieldLdf with the same locale");
			}
		}

		this.getModifierFieldsLdfInternal().add(modifierFieldLdf);
	}

	@Override
	public void removeModifierFieldLdf(final ModifierFieldLdf modifierFieldLdf) {
		boolean remove = this.getModifierFieldsLdfInternal().remove(modifierFieldLdf);
		if (!remove) {
			throw new IllegalStateException("Cannot remove modifierFieldLdf");
		}
	}

	@Override
	public void addModifierFieldOption(final ModifierFieldOption modifierFieldOption) {
		if (StringUtils.isEmpty(modifierFieldOption.getValue())) {
			throw new IllegalArgumentException("Cannot have empty modifierFieldOption.value");
		}

		for (ModifierFieldOption existentModifierFieldOption : this.getModifierFieldOptionsInternal()) {
			if (existentModifierFieldOption.getOrdering() == modifierFieldOption.getOrdering()) {
				throw new IllegalArgumentException("Cannot have modifierFieldOption with the same ordering as existent object"
						+ modifierFieldOption.getOrdering());
			}
		}

		this.getModifierFieldOptionsInternal().add(modifierFieldOption);
	}

	@Override
	public void removeModifierFieldOption(final ModifierFieldOption modifierFieldOption) {
		boolean remove = this.getModifierFieldOptionsInternal().remove(modifierFieldOption);
		if (!remove) {
			throw new IllegalStateException("Cannot remove modifierFieldOption");
		}
	}

	@Override
	public ModifierFieldLdf findModifierFieldLdfByLocale(final String language) {
		for (ModifierFieldLdf existentModifierFieldLdf : this.getModifierFieldsLdfInternal()) {
			if (language.equals(existentModifierFieldLdf.getLocale())) {
				return existentModifierFieldLdf;
			}
		}
		return null;
	}

	@Override
	public ModifierFieldOption findModifierFieldOptionByValue(final String value) {
		for (ModifierFieldOption existentModifierFieldOption : this.getModifierFieldOptionsInternal()) {
			if (value.equals(existentModifierFieldOption.getValue())) {
				return existentModifierFieldOption;
			}
		}
		return null;
	}

	/*
	 * Sorting on ordering is natural sorting for ModifierField.
	 */
	@Override
	public int compareTo(final ModifierField obj) {
		return this.ordering - obj.getOrdering();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof ModifierFieldImpl)) {
			return false;
		}

		ModifierFieldImpl otherEntity = (ModifierFieldImpl) other;
		return Objects.equals(getGuid(), otherEntity.getGuid());
	}
}
