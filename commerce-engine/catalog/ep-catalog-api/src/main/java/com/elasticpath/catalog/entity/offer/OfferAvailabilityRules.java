/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an entity of Offer available rules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferAvailabilityRules extends AvailabilityRules {

	private final ZonedDateTime releaseDateTime;
	private final Set<String> canDiscover;
	private final Set<String> canView;
	private final Set<String> canAddToCart;

	/**
	 * Constructor.
	 *
	 * @param enableDateTime  is enableDateTime of Offer.
	 * @param disableDateTime is disableDateTime of Offer.
	 * @param releaseDateTime is releaseDateTime of Offer.
	 * @param canDiscover     condition if Offer can discover.
	 * @param canView         condition if Offer can view.
	 * @param canAddToCart    condition if Offer can add to cart.
	 */
	@JsonCreator
	public OfferAvailabilityRules(@JsonProperty("enableDateTime") final ZonedDateTime enableDateTime,
								  @JsonProperty("disableDateTime") final ZonedDateTime disableDateTime,
								  @JsonProperty("releaseDateTime") final ZonedDateTime releaseDateTime,
								  @JsonProperty("canDiscover") final Set<String> canDiscover,
								  @JsonProperty("canView") final Set<String> canView,
								  @JsonProperty("canAddToCart") final Set<String> canAddToCart) {
		super(enableDateTime, disableDateTime);
		this.releaseDateTime = releaseDateTime;
		this.canDiscover = canDiscover;
		this.canView = canView;
		this.canAddToCart = canAddToCart;
	}

	/**
	 * Get releaseDateTime of Offer.
	 *
	 * @return releaseDateTime of Offer.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getReleaseDateTime() {
		return releaseDateTime;
	}

	/**
	 * Get set of conditions for Offer discovering.
	 *
	 * @return set of conditions for Offer discovering.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Set<String> getCanDiscover() {
		return canDiscover;
	}

	/**
	 * Get set of conditions for Offer viewing.
	 *
	 * @return set of conditions for Offer viewing.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Set<String> getCanView() {
		return canView;
	}

	/**
	 * Get set of conditions for adding Offer to cart.
	 *
	 * @return set of conditions for adding Offer to cart.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Set<String> getCanAddToCart() {
		return canAddToCart;
	}

}
