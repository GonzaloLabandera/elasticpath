/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.cmclient.catalog.editors.catalog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.progress.IProgressService;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.catalog.dialogs.cartitemmodifier.AddEditCartItemModifierGroupDialog;
import com.elasticpath.cmclient.catalog.editors.catalog.tablelabelprovider.CatalogCartItemModifierGroupsTableLabelProviderDecorator;
import com.elasticpath.cmclient.catalog.editors.catalog.tablelabelprovider.ChangeSetTableLabelProviderDecorator;
import com.elasticpath.cmclient.core.tablelableprovider.TableLabelProviderAdapter;
import com.elasticpath.cmclient.catalog.editors.model.CatalogModel;
import com.elasticpath.cmclient.core.EpUiException;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPage;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.cmclient.core.service.CatalogEventService;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.policy.StatePolicy;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareEpTableSection;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.service.modifier.ModifierService;
import com.elasticpath.service.changeset.ChangeSetMemberAction;

/**
 * Implements a section of the <code>CatalogCategoryPage</code> providing
 * {@link com.elasticpath.domain.modifier.ModifierGroup}s within a catalog.
 */
@SuppressWarnings({"PMD.GodClass"})
public class CatalogCartItemModifierGroupsSection extends AbstractPolicyAwareEpTableSection<ModifierGroup> {


	private static final String CATALOG_CART_ITEM_TABLE = "Catalog Cart Item"; //$NON-NLS-1$
	private final ModifierService modifierService = BeanLocator.getSingletonBean(ContextIdNames.MODIFIER_SERVICE, ModifierService.class);
	private final ChangeSetHelper changeSetHelper = BeanLocator.getSingletonBean(ChangeSetHelper.BEAN_ID, ChangeSetHelper.class);

	private final ChangeSetColumnDecorator changeSetColumnDecorator = new ChangeSetColumnDecorator();
	private List<ModifierGroup> catalogCartItemModifierGroups;

	/**
	 * Default constructor.
	 *
	 * @param formPage the form page
	 * @param editor   the editor
	 */
	public CatalogCartItemModifierGroupsSection(final FormPage formPage, final AbstractCmClientFormEditor editor) {
		super(formPage, editor);
	}

	/**
	 * Constructor used when you already have a list o CatalogCartItemModifierGroupsSection to populate the table.
	 *
	 * @param formPage                      the form page
	 * @param editor                        the editor
	 * @param catalogCartItemModifierGroups the list of groups
	 */
	public CatalogCartItemModifierGroupsSection(final FormPage formPage, final AbstractCmClientFormEditor editor,
												final List<ModifierGroup> catalogCartItemModifierGroups) {
		super(formPage, editor);
		this.catalogCartItemModifierGroups = catalogCartItemModifierGroups;
		setTableItems(getModel().getCartItemModifierGroupTableItems());
	}

	@Override
	protected String getSectionType() {
		return CatalogMessages.get().CatalogCartItemModifierGroupsSection_ButtonText;
	}

	@Override
	protected void initializeTable(final IEpTableViewer table) {
		table.getSwtTableViewer().setComparator(new Comparator());
		addColumns(table);
		getViewer().setContentProvider(new TableContentProvider());
		addLabelProvider();

		/**
		 * TODO: Filter this by catalog?
		 */
		final List<ModifierGroup> readOnlyModifierGroup =
		modifierService.getFilteredModifierGroups(Catalog.class.getSimpleName(), getModel().getCatalog().getGuid());

		catalogCartItemModifierGroups = new ArrayList<>(readOnlyModifierGroup.size());
		catalogCartItemModifierGroups.addAll(readOnlyModifierGroup);
		refreshViewerInput();
	}

	private void addColumns(final IEpTableViewer table) {
		if (changeSetColumnDecorator.isDecoratable()) {
			changeSetColumnDecorator.addLockColumn(table);
			changeSetColumnDecorator.addActionColumn(table);
		}

		final String[] columnNames = {CatalogMessages.get().CatalogCartItemModifierGroupsPage_TableCodeColumn,
				CatalogMessages.get().CatalogCartItemModifierGroupsPage_TableDisplayNameColumn};
		final int[] columnWidths = {150, 260};

		for (int i = 0; i < columnNames.length; ++i) {
			table.addTableColumn(columnNames[i], columnWidths[i]);
		}
	}

	private void addLabelProvider() {
		if (changeSetColumnDecorator.isDecoratable()) {
			getViewer().setLabelProvider(
					new CatalogCartItemModifierGroupsTableLabelProviderDecorator(
							new ChangeSetTableLabelProviderDecorator<>(
									new TableLabelProviderAdapter(), getModel().getCartItemModifierGroupTableItems()),
							((AbstractCmClientEditorPage) getPage()).getSelectedLocale()));
		} else {
			getViewer().setLabelProvider(new CatalogCartItemModifierGroupsTableLabelProviderDecorator(new
					TableLabelProviderAdapter(), ((AbstractCmClientEditorPage) getPage()).getSelectedLocale()));
		}
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// Not used.
	}

	@Override
	protected EpState getEditorTableState() {
		return getStatePolicy().determineState(getEpTableSectionControlPane());
	}

	@Override
	public void commit(final boolean onSave) {
		if (onSave) {
			final Set<ModifierGroup> additionModifierGroup = getAddedItems();
			for (final ModifierGroup group : additionModifierGroup) {
				ModifierGroup createdGroup = modifierService.add(group);
				modifierService.addGroupFilter(Catalog.class.getSimpleName(), getModel().getCatalog().getGuid(),
						createdGroup.getCode());
				changeSetHelper.addObjectToChangeSet(group, ChangeSetMemberAction.ADD);
				CatalogEventService.getInstance().notifyGroupChanged(
						new ItemChangeEvent<>(this, group, ItemChangeEvent.EventType.ADD));
			}
			additionModifierGroup.clear();

			final Set<ModifierGroup> modifiedModifierGroup = getModifiedItems();
			for (final ModifierGroup group : modifiedModifierGroup) {
				modifierService.update(group);
				changeSetHelper.addObjectToChangeSet(group, ChangeSetMemberAction.EDIT);
				CatalogEventService.getInstance().notifyGroupChanged(
						new ItemChangeEvent<>(this, group, ItemChangeEvent.EventType.CHANGE));
			}
			modifiedModifierGroup.clear();

			final Set<ModifierGroup> removedModifierGroup = getRemovedItems();
			for (final ModifierGroup group : removedModifierGroup) {
				if (group.isPersisted()) {
					changeSetHelper.addObjectToChangeSet(group, ChangeSetMemberAction.DELETE);
					modifierService.remove(group);
					CatalogEventService.getInstance().notifyGroupChanged(
							new ItemChangeEvent<>(this, group, ItemChangeEvent.EventType.REMOVE));
				}
			}
			removedModifierGroup.clear();


			refreshViewerInput();
			super.commit(onSave);
		}
	}

	@Override
	public CatalogModel getModel() {
		return (CatalogModel) super.getModel();
	}

	@Override
	protected String getRemoveDialogTitle() {
		return CatalogMessages.get().CatalogCartItemModifierGroupsSection_RemoveDialog_title;
	}

	@Override
	protected String getRemoveDialogDescription(final ModifierGroup item) {
		return
			NLS.bind(CatalogMessages.get().CatalogGroupsSection_RemoveDialog_description,
			getItemName(item));
	}

	@Override
	protected ModifierGroup addItemAction() {
		final CatalogCartItemModifierGroupsPage page = (CatalogCartItemModifierGroupsPage) getFormPage();
		final AddEditCartItemModifierGroupDialog dialog = new AddEditCartItemModifierGroupDialog(page.getSelectedLocale(), null, getModel());
		if (dialog.open() == Window.OK) {
			return dialog.getCartItemModifierGroup();
		}
		return null;
	}

	@Override
	protected boolean editItemAction(final ModifierGroup object) {
		final CatalogCartItemModifierGroupsPage page = (CatalogCartItemModifierGroupsPage) getFormPage();
		final AddEditCartItemModifierGroupDialog dialog = new AddEditCartItemModifierGroupDialog(page.getSelectedLocale(), object, getModel());
		return dialog.open() == Window.OK;
	}

	@Override
	protected boolean removeItemAction(final ModifierGroup object) {
		final ParameterPasser passer = new ParameterPasser();
		passer.canRemove = false;
		final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			// we can use the passer variable here because this is a blocking job
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (modifierService.isInUse(object.getUidPk())) {
						passer.canRemove = false;
					} else {
						passer.canRemove = true;
						updateChangeSetListsOnRemove(object);
					}
				}

				private void updateChangeSetListsOnRemove(final ModifierGroup object) {
					Set<ModifierGroup> addedItems = getAddedItems();
					if (isNewObjectAndInList(object, addedItems)) {
						addedItems.remove(object);
						catalogCartItemModifierGroups.remove(object);
					} else {
						getModel().getCartItemModifierGroupTableItems().addRemovedItem(object);
					}
				}

				private boolean isNewObjectAndInList(final ModifierGroup object, final Set<ModifierGroup> list) {
					return list.contains(object) && !object.isPersisted();
				}
			});
		} catch (final InvocationTargetException | InterruptedException e) {
			throw new EpUiException("Error removing: " + e.getMessage(), e); //$NON-NLS-1$
		}

		if (!passer.canRemove) {
			MessageDialog.openError(getPage().getSite().getShell(), CatalogMessages.get().CatalogCartItemModifierGroupsSection_RemoveDialog_title,

					NLS.bind(CatalogMessages.get().CatalogCartItemModifierGroupsSection_ErrorDialog_InUse_desc,
					getItemName(object)));
		}
		return passer.canRemove;
	}

	@Override
	protected String getItemName(final ModifierGroup group) {
		final Locale selectedLocale = ((AbstractCmClientEditorPage) getPage()).getSelectedLocale();
		ModifierGroupLdf cartItemModifierGroupLdfByLocale = group.getModifierGroupLdfByLocale(selectedLocale.toString());
		return String.format("%1$s - %2$s", group.getCode(), cartItemModifierGroupLdfByLocale.getDisplayName()); //$NON-NLS-1$
	}

	@Override
	protected void addAddedItem(final ModifierGroup item) {
		super.addAddedItem(item);
		catalogCartItemModifierGroups.add(item);
		markDirty();
	}

	@Override
	protected void addModifiedItem(final ModifierGroup item) {
		super.addModifiedItem(item);
		markDirty();
	}

	@Override
	protected void addRemovedItem(final ModifierGroup item) {
		super.addRemovedItem(item);
		if (!changeSetHelper.isChangeSetsEnabled()) {
			catalogCartItemModifierGroups.remove(item);
		}
		markDirty();
	}

	/**
	 * Content provider for the table.
	 */
	private class TableContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(final Object inputElement) {
			return catalogCartItemModifierGroups.toArray();
		}

		@Override
		public void dispose() {
			// not used
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// not used
		}
	}

	/**
	 * Parameter holder class for passing a parameter between threads.
	 */
	private final class ParameterPasser {
		private boolean canRemove;
	}

	/**
	 * ModifierGroup specific sorter to sort on the name.
	 */
	private class Comparator extends ViewerComparator {

		@Override
		public int compare(final Viewer viewer, final Object object1, final Object object2) {
			final ModifierGroup cartItemModifierGroup1 = (ModifierGroup) object1;
			final ModifierGroup cartItemModifierGroup2 = (ModifierGroup) object2;
			return cartItemModifierGroup1.getCode().compareToIgnoreCase(cartItemModifierGroup2.getCode());
		}
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final ModifierGroup cartItemModifierGroup =
				(ModifierGroup) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
		applyStatePolicyForCartItemModifierGroup(getStatePolicy(), cartItemModifierGroup);
	}

	private void applyStatePolicyForCartItemModifierGroup(final StatePolicy statePolicy, final ModifierGroup cartItemModifierGroup) {
		setStatePolicy(statePolicy);
		updatePoliciesWithDependentObject(cartItemModifierGroup);
		applyStatePolicy();
	}

	@Override
	public String getTargetIdentifier() {
		return "catalogCartItemModifierGroupsSection"; //$NON-NLS-1$
	}

	@Override
	public void refresh() {
		// do nothing
	}

	@Override
	protected String getTableName() {
		return CATALOG_CART_ITEM_TABLE;
	}
}
