/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.shoppingcart.impl; //NOPMD

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * The default implementation of <code>ShoppingCartMemento</code>. Note: The guid of a shopping cart is actually the guid of the customer session it
 * belongs to.
 */
@Entity
@Table(name = ShoppingCartMementoImpl.TABLE_NAME)
@DataCache(enabled = false)
@FetchGroups({
	@FetchGroup(
			name = FetchGroupConstants.SHOPPING_ITEM_CHILD_ITEMS, 
			attributes = {
					@FetchAttribute(name = "allItems"),
					@FetchAttribute(name = "storeCode")
			}
		)
	})
public class ShoppingCartMementoImpl extends AbstractShoppingListImpl implements ShoppingCartMemento {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TSHOPPINGCART";

	private long uidPk;
	
	private String storeCode;
	private Date lastModifiedDate;

	/** by default, all newly created carts are active. */
	private ShoppingCartStatus status = ShoppingCartStatus.ACTIVE;

	/** List of ALL the objects in this Cart. */
	private List<ShoppingItem> allItems = new ArrayList<>();


	private static final String FK_COLUMN_NAME = "SHOPPING_CART_UID";
	private Map<String, CartData> cartData = new HashMap<>();
	private Boolean defaultCart;


	/**
	 * Default Constructor.
	 */
	public ShoppingCartMementoImpl() {
		super();
	}

	
	/**
	 * Accesses the field for {@code name} and returns the current value. If the field has not been set
	 * then will return null.
	 *
	 * @param name The name of the field.
	 * @return The current value of the field or null.
	 */
	@Override
	@Transient
	public String getCartDataFieldValue(final String name) {
		CartData data = getCartData().get(name);
		if (data == null) {
			return null;
		}
		return data.getValue();
	}

	@Override
	public void setCartDataFieldValue(final String name, final String value) {
		CartData data;
		if (getCartData().containsKey(name)) {
			data = getCartData().get(name);
			data.setValue(value);
		} else {
			data = createCartData(name, value);
			getCartData().put(name, data);
		}
	}

	private CartData createCartData(final String name, final String value) {
		return new CartData(name, value);
	}

	/**
	 * Internal JPA method to get Item Data.
	 * @return the item data
	 *
	 *

	 */
	@OneToMany(targetEntity = CartData.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@ElementJoinColumn(name = FK_COLUMN_NAME, nullable = false)
	@ElementForeignKey
	@MapKey(name = "key")
	@ElementDependent
	@Override
	public Map<String, CartData> getCartData() {
		return this.cartData;
	}

	@Override
	public void setCartData(final Map<String, CartData> cartData) {
		this.cartData = cartData;
	}


	/**
	 * Initializes the shopping cart. Call setElasticPath before initializing.
	 */
	public void init() {
		// do nothing now.
	}
	
	/**
	 * Get all the items in the shopping cart, including the
	 * ShoppingCartItems, WishListItems, GiftCertificateItems.
	 * 
	 * @return all the items in the shopping cart
	 */
	@Override
	@OneToMany(targetEntity = ShoppingItemImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@ElementJoinColumn(name = "SHOPPING_CART_UID", updatable = false)
	@ElementForeignKey(updateAction = ForeignKeyAction.CASCADE)
	@ElementDependent
	@OrderBy("ordering")
	public List<ShoppingItem> getAllItems() {
		return allItems;
	}
	
	/**
	 * Set the items in the cart after loading a saved cart from the database.
	 * Since JPA can't separate the items between ShoppingCartItem, GiftCertificateItem,
	 * and WishListItem, we need to set them by ourself.
	 * 
	 * @param allItems the allItems to set
	 */
	@Override
	public void setAllItems(final List<ShoppingItem> allItems) {
		this.allItems = allItems;
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
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", 
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME,  allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
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

	@Basic(optional = false)
	@Column(name = "STORECODE")
	@Override
	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public void setStoreCode(final String code) {
		this.storeCode = code;
	}

	@Override
	@Basic
	@Column(name = "LAST_MODIFIED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastModifiedDate() {
		return this.lastModifiedDate;
	}
	
	@Override
	public void setLastModifiedDate(final Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	@Persistent(optional = false)
	@Column(name = "STATUS")
	@Externalizer("getName")
	@Factory("valueOf")
	public ShoppingCartStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(final ShoppingCartStatus status) {
		this.status = status;
	}

	@Basic
	@Column(name = "DEFAULTCART")
	protected Boolean isDefaultInternal() {
		return defaultCart;
	}

	protected void setDefaultInternal(final Boolean defaultCart) {
		this.defaultCart = defaultCart;
	}

	@Override
	@Transient
	public boolean isDefault() {
		return isDefaultInternal() != null && isDefaultInternal();
	}

	@Override
	public void setDefault(final boolean defaultCart) {
		if (defaultCart) {
			setDefaultInternal(true);
		} else {
			setDefaultInternal(null);
		}
	}

}
