/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.store.promotions.editors;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.EpUiException;
import com.elasticpath.cmclient.core.editors.CancelSaveException;
import com.elasticpath.cmclient.core.editors.GuidEditorInput;
import com.elasticpath.cmclient.policy.StatePolicy;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareFormEditor;
import com.elasticpath.cmclient.store.StorePlugin;
import com.elasticpath.cmclient.store.promotions.PromotionsMessages;
import com.elasticpath.cmclient.store.promotions.event.PromotionsChangeEvent;
import com.elasticpath.cmclient.store.promotions.event.PromotionsEventService;
import com.elasticpath.cmclient.store.targetedselling.TargetedSellingMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleElement;
import com.elasticpath.domain.rules.RuleException;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.sellingcontext.SellingContextService;
import com.elasticpath.tags.domain.ConditionalExpression;
import com.elasticpath.tags.domain.TagDictionary;

/**
 * Implements a multi-page editor for displaying/editing catalog promotions.
 */
public class CatalogPromotionsEditor extends AbstractPolicyAwareFormEditor {

	/**
	 * ID of the editor. It is the same as the class name.
	 */
	public static final String ID_EDITOR = "com.elasticpath.cmclient.store.promotions.editors.PromotionsEditor.Catalog"; //$NON-NLS-1$

	private static final int TOTAL_WORK_UNITS = 3;

	private static final int RULES_PAGE_INDEX = 1;

	private final RuleService ruleService =
			BeanLocator.getService(ContextIdNames.RULE_SERVICE);
	private final SellingContextService sellingContextService =
			BeanLocator.getService(ContextIdNames.SELLING_CONTEXT_SERVICE);

	private SellingContext sellingContextForDelete;

	private Rule rule;

	@Override
	public void initEditor(final IEditorSite site, final IEditorInput input) {
		// empty
	}

	private Rule getRule(final String guid) {
		Rule rule = ruleService.findByRuleCode(guid);
		SellingContext sellingContext = rule.getSellingContext();
		if (Objects.isNull(sellingContext)) {
			sellingContext = BeanLocator.getService(ContextIdNames.SELLING_CONTEXT);
			sellingContext.setName(rule.getName());
			rule.setSellingContext(sellingContext);
		}
		return rule;
	}

	@Override
	protected void addPages() {
		final PolicyActionContainer pageContainer = addPolicyActionContainer("catalogPromotionEditor"); //$NON-NLS-1$

		try {
			addPage(new PromotionDetailsPage(this, true), pageContainer);
			addPage(new PromotionShopperPage(this, false), pageContainer);
			addPage(new PromotionRulesPage(this, true), pageContainer);
			addExtensionPages(getClass().getSimpleName(), StorePlugin.PLUGIN_ID);
		} catch (final PartInitException e) {
			throw new EpUiException(e);
		}
	}

	@Override
	protected void saveModel(final IProgressMonitor monitor) {
		monitor.beginTask(PromotionsMessages.get().PromotionEditor_Save_StatusBarMsg, TOTAL_WORK_UNITS);
		try {
			if (!checkProductCategorySkuSelected()) {
				monitor.setCanceled(true);
				return;
			}
			monitor.worked(1);

			if (!checkSellingContext()) {
				throw new CancelSaveException("Failed to check Shopper."); //$NON-NLS-1$
			}
			monitor.worked(1);

			Rule updatedRule = ruleService.update(rule);
			monitor.worked(1);

			// update selling context
			if (!Objects.isNull(this.sellingContextForDelete)) {
				this.sellingContextService.remove(sellingContextForDelete);
			}
			monitor.worked(1);

			PromotionsEventService.getInstance().firePromotionsChangeEvent(new PromotionsChangeEvent(this, updatedRule));
			monitor.worked(1);

			this.reloadModel();
			this.reinitStatePolicy();
			this.refreshEditorPages();
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns true if all rule elements with Product/Category/SKU values have valid values (i.e. valid means the user has selected a specific
	 * Product/Category/SKU; false otherwise.
	 *
	 * @return true if all rule elements with Product/Category/SKU values have valid values (i.e. valid means the user has selected a specific
	 * Product/Category/SKU; false otherwise.
	 */
	private boolean checkProductCategorySkuSelected() {
		// Iterate through the Rule's rule-elements
		for (RuleElement currRuleElement : getModel().getRuleElements()) {
			// Check the parameters of the rule-elements
			for (RuleParameter currElementParam : currRuleElement.getParameters()) {
				if (!isRuleParameterComplete(currElementParam)) {
					this.setActivePage(RULES_PAGE_INDEX);
					MessageDialog.openWarning(this.getSite().getShell(), PromotionsMessages.get().PromoRulesDefintion_Error_Title,
							PromotionsMessages.get().PromoRulesDefintion_Error_Select_Links);
					return false;
				}
			}

			// Check the parameters of rule-exceptions
			for (RuleException currRuleException : currRuleElement.getExceptions()) {
				for (RuleParameter currExceptionParam : currRuleException.getParameters()) {
					if (!isRuleParameterComplete(currExceptionParam)) {
						this.setActivePage(RULES_PAGE_INDEX);
						MessageDialog.openWarning(this.getSite().getShell(), PromotionsMessages.get().PromoRulesDefintion_Error_Title,
								PromotionsMessages.get().PromoRulesDefintion_Error_Select_Links);
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Returns true if all Product/Category/SKU values have been selected by the user; false otherwise.
	 *
	 * @param parameter the RuleParameter to check
	 * @return true if all Product/Category/SKU values have been selected by the user; false otherwise.
	 */
	private boolean isRuleParameterComplete(final RuleParameter parameter) {
		final boolean productIsNull = parameter.getKey().equals(RuleParameter.PRODUCT_CODE_KEY) && (parameter.getValue() == null);
		final boolean categoryIsNull = parameter.getKey().equals(RuleParameter.CATEGORY_CODE_KEY) && (parameter.getValue() == null);
		final boolean skuIsNull = parameter.getKey().equals(RuleParameter.SKU_CODE_KEY) && (parameter.getValue() == null);

		return !productIsNull && !categoryIsNull && !skuIsNull;
	}

	@Override
	public Rule getModel() {
		return rule;
	}

	@Override
	public Collection<Locale> getSupportedLocales() {
		return Collections.emptyList();
	}

	@Override
	public Locale getDefaultLocale() {
		return null;
	}

	@Override
	public void reloadModel() {
		this.setInput(getEditorInput());
	}

	@Override
	protected void setInput(final IEditorInput input) {
		super.setInput(input);
		GuidEditorInput guidEditorInput = (GuidEditorInput) input;
		rule = getRule(guidEditorInput.getGuid());
		setPartName(rule.getName());
	}

	@Override
	protected String getSaveOnCloseMessage() {
		return
			NLS.bind(PromotionsMessages.get().PromotionEditor_OnSavePrompt,
			getEditorName());
	}

	@Override
	public void applyStatePolicy(final StatePolicy statePolicy) {
		statePolicy.init(getModel());
		super.applyStatePolicy(statePolicy);
	}

	@Override
	public String getTargetIdentifier() {
		return "catalogPromotionEditor"; //$NON-NLS-1$
	}

	@Override
	protected String getEditorName() {
		String name = ""; //$NON-NLS-1$
		if (!Objects.isNull(rule)) {
			name = rule.getName();
		}
		return name;
	}

	@Override
	protected String getEditorToolTip() {
		return getEditorName();
	}

	private boolean checkSellingContext() {
		Rule model = this.getModel();
		SellingContext sellingContext = model.getSellingContext();
		if (Objects.isNull(sellingContext)) {
			return true;
		}

		ConditionalExpression shopperCE = sellingContext.getCondition(TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID);
		if (Objects.isNull(shopperCE)) {
			model.setSellingContext(null);
			this.sellingContextForDelete = sellingContext;
		}

		if (!isConditionalExpressionValid(shopperCE)) {
			showErrorDialog(TargetedSellingMessages.get().TotalLengthOfConditionsReached);
			return false;
		}

		return true;
	}

	private boolean isConditionalExpressionValid(final ConditionalExpression conditionalExpression) {
		if (!Objects.isNull(conditionalExpression)) {
			return isConditionLengthValid(conditionalExpression.getConditionString());
		}
		return true;
	}

	private boolean isConditionLengthValid(final String conditionString) {
		return (Objects.isNull(conditionString) || conditionString.length() <= ConditionalExpression.CONDITION_STRING_MAX_LENGTH);
	}

	private void showErrorDialog(final String message) {
		MessageBox messageBox = new MessageBox(this.getSite().getShell(), SWT.OK);
		messageBox.setMessage(message);
		messageBox.open();
	}

}
