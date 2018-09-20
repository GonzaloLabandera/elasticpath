/*
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.domain.catalog.impl;

import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.ForeignKey;

import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductAssociation;
import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.domain.impl.AbstractLegacyEntityImpl;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * Represents a link between two products for the purpose of displaying information about related products when viewing a particular product.
 * Terminology: Source Product - The product that the user is viewing when additional products are to be displayed, e.g. for upselling to a more
 * expensive product. Target Product - The other product that is to be displayed when viewing the source product.
 */
@Entity
@Table(name = ProductAssociationImpl.TABLE_NAME)
@FetchGroups({
		@FetchGroup(name = FetchGroupConstants.PRODUCT_ASSOCIATION_MINIMAL, attributes = {
				@FetchAttribute(name = "associationType"),
				@FetchAttribute(name = "catalog"), @FetchAttribute(name = "sourceProduct"),
				@FetchAttribute(name = "targetProduct")})
})
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.GodClass"})
public class ProductAssociationImpl extends AbstractLegacyEntityImpl implements ProductAssociation {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private static final int DEFAULT_DEFAULT_QUANTITY = 1;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TPRODUCTASSOCIATION";

	private ProductAssociationType associationType;

	private Product sourceProduct;

	private Product targetProduct;

	private Catalog catalog;

	private Date startDate;

	private Date endDate;

	private int defaultQuantity = DEFAULT_DEFAULT_QUANTITY;

	private boolean sourceProductDependent;

	private int ordering;

	private long uidPk;

	private String guid;

	@Persistent
	@Column(name = "ASSOCIATION_TYPE")
	@Externalizer("getOrdinal")
	@Factory("fromOrdinal")
	@Override
	public ProductAssociationType getAssociationType() {
		return associationType;
	}

	@Override
	public void setAssociationType(final ProductAssociationType associationType) {
		this.associationType = associationType;
	}

	/**
	 * Get the source product by this association.
	 *
	 * @return the sourceProduct
	 */
	@Override
	@ManyToOne(targetEntity = ProductImpl.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "SOURCE_PRODUCT_UID")
	@ForeignKey
	public Product getSourceProduct() {
		return sourceProduct;
	}

	/**
	 * Gets the source product.
	 *
	 * @param sourceProduct the sourceProduct to set
	 */
	@Override
	public void setSourceProduct(final Product sourceProduct) {
		this.sourceProduct = sourceProduct;
	}

	/**
	 * Get the product targeted by this association. This is the product that is to be displayed when viewing the source product.
	 *
	 * @return the target product
	 */
	@Override
	@ManyToOne(targetEntity = ProductImpl.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "TARGET_PRODUCT_UID")
	@ForeignKey
	public Product getTargetProduct() {
		return this.targetProduct;
	}

	/**
	 * Set the target product.
	 *
	 * @param targetProduct the target product
	 */
	@Override
	public void setTargetProduct(final Product targetProduct) {
		this.targetProduct = targetProduct;
	}

	/**
	 * Set the catalog in which this association applies.
	 *
	 * @param catalog the catalog in which this association applies
	 */
	@Override
	public void setCatalog(final Catalog catalog) {
		this.catalog = catalog;
	}

	/**
	 * Get the catalog in which this association applies.
	 *
	 * @return the catalog in which this association applies
	 */
	@Override
	@ManyToOne(targetEntity = CatalogImpl.class, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "CATALOG_UID")
	@ForeignKey
	public Catalog getCatalog() {
		return this.catalog;
	}

	/**
	 * Internal method used by JPA.
	 *
	 * @return the start date
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_DATE", nullable = false)
	protected Date getStartDateInternal() {
		return this.startDate;
	}

	/**
	 * Get the starting date on which this <code>MerchandiseAssociation</code> is valid for display.
	 *
	 * @return the start date
	 */
	@Override
	@Transient
	public Date getStartDate() {
		return getStartDateInternal();
	}

	/**
	 * Internal method used by JPA.
	 *
	 * @param startDate the start date
	 */
	@Override
	public void setStartDate(final Date startDate) {
		if (startDate != null && getEndDateInternal() != null
				&& getEndDateInternal().getTime() < startDate.getTime()) {
			throw new EpDomainException("Start date cannot be after end date");
		}
		this.setStartDateInternal(startDate);

	}

	/**
	 * Set the starting date on which this <code>MerchandiseAssociation</code> is valid for display.
	 *
	 * @param startDate the start date
	 */
	protected void setStartDateInternal(final Date startDate) {
		this.startDate = startDate;

	}

	/**
	 * Internal method used by JPA.
	 *
	 * @return the end date
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_DATE")
	protected Date getEndDateInternal() {
		return this.endDate;
	}

	/**
	 * Get the end date after which this <code>MerchandiseAssociation</code> is no longer valid for display.
	 *
	 * @return the end date
	 */
	@Override
	@Transient
	public Date getEndDate() {
		return getEndDateInternal();
	}

	/**
	 * Set the end date after which this <code>MerchandiseAssociation</code> is no longer valid for display.
	 *
	 * @param endDate the end date
	 */
	@Override
	public void setEndDate(final Date endDate) {
		if (endDate != null && getStartDateInternal() != null
				&& endDate.getTime() < getStartDateInternal().getTime()) {
			throw new EpDomainException("End date cannot be before start date");
		}
		this.setEndDateInternal(endDate);
	}

	/**
	 * Internal method used by JPA.
	 *
	 * @param endDate the end date
	 */
	protected void setEndDateInternal(final Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * Get the default quantity of the product targeted by this <code>MerchandiseAssociation</code>. If no default quantity has been set, the
	 * default defaultQuantity is 1. If the target product is added to the cart automatically, it should be added in this default quantity.
	 *
	 * @return the default quantity of the target product
	 */
	@Override
	@Basic
	@Column(name = "DEFAULT_QUANTITY")
	public int getDefaultQuantity() {
		return this.defaultQuantity;
	}

	/**
	 * Set the default quantity of the product targeted by this <code>MerchandiseAssociation</code>.
	 *
	 * @param defaultQuantity the default quantity
	 */
	@Override
	public void setDefaultQuantity(final int defaultQuantity) {
		if (defaultQuantity < 1) {
			// throw new EpDomainException("Default Quantity must be >= 1");
			this.defaultQuantity = 1;
		} else {
			this.defaultQuantity = defaultQuantity;
		}
	}

	/**
	 * Get the order in which this product should appear on the page relative to other products having the same source product.
	 *
	 * @return the ordering
	 */
	@Override
	@Basic
	@Column(name = "ORDERING")
	public int getOrdering() {
		return this.ordering;
	}

	/**
	 * Set the order in which this product should appear on the page relative to other products having the same source product.
	 *
	 * @param ordering the ordering
	 */
	@Override
	public void setOrdering(final int ordering) {
		this.ordering = ordering;
	}

	/**
	 * Returns true if the product targeted by this <code>MerchandiseAssociation</code> depends on the source product such that it should be
	 * removed from the cart if the source product is removed.
	 *
	 * @return true if the target product depends on the source product
	 */
	@Override
	@Basic
	@Column(name = "SOURCE_PRODUCT_DEPENDENT")
	public boolean isSourceProductDependent() {
		return this.sourceProductDependent;
	}

	/**
	 * Set to true if the product targeted by this <code>MerchandiseAssociation</code> depends on the source product such that it should be removed
	 * from the cart if the source product is removed.
	 *
	 * @param sourceProductDependent sets whether the target product depends on the source product
	 */
	@Override
	public void setSourceProductDependent(final boolean sourceProductDependent) {
		this.sourceProductDependent = sourceProductDependent;
	}

	/**
	 * Returns true if this association is valid because the current date is within the start and end dates.
	 *
	 * @return true if the product association is available at today's date.
	 */
	@Override
	@Transient
	public boolean isValid() {
		Date currentDate = new Date();
		if (currentDate.getTime() < getStartDate().getTime()) {
			return false;
		} else if (getEndDate() != null && currentDate.getTime() > getEndDate().getTime()) {
			return false;
		}
		return true;
	}

	@Override
	@Transient
	public boolean isValidProductAssociation() {
		Date currentDate = new Date();
		return !getTargetProduct().isHidden() //target product is not hidden
				&& getStartDate().before(currentDate)
				&& (getEndDate() == null || getEndDate().after(currentDate));
	}


	/**
	 * Returns <code>true</code> if the given association type is valid.
	 *
	 * @param associationType the association type
	 * @return <code>true</code> if the given association type is valid
	 */
	@Override
	public boolean isValidAssociationType(final int associationType) {
		try {
			ProductAssociationType.fromOrdinal(associationType);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Create a deep copy of this <code>ProductAssociation</code>.
	 * Not implementing clone here because the intention is not to create a fresh usable object
	 * to create a stale copy of the ProductAssociation which can be checked to see if changes have been made. Does
	 * not deep copy mutable fields, so if you modify a mutable field you will change the copy and the original.
	 *
	 * @return a deep copy
	 */
	@Override
	public ProductAssociation deepCopy() {
		ProductAssociation deepCopy;
		try {
			deepCopy = this.getClass().newInstance();
		} catch (IllegalAccessException e) {
			throw new EpDomainException("Could not create new ProductAssociation", e);
		} catch (InstantiationException e) {
			throw new EpDomainException("Could not create new ProductAssociation", e);
		}

		deepCopy.setAssociationType(this.getAssociationType());
		deepCopy.setCatalog(this.getCatalog());
		deepCopy.setDefaultQuantity(this.getDefaultQuantity());
		deepCopy.setEndDate(this.getEndDate());
		deepCopy.setGuid(this.getGuid());
		deepCopy.setOrdering(this.getOrdering());
		deepCopy.setSourceProduct(this.getSourceProduct());
		deepCopy.setSourceProductDependent(this.isSourceProductDependent());
		deepCopy.setStartDate(this.getStartDate());
		deepCopy.setTargetProduct(this.getTargetProduct());
		deepCopy.setUidPk(this.getUidPk());
		return deepCopy;
	}

	/**
	 * Returns <code>true</code> if this object equals to the given object.
	 *
	 * @param other the given object
	 * @return <code>true</code> if this object equals to the given object
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ProductAssociationImpl)) {
			// This catches null objects too.
			return false;
		}

		ProductAssociationImpl otherAssociation = (ProductAssociationImpl) other;

		// first check the object IDs
		if (this == otherAssociation) {
			return true;
		}

		// association type has to be checked next as it is only an integer
		return this.associationType == otherAssociation.associationType
				&& Objects.equals(this.catalog, otherAssociation.catalog)
				&& Objects.equals(this.sourceProduct, otherAssociation.sourceProduct)
				&& Objects.equals(this.targetProduct, otherAssociation.targetProduct);
	}

	/**
	 * Generate the hash code.
	 *
	 * @return the hash code.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(associationType, sourceProduct, targetProduct, catalog);
	}

	/**
	 * Method which checks all fields to see if any are different. This is different to equals
	 * in that it compares all fields including mutable ones which shouldn't be used in equals and
	 * hashCode.
	 *
	 * @param otherAssociation the association to compare with.
	 * @return true if the values of both objects are equal for all fields
	 */
	@Override
	@SuppressWarnings("PMD.NPathComplexity")
	public boolean isSameAs(final ProductAssociation otherAssociation) {
		if (otherAssociation == null) {
			return false;
		}

		// first check the object IDs
		if (this == otherAssociation) {
			return true;
		}

		if (getUidPk() != otherAssociation.getUidPk()) {
			return false;
		}

		if (getDefaultQuantity() != otherAssociation.getDefaultQuantity()) {
			return false;
		}

		if (!Objects.equals(getStartDate(), otherAssociation.getStartDate())) {
			return false;
		}

		if (!Objects.equals(getEndDate(), otherAssociation.getEndDate())) {
			return false;
		}

		if (isSourceProductDependent() != otherAssociation.isSourceProductDependent()) {
			return false;
		}

		if (getOrdering() != otherAssociation.getOrdering()) {
			return false;
		}

		// association type has to be checked next
		if (!Objects.equals(getAssociationType(), otherAssociation.getAssociationType())) {
			return false;
		}

		if (!Objects.equals(getCatalog(), otherAssociation.getCatalog())) {
			return false;
		}

		if (!Objects.equals(getSourceProduct(), otherAssociation.getSourceProduct())) {
			return false;
		}

		return Objects.equals(getTargetProduct(), otherAssociation.getTargetProduct());

	}


	/**
	 * Set default values for those fields need default values.
	 */
	@Override
	public void initialize() {
		super.initialize();
		if (getStartDate() == null) {
			setStartDate(new Date());
		}
	}

	/**
	 * String representation of this object.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("startDate", getStartDate())
				.append("endDate", getEndDate())
				.append("defaultQuantity", getDefaultQuantity())
				.append("ordering", getOrdering())
				.append("associationType", getAssociationType())
				.append("targetProduct", getTargetProduct().getUidPk())
				.toString();
	}

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return this.uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	public int compareTo(final ProductAssociation anotherProductAssociation) {
		if (this.getAssociationType().equals(anotherProductAssociation.getAssociationType())) {
			return this.getOrdering() - anotherProductAssociation.getOrdering();
		}
		return this.getAssociationType().getOrdinal() - anotherProductAssociation.getAssociationType().getOrdinal();
	}

	/**
	 * @return The guid.
	 */
	@Override
	@Basic(optional = false)
	@Column(name = "GUID", unique = true)
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid The guid.
	 */
	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

}
