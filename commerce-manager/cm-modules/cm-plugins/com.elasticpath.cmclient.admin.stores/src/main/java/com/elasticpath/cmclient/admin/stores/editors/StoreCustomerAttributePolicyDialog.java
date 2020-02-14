/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.helpers.store.StoreCustomerAttributeModel;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

/**
 * Store customer attribute policy dialog.
 */
public class StoreCustomerAttributePolicyDialog extends AbstractEpDialog {

	private CCombo attributeCombo;

	private CCombo policyCombo;

	private Label permissionsLabel;

	private List<Attribute> attributes;

	private List<PolicyKey> policies;

	private final StoreCustomerAttributeModel attributeModel;

	private final List<StoreCustomerAttributeModel> attributeModels;

	private final Function<PolicyKey, String> policyPermissionFormatter;

	private final String windowTitle;

	private final DataBindingContext dataBindingContext = new DataBindingContext();

	private IEpLayoutComposite dialogComposite;

	/**
	 * Creates the edit settings dialog.
	 *
	 * @param parentShell                  the parent shell
	 * @param windowTitle                  the window title
	 * @param attributeModel                        the attribute model
	 * @param attributeModels the attribute models
	 * @param policyPermissionFormatter    the policy permission formatter
	 */
	public StoreCustomerAttributePolicyDialog(final Shell parentShell, final String windowTitle, final StoreCustomerAttributeModel attributeModel,
											  final List<StoreCustomerAttributeModel> attributeModels,
											  final Function<PolicyKey, String> policyPermissionFormatter) {
		super(parentShell, 2, false);
		this.windowTitle = windowTitle;
		this.attributeModel = attributeModel;
		this.attributeModels = attributeModels;
		this.policyPermissionFormatter = policyPermissionFormatter;
	}

	@Override
	protected void bindControls() {
		EpControlBindingProvider.getInstance().bind(dataBindingContext,
				attributeCombo, new CompoundValidator(EpValidatorFactory.REQUIRED_COMBO_FIRST_ELEMENT_NOT_VALID,
						 new DuplicateAttributePolicyValidator()), null, new AttributeUpdateStrategy(),
				true);

		EpControlBindingProvider.getInstance().bind(dataBindingContext,
				policyCombo, EpValidatorFactory.REQUIRED_COMBO_FIRST_ELEMENT_NOT_VALID, null, new PolicyUpdateStrategy(),
				true);

		EpDialogSupport.create(this, dataBindingContext);
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		final IEpLayoutData labelData = dialogComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.BEGINNING);
		final IEpLayoutData fieldData = dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);

		dialogComposite.addLabelBoldRequired(AdminStoresMessages.get().Store_AttributePolicies_Attribute, EpState.EDITABLE, labelData);

		attributeCombo = dialogComposite.addComboBox(EpState.EDITABLE, fieldData);
		attributeCombo.add(AdminStoresMessages.get().Store_AttributePolicies_SelectAttribute);
		attributeCombo.select(0);
		attributeCombo.setEditable(false);

		dialogComposite.addLabelBoldRequired(AdminStoresMessages.get().Store_AttributePolicies_Policy, EpState.EDITABLE, labelData);

		policyCombo = dialogComposite.addComboBox(EpState.EDITABLE, fieldData);
		policyCombo.add(AdminStoresMessages.get().Store_AttributePolicies_SelectPolicy);
		policyCombo.select(0);
		policyCombo.setEditable(false);

		dialogComposite.addLabelBold(AdminStoresMessages.get().Store_AttributePolicies_Permissions, labelData);
		permissionsLabel = dialogComposite.addLabelBold(StringUtils.EMPTY, labelData);
		permissionsLabel.setText(AdminStoresMessages.get().PolicyPermission_Null);

		this.dialogComposite = dialogComposite;
	}

	/**
	 * Attribute update strategy.
	 */
	private class AttributeUpdateStrategy extends ObservableUpdateValueStrategy {
		@Override
		protected IStatus doSet(final IObservableValue observableValue,
								final Object newValue) {
			if (attributeCombo.getSelectionIndex() == 0) {
				return Status.OK_STATUS;
			}

			attributeModel.setAttributeKey(attributes.get(attributeCombo.getSelectionIndex() - 1).getKey());

			return Status.OK_STATUS;
		}
	}

	/**
	 * Policy update strategy.
	 */
	private class PolicyUpdateStrategy extends ObservableUpdateValueStrategy {
		@Override
		protected IStatus doSet(final IObservableValue observableValue,
								final Object newValue) {
			if (policyCombo.getSelectionIndex() == 0) {
				return Status.OK_STATUS;
			}

			final PolicyKey policyKey = policies.get(policyCombo.getSelectionIndex() - 1);
			attributeModel.setPolicyKey(policyKey);

			permissionsLabel.setText(policyPermissionFormatter.apply(policyKey));
			dialogComposite.getSwtComposite().layout(true, true);

			return Status.OK_STATUS;
		}
	}

	/**
	 * Duplicate attribute policy validator.
	 */
	private class DuplicateAttributePolicyValidator implements IValidator {

		@Override
		public IStatus validate(final Object value) {
			final Integer index = (Integer) value;
			if (index == 0) {
				return Status.OK_STATUS;
			}
			if (attributePolicyExists(attributes.get(index - 1))) {
				return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, IStatus.ERROR, AdminStoresMessages.get().AttributePolicyExists, null);
			}
			return Status.OK_STATUS;
		}

		private boolean attributePolicyExists(final Attribute attribute) {
			final String newAttributeKey = attribute.getKey();
			return attributeModels.stream()
					.anyMatch(attributeModel -> attributeModel.getAttributeKey().equals(newAttributeKey)
							&& !attributeModel.getGuid().equals(attributeModel.getGuid()));
		}
	}

	@Override
	protected String getInitialMessage() {
		return null;
	}

	@Override
	protected String getTitle() {
		return getWindowTitle();
	}

	@Override
	protected Image getWindowImage() {
		return null;
	}

	@Override
	protected String getWindowTitle() {
		return windowTitle;
	}

	@Override
	protected String getPluginId() {
		return CorePlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return getAttributeModel();
	}

	@Override
	protected void populateControls() {
		populateAttributes();
		populatePolicies();
	}

	private void populateAttributes() {
		final CustomerProfileAttributeService customerProfileAttributeService =
				BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_PROFILE_ATTRIBUTE_SERVICE, CustomerProfileAttributeService.class);

		final AttributeService attributeService = BeanLocator.getSingletonBean(ContextIdNames.ATTRIBUTE_SERVICE, AttributeService.class);
		attributes = attributeService.getCustomerProfileAttributes().stream()
				// don't show attributes that have predefined policies
				.filter(attribute -> !customerProfileAttributeService.getPredefinedProfileAttributePolicies().containsKey(attribute.getKey()))
				.sorted(Comparator.comparing(Attribute::getKey))
				.collect(Collectors.toList());
		attributes.forEach(attribute -> attributeCombo.add(attribute.getDisplayName(CorePlugin.getDefault().getDefaultLocale())));

		final String attributeKey = attributeModel.getAttributeKey();
		if (attributeKey != null) {
			attributes.stream()
					.filter(attribute -> attribute.getKey().equals(attributeKey))
					.findFirst()
					.ifPresent(attribute -> attributeCombo.select(attributes.indexOf(attribute) + 1));
		}
	}

	private void populatePolicies() {
		final AttributePolicyService attributePolicyService = BeanLocator.getSingletonBean(ContextIdNames
				.ATTRIBUTE_POLICY_SERVICE, AttributePolicyService.class);
		policies = attributePolicyService.findAll().stream()
				.map(AttributePolicy::getPolicyKey)
				.distinct()
				.collect(Collectors.toList());
		policies.forEach(policy -> policyCombo.add(policy.getName()));

		final PolicyKey policyKey = attributeModel.getPolicyKey();
		if (policyKey != null) {
			policyCombo.select(policies.indexOf(policyKey) + 1);
		}
	}

	public StoreCustomerAttributeModel getAttributeModel() {
		return attributeModel;
	}
}