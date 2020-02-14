/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.dialogs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderManagementService;

/**
 * Create payment configuration dialog.
 */
class PaymentConfigurationsCreateDialog extends AbstractPaymentConfigurationsDialog {


	private final Map<String, List<PaymentProviderPluginDTO>> paymentProviderPlugins;
	private int currentPaymentProviderIndex;
	private int currentPaymentMethodIdIndex;

	/**
	 * Payment Configuration which will be created in this create dialog.
	 */
	private PaymentProviderConfigDTO paymentProviderConfigDTO;

	/**
	 * The create dialog constructor. Doesn't need configuration parameter. The configuration will be created at the time user clicks save button,
	 * because
	 * configuration's implementations is unknown up front.
	 *
	 * @param parentShell the parent shell
	 * @param image       this create dialog image
	 * @param title       this create dialog title
	 */
	protected PaymentConfigurationsCreateDialog(final Shell parentShell, final Image image, final String title) {
		super(parentShell, image, title);
		PaymentProviderManagementService paymentProviderManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_MANAGEMENT_SERVICE, PaymentProviderManagementService.class);

		paymentProviderPlugins = paymentProviderManagementService.findAll().values().stream()
				.collect(Collectors.groupingBy(PaymentProviderPluginDTO::getPaymentVendorId));
	}

	@Override
	public PaymentProviderConfigDTO getPaymentProviderConfigDTO() {
		return paymentProviderConfigDTO;
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		super.createEpDialogContent(dialogComposite);
		super.setEditablePaymentConfigurationData();
		getPaymentConfigurationProviderCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				LOG.debug("widgetDefaultSelected: " + event); //$NON-NLS-1$
			}

			/**
			 * Forces configuration's property table to be updated when user changes configuration implementation in UI.
			 *
			 * @param event SelectionEvent
			 */
			@Override
			public void widgetSelected(final SelectionEvent event) {

				/**
				 * Update property table for the specified configuration implementation.
				 */
				if (updatePropertiesTable(getPaymentConfigurationProviderCombo().getText(), getPaymentConfigurationMethodCombo().getText(),
						false)
						&& updateMethodCombo(getPaymentConfigurationProviderCombo().getText())) {
					currentPaymentProviderIndex = getPaymentConfigurationProviderCombo().getSelectionIndex();
				} else {
					/**
					 * select previously used implementation if user do not want to discard properties' changes
					 */
					getPaymentConfigurationProviderCombo().select(currentPaymentProviderIndex);
				}
			}
		});

		getPaymentConfigurationMethodCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				LOG.debug("widgetDefaultSelected: " + event); //$NON-NLS-1$
			}

			/**
			 * Forces configuration's property table to be updated when user changes configuration implementation in UI.
			 *
			 * @param event SelectionEvent
			 */
			@Override
			public void widgetSelected(final SelectionEvent event) {

				/**
				 * Update property table for the specified configuration implementation.
				 */
				if (updatePropertiesTable(getPaymentConfigurationProviderCombo().getText(), getPaymentConfigurationMethodCombo().getText(),
						true)) {
					currentPaymentMethodIdIndex = getPaymentConfigurationMethodCombo().getSelectionIndex();
				} else {
					/**
					 * select previously used implementation if user do not want to discard properties' changes
					 */
					getPaymentConfigurationMethodCombo().select(currentPaymentMethodIdIndex);
				}
			}
		});
		addDefaultLocaleArea();
	}

	@Override
	protected void populateControls() {
		getPaymentConfigurationProviderCombo().add(AdminPaymentConfigurationMessages.get().ComboFirstItem, 0);
		for (String vendorId : paymentProviderPlugins.keySet()) {
			getPaymentConfigurationProviderCombo().add(vendorId);
		}
		getPaymentConfigurationProviderCombo().select(0);
		currentPaymentProviderIndex = 0;

		getPaymentConfigurationMethodCombo().add(AdminPaymentConfigurationMessages.get().ComboFirstItem, 0);
		getPaymentConfigurationMethodCombo().select(0);
		currentPaymentMethodIdIndex = 0;
		updatePropertiesTable(getPaymentConfigurationProviderCombo().getText(), getPaymentConfigurationMethodCombo().getText(),
				true);
	}

	/**
	 * Gets method list contextId using UI vendor id. Populates method combo using that vendor id.
	 *
	 * @param vendorId vendor id seen in UI
	 * @return true if configuration's method either were not modified or user want to reject modifications.
	 */
	protected boolean updateMethodCombo(final String vendorId) {

		getPaymentConfigurationMethodCombo().removeAll();
		getPaymentConfigurationMethodCombo().add(AdminPaymentConfigurationMessages.get().ComboFirstItem, 0);
		getPaymentConfigurationMethodCombo().select(0);
		currentPaymentMethodIdIndex = 0;

		if (isComboReset(vendorId)) {
			return true;
		}

		List<String> methods = paymentProviderPlugins.get(vendorId).stream()
				.map(PaymentProviderPluginDTO::getPaymentMethodId)
				.sorted()
				.collect(Collectors.toList());

		for (String methodId : methods) {
			getPaymentConfigurationMethodCombo().add(methodId);
		}

		return true;
	}

	/**
	 * Gets configuration contextId using UI vendor id and method id. Creates configuration using that contextId.
	 * Gets default properties for that configuration. Questions user to reject previously modified properties.
	 *
	 * @param vendorId                        vendor id seen in UI
	 * @param methodId                        method id seen in UI
	 * @param updatePropertiesTableWithValues whether the properties table should be updated with values from provider
	 * @return true if configuration's properties either were not modified or user want to reject modifications.
	 */
	protected boolean updatePropertiesTable(final String vendorId,
											final String methodId,
											final boolean updatePropertiesTableWithValues) {

		if (isPaymentConfigurationPropertiesModified()
				&& !MessageDialog.openQuestion(getShell(), AdminPaymentConfigurationMessages.get().PaymentConfigurationPropertiesModifiedTitle,
				AdminPaymentConfigurationMessages.get().PaymentConfigurationPropertiesModifiedText)) {
			return false;
		}

		if (isComboReset(vendorId) || isComboReset(methodId)) {
			setPaymentConfigurationProperties(Collections.emptyMap());
			return true;
		}

		Optional<PaymentProviderPluginDTO> optionalPluginDTO = paymentProviderPlugins.get(vendorId).stream()
				.findFirst();

		optionalPluginDTO.map(PaymentProviderPluginDTO::getConfigurationKeys)
				.map(keys -> keys.stream()
						.collect(LinkedHashMap::new,
								(map, key) -> map.put(StringUtils.defaultIfBlank(key.getDescription(), key.getKey()),
										StringUtils.EMPTY),
								Map::putAll))
				.ifPresent(this::setPaymentConfigurationProperties);

		return optionalPluginDTO.isPresent();
		}

	private boolean isComboReset(final String comboValue) {
		return comboValue == null || comboValue.isEmpty() || comboValue.equalsIgnoreCase(AdminPaymentConfigurationMessages.get().ComboFirstItem);
	}

	@Override
	protected boolean prepareForSave() {
		int paymentVendorIndex = getPaymentConfigurationProviderCombo().getSelectionIndex();
		int paymentMethodIndex = getPaymentConfigurationMethodCombo().getSelectionIndex();

		if (paymentVendorIndex == 0 || paymentMethodIndex == 0) {
			paymentProviderConfigDTO = null;
			return false;
		}

		String paymentVendorId = getPaymentConfigurationProviderCombo().getText();
		String paymentMethodId = getPaymentConfigurationMethodCombo().getText();

		Optional<PaymentProviderPluginDTO> optionalPluginDTO = paymentProviderPlugins.get(paymentVendorId).stream()
				.filter(plugins -> plugins.getPaymentMethodId().equalsIgnoreCase(paymentMethodId))
				.findFirst();

		if (!optionalPluginDTO.isPresent()) {
			return false;
		}

		PaymentProviderPluginDTO paymentProviderPluginDTO = optionalPluginDTO.get();

		paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setConfigurationName(getPaymentConfigurationNewName());
		paymentProviderConfigDTO.setDefaultDisplayName(getPaymentDisplayNameText().getText());
		paymentProviderConfigDTO.setPaymentProviderPluginBeanName(paymentProviderPluginDTO.getPluginBeanName());
		paymentProviderConfigDTO.setPaymentConfigurationData(getPaymentConfigurationData(paymentProviderPluginDTO));

		Map<String, String> localizedNames = getLocalizationControls().stream()
				.filter(localizationPair -> localizationPair.getFirst().getText() != null
						&& localizationPair.getSecond().getSelectionIndex() != -1)
				.collect(Collectors.toMap(localizationPair -> getLanguageTag(localizationPair.getSecond()),
						localizationPair -> localizationPair.getFirst().getText()));

		paymentProviderConfigDTO.setLocalizedNames(localizedNames);
		return true;
	}
}
