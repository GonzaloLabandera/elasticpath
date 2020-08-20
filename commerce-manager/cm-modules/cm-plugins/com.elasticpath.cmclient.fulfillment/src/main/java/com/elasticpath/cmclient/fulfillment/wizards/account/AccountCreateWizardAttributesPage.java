/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.account;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;

import com.elasticpath.cmclient.catalog.editors.attribute.AttributesViewPart;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ui.framework.ControlModificationListener;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.attribute.impl.CustomerProfileValueImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.attribute.AttributeService;

/**
 * Account attributes values wizard.
 */
public class AccountCreateWizardAttributesPage extends AbstractEPWizardPage<Customer>
		implements ControlModificationListener {

	private static final int PAGE_LAYOUT_NUM_COLUMNS = 2;

	private AttributesViewPart attributesViewPart;

	private final AttributeService attributeService = BeanLocator.getSingletonBean(ContextIdNames.ATTRIBUTE_SERVICE, AttributeService.class);

	/**
	 * Constructor.
	 *
	 * @param pageName the page name
	 * @param title    the page title
	 */
	protected AccountCreateWizardAttributesPage(final String pageName, final String title) {
		super(PAGE_LAYOUT_NUM_COLUMNS, false, pageName, new DataBindingContext());

		setTitle(title);
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		attributesViewPart = new CustomerAttributesViewPart(getModel(), EpControlFactory.EpState.EDITABLE, null);
		attributesViewPart.setControlModificationListener(this);
		attributesViewPart.createControls(pageComposite);

		this.setControl(pageComposite.getSwtComposite());
	}

	@Override
	protected void populateControls() {
		// do nothing
	}

	@Override
	protected void bindControls() {
		// Auto-generated method stub
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		if (!attributesViewPart.isInitialized()) {
			attributesViewPart.setInput(getAttributes());
		}
		return true;
	}

	private AttributeValue[] getAttributes() {
		return getModel().getCustomerProfile()
				.getProfileAttributes(getModel().getCustomerType())
				.stream()
				.filter(attribute -> !attribute.isSystem())
				.map(this::createEmptyAttributeValue)
				.toArray(AttributeValue[]::new);
	}

	private AttributeValue createEmptyAttributeValue(final Attribute attribute) {
		final AttributeValue empty = new CustomerProfileValueImpl();
		empty.setAttribute(attribute);
		empty.setLocalizedAttributeKey(attribute.getKey());
		empty.setAttributeType(attribute.getAttributeType());
		empty.setValue(null);

		return empty;
	}

	/**
	 * Validates profile attributes before finish.
	 *
	 * @return false if validation failed.
	 */
	public boolean validate() {
		final String requiredAttributesWithoutValues = findRequiredAttributesWithoutValues(getModel());
		return StringUtils.isEmpty(requiredAttributesWithoutValues);
	}

	/**
	 * Finds empty required attributes.
	 *
	 * @param customer account to verify.
	 * @return string with empty required attributes.
	 */
	public String findRequiredAttributesWithoutValues(final Customer customer) {
		final Collection<Attribute> profileAttributes = attributeService.getCustomerProfileAttributes(AttributeUsageImpl.ACCOUNT_PROFILE_USAGE);

		return profileAttributes.stream()
				.filter(Attribute::isRequired)
				.filter(attribute -> checkAttributeValues(customer, attribute))
				.map(Attribute::getKey)
				.collect(Collectors.joining(","));
	}

	private boolean checkAttributeValues(final Customer customerToSave, final Attribute attribute) {
		final Object attributeValue = customerToSave.getCustomerProfile().getProfileValue(attribute.getKey());

		if (attributeValue instanceof String) {
			return StringUtils.isEmpty((String) attributeValue);
		}

		return Objects.isNull(attributeValue);
	}

	@Override
	public void controlModified() {
		getWizard().getContainer().updateButtons();
	}
}
