/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.actions.AddTagGroupAction;
import com.elasticpath.cmclient.admin.configuration.actions.EditTagGroupAction;
import com.elasticpath.cmclient.admin.configuration.actions.RemoveTagGroupAction;
import com.elasticpath.cmclient.admin.configuration.dialogs.EditTagDefinitionDialog;
import com.elasticpath.cmclient.admin.configuration.dialogs.EditTagGroupDialog;
import com.elasticpath.cmclient.admin.configuration.listener.TagGroupUpdateListener;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.ui.framework.impl.GridLayoutComposite;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagGroup;

/**
 * A composite to display all TagGroup objects and their child TagDefinitions.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.ConstructorCallsOverridableMethod"})
public class TagGroupComposite extends GridLayoutComposite implements ISelectionProvider, TagGroupUpdateListener {

	private static final int TAG_GROUP_GUID_COLUMN_NUMBER = 0;
	private static final int TAG_GROUP_NAME_COLUMN_NUMBER = 1;
	private static final String TAG_GROUP_TABLE_NAME = "Tag Groups";

	private static final String TAG_DEFINITION_TABLE_NAME = "Tag Definitions";
	private static final int GUID_WIDTH = 175;
	private static final int NAME_WIDTH = 275;
	private static final int TYPE_WIDTH = 135;
	private static final int NUM_MAIN_COMPOSITE_COLUMNS = 4;
	private static final int TAG_GROUP_COMPOSITE_HORIZONTAL_INDENT = 5;
	private static final int TOOLBAR_HORIZONTAL_SPAN = 4;
	private static final int TOOLBAR_VERTICAL_SPAN = 1;
	private static final int FILTER_HORIZONTAL_INDENT = 5;
	private static final int FILTER_VERTICAL_INDENT = 5;
	private static final int FILTER_TEXTFIELD_VERTICAL_INDENT = 5;
	private static final int LANG_SELECTOR_VERTICAL_INDENT = 5;

	private IEpTableViewer tableViewer;
	private final TagGroupModel tagGroupModel;

	private CCombo languageSelector;

	private Locale selectedLocale;
	private List<Locale> allLocales;
	private TagGroupViewerComparator comparator;
	private Button editTag;
	private Button removeTag;
	private IEpTableViewer tagDefTableViewer;

	private ToolBarManager toolbarManager;
	private EditTagGroupAction editTagGroupAction;
	private RemoveTagGroupAction removeTagGroupAction;
	private IEpLayoutComposite tagDefTableComposite;

	/**
	 * @param parent        the parent composite
	 * @param tagGroupModel the model for manipulating tags
	 */
	public TagGroupComposite(final IEpLayoutComposite parent, final TagGroupModel tagGroupModel) {
		super(parent.getSwtComposite(), NUM_MAIN_COMPOSITE_COLUMNS, false);
		this.tagGroupModel = tagGroupModel;

		this.createControls();
	}

	private void createControls() {
		GridLayout gridLayout = (GridLayout) getSwtComposite().getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		this.createToolbar();
		this.createFilterWidget();
		this.createLanguageSelector();
		this.createTagGroupTableComposite();
		this.createTagDefinitionTableComposite();
		this.createTagDefinitionButtonComposite();
		this.createTagGroupButtons();
	}

	private void createToolbar() {
		toolbarManager = new ToolBarManager(SWT.FLAT | SWT.RIGHT | SWT.HORIZONTAL | SWT.BORDER);

		IEpLayoutData toolbarLayoutData = createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, false, false, TOOLBAR_HORIZONTAL_SPAN,
				TOOLBAR_VERTICAL_SPAN);

		ToolBar toolbar = toolbarManager.createControl(getSwtComposite());
		toolbar.setLayoutData(toolbarLayoutData.getSwtLayoutData());
	}

	private void createTagGroupTableComposite() {
		IEpLayoutComposite tableComposite = addGroup(AdminConfigurationMessages.get().TagGroup_TableName, 1, false,
				createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true, 2, 1));

		GridLayout tcGridLayout = (GridLayout) tableComposite.getSwtComposite().getLayout();
		tcGridLayout.marginWidth = 0;
		tcGridLayout.verticalSpacing = 0;

		((GridData) tableComposite.getSwtComposite().getLayoutData()).horizontalIndent = TAG_GROUP_COMPOSITE_HORIZONTAL_INDENT;

		tableViewer = tableComposite.addTableViewer(false, EpControlFactory.EpState.EDITABLE,
				createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true), TAG_GROUP_TABLE_NAME);

		final IEpTableColumn guidColumn = tableViewer.addTableColumn(AdminConfigurationMessages.get().tagGroupGuid, GUID_WIDTH);
		final IEpTableColumn nameColumn = tableViewer.addTableColumn(AdminConfigurationMessages.get().tagGroupName, NAME_WIDTH);
		tableViewer.setContentProvider(new TagGroupContentProvider());
		tableViewer.setLabelProvider(new TagGroupLabelProvider());
		tableViewer.setInput(tagGroupModel.getAllTagGroups());

		final TableViewer swtTableViewer = tableViewer.getSwtTableViewer();

		swtTableViewer.addDoubleClickListener(event -> {
			final EditTagGroupDialog dialog = new EditTagGroupDialog(getSwtComposite().getShell(),
					(TagGroup) ((IStructuredSelection) swtTableViewer.getSelection()).getFirstElement(), tagGroupModel);
			dialog.open();
		});

		comparator = new TagGroupViewerComparator();
		swtTableViewer.setComparator(comparator);
		guidColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(TAG_GROUP_GUID_COLUMN_NUMBER,
				guidColumn.getSwtTableColumn()));
		nameColumn.getSwtTableColumn().addSelectionListener(getSelectionAdapterForSortingColumnNames(TAG_GROUP_NAME_COLUMN_NUMBER,
				nameColumn.getSwtTableColumn()));

		swtTableViewer.addSelectionChangedListener(selectionChangedEvent -> {
			if ((selectionChangedEvent.getSelection() instanceof IStructuredSelection)
					&& ((IStructuredSelection) selectionChangedEvent.getSelection()).getFirstElement() instanceof TagGroup) {
				editTagGroupAction.setEnabled(true);
				removeTagGroupAction.setEnabled(true);
				tagDefTableViewer.setInput(getSelectedTagGroup().getTagDefinitions());
			} else {
				editTagGroupAction.setEnabled(false);
				removeTagGroupAction.setEnabled(false);
				tagDefTableViewer.setInput(null);
			}
		});
	}

	private void createTagDefinitionTableComposite() {
		tagDefTableComposite = addGroup(AdminConfigurationMessages.get().TagDefinition_TableName, 2, false,
				createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true, 2, 1));
		GridLayout tcGridLayout = (GridLayout) tagDefTableComposite.getSwtComposite().getLayout();
		tcGridLayout.marginWidth = 0;
		tcGridLayout.verticalSpacing = 0;

		tagDefTableViewer = tagDefTableComposite.addTableViewer(false, EpControlFactory.EpState.EDITABLE,
				createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true), TAG_DEFINITION_TABLE_NAME);

		tagDefTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDefinition_Label_Guid, GUID_WIDTH);
		tagDefTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDefinition_Label_Name, NAME_WIDTH);
		tagDefTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDefinition_Label_Type, TYPE_WIDTH);

		tagDefTableViewer.setLabelProvider(new TagDefinitionLabelProvider());
		tagDefTableViewer.setContentProvider(new TagDefinitionContentProvider());

		tagDefTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				if (event.getSelection().isEmpty()) {
					editTag.setEnabled(false);
					removeTag.setEnabled(false);
				} else {
					editTag.setEnabled(true);
					removeTag.setEnabled(true);
				}
			}
		});
		tagDefTableViewer.getSwtTableViewer().setSorter(new ViewerSorter() {
			@Override
			public int compare(final Viewer viewer, final Object obj1, final Object obj2) {
				return ((TagDefinition) obj2).getGuid().compareTo(((TagDefinition) obj1).getGuid());
			}
		});
		tagDefTableViewer.getSwtTableViewer().addDoubleClickListener(event -> {
			TagDefinition selectedTagDef = (TagDefinition) ((IStructuredSelection) tagDefTableViewer.getSwtTableViewer().getSelection())
					.getFirstElement();
			final EditTagDefinitionDialog dialog = new EditTagDefinitionDialog(
					getSwtComposite().getShell(),
					getSelectedTagGroup(),
					selectedTagDef,
					tagGroupModel
			);
			dialog.open();
		});
	}

	private void createTagDefinitionButtonComposite() {
		final IEpLayoutComposite buttonsPane = tagDefTableComposite.addTableWrapLayoutComposite(1, true,
				tagDefTableComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		this.editTag = buttonsPane.addPushButton(AdminConfigurationMessages.get().TagDefinition_Edit_Button, CoreImageRegistry
				.getImage(CoreImageRegistry.IMAGE_EDIT), EpControlFactory.EpState.EDITABLE, createLayoutData());
		this.editTag.setEnabled(false);

		Button addTag = buttonsPane.addPushButton(AdminConfigurationMessages.get().TagDefinition_Add_Button, CoreImageRegistry
				.getImage(CoreImageRegistry.IMAGE_ADD), EpControlFactory.EpState.EDITABLE, createLayoutData());

		this.removeTag = buttonsPane.addPushButton(AdminConfigurationMessages.get().TagDefinition_Remove_Button, CoreImageRegistry
				.getImage(CoreImageRegistry.IMAGE_REMOVE), EpControlFactory.EpState.EDITABLE, createLayoutData());
		this.removeTag.setEnabled(false);

		editTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final EditTagDefinitionDialog dialog = new EditTagDefinitionDialog(getSwtComposite().getShell(), getSelectedTagGroup(),
						(TagDefinition) ((IStructuredSelection) tagDefTableViewer.getSwtTableViewer().getSelection()).getFirstElement(), 
						tagGroupModel
				);
				dialog.open();
			}
		});

		addTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final EditTagDefinitionDialog dialog = new EditTagDefinitionDialog(getSwtComposite().getShell(), getSelectedTagGroup(),
						null, tagGroupModel);
				dialog.open();
			}
		});

		removeTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				TagDefinition tagDefinitionToDelete =
						(TagDefinition) ((IStructuredSelection) tagDefTableViewer.getSwtTableViewer().getSelection()).getFirstElement();
				tagGroupModel.removeTagDefinition(tagDefinitionToDelete);
			}
		});
	}

	private void createFilterWidget() {
		IEpLayoutData filterLabelLayout = createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER, false, false);

		Label filterLabel = addLabel(AdminConfigurationMessages.get().filterLabel, filterLabelLayout);
		((GridData) filterLabel.getLayoutData()).horizontalIndent = FILTER_HORIZONTAL_INDENT;
		((GridData) filterLabel.getLayoutData()).verticalIndent = FILTER_VERTICAL_INDENT;

		IEpLayoutData filterTextLayout = createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false);

		Text tagGroupFilter = addTextField(EpControlFactory.EpState.EDITABLE, filterTextLayout, SWT.SEARCH | SWT.ICON_CANCEL);
		((GridData) tagGroupFilter.getLayoutData()).verticalIndent = FILTER_TEXTFIELD_VERTICAL_INDENT;

		tagGroupFilter.addModifyListener((ModifyListener) event -> {
			final Text source = (Text) event.getSource();
			String filterText = source.getText();
			if (StringUtils.isBlank(filterText)) {
				tableViewer.getSwtTableViewer().setFilters(new ViewerFilter[0]);
			} else {
				tableViewer.getSwtTableViewer().setFilters(new ViewerFilter[] { new TagGroupFilter(filterText) });
			}
		});
	}

	private void createLanguageSelector() {
		IEpLayoutData langLabelLayout = createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER, true, false);
		addLabel(AdminConfigurationMessages.get().languageLabel, langLabelLayout);

		IEpLayoutData languageLayout = createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING, false, false);
		languageSelector = addComboBox(EpControlFactory.EpState.EDITABLE, languageLayout);
		((GridData) languageSelector.getLayoutData()).verticalIndent = LANG_SELECTOR_VERTICAL_INDENT;

		List<Locale> allAvailableLocales = getAllAvailableLocales();
		final String[] localesForCCombo = allAvailableLocales.stream().map(Locale::getDisplayName).toArray(String[]::new);

		languageSelector.setItems(localesForCCombo);
		languageSelector.select(languageSelector.indexOf(getSelectedLocale().getDisplayName()));
		languageSelector.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				// do nothing.
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Locale selectedLocale = getAllAvailableLocales().get(languageSelector.getSelectionIndex());
				setSelectedLocale(selectedLocale);
				tableViewer.getSwtTableViewer().refresh();
				tagDefTableViewer.getSwtTableViewer().refresh();
			}
		});

	}

	private void createTagGroupButtons() {
		editTagGroupAction = new EditTagGroupAction(getSwtComposite().getShell(), tagGroupModel, tableViewer.getSwtTableViewer());
		editTagGroupAction.setEnabled(false);

		AddTagGroupAction addTagGroupAction = new AddTagGroupAction(getSwtComposite().getShell(), tagGroupModel);
		addTagGroupAction.setEnabled(true);

		removeTagGroupAction = new RemoveTagGroupAction(tagGroupModel, tableViewer.getSwtTableViewer());
		removeTagGroupAction.setEnabled(false);

		final Separator tagGroupActionGroup = new Separator("tagGroupActionGroup");
		getToolbarManager().add(tagGroupActionGroup);
		addToolbarAction(tagGroupActionGroup, editTagGroupAction);
		addToolbarAction(tagGroupActionGroup, addTagGroupAction);
		addToolbarAction(tagGroupActionGroup, removeTagGroupAction);

		getToolbarManager().update(true);
	}

	private void addToolbarAction(final Separator actionGroup, final IAction action) {
		ActionContributionItem actionCI = new ActionContributionItem(action);
		actionCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getToolbarManager().appendToGroup(actionGroup.getGroupName(), actionCI);
	}

	/**
	 * Provides the content for the TagGroup display table, given an input.
	 */
	@Override
	public void tagGroupUpdated(final TagGroup tagGroup) {
		this.tableViewer.setInput(tagGroupModel.getAllTagGroups());
		this.tableViewer.getSwtTableViewer().refresh();
		TagGroup selectedTagGroup = getSelectedTagGroup();
		Set<TagDefinition> tagDefinitions;
		if (selectedTagGroup == null) {
			tagDefinitions = Collections.emptySet();
		} else {
			tagDefinitions = selectedTagGroup.getTagDefinitions();
		}
		tagDefTableViewer.setInput(tagDefinitions);
		tagDefTableViewer.getSwtTableViewer().refresh();
	}

	private Locale getSelectedLocale() {
		if (selectedLocale == null) {
			return Locale.getDefault();
		}
		return selectedLocale;
	}

	private void setSelectedLocale(final Locale selectedLocale) {
		this.selectedLocale = selectedLocale;
	}

	private List<Locale> getAllAvailableLocales() {
		if (allLocales == null) {
			allLocales = new ArrayList<>();
			final Locale[] availableLocales = Locale.getAvailableLocales();
			allLocales = Arrays.stream(availableLocales)
					.filter(locale -> StringUtils.isNotEmpty(locale.getDisplayName()))
					.collect(Collectors.toList());

		}
		return allLocales;
	}

	/**
	 * Filter setting definitions against a specified string.
	 */
	private final class TagGroupFilter extends ViewerFilter {

		private final String filterText;

		TagGroupFilter(final String filterText) {
			this.filterText = filterText;
		}

		@Override
		public boolean select(final Viewer viewer, final Object parent, final Object element) {
			TagGroup tagGroup = (TagGroup) element;
			return StringUtils.containsIgnoreCase(tagGroup.getGuid(), filterText);
		}
	}

	/**
	 * Add a SelectionChangedListener that will be notified when the table's
	 * selected tag group table viewer changes.
	 *
	 * @param listener the listener to add
	 */
	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		this.tableViewer.getSwtTableViewer().addSelectionChangedListener(listener);
	}

	/**
	 * Get the selected TagGroup.
	 *
	 * @return the selected TagGroup selection
	 */
	@Override
	public ISelection getSelection() {
		return tableViewer.getSwtTableViewer().getSelection();
	}

	/**
	 * Remove the given SelectionChangedListener.
	 *
	 * @param listener the listener to remove
	 */
	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		this.tableViewer.getSwtTableViewer().removeSelectionChangedListener(listener);
	}

	/**
	 * Sets the current selected TagGroup.
	 *
	 * @param iselection the new TagGroup
	 */
	@Override
	public void setSelection(final ISelection iselection) {
		this.tableViewer.getSwtTableViewer().setSelection(iselection);
	}

	protected ToolBarManager getToolbarManager() {
		return toolbarManager;
	}

	private TagGroup getSelectedTagGroup() {
		TagGroup selectedTagGroup = null;
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection
				&& ((IStructuredSelection) selection).getFirstElement() instanceof TagGroup
		) {
			selectedTagGroup = (TagGroup) ((IStructuredSelection) getSelection()).getFirstElement();
		}
		return selectedTagGroup;
	}

	/**
	 * Provides the labels for the tag group display table.
	 */
	final class TagGroupLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Not implemented.
		 *
		 * @param element     not used
		 * @param columnIndex not used
		 * @return null
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Get the column text for the given TagGroup and column index.
		 *
		 * @param element     the TagGroup
		 * @param columnIndex the index of the column for which to retrieve the text
		 * @return the column text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final TagGroup tagGroup = (TagGroup) element;

			if (columnIndex == 0) {
				return tagGroup.getGuid();
			} else if (columnIndex == 1) {
				return tagGroup.getLocalizedProperties().getValueWithoutFallBack(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale());
			}
			return StringUtils.EMPTY;
		}
	}

	/**
	 * The content provider that provides the tag groups that are displayed in the tag group table.
	 */
	final class TagGroupContentProvider implements IStructuredContentProvider {

		/**
		 * Get the elements to display in the table.
		 *
		 * @param tagGroups List of TagGroups
		 * @return array of elements to pass to the label provider for the table
		 */
		@Override
		public Object[] getElements(final Object tagGroups) {
			Object[] result = null;
			if (tagGroups instanceof List) {
				//check that all objects in the List are TagGroup objects
				for (final Object object : ((List<Object>) tagGroups)) {
					if (!(object instanceof TagGroup)) {
						return null;
					}
				}
				result = ((List<Object>) tagGroups).toArray();
			}
			return result;
		}

		/**
		 * not needed.
		 */
		@Override
		public void dispose() {
			// nothing to dispose
		}

		/**
		 * Not needed.
		 *
		 * @param arg0 not used
		 * @param arg1 not used
		 * @param arg2 not used
		 */
		@Override
		public void inputChanged(final Viewer arg0, final Object arg1, final Object arg2) {
			//not needed
		}

	}

	/**
	 * The comparator that provides the sort facility to sort the tag group table by clicking on the headers.
	 */
	final class TagGroupViewerComparator extends ViewerComparator {
		private int columnIndex;
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;

		private int direction = ASCENDING;

		/**
		 * Constructor.
		 */
		TagGroupViewerComparator() {
			this.columnIndex = 0;
		}

		/**
		 * Get the sorting direction.
		 *
		 * @return SWT.UP or SWT.DOWN
		 */
		public int getDirection() {
			return direction == ASCENDING ? SWT.UP : SWT.DOWN;
		}

		/**
		 * Set the column selected by the user and switch the direction.
		 *
		 * @param columnIndex column index of the column selected
		 */
		public void setColumnIndex(final int columnIndex) {
			if (columnIndex == this.columnIndex) {
				direction ^= 1;
			} else {
				this.columnIndex = columnIndex;
				direction = ASCENDING;
			}
		}

		@Override
		public int compare(final Viewer viewer, final Object element, final Object otherElement) {
			TagGroup tagGroup1 = (TagGroup) element;
			TagGroup tagGroup2 = (TagGroup) otherElement;
			int value;
			switch (columnIndex) {
				case 0:
					value = tagGroup1.getGuid().compareTo(tagGroup2.getGuid());
					break;
				case 1:
					String displayName2 = tagGroup2.getLocalizedProperties()
							.getValueWithoutFallBack(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale());
					String displayName1 = tagGroup1.getLocalizedProperties()
							.getValueWithoutFallBack(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale());
					value = displayName1.compareTo(displayName2);
					break;
				default:
					value = 0;
					break;
			}
			return direction == DESCENDING ? -value : value;
		}

	}

	private SelectionAdapter getSelectionAdapterForSortingColumnNames(final int columnIndex, final TableColumn tableColumn) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent selectionEvent) {
				comparator.setColumnIndex(columnIndex);
				Table swtTable = tableViewer.getSwtTable();
				swtTable.setSortDirection(comparator.getDirection());
				swtTable.setSortColumn(tableColumn);

				tableViewer.getSwtTableViewer().refresh();
			}
		};
	}

	/**
	 * This label provider returns the text that should appear in each column for a given <code>TagDefinition</code> object.
	 */
	final class TagDefinitionLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Gets the image for each column.
		 *
		 * @param element     the image
		 * @param columnIndex the index for the column
		 * @return Image the image
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Gets the text for each column.
		 *
		 * @param element     the text
		 * @param columnIndex the index for the column
		 * @return String the text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final TagDefinition tagDefinition = (TagDefinition) element;
			switch (columnIndex) {
				case 0:
					return tagDefinition.getGuid();
				case 1:
					return tagDefinition.getLocalizedProperties().getValueWithoutFallBack(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME,
							getSelectedLocale());
				case 2:
					return tagDefinition.getValueType().getGuid();
				default:
					return StringUtils.EMPTY;
			}
		}
	}

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters or
	 * simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and always show
	 * the same content.
	 */
	final class TagDefinitionContentProvider implements IStructuredContentProvider {

		/**
		 * Gets the tag definition from the list for each row.
		 *
		 * @param inputElement the input tag group element
		 * @return Object[] the returned input
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public Object[] getElements(final Object inputElement) {
			return ((Set<TagDefinition>) inputElement).toArray();
		}

		/**
		 * dispose the provider.
		 */
		@Override
		public void dispose() {
			// does nothing
		}

		/**
		 * Notify the provider the input has changed.
		 *
		 * @param viewer   the tagDefinitionViewer
		 * @param oldInput the previous input
		 * @param newInput the current selected input
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// does nothing
		}
	}

}
