/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.shoppingcart;

import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Cart type domain object.
 */
@Entity
@Table(name = CartType.TABLE_NAME)
public class CartType extends AbstractPersistableImpl implements GloballyIdentifiable {
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the db table.
	 */
	public static final String TABLE_NAME = "TCARTTYPE";
	private long uidPk;
	private String name;
	private List<ModifierGroup> modifiers;
	private String guid;


	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}


	/**
	 * Get the modifiers.
	 * @return the modifiers.
	 */
	@ManyToMany(targetEntity = ModifierGroupImpl.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "TCARTTYPEMODIFIERS", joinColumns = @JoinColumn(name = "CARTTYPE_UID"),
			inverseJoinColumns = @JoinColumn(name = "MODIFIER_UID"))
	public List<ModifierGroup> getModifiers() {
		return modifiers;
	}

	/**
	 * Set the modifiers.
	 * @param modifiers the modifiers.
	 */
	public void setModifiers(final List<ModifierGroup> modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Return the guid.
	 *
	 * @return the guid.
	 */
	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid to set.
	 */
	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Gets the name of the cart type.
	 * @return the name.
	 */
	@Column(name = "NAME")
	@Basic
	public String getName() {
		return name;
	}

	/**
	 * Sets the cartType Name.
	 * @param name the name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Return the hash code.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getName(), getGuid(), getModifiers());
	}

	/**
	 * Return{@code true} if the given object is a {@link CartType} and has the same fields.
	 *
	 * @param obj the object to compare
	 * @return <code>true</code> if the given object is equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CartType)) {
			return false;
		}
		CartType otherCartType = (CartType) obj;
		return otherCartType.getGuid().equals(this.getGuid())
				&& otherCartType.getModifiers().equals(this.getModifiers())
				&& otherCartType.getName().equals(this.getName());


	}

}
