/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.order.impl;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.elasticpath.domain.order.ServiceOrderShipment;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.money.Money;

/**
 * <code>ServiceOrderShipmentImpl</code> represents a collection of service items in the order.
 */
@Entity
@DiscriminatorValue(ShipmentType.SERVICE_STRING)
public class ServiceOrderShipmentImpl extends AbstractOrderShipmentImpl implements ServiceOrderShipment {

	private static final long serialVersionUID = 5000000001L;

	@Override
	@Transient
	public ShipmentType getOrderShipmentType() {
		return ShipmentType.SERVICE;
	}

	@Override
	protected void recalculate() {
		if (isRecalculationEnabled()) {
			synchronized (this) {
				setItemTax(BigDecimal.ZERO);
			}
		}
	}

	@Override
	protected void recalculateTransientDerivedValues() {
		setSubtotal(BigDecimal.ZERO);
		setTotal(BigDecimal.ZERO);
		setItemSubtotal(BigDecimal.ZERO);
		Money totalBeforeTaxMoney = Money.valueOf(BigDecimal.ZERO, getOrder().getCurrency());
		setTotalBeforeTaxMoney(totalBeforeTaxMoney);
	}

	@Override
	@Transient
	public Money getTotalTaxMoney() {
		return Money.valueOf(BigDecimal.ZERO.setScale(2), getOrder().getCurrency());
	}

	@Override
	@Transient
	public boolean isCancellable() {
		return false;
	}

	/**
	 * Implements equals semantics.<br>
	 * This class more than likely would be extended to add functionality that would not effect the equals method in comparisons, and as such would
	 * act as an entity type. In this case, content is not crucial in the equals comparison. Using instanceof within the equals method enables
	 * comparison in the extended classes where the equals method can be reused without violating symmetry conditions. If getClass() was used in the
	 * comparison this could potentially cause equality failure when we do not expect it. If when extending additional fields are included in the
	 * equals method, then the equals needs to be overridden to maintain symmetry.
	 *
	 * @param obj the other object to compare
	 * @return true if equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ServiceOrderShipmentImpl)) {
			return false;
		}

		final ServiceOrderShipmentImpl other = (ServiceOrderShipmentImpl) obj;
		return Objects.equals(getOrderShipmentType(), other.getOrderShipmentType())
			&& super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getOrderShipmentType(), super.hashCode());
	}

}
