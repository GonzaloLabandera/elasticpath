/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.email.handler.customer.helper.impl;

import java.util.Locale;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.impl.AbstractEpDomainImpl;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.WishListMessage;
import com.elasticpath.domain.store.Store;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.customer.helper.WishListEmailPropertyHelper;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Helper for processing email properties for wish list e-mails.
 */
public class WishListEmailPropertyHelperImpl extends AbstractEpDomainImpl implements WishListEmailPropertyHelper {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	private static final String WISHLIST_EMAIL_HTML_TEMPLATE = "wishList.html";

	private static final String WISHLIST_EMAIL_TXT_TEMPLATE = "wishList.txt";

	private ProductSkuLookup productSkuLookup;

	@Override
	public EmailProperties getWishListEmailProperties(final WishListMessage wishListMessage, final WishList wishList,
			final Store store, final Locale locale) {
		final EmailProperties emailProperties = getEmailPropertiesBeanInstance();
		emailProperties.getTemplateResources().put("shoppingCartUid", wishList.getUidPk());
		emailProperties.getTemplateResources().put("wishListItems", wishList.getAllItems());
		emailProperties.getTemplateResources().put("wishListMessage", wishListMessage);
		emailProperties.getTemplateResources().put("locale", locale);
		emailProperties.getTemplateResources().put("productSkuLookup", productSkuLookup);
		emailProperties.setLocaleDependentSubjectKey("wishList.emailSubject");
		emailProperties.setDefaultSubject("My Wish List");
		emailProperties.setEmailLocale(locale);
		emailProperties.setHtmlTemplate(WISHLIST_EMAIL_HTML_TEMPLATE);
		emailProperties.setTextTemplate(WISHLIST_EMAIL_TXT_TEMPLATE);
		emailProperties.setStoreCode(store.getCode());
		return emailProperties;
	}

	/**
	 *
	 * @return
	 */
	private EmailProperties getEmailPropertiesBeanInstance() {
		return getBean(ContextIdNames.EMAIL_PROPERTIES);
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

}
