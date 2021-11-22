/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.LocaleUtils;
import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.impl.GridLayoutComposite;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.domain.search.FacetType;

/**
 * Dialog for facet display name.
 */
@SuppressWarnings("PMD.GodClass")
public class FacetDisplayNameDialog extends Dialog {

	private static final String DASH = "-";
	private static final String ASTERISK = "*";
	private static final String CONFIGURE_RANGE_FACET = AdminStoresMessages.get().ConfigureRangeFacet;
	private static final String CONFIGURE_FACET = AdminStoresMessages.get().ConfigureFacet;

	private static final int ROW_TEXT_FIELD_WIDTH = 40;
	private static final int ROW_TEXT_FIELD_HEIGHT = 15;
	private static final int TEXT_FIELD_WIDTH = 120;
	private static final int TEXT_FIELD_HEIGHT = 15;
	private static final int FACET_DIALOG_WIDTH = 410;
	private static final int FACET_DIALOG_HEIGHT = 340;
	private static final int RANGE_FACET_DIALOG_WIDTH = 410;
	private static final int RANGE_FACET_DIALOG_HEIGHT = 535;
	private static final int FACET_GROUP_COMPOSITE_HEIGHT_LIMIT = 250;
	private static final int DISPLAY_NAME_HORIZONTAL_INDENT = 88;
	private static final int RANGE_ROW_HORIZONTAL_INDENT = 63;
	private static final int FACET_ROW_NUM_OF_COLUMNS = 5;
	private static final int FIRST_TO_TEXT_INDEX = 3;
	private static final int FIRST_DISPLAY_NAME_INDEX = 4;
	private static final int FIRST_FROM_TEXT_INDEX = 1;
	private static final int FROM_TEXT_INDEX = 0;
	private static final int TO_TEXT_INDEX = 2;
	private static final int DISPLAY_NAME_TEXT_INDEX = 3;
	private static final int VERTICAL_INDENT = 10;
	private static final int FIELD_KEY_TEXT_HORIZONTAL_INDENT = 99;
	private static final int ADD_LABEL_HORIZONTAL_INDENT = 180;
	private static final int SCROLLED_COMPOSITE_HORIZONTAL_INDENT = 100;
	private static final int X_HORIZONTAL_INDENT = 6;
	private static final int FIRST_ROW_WIDTH_HINT = 305;
	private static final int FACET_GROUP_WIDTH_HINT = 325;
	private static final int MAX_NUM_OF_ROW_VISIBLE = 3;
	private static final int ADD_BUTTON_WIDTH = 20;
	private static final int ADD_BUTTON_HEIGHT = 22;

	private final EpControlBindingProvider binder = EpControlBindingProvider.getInstance();
	private final FacetModel facetModel;
	private String localeChosen;
	private IEpLayoutComposite facetGroupParent;
	private IEpLayoutComposite parent;
	private final DataBindingContext bindingContext = new DataBindingContext();
	private Button okButton;
	private final Locale defaultLocale;
	private final String defaultLocaleDisplayName;
	private final Map<String, String> displayNameMap;
	private final Map<Text, Map<String, String>> displayTextMap = new HashMap<>();
	private final Map<String, String> localeMap = new HashMap<>();
	private final AggregateValidationStatus aggregateValidationStatus;
	private Label addValueRow;
	private IEpLayoutComposite warningGroup;

	/**
	 * Constructor.
	 *
	 * @param shell      parent shell
	 * @param facetModel facet model
	 */
	public FacetDisplayNameDialog(final Shell shell, final FacetModel facetModel) {
		super(shell);
		this.facetModel = facetModel;
		facetModel.updateDefaultValuesInMaps();
		aggregateValidationStatus = new AggregateValidationStatus(bindingContext.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		defaultLocale = LocaleUtils.toLocale(facetModel.getDefaultLocaleDisplayName());
		defaultLocaleDisplayName = defaultLocale.getDisplayName();
		localeChosen = defaultLocaleDisplayName;
		displayNameMap = new HashMap<>(facetModel.getDisplayNameMap());
	}

	@Override
	protected Control createDialogArea(final Composite composite) {
		final Composite area = (Composite) super.createDialogArea(composite);
		parent = CompositeFactory.createGridLayoutComposite(area, 1, false);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		warningGroup = parent.addGroup(2, false, parent.createLayoutData());
		IEpLayoutData warningLayout = warningGroup.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);
		warningGroup.addImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_WARNING_SMALL), warningLayout);

		IEpLayoutData labelLayout = warningGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER, true, false);
		warningGroup.addLabel(CoreMessages.get().EpValidatorFactory_LocaleDisplayNamesRequired, labelLayout);

		warningGroup.getSwtComposite().setVisible(localeDisplayNamesNotPopulated());

		final IEpLayoutData comboLayoutData = parent.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL);
		CCombo cCombo = parent.addComboBox(EpControlFactory.EpState.READ_ONLY, comboLayoutData);
		cCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent selectionEvent) {
				String prevLocale = localeChosen;
				localeChosen = cCombo.getItem(cCombo.getSelectionIndex());
				if (!prevLocale.equals(localeChosen)) {
					String localeString = localeMap.get(prevLocale);
					Control[] facetGroup = facetGroupParent.getSwtComposite().getChildren();
					Text displayName = (Text) ((Composite) facetGroup[0]).getChildren()[1];
					displayName.setText(displayNameMap.getOrDefault(localeMap.get(localeChosen), EMPTY));
					if (facetModel.getFacetType() == FacetType.RANGE_FACET) {
						updateDisplayMaps(localeString);
					}
					parent.getSwtComposite().layout();
				}
			}
		});
		cCombo.add(defaultLocaleDisplayName);
		localeMap.put(defaultLocaleDisplayName, defaultLocale.toString());
		facetModel.getLocales().stream()
				.map(this::getDisplayName)
				.filter(localeDisplayName -> !localeDisplayName.equals(defaultLocaleDisplayName))
				.forEach(cCombo::add);

		cCombo.setEnabled(true);
		cCombo.select(0);

		IEpLayoutComposite facetGroup = parent.addGroup(2, false,
				parent.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false, 2, 1));

		IEpLayoutData fieldKeyLabelLayout = facetGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER);
		facetGroup.addLabel(FacetTable.FACET_NAME, fieldKeyLabelLayout);

		IEpLayoutData fieldKeyTextLayout = facetGroup.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);
		Text fieldKeyText = facetGroup.addTextField(EpControlFactory.EpState.READ_ONLY, fieldKeyTextLayout);
		fieldKeyText.setText(facetModel.getFacetName());

		GridData fieldKeyTextLayoutData = (GridData) fieldKeyText.getLayoutData();
		fieldKeyTextLayoutData.widthHint = TEXT_FIELD_WIDTH;
		fieldKeyTextLayoutData.heightHint = TEXT_FIELD_HEIGHT;
		fieldKeyTextLayoutData.horizontalIndent = FIELD_KEY_TEXT_HORIZONTAL_INDENT;

		GridData cComboLayoutData = (GridData) cCombo.getLayoutData();
		cComboLayoutData.verticalIndent = VERTICAL_INDENT;

		((GridLayoutComposite) facetGroup).getGridData().verticalIndent = VERTICAL_INDENT;

		createDisplayNameLayout(parent);
		return area;
	}

	private void updateDisplayMaps(final String localeString) {
		for (Text text : displayTextMap.keySet()) {
			Map<String, String> displayNameMap = displayTextMap.get(text);
			displayNameMap.put(localeString, text.getText());
			text.setText(displayNameMap.get(localeMap.get(localeChosen)));
		}
	}

	private String getDisplayName(final Locale locale) {
		String localeDisplayName = locale.getDisplayName();
		localeMap.put(localeDisplayName, locale.toString());
		return localeDisplayName;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	private String getTitle() {
		return facetModel.getFacetType() == FacetType.RANGE_FACET ? CONFIGURE_RANGE_FACET : CONFIGURE_FACET;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, CoreMessages.get().AbstractEpDialog_ButtonSave, true);
		createButton(parent, IDialogConstants.CANCEL_ID, CoreMessages.get().AbstractEpDialog_ButtonCancel, false);
		aggregateValidationStatus.addValueChangeListener(event -> changeOkButtonStatus(event.diff.getNewValue()));
		okButton.setEnabled(false);
	}

	@Override
	protected Point getInitialSize() {
		if (facetModel.getFacetType() == FacetType.FACET) {
			return new Point(FACET_DIALOG_WIDTH, FACET_DIALOG_HEIGHT);
		}
		return new Point(RANGE_FACET_DIALOG_WIDTH, RANGE_FACET_DIALOG_HEIGHT);
	}

	private void createDisplayNameLayout(final IEpLayoutComposite parent) {
		facetGroupParent = parent.addGroup(1, false, parent.createLayoutData());
		((GridLayout) facetGroupParent.getSwtComposite().getLayout()).marginWidth = 0;
		IEpLayoutComposite facetGroup = facetGroupParent.addGroup(2, false,
				parent.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false, 2, 1));
		((GridData) facetGroup.getSwtComposite().getLayoutData()).widthHint = FACET_GROUP_WIDTH_HINT;

		facetGroupParent.getSwtComposite().addListener(SWT.Resize, (Listener) event -> {
			Point size = facetGroupParent.getSwtComposite().getSize();
			if (size.y > FACET_GROUP_COMPOSITE_HEIGHT_LIMIT) {
				facetGroupParent.getSwtComposite().setSize(size.x, FACET_GROUP_COMPOSITE_HEIGHT_LIMIT);
			}
		});

		final IEpLayoutData displayNameLabelLayout = facetGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER);
		facetGroup.addLabel(FacetTable.DISPLAY_NAME, displayNameLabelLayout);
		Text displayNameText = facetGroup.addTextField(EpControlFactory.EpState.EDITABLE,
				facetGroup.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL));
		displayNameText.setText(facetModel.getDisplayName(localeMap.get(localeChosen)));
		displayNameText.addModifyListener((ModifyListener) modifyEvent -> {
			displayNameMap.put(localeMap.get(localeChosen), displayNameText.getText());
			warningGroup.getSwtComposite().setVisible(localeDisplayNamesNotPopulated());
		});
		bind(displayNameText, EpValidatorFactory.STRING_255_REQUIRED);

		GridData displayNameTextLayoutData = (GridData) displayNameText.getLayoutData();
		displayNameTextLayoutData.widthHint = TEXT_FIELD_WIDTH;
		displayNameTextLayoutData.heightHint = TEXT_FIELD_HEIGHT;
		displayNameTextLayoutData.horizontalIndent = DISPLAY_NAME_HORIZONTAL_INDENT;

		if (facetModel.getFacetType() == FacetType.RANGE_FACET) {
			addRangedValueSection(facetGroupParent);
		}
	}

	private void addRangedValueSection(final IEpLayoutComposite facetGroupParent) {
		IEpLayoutComposite valueGroup = facetGroupParent.addGroup(FACET_ROW_NUM_OF_COLUMNS, false,
				facetGroupParent.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL));
		((GridData) valueGroup.getSwtComposite().getLayoutData()).widthHint = FIRST_ROW_WIDTH_HINT;
		valueGroup.addLabel(AdminStoresMessages.get().Values, valueGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL));

		IEpLayoutComposite rangeValueGroups = facetGroupParent.addScrolledGridLayoutComposite(1, false, true, SWT.V_SCROLL);
		ScrolledComposite scrolledComposite = (ScrolledComposite) facetGroupParent.getSwtComposite().getChildren()[2];
		scrolledComposite.setAlwaysShowScrollBars(true);
		((GridData) scrolledComposite.getLayoutData()).horizontalIndent = SCROLLED_COMPOSITE_HORIZONTAL_INDENT;

		SortedSet<RangeFacet> rangeFacets = facetModel.getRangeFacets();
		if (rangeFacets.isEmpty()) {
			facetModel.populateDefaultRangeFacets();
		}

		Iterator<RangeFacet> iterator = rangeFacets.iterator();
		RangeFacet firstRangeFacet = iterator.next();
		addRangedValueRow(valueGroup, RANGE_ROW_HORIZONTAL_INDENT, firstRangeFacet);
		iterator.forEachRemaining(rangeFacet -> addRangeRow(rangeValueGroups, rangeFacet));

		addValueRow = facetGroupParent.addImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_PLUS),
				facetGroupParent.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		((GridData) addValueRow.getLayoutData()).horizontalIndent = ADD_LABEL_HORIZONTAL_INDENT;
		addValueRow.addListener(SWT.Resize, (Listener) event -> addValueRow.setSize(ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT));
		addValueRow.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent mouseEvent) {
				addRangeRow(rangeValueGroups, EMPTY, EMPTY, EMPTY, null);
				parent.getSwtComposite().layout();
			}
		});
	}

	private void addRangeRow(final IEpLayoutComposite iEpLayoutComposite, final RangeFacet rangeFacet) {
		String fromValue = rangeFacet.getStart() == null ? ASTERISK : rangeFacet.getStart().toString();
		String endValue = rangeFacet.getEnd() == null ? ASTERISK : rangeFacet.getEnd().toString();
		Map<String, String> displayNameMap = rangeFacet.getDisplayNameMap();
		addRangeRow(iEpLayoutComposite, fromValue, endValue, displayNameMap.getOrDefault(localeMap.get(localeChosen), EMPTY), displayNameMap);
	}

	private void addRangeRow(final IEpLayoutComposite iEpLayoutComposite, final String fromStringValue, final String toStringValue,
							 final String displayName, final Map<String, String> displayNameMap) {
		IEpLayoutComposite rangeValueGroup = iEpLayoutComposite.addGroup(FACET_ROW_NUM_OF_COLUMNS, false,
				iEpLayoutComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL));
		addRangedValueRow(rangeValueGroup, 0, fromStringValue, toStringValue, displayName, displayNameMap);

		Label deleteValueGroup = rangeValueGroup.addImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_X),
				rangeValueGroup.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		((GridData) deleteValueGroup.getLayoutData()).horizontalIndent = X_HORIZONTAL_INDENT;
		deleteValueGroup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent mouseEvent) {
				Composite childComposite = rangeValueGroup.getSwtComposite();
				Text displayText = (Text) childComposite.getChildren()[DISPLAY_NAME_TEXT_INDEX];
				if (displayTextMap.containsKey(displayText)) {
					displayTextMap.remove(displayText);
				}
				Composite composite = iEpLayoutComposite.getSwtComposite();
				int height = childComposite.getSize().y;
				childComposite.dispose();
				if (composite.getChildren().length < MAX_NUM_OF_ROW_VISIBLE) {
					Composite scrolledComposite = composite.getParent();
					Point size = scrolledComposite.getSize();
					scrolledComposite.setSize(size.x, size.y - height);
				}
				parent.getSwtComposite().layout();
			}
		});
	}

	private void addRangedValueRow(final IEpLayoutComposite iEpLayoutComposite, final int indent, final RangeFacet rangeFacet) {
		String fromValue = rangeFacet.getStart() == null ? ASTERISK : rangeFacet.getStart().toString();
		String endValue = rangeFacet.getEnd() == null ? ASTERISK : rangeFacet.getEnd().toString();
		Map<String, String> displayNameMap = new HashMap<>(rangeFacet.getDisplayNameMap());
		addRangedValueRow(iEpLayoutComposite, indent, fromValue, endValue,
				displayNameMap.getOrDefault(localeMap.get(localeChosen), EMPTY), displayNameMap);
	}

	private void addRangedValueRow(final IEpLayoutComposite iEpLayoutComposite, final int indent, final String fromStringValue,
								   final String toStringValue, final String displayName, final Map<String, String> displayNameMap) {
		Text fromText = createTextField(iEpLayoutComposite, fromStringValue);

		((GridData) fromText.getLayoutData()).horizontalIndent = indent;

		iEpLayoutComposite.addLabel(DASH, iEpLayoutComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));

		Text toText = createTextField(iEpLayoutComposite, toStringValue);

		Text displayText = createTextField(iEpLayoutComposite, displayName);

		displayText.addModifyListener(modifyEvent -> changeOkButtonStatus(aggregateValidationStatus.getValue()));

		Map<String, String> displayMap;

		if (displayNameMap == null) {
			displayMap = new HashMap<>();
			for (Locale locale : facetModel.getLocales()) {
				displayMap.put(locale.toString(), EMPTY);
			}
		} else {
			displayMap = new HashMap<>(displayNameMap);
		}

		displayTextMap.put(displayText, displayMap);

		bind(fromText, EpValidatorFactory.ASTERISK_OR_BIG_DECIMAL_REQUIRED);
		bind(toText, EpValidatorFactory.ASTERISK_OR_BIG_DECIMAL_REQUIRED);
	}

	private void bind(final Control control, final IValidator iValidator) {
		binder.bind(bindingContext, control, iValidator, null, buildUpdateStrategy(iValidator), false);
	}

	private ObservableUpdateValueStrategy buildUpdateStrategy(final IValidator iValidator) {
		return new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object value) {
				return iValidator.validate(value);
			}
		};
	}

	private Text createTextField(final IEpLayoutComposite iEpLayoutComposite, final String fromStringValue) {
		Text text = iEpLayoutComposite.addTextField(EpControlFactory.EpState.EDITABLE,
				iEpLayoutComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		text.setText(fromStringValue);

		GridData textLayoutData = (GridData) text.getLayoutData();
		textLayoutData.widthHint = ROW_TEXT_FIELD_WIDTH;
		textLayoutData.heightHint = ROW_TEXT_FIELD_HEIGHT;
		return text;
	}

	private void changeOkButtonStatus(final IStatus iStatus) {
		if (iStatus != null && iStatus.getSeverity() == IStatus.ERROR) {
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
	}

	private boolean localeDisplayNamesNotPopulated() {
		return displayNameMap.entrySet().stream().anyMatch(entry -> entry.getValue().isEmpty());
	}

	@Override
	protected void okPressed() {
		String localeString = localeMap.get(localeChosen);
		Control[] facetGroup = facetGroupParent.getSwtComposite().getChildren();
		displayNameMap.put(localeString, ((Text) ((Composite) facetGroup[0]).getChildren()[1]).getText());
		facetModel.setDisplayNameMap(displayNameMap);

		if (facetModel.getFacetType() == FacetType.RANGE_FACET) {
			updateDisplayMaps(localeString);
			SortedSet<RangeFacet> rangeFacets = new TreeSet<>();

			Control[] firstRow = ((Composite) facetGroup[1]).getChildren();

			String fromText = ((Text) firstRow[FIRST_FROM_TEXT_INDEX]).getText();
			String toText = ((Text) firstRow[FIRST_TO_TEXT_INDEX]).getText();
			Text displayText = (Text) firstRow[FIRST_DISPLAY_NAME_INDEX];
			addRangeFacetToSortedSet(rangeFacets, fromText, toText, displayTextMap.get(displayText));

			Control[] rangeRows = ((Composite) ((Composite) facetGroup[2]).getChildren()[0]).getChildren();
			for (Control control : rangeRows) {
				Composite composite = (Composite) control;
				Control[] children = composite.getChildren();
				fromText = ((Text) children[FROM_TEXT_INDEX]).getText();
				toText = ((Text) children[TO_TEXT_INDEX]).getText();
				displayText = (Text) children[DISPLAY_NAME_TEXT_INDEX];
				addRangeFacetToSortedSet(rangeFacets, fromText, toText, displayTextMap.get(displayText));
			}
			facetModel.setRangeFacets(rangeFacets);
		}
		super.okPressed();
	}

	private void addRangeFacetToSortedSet(final SortedSet<RangeFacet> rangeFacets, final String fromText, final String toText,
										  final Map<String, String> displayName) {
		rangeFacets.add(new RangeFacet(fromText.equals(ASTERISK) ? null : new BigDecimal(fromText),
				toText.equals(ASTERISK) ? null : new BigDecimal(toText), displayName));
	}
}