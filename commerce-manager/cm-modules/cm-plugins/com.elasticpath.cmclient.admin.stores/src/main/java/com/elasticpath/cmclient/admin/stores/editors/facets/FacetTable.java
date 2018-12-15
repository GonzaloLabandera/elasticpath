/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.actions.StoreConfigurationForSearchAndFacet;
import com.elasticpath.cmclient.admin.stores.actions.StoreFacets;
import com.elasticpath.cmclient.admin.stores.editors.facets.editingsupport.FacetTypeSupport;
import com.elasticpath.cmclient.admin.stores.editors.facets.editingsupport.SearchableCheckboxEditingSupport;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;

/**
 * The Facet Configuration table.
 */
public class FacetTable extends AbstractCmClientEditorPageSectionPart {

	/**
	 * Field key string.
	 */
	static final String FACET_NAME = AdminStoresMessages.get().FacetName;
	/**
	 * Display name string.
	 */
	static final String DISPLAY_NAME = AdminStoresMessages.get().DisplayName;
	private static final String FILTER = "Filter";
	private static final String EDIT = "Edit";

	/**
	 * Field key column number.
	 */
	static final int FACET_NAME_COLUMN_NUMBER = 0;
	/**
	 * Field Group column number.
	 */
	static final int FACET_GROUP_COLUMN_NUMBER = 1;
	/**
	 * Field type column number.
	 */
	static final int FIELD_TYPE_COLUMN_NUMBER = 2;
	/**
	 * Searchable column number.
	 */
	static final int SEARCHABLE_COLUMN_NUMBER = 3;
	/**
	 * Facet type column number.
	 */
	static final int FACET_TYPE_COLUMN_NUMBER = 4;
	/**
	 * Display name column number.
	 */
	static final int DISPLAY_NAME_COLUMN_NUMBER = 5;

	private static final int TABLE_HEIGHT = 500;
	private static final int TABLE_MAX_HEIGHT = 600;
	private static final int GROUP_WIDTH = 915;
	private static final int FACET_COLUMN_WIDTH = 170;
	private static final int BUTTON_COLUMN_WIDTH = 130;
	private static final int LABEL_GROUP_NUM_COLUMNS = 3;

	private FacetFilter facetFilter;
	private FieldGroupFilter fieldGroupFilter;
	private Text searchText;
	private FacetViewerComparator comparator;
	private List<FacetModel> input = new ArrayList<>();
	private final AbstractCmClientFormEditor editor;
	private IEpTableViewer table;
	private Button editButton;
	private final Set<String> defaultProductAttributes = ImmutableSet.of("Product Name", "Product Sku Code");

	private final StoreEditorModel storeEditorModel;

	private  IEpLayoutComposite mainPane;

	private TableViewer tableViewer;

	/**
	 * Constructor.
	 *
	 * @param formPage formpage
	 * @param editor edtor
	 * @param style style
	 * @param storeEditorModel storeEditorModel id
	 */
	public FacetTable(final FormPage formPage, final AbstractCmClientFormEditor editor, final int style, final StoreEditorModel storeEditorModel) {

		super(formPage, editor, style);
		this.editor = editor;
		this.storeEditorModel = storeEditorModel;
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);
		mainPane.setControlModificationListener(getEditor());
	}

	@Override
	protected void createControls(final Composite composite, final FormToolkit formToolkit) {
		mainPane = CompositeFactory.createTableWrapLayoutComposite(composite, 2, false);
		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		IEpLayoutData tableLayoutData = mainPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false);

		IEpLayoutData labelGroupData = mainPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false, 2, 1);
		IEpLayoutComposite labelGroup = mainPane.addGroup(LABEL_GROUP_NUM_COLUMNS, false, labelGroupData);
		((TableWrapData) labelGroup.getSwtComposite().getLayoutData()).maxWidth = GROUP_WIDTH;
		final IEpLayoutData layoutData = labelGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER);
		final IEpLayoutData textLayoutData = labelGroup.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);

		labelGroup.addLabel(FILTER, layoutData);
		searchText = labelGroup.addTextField(EpControlFactory.EpState.EDITABLE, textLayoutData, SWT.SEARCH | SWT.ICON_CANCEL);

		createConfigFilterGroup(labelGroup);

		table = mainPane.addTableViewer(false, EpControlFactory.EpState.EDITABLE, tableLayoutData, AdminStoresMessages.get().StoreFacetConfiguration);
		tableViewer = table.getSwtTableViewer();

		searchText.addModifyListener(modifyEvent -> {
			facetFilter.setFilterText(searchText.getText());
			tableViewer.refresh();
		});

		final IEpLayoutData editButtonLayout = mainPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING, true, false);
		editButton = mainPane.addPushButton(EDIT, EpControlFactory.EpState.READ_ONLY, editButtonLayout);
		editButton.setImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT));
		editButton.setEnabled(false);

		createFieldKeyColumn();
		createFieldGroupColumn();
		createFieldTypeColumn();
		createSearchableCheckboxColumn();
		createFacetableComboBox();
		createDisplayNameColumn();

		table.setContentProvider(new ArrayContentProvider());
		table.setInput(input);

		comparator = new FacetViewerComparator();
		tableViewer.setComparator(comparator);

		facetFilter = new FacetFilter();
		fieldGroupFilter = new FieldGroupFilter();
		tableViewer.addFilter(fieldGroupFilter);
		tableViewer.addFilter(facetFilter);

		this.tableViewer.addSelectionChangedListener(selectionChangedEvent -> {
			if ((selectionChangedEvent.getSelection() instanceof IStructuredSelection)) {
				Object element = ((IStructuredSelection) selectionChangedEvent.getSelection()).getFirstElement();
				if (element instanceof FacetModel
						&& ((FacetModel) element).getFacetType() != FacetType.NO_FACET) {
					editButton.setEnabled(true);
				} else {
					editButton.setEnabled(false);
				}
			}
		});

		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final FacetDisplayNameDialog dialog = new FacetDisplayNameDialog(composite.getShell(),
						(FacetModel) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement());

				if (dialog.open() == Window.OK) {
					tableViewer.refresh();
					editor.controlModified();
					markDirty();
				}
			}
		});

		TableWrapData tableWrapData = (TableWrapData) table.getSwtTable().getLayoutData();
		tableWrapData.heightHint = TABLE_HEIGHT;
		tableWrapData.maxHeight = TABLE_MAX_HEIGHT;
	}

	private void createConfigFilterGroup(final IEpLayoutComposite labelGroup) {

		IEpLayoutData configFilterData = mainPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, true, false, 1, 1);
		CCombo configFilterCombo = labelGroup.addComboBox(EpControlFactory.EpState.READ_ONLY, configFilterData);
		configFilterCombo.setEnabled(true);
		configFilterCombo.add("All");
		for (FacetGroup fieldGroup : FacetGroup.values()) {
			configFilterCombo.add(fieldGroup.getName());
		}
		configFilterCombo.select(0);
		configFilterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				fieldGroupFilter.setAttributeType(configFilterCombo.getItem(configFilterCombo.getSelectionIndex()));

				tableViewer.refresh();
				editor.controlModified();
			}
		});

	}

	@Override
	protected void populateControls() {
		StoreConfigurationForSearchAndFacet storeConfigurationForSearchAndFacet =
				new StoreConfigurationForSearchAndFacet(storeEditorModel);
		input = storeConfigurationForSearchAndFacet.getFacetAndSearchableConfiguration();
		table.setInput(input);

		tableViewer.refresh();
	}

	@Override
	protected void bindControls(final DataBindingContext dataBindingContext) {
		// nothing to bind
	}

	private void createFieldKeyColumn() {
		IEpTableColumn fieldKeyColumn = table.addTableColumn(FACET_NAME, FACET_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		fieldKeyColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(FACET_NAME_COLUMN_NUMBER,
				fieldKeyColumn.getSwtTableColumn()));
		fieldKeyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((FacetModel) element).getFacetName();
			}
		});
	}

	private void createFieldGroupColumn() {
		IEpTableColumn fieldKeyColumn = table.addTableColumn(AdminStoresMessages.get().FacetGroup, FACET_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		fieldKeyColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(FACET_GROUP_COLUMN_NUMBER,
				fieldKeyColumn.getSwtTableColumn()));
		fieldKeyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((FacetModel) element).getFacetGroup().getName();
			}
		});
	}

	private void createFieldTypeColumn() {
		IEpTableColumn fieldTypeColumn = table.addTableColumn(AdminStoresMessages.get().FieldType, BUTTON_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		fieldTypeColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(FIELD_TYPE_COLUMN_NUMBER,
				fieldTypeColumn.getSwtTableColumn()));
		fieldTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((FacetModel) element).getFieldKeyType().getName();
			}
		});
	}

	private void createSearchableCheckboxColumn() {
		IEpTableColumn searchableColumn = table.addTableColumn(AdminStoresMessages.get().Searchable, BUTTON_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		searchableColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(SEARCHABLE_COLUMN_NUMBER,
				searchableColumn.getSwtTableColumn()));
		searchableColumn.setEditingSupport(new SearchableCheckboxEditingSupport(table, editor) {
			@Override
			protected void setValue(final Object element, final Object value) {
				super.setValue(element, value);
				markDirty();
			}
		});
		searchableColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				return EMPTY;
			}

			@Override
			public Image getImage(final Object element) {
				if (((FacetModel) element).isSearchable()) {
					return CoreImageRegistry.getImage(CoreImageRegistry.CHECKBOX_CHECKED);
				} else {
					return CoreImageRegistry.getImage(CoreImageRegistry.CHECKBOX_UNCHECKED);
				}
			}
		});
	}

	private void createFacetableComboBox() {
		IEpTableColumn facetableColumn = table.addTableColumn(AdminStoresMessages.get().Facetable, BUTTON_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		facetableColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(FACET_TYPE_COLUMN_NUMBER,
				facetableColumn.getSwtTableColumn()));
		facetableColumn.setEditingSupport(new FacetTypeSupport(tableViewer, defaultProductAttributes, editButton, this) {

			@Override
			protected void setValue(final Object element, final Object value) {
				super.setValue(element, value);
				markDirty();
			}
		});

		facetableColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				return defaultProductAttributes.contains(((FacetModel) element).getFacetName())
						? null : CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT);
			}

			@Override
			public String getText(final Object element) {
				return ((FacetModel) element).getFacetType().getName();
			}
		});
	}

	private void createDisplayNameColumn() {
		IEpTableColumn displayNameColumn = table.addTableColumn(DISPLAY_NAME, FACET_COLUMN_WIDTH, IEpTableColumn.TYPE_NONE);
		displayNameColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(DISPLAY_NAME_COLUMN_NUMBER,
				displayNameColumn.getSwtTableColumn()));
		displayNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final FacetModel model = (FacetModel) element;
				String text = model.getDefaultDisplayName();
				return StringUtils.isBlank(text) ? AdminStoresMessages.get().TextField_To_Edit : text;
			}
		});
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
	@Override
	public void commit(final boolean onSave) {

		if (!input.isEmpty()) {
			StoreFacets storeFacets = new StoreFacets(storeEditorModel);
			storeFacets.saveStoreFacets(input);
		}

		super.commit(onSave);
	}

}