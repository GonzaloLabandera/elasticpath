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
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;

/**
 * Implements ModifierField.
 */
@Entity
@Table(name = ModifierGroupImpl.TABLE_NAME)
public class ModifierGroupImpl extends AbstractLegacyEntityImpl implements ModifierGroup {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TMODIFIERGROUP";

	private long uidPk;

	private String code;

	private Set<ModifierGroupLdf> modifierGroupLdfs = new HashSet<>();

	private Set<ModifierField> modifierFields = new HashSet<>();

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
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

	@OneToMany(targetEntity = ModifierGroupLdfImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "MODIFIER_GROUP_UID", nullable = false)
	@ElementForeignKey(name = "TMODIFIERGROUP_IBFK_1")
	@ElementDependent
	protected Set<ModifierGroupLdf> getModifierGroupsLdfInternal() {
		return modifierGroupLdfs;
	}

	protected void setModifierGroupsLdfInternal(final Set<ModifierGroupLdf> modifierGroupLdfs) {
		this.modifierGroupLdfs = modifierGroupLdfs;
	}

	@OneToMany(targetEntity = ModifierFieldImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "MODIFIER_GROUP_UID", nullable = false)
	@ElementForeignKey(name = "TMODGROUPLDF_IBFK_1")
	@ElementDependent
	protected Set<ModifierField> getModifierFieldsInternal() {
		return modifierFields;
	}

	protected void setModifierFieldsInternal(final Set<ModifierField> modifierFields) {
		this.modifierFields = modifierFields;
	}


	/**
	 * See interface javadoc.
	 *
	 * @return a ModifierGroupLdf set
	 */
	@Override
	public Set<ModifierGroupLdf> getModifierGroupLdf() {
		return Collections.unmodifiableSet(getModifierGroupsLdfInternal());
	}

	/**
	 * See interface javadoc.
	 *
	 * @return a ModifierField set
	 */
	@Override
	public Set<ModifierField> getModifierFields() {
		return Collections.unmodifiableSet(getModifierFieldsInternal());
	}

	@Override
	public void addModifierField(final ModifierField modifierField) {
		if (StringUtils.isEmpty(modifierField.getCode())) {
			throw new IllegalArgumentException("Cannot add a newModifierField with empty code");
		}

		ModifierField modifierField1 = getModifierFieldByCode(modifierField.getCode());
		if (modifierField1 != null) {
			return;
		}

		for (ModifierField existentModifierField : this.getModifierFieldsInternal()) {
			if (existentModifierField.getOrdering() == modifierField.getOrdering()) {
				throw new IllegalArgumentException(
						"Cannot have two newModifierField with the same ordering: " + modifierField.getOrdering()
								+ ". Existent guid: " + existentModifierField.getCode() + ", New Guid: "
								+ modifierField.getCode());
			}
		}

		this.getModifierFieldsInternal().add(modifierField);
	}

	@Override
	public void removeModifierField(final ModifierField modifierField) {
		boolean remove = this.getModifierFieldsInternal().remove(modifierField);
		if (!remove) {
			throw new IllegalStateException("Cannot remove ModifierField");
		}
	}

	@Override
	public void addModifierGroupLdf(final ModifierGroupLdf modifierGroupLdf) {
		if (StringUtils.isEmpty(modifierGroupLdf.getDisplayName())) {
			throw new IllegalArgumentException("Cannot have a ModifierGroupLdf with empty display name");
		}
		if (StringUtils.isEmpty(modifierGroupLdf.getLocale())) {
			throw new IllegalArgumentException("Cannot a ModifierGroupLdf with empty locale");
		}

		for (ModifierGroupLdf existentModifierGroupLdf : this.getModifierGroupsLdfInternal()) {
			if (existentModifierGroupLdf.equals(modifierGroupLdf)) {
				return;
			}

			if (existentModifierGroupLdf.getLocale().equals(modifierGroupLdf.getLocale())) {
				throw new IllegalArgumentException("Cannot have two ModifierGroupLdf with the same locale");
			}
		}

		this.getModifierGroupsLdfInternal().add(modifierGroupLdf);
	}

	@Override
	public void removeModifierGroupLdf(final ModifierGroupLdf modifierGroupLdf) {
		boolean remove = this.getModifierGroupsLdfInternal().remove(modifierGroupLdf);
		if (!remove) {
			throw new IllegalStateException("Cannot remove ModifierGroupLdf");
		}
	}

	@Override
	public void removeAllModifierGroupLdf() {
		this.getModifierGroupsLdfInternal().clear();
	}

	@Override
	public void removeAllModifierFields() {
		this.getModifierFieldsInternal().clear();
	}

	@Override
	public ModifierGroupLdf getModifierGroupLdfByLocale(final String language) {
		for (ModifierGroupLdf modifierGroupLdf : this.getModifierGroupsLdfInternal()) {
			if (modifierGroupLdf.getLocale().equals(language)) {
				return modifierGroupLdf;
			}
		}
		return null;
	}

	@Override
	public ModifierField getModifierFieldByCode(final String code) {
		for (ModifierField existentModifierField : this.getModifierFieldsInternal()) {
			if (existentModifierField.getCode().equals(code)) {
				return existentModifierField;
			}
		}
		return null;
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

		if (!(other instanceof ModifierGroupImpl)) {
			return false;
		}

		ModifierGroupImpl otherEntity = (ModifierGroupImpl) other;
		return Objects.equals(getGuid(), otherEntity.getGuid());
	}

}
