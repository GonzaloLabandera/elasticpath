/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.store.promotions.wizard;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;

import com.elasticpath.cmclient.conditionbuilder.wizard.pages.AbstractSellingContextConditionWizardPage;
import com.elasticpath.cmclient.conditionbuilder.wizard.pages.SellingContextConditionShopperWizardPage;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareWizard;
import com.elasticpath.cmclient.store.promotions.PromotionsImageRegistry;
import com.elasticpath.cmclient.store.promotions.PromotionsMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.DuplicateNameException;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.service.changeset.ChangeSetMemberAction;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.RuleSetService;
import com.elasticpath.tags.domain.ConditionalExpression;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.service.InvalidConditionTreeException;

/**
 * The wizard for creating catalog promotions.
 */
public class NewCatalogPromotionWizard extends AbstractPolicyAwareWizard<Rule> {

	private static final String DETAILS_PAGE_NAME = "NewPromotionWizardStep1Page"; //$NON-NLS-1$

	private static final String RULES_PAGE_NAME = "NewPromotionWizardRulesPage"; //$NON-NLS-1$
	private static final String NEW_CATALOG_PROMOTION_WIZARD = "newCatalogPromotionWizard";

	private Rule model;

	private final RuleService ruleService;

	private final ChangeSetHelper changeSetHelper;

	private AbstractSellingContextConditionWizardPage<SellingContext> shopperPage;

	private static final String SHOPPER_CONDITION_PAGE = "WHO_CONDITION_PAGE"; //$NON-NLS-1$

	/**
	 * Default constructor.
	 */
	public NewCatalogPromotionWizard() {
		super(PromotionsMessages.get().NewCatalogPromotionWizard_Title, null, PromotionsImageRegistry
				.getImage(PromotionsImageRegistry.PROMOTION_CATALOG_CREATE));

		final RuleSetService ruleSetService = BeanLocator.getService(
				ContextIdNames.RULE_SET_SERVICE);

		SellingContext sellingContext = BeanLocator.getService(ContextIdNames.SELLING_CONTEXT);

		model = BeanLocator.getService(ContextIdNames.PROMOTION_RULE);
		model.setRuleSet(ruleSetService.findByScenarioId(RuleScenarios.CATALOG_BROWSE_SCENARIO));
		model.setEnabled(true);
		model.setSellingContext(sellingContext);

		ruleService = BeanLocator.getService(ContextIdNames.RULE_SERVICE);
		changeSetHelper = BeanLocator.getService(ChangeSetHelper.BEAN_ID);
	}

	@Override
	public boolean performFinish() {

		if (ruleNameExists(model.getName())) {
			showPromotionalExistsErrorPage();
			return false;
		}

		String errorMessage = StringUtils.EMPTY;

		ConditionalExpression shopperCE = null;
		try {
			shopperCE = this.shopperPage.getModelAdapter().getModel();

		} catch (InvalidConditionTreeException icte) {
			errorMessage = icte.getLocalizedMessage();
		}

		setSellingContext(shopperCE);

		if (isInvalidConditions(errorMessage, shopperCE)) {
			return false;
		}

		try {
			getModel().validate();
		} catch (Exception exception) {
			showPromotionValidationErrorPage(exception);
			return false;
		}

		persistModel();

		return true;
	}

	private boolean isInvalidConditions(final String errorMessage, final ConditionalExpression shopperCE) {
		if (StringUtils.isNotEmpty(errorMessage)) {
			showErrorDialog(errorMessage);
			return true;
		}

		if (!isConditionalExpressionValid(shopperCE)) {
			showErrorDialog(com.elasticpath.cmclient.store.targetedselling.TargetedSellingMessages.get().TotalLengthOfConditionsReached);
			return true;
		}
		return false;
	}

	private void setSellingContext(final ConditionalExpression shopperCE) {
		if (Objects.isNull(shopperCE)) {
			this.getModel().setSellingContext(null);
		} else {
			this.getModel().getSellingContext().setName(model.getName());
			this.getModel().getSellingContext().setCondition(TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID, shopperCE);
		}
	}

	private void showPromotionValidationErrorPage(final Exception exception) {
		NewPromotionWizardRulesPage newPromoRulesPage = ((NewPromotionWizardRulesPage) this.getPage(RULES_PAGE_NAME));
		newPromoRulesPage.setErrorMessage(PromotionsMessages.get().CreatePromotionWizardRulesPage_Validation_Error
				+ exception.getLocalizedMessage());
		this.getContainer().showPage(newPromoRulesPage);
	}

	private void showPromotionalExistsErrorPage() {
		NewPromotionWizardDetailsPage newPromoDetailsPage = ((NewPromotionWizardDetailsPage) this.getPage(DETAILS_PAGE_NAME));
		newPromoDetailsPage.setErrorMessage(PromotionsMessages.get().CreatePromotionWizardDetailsPage_Name_Uniqueness_Error);
		this.getContainer().showPage(newPromoDetailsPage);
	}

	private void persistModel() {
		model = ruleService.add(model);
		changeSetHelper.addObjectToChangeSet(model, ChangeSetMemberAction.ADD);
	}

	private boolean isConditionalExpressionValid(final ConditionalExpression conditionalExpression) {
		if (conditionalExpression != null) {
			return isConditionLengthValid(conditionalExpression.getConditionString());
		}
		return true;
	}

	private boolean isConditionLengthValid(final String conditionString) {
		return (conditionString == null || conditionString.length() <= ConditionalExpression.CONDITION_STRING_MAX_LENGTH);
	}

	private void showErrorDialog(final String message) {
		MessageDialog.openError(this.getShell(), PromotionsMessages.get().CreatePromotionsWizard_Error_Dialog_Title, message);
	}

	private boolean ruleNameExists(final String ruleName) {
		final RuleService ruleService = BeanLocator.getService(ContextIdNames.RULE_SERVICE);
		try {
			final Rule rule = ruleService.findByName(ruleName);
			if (rule != null) {
				return true;
			}
		} catch (DuplicateNameException e) {
			return true;
		}
		return false;
	}

	@Override
	public String getTargetIdentifier() {
		return NEW_CATALOG_PROMOTION_WIZARD;
	}

	@Override
	public Rule getModel() {
		return model;
	}

	@Override
	public void addPages() {
		PolicyActionContainer policyActionContainer = addPolicyActionContainer(getTargetIdentifier());
		List<ConditionalExpression> shopperNameConditions = null;

		addPage(new NewPromotionWizardDetailsPage(DETAILS_PAGE_NAME,
						PromotionsMessages.get().CreateCatalogPromotionWizardDetailsPage_Catalog_Title,
						true),
				policyActionContainer);

		shopperPage = createShopperPage(shopperNameConditions);
		addPage(shopperPage, policyActionContainer);

		addPage(new NewPromotionWizardRulesPage(RULES_PAGE_NAME,
						PromotionsMessages.get().CreateCatalogPromotionWizardRulesPage_Title,
						true),
				policyActionContainer);

	}

	private SellingContextConditionShopperWizardPage<SellingContext> createShopperPage(final List<ConditionalExpression> shopperNameConditions) {
		return new SellingContextConditionShopperWizardPage<>(
				SHOPPER_CONDITION_PAGE,
				PromotionsMessages.get().CreateCatalogPromotionWizard_Page_Step2_Title,
				PromotionsMessages.get().CreatePromotionWizard_Page_Step2_Description,
				shopperNameConditions,
				getModel().getSellingContext(),
				TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID);
	}
}