/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier.impl;

import java.util.Collections;
import java.util.HashSet;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Implements ModifierField.
 */
@Entity
@Table(name = ModifierFieldOptionImpl.TABLE_NAME)
public class ModifierFieldOptionImpl extends AbstractPersistableImpl implements ModifierFieldOption {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TMODIFIERFIELDOPTION";

	private long uidPk;

	private String value;

	private int ordering;

	private Set<ModifierFieldOptionLdf> modifierFieldOptionLdfs = new HashSet<>();

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
	@Basic
	@Column(name = "OPTION_VALUE")
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(final String value) {
		this.value = value;
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

	@OneToMany(targetEntity = ModifierFieldOptionLdfImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "MOD_FIELD_OPTION_UID", nullable = false)
	@ElementForeignKey(name = "TMODFIELDOPTLDF_IBFK_1")
	@ElementDependent
	protected Set<ModifierFieldOptionLdf> getModifierFieldOptionsLdfInternal() {
		return modifierFieldOptionLdfs;
	}

	protected void setModifierFieldOptionsLdfInternal(final Set<ModifierFieldOptionLdf> modifierFieldOptionLdfs) {
		this.modifierFieldOptionLdfs = modifierFieldOptionLdfs;
	}

	/**
	 * See interface javadoc.
	 *
	 * @return a ModifierFieldOptionLdf set
	 */
	@Override
	public Set<ModifierFieldOptionLdf> getModifierFieldOptionsLdf() {
		return Collections.unmodifiableSet(getModifierFieldOptionsLdfInternal());
	}

	@Override
	public void addModifierFieldOptionLdf(final ModifierFieldOptionLdf modifierFieldOptionLdf) {
		if (StringUtils.isEmpty(modifierFieldOptionLdf.getDisplayName())) {
			throw new IllegalArgumentException("Cannot have ModifierFieldOptionLdf with empty display name");
		}
		if (StringUtils.isEmpty(modifierFieldOptionLdf.getLocale())) {
			throw new IllegalArgumentException("Cannot have ModifierFieldOptionLdf with empty locale");
		}

		for (ModifierFieldOptionLdf existentModifierFieldOptionLdf : this.getModifierFieldOptionsLdfInternal()) {
			if (existentModifierFieldOptionLdf.getLocale().equals(modifierFieldOptionLdf.getLocale())) {
				throw new IllegalArgumentException("Cannot have two ModifierFieldOptionLdf with the same locale");
			}
		}

		this.getModifierFieldOptionsLdfInternal().add(modifierFieldOptionLdf);
	}

	@Override
	public void removeModifierFieldOptionLdf(final ModifierFieldOptionLdf modifierFieldOptionLdf) {
		boolean remove = this.getModifierFieldOptionsLdfInternal().remove(modifierFieldOptionLdf);
		if (!remove) {
			throw new IllegalStateException("Cannot remove ModifierFieldOptionLdf");
		}
	}

	@Override
	public ModifierFieldOptionLdf getModifierFieldOptionsLdfByLocale(final String locale) {
		for (ModifierFieldOptionLdf existentModifierFieldOptionLdf : this.getModifierFieldOptionsLdfInternal()) {
			if (existentModifierFieldOptionLdf.getLocale().equals(locale)) {
				return existentModifierFieldOptionLdf;
			}
		}

		return null;
	}

	/*
	 * Sorting on ordering is natural sorting for ModifierFieldOption.
     */
	@Override
	public int compareTo(final ModifierFieldOption obj) {
		return this.ordering - obj.getOrdering();
	}
}
