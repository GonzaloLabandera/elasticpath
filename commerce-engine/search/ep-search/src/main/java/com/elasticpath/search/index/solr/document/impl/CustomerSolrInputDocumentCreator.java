/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.document.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * Creates a {@link SolrInputDocument} for an {@link Customer}.
 */
public class CustomerSolrInputDocumentCreator extends AbstractDocumentCreatingTask<Customer> {

	/**
	 * Creates a {@link SolrInputDocument} based on the {@link Customer}.
	 *
	 * @return the {@link SolrInputDocument}.
	 */
	@Override
	public SolrInputDocument createDocument() {
		if (getEntity() == null) {
			return null;
		}
		if (getEntity().getSharedId() == null || getEntity().getSharedId().trim().length() == 0) {
			return null;
		}

		final SolrInputDocument document = new SolrInputDocument();
		addFieldToDocument(document, SolrIndexConstants.OBJECT_UID, String.valueOf(getEntity().getUidPk()));
		addFieldToDocument(document, SolrIndexConstants.SHARED_ID, getAnalyzer().analyze(getEntity().getSharedId()));
		addFieldToDocument(document, SolrIndexConstants.USERNAME, getAnalyzer().analyze(getEntity().getUsername()));
		addFieldToDocument(document, SolrIndexConstants.FIRST_NAME, getAnalyzer().analyze(getEntity().getFirstName()));
		addFieldToDocument(document, SolrIndexConstants.LAST_NAME, getAnalyzer().analyze(getEntity().getLastName()));
		addFieldToDocument(document, SolrIndexConstants.EMAIL, getAnalyzer().analyze(getEntity().getEmail()));
		addFieldToDocument(document, SolrIndexConstants.PHONE_NUMBER, getAnalyzer().analyze(getEntity().getPhoneNumber()));
		addFieldToDocument(document, SolrIndexConstants.CREATE_TIME, getAnalyzer().analyze(getEntity().getCreationDate()));
		addFieldToDocument(document, SolrIndexConstants.STORE_CODE, getAnalyzer().analyze(getEntity().getStoreCode()));
		addFieldToDocument(document, SolrIndexConstants.CUSTOMER_TYPE, getEntity().getCustomerType().getName());
		addFieldToDocument(document, SolrIndexConstants.ROOT_LEVEL, getAnalyzer().analyze(calculateRootLevel()));
		addFieldToDocument(document, SolrIndexConstants.BUSINESS_NAME, getAnalyzer().analyze(getEntity().getBusinessName()));
		addFieldToDocument(document, SolrIndexConstants.BUSINESS_NUMBER, getAnalyzer().analyze(getEntity().getAccountBusinessNumber()));
		addFieldToDocument(document, SolrIndexConstants.AP_PHONE_NUMBER, getAnalyzer().analyze(getEntity().getAccountPhoneNumber()));
		addFieldToDocument(document, SolrIndexConstants.AP_FAX_NUMBER, getAnalyzer().analyze(getEntity().getAccountFaxNumber()));
		addFieldToDocument(document, SolrIndexConstants.AP_TAX_EXEMPTION_ID, getAnalyzer().analyze(getEntity().getAccountTaxExemptionId()));


		List<String> addresses = new ArrayList<>();

		for (CustomerAddress address : getEntity().getAddresses()) {
			addresses.add(getAnalyzer().analyze(address.getZipOrPostalCode()));
		}

		addFieldToDocument(document, SolrIndexConstants.ZIP_POSTAL_CODE, addresses);

		addPreferredBillingAddress(getEntity(), document);

		return document;
	}

	private String calculateRootLevel() {
		final Customer customer = getEntity();
		if (customer.getCustomerType() == CustomerType.ACCOUNT) {
			return isRootAccount(customer)
					? Boolean.TRUE.toString()
					: Boolean.FALSE.toString();
		}
		return null;
	}

	private boolean isRootAccount(final Customer customer) {
		return StringUtils.isEmpty(customer.getParentGuid());
	}

	private void addPreferredBillingAddress(final Customer customer, final SolrInputDocument document) {
		CustomerAddress preferredBillingAddress = customer.getPreferredBillingAddress();

		if (preferredBillingAddress != null) {
			addFieldToDocument(document, SolrIndexConstants.PREFERRED_BILLING_ADDRESS, getAnalyzer()
					.analyze(preferredBillingAddress.toPlainString()));
		}
	}
}
