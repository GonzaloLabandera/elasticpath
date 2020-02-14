/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.cmclient.admin.paymentconfigurations.dialogs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderManagementService;

/**
 * The Edit payment configuration dialog box.
 */
public class PaymentConfigurationsEditDialog extends AbstractPaymentConfigurationsDialog {

	private final PaymentConfigurationsListModel selectedConfig;
	private PaymentProviderConfigDTO paymentProviderConfigDTO;
	private final Map<String, List<PaymentProviderPluginDTO>> paymentProviderPlugins;

	/**
	 * Create a new edit payment configuration dialog.
	 *
	 * @param selectedConfig the payment configurationt to display and edit.
	 * @param parentShell    the parent shell control.
	 * @param image          the image for edit.
	 * @param title          the title of the edit dialog.
	 */
	public PaymentConfigurationsEditDialog(final PaymentConfigurationsListModel selectedConfig, final Shell parentShell, final Image image,
										   final String title) {
		super(parentShell, image, title);
		this.selectedConfig = selectedConfig;

		final PaymentProviderManagementService paymentProviderManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_MANAGEMENT_SERVICE, PaymentProviderManagementService.class);
		paymentProviderPlugins = paymentProviderManagementService.findAll().values().stream()
				.collect(Collectors.groupingBy(PaymentProviderPluginDTO::getPaymentVendorId));
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		super.createEpDialogContent(dialogComposite);
		getPaymentConfigurationProviderCombo().setEnabled(false);
		getPaymentConfigurationMethodCombo().setEnabled(false);
		if (PaymentProviderConfigurationStatus.DRAFT.equals(selectedConfig.getStatus())) {
			super.setEditablePaymentConfigurationData();
		}
	}

	@Override
	public PaymentProviderConfigDTO getPaymentProviderConfigDTO() {
		return paymentProviderConfigDTO;
	}

	@Override
	protected void populateControls() {
		setPaymentProviderAndMethod();
		getPaymentConfigurationNameText().setText(selectedConfig.getConfigurationName());
		Optional.ofNullable(selectedConfig.getConfigDto().getDefaultDisplayName()).ifPresent(getPaymentDisplayNameText()::setText);
		setLocalizedNames();
		setConfigurationData();
	}

	@Override
	protected boolean prepareForSave() {
		String paymentVendorId = getPaymentConfigurationProviderCombo().getText();
		String paymentMethodId = getPaymentConfigurationMethodCombo().getText();
		Optional<PaymentProviderPluginDTO> optionalPluginDTO = paymentProviderPlugins.get(paymentVendorId).stream()
				.filter(plugins -> plugins.getPaymentMethodId().equalsIgnoreCase(paymentMethodId))
				.findFirst();

		if (!optionalPluginDTO.isPresent()) {
			return false;
		}

		final PaymentProviderPluginDTO paymentProviderPluginDTO = optionalPluginDTO.get();

		paymentProviderConfigDTO = selectedConfig.getConfigDto();
		paymentProviderConfigDTO.setConfigurationName(getPaymentConfigurationNewName());
		paymentProviderConfigDTO.setDefaultDisplayName(getPaymentDisplayNameText().getText());
		if (PaymentProviderConfigurationStatus.DRAFT.equals(selectedConfig.getStatus())) {
			paymentProviderConfigDTO.setPaymentConfigurationData(getPaymentConfigurationData(paymentProviderPluginDTO));
		}
		Map<String, String> localizedNames = getLocalizationControls().stream()
				.filter(localizationPair -> localizationPair.getFirst().getText() != null
						&& localizationPair.getSecond().getSelectionIndex() != -1)
				.collect(Collectors.toMap(localizationPair -> getLanguageTag(localizationPair.getSecond()),
						localizationPair -> localizationPair.getFirst().getText()));

		paymentProviderConfigDTO.setLocalizedNames(localizedNames);
		return true;
	}

	@Override
	protected boolean doesPaymentConfigurationNameNotExist(final String paymentConfigurationName) {
		return Objects.equals(paymentConfigurationName, selectedConfig.getConfigurationName())
				|| super.doesPaymentConfigurationNameNotExist(paymentConfigurationName);
	}

	private void setLocalizedNames() {
		Map<String, String> persistedLocalizedNames = selectedConfig.getConfigDto().getLocalizedNames();
		if (persistedLocalizedNames == null || persistedLocalizedNames.isEmpty()) {
			super.addDefaultLocaleArea();
			return;
		}
		persistedLocalizedNames.forEach((language, displayName) -> {
			Pair<Text, CCombo> localeControls = super.addLocaleArea(true);
			localeControls.getFirst().setText(displayName);
			String languageDisplayName = Locale.forLanguageTag(language.replaceAll("_", "-")).getDisplayName();
			for (int i = 0; i < localeControls.getSecond().getItems().length; ++i) {
				if (languageDisplayName.equals(localeControls.getSecond().getItem(i))) {
					localeControls.getSecond().select(i);
					break;
				}
			}
		});
	}

	private void setConfigurationData() {
		final List<PaymentProviderPluginDTO> pluginDTOS = paymentProviderPlugins.get(getPaymentConfigurationProviderCombo().getText());

		if (Objects.nonNull(selectedConfig.getPaymentConfigurationData()) && Objects.nonNull(pluginDTOS)) {
			pluginDTOS.stream().findFirst()
					.map(PaymentProviderPluginDTO::getConfigurationKeys)
					.map(keys -> keys.stream()
							.collect(LinkedHashMap::new,
									(map, key) -> map.put(StringUtils.defaultIfBlank(key.getDescription(), key.getKey()),
											selectedConfig.getPaymentConfigurationData().get(key.getKey())),
									Map::putAll))
					.ifPresent(this::setPaymentConfigurationProperties);
		}
	}

	private void setPaymentProviderAndMethod() {
		getPaymentConfigurationProviderCombo().add(AdminPaymentConfigurationMessages.get().ComboFirstItem, 0);
		getPaymentConfigurationProviderCombo().add(selectedConfig.getPaymentVendorId(), 1);
		getPaymentConfigurationProviderCombo().select(1);
		getPaymentConfigurationProviderCombo().setEditable(false);
		getPaymentConfigurationMethodCombo().add(AdminPaymentConfigurationMessages.get().ComboFirstItem, 0);
		getPaymentConfigurationMethodCombo().add(selectedConfig.getPaymentMethodId(), 1);
		getPaymentConfigurationMethodCombo().select(1);
		getPaymentConfigurationMethodCombo().setEditable(false);
	}
}
