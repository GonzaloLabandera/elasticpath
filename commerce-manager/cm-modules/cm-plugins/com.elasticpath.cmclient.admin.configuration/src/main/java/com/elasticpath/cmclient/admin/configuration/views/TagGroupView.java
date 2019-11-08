/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.views;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.views.AbstractCmClientView;

/**
 * View to show and allow the manipulation of the available TagGroups.
 */
public class TagGroupView extends AbstractCmClientView {
	/** The view ID. */
	public static final String VIEW_ID = "com.elasticpath.cmclient.admin.configuration.views.TagGroupView"; //$NON-NLS-1$

	private TagGroupModel tagGroupModel;

	/**
	 * The constructor.
	 */
	public TagGroupView() {
		super();
	}

	@Override
	protected void createViewPartControl(final Composite parentComposite) {

		tagGroupModel = new TagGroupModel();

		IEpLayoutComposite parentEpComposite = CompositeFactory.createGridLayoutComposite(parentComposite, 2, false);
		GridLayout gridLayout = (GridLayout) parentEpComposite.getSwtComposite().getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		//create a composite to hold the table element that displays the table,buttons, filter and language selector
		TagGroupComposite tagGroupComposite = createTagGroupComposite(parentEpComposite);

		tagGroupComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		tagGroupModel.registerTagGroupUpdateListener(tagGroupComposite);
	}

	/**
	 * Create the composite within which the setting definition table should be rendered (Left-hand-side).
	 * @param parentComposite the composite within which the created composite should be rendered
	 * @return the created composite
	 */
	protected TagGroupComposite createTagGroupComposite(final IEpLayoutComposite parentComposite) {
		return new TagGroupComposite(parentComposite, tagGroupModel);
	}

	@Override
	protected Object getModel() {
		return tagGroupModel;
	}

	@Override
	public void setFocus() {
		//do nothing.		
	}

	@Override
	protected String getPartId() {
		return VIEW_ID;
	}
}
