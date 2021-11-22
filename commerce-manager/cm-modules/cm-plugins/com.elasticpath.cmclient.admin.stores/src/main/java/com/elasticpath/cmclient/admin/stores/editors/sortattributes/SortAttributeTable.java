/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.actions.StoreSortAttributes;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.dialog.value.dialog.ConfirmationDialog;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortLocalizedName;
import com.elasticpath.domain.search.impl.SortAttributeImpl;

/**
 * Sort attribute table.
 */
public class SortAttributeTable extends AbstractCmClientEditorPageSectionPart {

	private static final String ADD = "Add";

	private static final String EDIT = "Edit";

	private static final String REMOVE = "Remove";

	private static final int TABLE_HEIGHT = 250;

	private static final int NAME_COLUMN_WIDTH = 170;

	private static final int TYPE_COLUMN_WIDTH = 130;

	/** Column number for key. */
	static final int SORT_ATTRIBUTE_KEY_COLUMN = 0;

	/** Column number for direction. */
	static final int SORT_ATTRIBUTE_DIRECTION_COLUMN = 1;

	/** Column number for type. */
	static final int SORT_ATTRIBUTE_TYPE_COLUMN = 3;

	/** Column number for group. */
	static final int SORT_ATTIBUTE_GROUP_COLUMN = 2;

	/** Column number for display name. */
	static final int SORT_ATTIBUTE_DISPLAY_NAME_COLUMN = 4;

	private final AbstractCmClientFormEditor editor;

	private final StoreEditorModel storeEditorModel;

	private final StoreSortAttributes storeSortAttributes;

	private SortAttribute defaultSortAttribute;

	private final List<SortAttribute> persistedAttributesToBeRemoved;

	private Map<String, SortAttribute> input;

	private final Map<String, SortAttribute> modifiedSortAttributes;

	private IEpTableViewer table;

	private TableViewer tableViewer;

	private IEpLayoutComposite mainPane;

	private SortAttributeViewerComparator comparator;

	/**
	 * Constructor.
	 * @param formPage form page
	 * @param editor editor
	 * @param style style
	 * @param storeEditorModel store model
	 */
	public SortAttributeTable(final FormPage formPage, final AbstractCmClientFormEditor editor, final int style,
							  final StoreEditorModel storeEditorModel) {
		super(formPage, editor, style);
		this.editor = editor;
		this.storeEditorModel = storeEditorModel;
		this.storeSortAttributes = new StoreSortAttributes(storeEditorModel);
		this.modifiedSortAttributes = new HashMap<>();
		persistedAttributesToBeRemoved = new ArrayList<>();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);
		mainPane.setControlModificationListener(getEditor());
	}

	@Override
	protected void createControls(final Composite composite, final FormToolkit toolkit) {
		mainPane = CompositeFactory.createTableWrapLayoutComposite(composite, 2, false);
		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		IEpLayoutData tableLayoutData = mainPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false);

		table = mainPane.addTableViewer(false, EpControlFactory.EpState.EDITABLE, tableLayoutData,
				AdminStoresMessages.get().StoreSortAttributeConfiguration);

		tableViewer = table.getSwtTableViewer();

		IEpLayoutData buttonGroupLayout = mainPane.createLayoutData();
		IEpLayoutComposite buttonGroup = mainPane.addGroup(1, false, buttonGroupLayout);

		Button addButton = buttonGroup.addPushButton(ADD, EpControlFactory.EpState.READ_ONLY, buttonGroup.createLayoutData());
		Button editButton = buttonGroup.addPushButton(EDIT, EpControlFactory.EpState.READ_ONLY, buttonGroup.createLayoutData());
		Button deleteButton = buttonGroup.addPushButton(REMOVE, EpControlFactory.EpState.READ_ONLY, buttonGroup.createLayoutData());

		addButton.setImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_ADD));
		editButton.setImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT));
		deleteButton.setImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_REMOVE));
		editButton.setEnabled(false);
		addButton.setEnabled(true);
		deleteButton.setEnabled(false);

		createNameColumn();
		createDefaultSortColumn();
		createDirectionColumn();
		createGroupColumn();
		createTypeColumn();
		createDisplayNameColumn();

		table.setContentProvider(new ArrayContentProvider());

		this.tableViewer.addSelectionChangedListener(selectionChangedEvent -> {
			if ((selectionChangedEvent.getSelection() instanceof IStructuredSelection)) {
				Object element = ((IStructuredSelection) selectionChangedEvent.getSelection()).getFirstElement();
				if (element instanceof SortAttribute) {
					editButton.setEnabled(true);
					deleteButton.setEnabled(true);
				} else {
					editButton.setEnabled(false);
					deleteButton.setEnabled(false);
				}
			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				SortAddDialog dialog = new SortAddDialog(composite.getShell(), storeEditorModel, storeSortAttributes);

				if (dialog.open() == Window.OK) {
					updateData(dialog);
				}
			}
		});

		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				SortEditDialog dialog = new SortEditDialog(composite.getShell(), storeEditorModel,
						deepCopy((SortAttribute) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement()));

				if (dialog.open() == Window.OK) {
					updateData(dialog);
				}
			}
		});

		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				SortAttribute sortAttribute = (SortAttribute) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
				String confirmationMessage = NLS.bind(AdminStoresMessages.get().SortDeleteMessage, sortAttribute.getBusinessObjectId(),
						sortAttribute.isDescending() ? AdminStoresMessages.get().SortDescending : AdminStoresMessages.get().SortAscending);

				ConfirmationDialog sortRemoveDialog = new ConfirmationDialog(composite.getShell(), AdminStoresMessages.get().SortDeleteTitle,
						confirmationMessage);

				if (sortRemoveDialog.open() == Window.OK) {
					removeSortAttribute(sortAttribute);
				}
			}
		});

		comparator = new SortAttributeViewerComparator(storeEditorModel.getDefaultLocale().toString());
		tableViewer.setComparator(comparator);
		TableWrapData tableWrapData = (TableWrapData) table.getSwtTable().getLayoutData();
		tableWrapData.heightHint = TABLE_HEIGHT;
		tableWrapData.maxHeight = TABLE_HEIGHT;
	}

	private void removeSortAttribute(final SortAttribute sortAttribute) {
		if (sortAttribute.isPersisted()) {
			persistedAttributesToBeRemoved.add(sortAttribute);
		}
		String key = sortAttribute.getGuid();
		input.remove(key);
		modifiedSortAttributes.remove(key);
		if (defaultSortAttribute.getGuid().equals(key)) {
			defaultSortAttribute = null;
			if (!input.isEmpty()) {
				SortAttribute firstAttribute = input.values().iterator().next();
				defaultSortAttribute = firstAttribute;
				firstAttribute.setDefaultAttribute(true);
				modifiedSortAttributes.put(firstAttribute.getGuid(), firstAttribute);
			}
		}
		populateTable();
		markDirty();
	}

	private void updateData(final AbstractSortDialog dialog) {
		SortAttribute selectedSortAttribute = dialog.getSelectedSortAttribute();
		if (selectedSortAttribute != null) {
			String key = selectedSortAttribute.getGuid();
			if (defaultSortAttribute == null) {
				defaultSortAttribute = selectedSortAttribute;
				defaultSortAttribute.setDefaultAttribute(true);
			}
			input.put(key, selectedSortAttribute);
			modifiedSortAttributes.put(key, selectedSortAttribute);
			populateTable();
			editor.controlModified();
			markDirty();
		}
	}

	private SortAttribute deepCopy(final SortAttribute sortAttribute) {
		SortAttribute copy = new SortAttributeImpl();
		copy.setUidPk(sortAttribute.getUidPk());
		copy.setGuid(sortAttribute.getGuid());
		copy.setBusinessObjectId(sortAttribute.getBusinessObjectId());
		copy.setStoreCode(sortAttribute.getStoreCode());
		copy.setDescending(sortAttribute.isDescending());
		copy.setLocalizedNames(new HashMap<>(sortAttribute.getLocalizedNames()));
		copy.setSortAttributeGroup(sortAttribute.getSortAttributeGroup());
		copy.setSortAttributeType(sortAttribute.getSortAttributeType());
		copy.setDefaultAttribute(sortAttribute.isDefaultAttribute());
		return copy;
	}

	@Override
	protected void populateControls() {
		input = storeSortAttributes.getConfiguredSortAttributes();
		defaultSortAttribute = input.values().stream().filter(SortAttribute::isDefaultAttribute).findFirst().orElse(null);
		populateTable();
	}

	private void populateTable() {
		table.setInput(input.values());

		tableViewer.refresh();
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// no binds
	}

	private void createNameColumn() {
		IEpTableColumn nameColumn = table.addTableColumn(AdminStoresMessages.get().SortAttributeKey, NAME_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		nameColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SORT_ATTRIBUTE_KEY_COLUMN,
				nameColumn.getSwtTableColumn()));
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((SortAttribute) element).getBusinessObjectId();
			}
		});
	}

	private void createDefaultSortColumn() {
		IEpTableColumn searchableColumn = table.addTableColumn(AdminStoresMessages.get().DefaultSort, TYPE_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		searchableColumn.setEditingSupport(new DefaultAttributeEditingSupport(this));
		searchableColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(final Object element) {
				if (((SortAttribute) element).isDefaultAttribute()) {
					return CoreImageRegistry.getImage(CoreImageRegistry.RADIO_BUTTON_CHECKED);
				} else {
					return CoreImageRegistry.getImage(CoreImageRegistry.RADIO_BUTTON_UNCHECKED);
				}
			}
		});
	}

	private void createDirectionColumn() {
		IEpTableColumn directionColumn = table.addTableColumn(AdminStoresMessages.get().SortOrder, NAME_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		directionColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SORT_ATTRIBUTE_DIRECTION_COLUMN,
				directionColumn.getSwtTableColumn()));
		directionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((SortAttribute) element).isDescending() ? AdminStoresMessages.get().SortDescending : AdminStoresMessages.get().SortAscending;
			}
		});
	}

	private void createTypeColumn() {
		IEpTableColumn typeColumn = table.addTableColumn(AdminStoresMessages.get().SortAttributeType, TYPE_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		typeColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SORT_ATTRIBUTE_TYPE_COLUMN,
				typeColumn.getSwtTableColumn()));
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return capitalToProper(((SortAttribute) element).getSortAttributeType().getName());
			}
		});
	}

	private void createGroupColumn() {
		IEpTableColumn groupColumn = table.addTableColumn(AdminStoresMessages.get().SortAttributeGroup, TYPE_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		groupColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SORT_ATTIBUTE_GROUP_COLUMN,
				groupColumn.getSwtTableColumn()));
		groupColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return capitalToProper(((SortAttribute) element).getSortAttributeGroup().getName());
			}
		});
	}

	private void createDisplayNameColumn() {
		IEpTableColumn displayColumn = table.addTableColumn(AdminStoresMessages.get().SortDisplayName, TYPE_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		displayColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SORT_ATTIBUTE_DISPLAY_NAME_COLUMN,
				displayColumn.getSwtTableColumn()));
		displayColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				SortLocalizedName sortLocalizedName = ((SortAttribute) element).getLocalizedNames().get(storeEditorModel.getDefaultLocale()
						.toString());
				return sortLocalizedName == null ? StringUtils.EMPTY : sortLocalizedName.getName();
			}
		});
	}

	private String capitalToProper(final String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}

		return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1).toLowerCase(Locale.ENGLISH);
	}

	private SelectionAdapter getSelectionAdapterForSortingColumnNames(final int columnIndex, final TableColumn tableColumn) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent selectionEvent) {
				comparator.setColumnIndex(columnIndex);
				Table swtTable = table.getSwtTable();
				swtTable.setSortDirection(comparator.getDirection());
				swtTable.setSortColumn(tableColumn);

				tableViewer.refresh();
			}
		};
	}

	/**
	 * Change the current default sort.
	 * @param sortAttribute the new default sort
	 */
	public void setDefaultSortAttribute(final SortAttribute sortAttribute) {
		if (defaultSortAttribute != null) {
			defaultSortAttribute.setDefaultAttribute(false);
			modifiedSortAttributes.put(defaultSortAttribute.getGuid(), defaultSortAttribute);
		}
		defaultSortAttribute = sortAttribute;
		sortAttribute.setDefaultAttribute(true);
		modifiedSortAttributes.put(defaultSortAttribute.getGuid(), defaultSortAttribute);

		tableViewer.refresh();
		markDirty();
	}

	/**
	 * Get table.
	 * @return table
	 */
	public IEpTableViewer getTable() {
		return table;
	}

	@Override
	public void commit(final boolean onSave) {
		storeSortAttributes.saveSortAttributes(modifiedSortAttributes.values());
		storeSortAttributes.deleteSortAttibutes(persistedAttributesToBeRemoved);
		super.commit(onSave);
	}
}
