/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services;

import java.util.List;
import java.util.Optional;

import org.apache.camel.language.Simple;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.reader.FindAllResponse;

/**
 * A service for reading {@link Offer}.
 */
public interface OfferService {

	/**
	 * Reads an Offer entity containing offer projections from a datasource.
	 *
	 * @param store the store code of the projection.
	 * @param code  the guid of the projection.
	 * @return the projection.
	 */
	Optional<Offer> get(@Simple("${header.storeCode}") String store, @Simple("${header.attributeCode}") String code);

	/**
	 * Returns a list of all Offer entities contained within a given Store.
	 * The results are sorted by Offer code in ascending order.
	 *
	 * @param store               the store code of the projection.
	 * @param limit               the number of Offers to return in a single request.
	 * @param startAfter          pagination cursor that instructs the service to return results starting after the provided Offer.
	 * @param modifiedSince       pagination threshold that instructs the service to return results starting after the provided data.
	 * @param modifiedSinceOffset the time in minutes by which to backdate the modifiedSince parameter value.
	 * @return list of all Offer projection entities contained within a given Store.
	 */
	FindAllResponse<Offer> getAllOffers(@Simple("${header.storeCode}") String store, @Simple("${header.limit}") String limit,
										@Simple("${header.startAfter}") String startAfter, @Simple("${header.modifiedSince}") String modifiedSince,
										@Simple("${header.modifiedSinceOffset}") String modifiedSinceOffset);

	/**
	 * Returns a list of latest Offer entities with codes from a given list.
	 *
	 * @param store the store code of the Offer.
	 * @param codes list of Offer codes.
	 * @return list of all Offers contained within a given Store.
	 */
	List<Offer> getLatestOffersWithCodes(@Simple("${body.getData.get(store)}") String store,
										 @Simple("${body.getData.get(codes)}") List<String> codes);

}
