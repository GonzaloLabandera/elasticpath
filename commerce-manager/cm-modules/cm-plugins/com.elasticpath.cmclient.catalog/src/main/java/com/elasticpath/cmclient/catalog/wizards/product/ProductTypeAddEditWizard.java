/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.catalog.wizards.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.catalog.editors.model.CatalogModel;
import com.elasticpath.cmclient.catalog.editors.model.CatalogModelImpl;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ObjectGuidReceiver;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.cmclient.core.ui.IChangeSetEditorAware;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.policy.StatePolicy;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareWizard;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeGroup;
import com.elasticpath.domain.attribute.AttributeGroupAttribute;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.service.catalog.ProductTypeService;
import com.elasticpath.service.changeset.ChangeSetMemberAction;

/**
 * The wizard to handle UI of Add/Edit Product type.
 */
public class ProductTypeAddEditWizard extends AbstractPolicyAwareWizard<ProductType> implements ObjectGuidReceiver,
		IChangeSetEditorAware {

	private boolean editMode;

	private boolean isFromChangeSetEditor;

	private ProductType productType;

	private ProductTypeAddEditWizardStep1 step1;

	private ProductTypeAddEditWizardStep2 step2;

	private CatalogModel catalogModel;

	private List<ProductType> productTypes;

	private static Image wizardImage;

	private ProductTypeService productTypeService;

	private PolicyActionContainer finishContainer;

	private final ChangeSetHelper changeSetHelper = BeanLocator.getSingletonBean(ChangeSetHelper.BEAN_ID, ChangeSetHelper.class);

	/**
	 * @param productType the product type passed in.
	 * @param catalogModel the catalog model
	 * @param productTypes list of already existing product types
	 */
	public ProductTypeAddEditWizard(final ProductType productType, final CatalogModel catalogModel, final
	List<ProductType> productTypes) {
		super(determinePageTitle(productType), determinePageTitle(productType), wizardImage);
		this.catalogModel = catalogModel;
		editMode = productType != null;
		this.productTypes = productTypes;
		if (editMode) {
			this.productType = productType;
		} else {
			this.productType = BeanLocator.getPrototypeBean(ContextIdNames.PRODUCT_TYPE, ProductType.class);
			this.productType.setCatalog(this.catalogModel.getCatalog());
		}
	}

	private static String determinePageTitle(final ProductType productType) {
		if (productType != null) {
			return CatalogMessages.get().ProductTypeAddEditWizard_EditWindowTitle;
		}
		return CatalogMessages.get().ProductTypeAddEditWizard_AddWindowTitle;
	}

	/**
	 * Constructs the Product Type Wizard.<br>
	 * Used in change set editor to create the edit wizard.
	 */
	public ProductTypeAddEditWizard() {
		super(CatalogMessages.get().ProductTypeAddEditWizard_EditWindowTitle,
				CatalogMessages.get().ProductTypeAddEditWizard_EditWindowTitle, wizardImage);
	}

	@Override
	public void addPages() {
		final PolicyActionContainer policyActionContainer = addPolicyActionContainer(getTargetIdentifier());

		step1 = new ProductTypeAddEditWizardStep1("FirstPage", getWindowTitle(), productType, editMode, productTypes); //$NON-NLS-1$
		step2 = new ProductTypeAddEditWizardStep2("SecondPage", getWindowTitle(), productType, editMode); //$NON-NLS-1$
		addPage(step1, policyActionContainer);
		addPage(step2, policyActionContainer);
		finishContainer = addPolicyActionContainer("Finish"); //$NON-NLS-1$
		setDependentObjectForPolicies(policyActionContainer);
	}

	private void setDependentObjectForPolicies(final PolicyActionContainer policyActionContainer) {
		Object dependentObject = null;
		if (editMode) {
			dependentObject = productType;
		}
		finishContainer.setPolicyDependent(dependentObject);
		policyActionContainer.setPolicyDependent(dependentObject);
	}

	@Override
	public boolean canFinish() {
		final IWizardPage current = getContainer().getCurrentPage();
		if (current.getName().equals("SecondPage") && isEditable()) { //$NON-NLS-1$
			return true;
		}
		return current.isPageComplete() && current.getNextPage() == null && isEditable();
	}

	/**
	 * @return the product type of the wizard.
	 */
	public ProductType getProductType() {
		return productType;
	}

	@Override
	public String getWindowTitle() {
		if (editMode) {
			return CatalogMessages.get().ProductTypeAddEditWizard_EditWindowTitle;
		}
		return CatalogMessages.get().ProductTypeAddEditWizard_AddWindowTitle;
	}

	@Override
	public boolean performFinish() {
		setProductTypeAttributes((List<Attribute>) step1.getAssignedAttributes());
		setProductTypeSkuOptions(step2.getAssignedSkuOptions());
		setProductTypeSkuAttributes((List<Attribute>) step2.getAssignedSkuAttributes());
		setProductTypeCartItemModifierGroup((List<ModifierGroup>) step1.getAssignedCartItemModifierGroups());

		if (editMode) {
			catalogModel.getProductTypeTableItems().addModifiedItem(getProductType());
		} else {
			catalogModel.getProductTypeTableItems().addAddedItem(getProductType());
		}

		if (isFromChangeSetEditor) {
			performSave();
		}
		return true;
	}

	private void performSave() {
		productTypeService.update(productType);
		changeSetHelper.addObjectToChangeSet(productType, ChangeSetMemberAction.EDIT);
	}

	private void setProductTypeSkuAttributes(final List<Attribute> attributes) {
		final AttributeGroup skuAttributeGroup = BeanLocator.getPrototypeBean(ContextIdNames.ATTRIBUTE_GROUP, AttributeGroup.class);
		skuAttributeGroup.setAttributeGroupAttributes(getGroupAttributeFromList(attributes, ContextIdNames.PRODUCT_TYPE_SKU_ATTRIBUTE));
		getProductType().setSkuAttributeGroup(skuAttributeGroup);
	}

	private Set<AttributeGroupAttribute> getGroupAttributeFromList(final List<Attribute> attributes, final String beanName) {
		int order = 0;
		final Set<AttributeGroupAttribute> attributeGroupAttributes = new HashSet<>();

		for (final Attribute attribute : attributes) {
			final AttributeGroupAttribute groupAttribute = BeanLocator.getPrototypeBean(beanName, AttributeGroupAttribute.class);
			groupAttribute.setAttribute(attribute);
			groupAttribute.setOrdering(order++);
			attributeGroupAttributes.add(groupAttribute);
		}
		return attributeGroupAttributes;
	}

	private void setProductTypeSkuOptions(final List<SkuOption> skuOptions) {
		final Set<SkuOption> skuOptionSet = new HashSet<>(skuOptions);
		getProductType().setSkuOptions(skuOptionSet);

	}

	private void setProductTypeAttributes(final List<Attribute> assignedAttributes) {
		getProductType().setProductAttributeGroupAttributes(
				getGroupAttributeFromList(assignedAttributes, ContextIdNames.PRODUCT_TYPE_PRODUCT_ATTRIBUTE));
	}

private void setProductTypeCartItemModifierGroup(final List<ModifierGroup> assignedCartItemModifierGroup) {
		final Set<ModifierGroup> groupModifierGroup = new HashSet<>();

		for (ModifierGroup cartItemModifierGroup:assignedCartItemModifierGroup) {
			groupModifierGroup.add(cartItemModifierGroup);
		}

		getProductType().setModifierGroups(groupModifierGroup);
	}

	/**
	 * Sets the object guid and determines whether to enable editMode.
	 *
	 * @param objectGuid string representing an object Guid
	 */
	@Override
	public void setObjectGuid(final String objectGuid) {
		if (objectGuid == null) {
			productType = BeanLocator.getPrototypeBean(ContextIdNames.PRODUCT_TYPE, ProductType.class);
			productType.setCatalog(catalogModel.getCatalog());
			editMode = false;
		} else {
			productTypeService = BeanLocator.getSingletonBean(ContextIdNames.PRODUCT_TYPE_SERVICE, ProductTypeService.class);
			productType = productTypeService.findByGuid(objectGuid);
			productTypes = new ArrayList<>();
			editMode = true;
		}
	}

	/**
	 * Enables editMode boolean, marks the isFromChangeSetEditor flag to true, and establishes the catalogModel if null.
	 */
	@Override
	public void setOpenedFromChangeSetEditor() {
		isFromChangeSetEditor = true;
		editMode = true;
		if (catalogModel == null) {
			catalogModel = new CatalogModelImpl(productType.getCatalog());
		}
	}

	@Override
	protected ProductType getModel() {
		return getProductType();
	}

	@Override
	public void applyStatePolicy(final StatePolicy statePolicy) {
		super.applyStatePolicy(statePolicy);

		if (statePolicy != null) {
			final EpState determineState = statePolicy.determineState(finishContainer);
			setState(determineState);
		}
	}

	@Override
	public String getTargetIdentifier() {
		return "productTypeWizard"; //$NON-NLS-1$
	}

}
