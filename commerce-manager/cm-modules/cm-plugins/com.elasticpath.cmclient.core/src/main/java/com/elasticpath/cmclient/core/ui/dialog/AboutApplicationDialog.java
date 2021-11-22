/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.core.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.impl.VersionService;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;

/**
 * Dialog to change the pagination settings system-wide.
 */
public class AboutApplicationDialog extends AbstractEpDialog {

	private Text applicationTitleText;
	private Text applicationVendorText;
	private Text applicationVersionText;


	private static final int NAME_COLUMN = 0;

	private static final int VALUE_COLUMN = 1;

	private static final int NAME_COLUMN_WIDTH = 225;

	private static final int VALUE_COLUMN_WIDTH = 450;

	private IEpTableViewer settingsTableViewer;
	/**
	 * Default constructor.
	 *
	 * @param parentShell the parent shell
	 */
	public AboutApplicationDialog(final Shell parentShell) {
		super(parentShell, 2, false);
	}

	@Override
	protected void bindControls() {
		// nothing to bind
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite parent) {
		final IEpLayoutComposite controlPane = parent;
		final IEpLayoutData labelData = controlPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER, false, false);
		final IEpLayoutData fieldData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER, false, false);

		controlPane.addLabelBoldRequired(CoreMessages.get().AboutApplicationDialog_ApplicationTitle, EpState.READ_ONLY, labelData);
		applicationTitleText = controlPane.addTextField(EpState.READ_ONLY, fieldData);

		controlPane.addLabelBoldRequired(CoreMessages.get().AboutApplicationDialog_Vendor, EpState.READ_ONLY, labelData);
		applicationVendorText = controlPane.addTextField(EpState.READ_ONLY, fieldData);

		controlPane.addLabelBoldRequired(CoreMessages.get().AboutApplicationDialog_Version, EpState.READ_ONLY, labelData);
		applicationVersionText = controlPane.addTextField(EpState.READ_ONLY, fieldData);

		controlPane.addLabelBoldRequired(CoreMessages.get().AboutApplicationDialog_SystemMetrics, EpState.READ_ONLY, labelData);
		final IEpLayoutData tableLayoutData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, true);
		this.settingsTableViewer = controlPane.addTableViewer(false, EpState.READ_ONLY, tableLayoutData,
				CoreMessages.get().AboutApplicationDialog_SystemMetrics);

		settingsTableViewer.addTableColumn(CoreMessages.get().AboutApplicationDialog_SystemMetrics_Name_Heading, NAME_COLUMN_WIDTH);
		settingsTableViewer.addTableColumn(CoreMessages.get().AboutApplicationDialog_SystemMetrics_Values_Heading, VALUE_COLUMN_WIDTH);

		settingsTableViewer.setContentProvider(new ArrayContentProvider());
		settingsTableViewer.setLabelProvider(new SettingsLabelProvider());
	}

	/**
	 * Settings label.
	 */
	protected class SettingsLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final SystemInformation systemInformation = (SystemInformation) element;
			switch (columnIndex) {
				case NAME_COLUMN:
					return systemInformation.getName();
				case VALUE_COLUMN:
					return systemInformation.getSimpleValue();
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}

	@Override
	protected String getInitialMessage() {
		return "";
	}

	@Override
	protected String getTitle() {
		return CoreMessages.get().AboutApplicationDialog_Title;
	}

	@Override
	protected Image getWindowImage() {
		return CoreImageRegistry.getImage(CoreImageRegistry.ABOUT_APPLICATION);
	}

	@Override
	protected String getWindowTitle() {
		return CoreMessages.get().AboutApplicationDialog_WindowTitle;
	}

	@Override
	protected String getPluginId() {
		return CorePlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, CoreMessages.get().AbstractEpDialog_ButtonClose, false);
	}

	@Override
	protected void populateControls() {
		applicationTitleText.setText(getVersionService().getApplicationName());
		applicationVendorText.setText(getVersionService().getApplicationVendor());
		applicationVersionText.setText(getVersionService().getApplicationVersion());

		List<SystemInformation> input = getSystemMetrics();
		settingsTableViewer.setInput(input.toArray(new SystemInformation[input.size()]));
	}

	private List<SystemInformation> getSystemMetrics() {
		List<SystemInformation> systemMetrics = getXpfExtensionLookup().getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorAny());
		return systemMetrics;
	}

	/**
	 * Get version logger.
	 *
	 * @return the version logger
	 */
	protected VersionService getVersionService() {
		return BeanLocator.getSingletonBean(ContextIdNames.VERSION_SERVICE, VersionService.class);
	}

	protected XPFExtensionLookup getXpfExtensionLookup() {
		return BeanLocator.getSingletonBean("xpfExtensionLookup", XPFExtensionLookup.class);
	}

}
