/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.admin.users.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

import com.elasticpath.cmclient.admin.users.AdminUsersMessages;
import com.elasticpath.cmclient.core.ServiceLocator;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.security.Activity;
import com.elasticpath.cmclient.core.security.Permission;
import com.elasticpath.cmclient.core.service.PermissionsProvider;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.ui.framework.IEpTreeViewer;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cmuser.UserPermission;
import com.elasticpath.domain.cmuser.UserRole;

/**
 * Contains a TreeViewer on the left and a TableViewer on the right. The TreeViewer shows all the currently-installed plugins (name strings) as nodes
 * in the tree and all the leaves are the permissions defined by each plugin. The TableViewer shows all the Permissions currently assigned to the
 * UserRole that is being edited or created. You can assign new Permissions to the current Role by clicking the ">" button, and you can remove
 * Permissions from the current Role by clicking the "<" button.
 */
@SuppressWarnings({ "PMD.GodClass" })
public class RolePermissionsDualListbox {
	private static final Logger LOG = Logger.getLogger(RolePermissionsDualListbox.class);

	private static final String ROLE_PERMISSIONS_TABLE = "Role Permissions"; //$NON-NLS-1$

	private final UserRole userRole;

	private final Button addButton;

	private final Button removeButton;

	private final IEpTreeViewer availableEpTreeViewer;

	private final IEpTableViewer assignedEpTableViewer;

	private final IEpLayoutComposite controlPane;

	private Map<Activity, Collection<Permission>> knownPermissions;

	private List<Permission> allExtensionPermissions;

	/**
	 * Constructor.
	 * 
	 * @param pageComposite the parent composite
	 * @param userRole the UserRole object
	 */
	public RolePermissionsDualListbox(final IEpLayoutComposite pageComposite, final UserRole userRole) {

		final int numColumns = 3;

		IEpLayoutData fillData = pageComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);
		controlPane = pageComposite.addGridLayoutComposite(numColumns, false, fillData);
		this.userRole = userRole;

		//
		// First Row
		//
		controlPane.addLabelBold(AdminUsersMessages.get().RolePermissions_AvailablePermissions, controlPane.createLayoutData(IEpLayoutData.BEGINNING,
				IEpLayoutData.CENTER));
		controlPane.addEmptyComponent(controlPane.createLayoutData(IEpLayoutData.CENTER, IEpLayoutData.CENTER));
		controlPane.addLabelBold(AdminUsersMessages.get().RolePermissions_AssignedPermissions, controlPane.createLayoutData(IEpLayoutData.BEGINNING,
				IEpLayoutData.CENTER));

		// ---- DOCRolePermissionsDualListbox
		//
		// Second Row
		//
		// List box
		availableEpTreeViewer = controlPane.addTreeViewer(true, EpState.READ_ONLY, fillData);
		
		availableEpTreeViewer.setContentProvider(new AvailablePermissionsContentProvider());
		availableEpTreeViewer.setLabelProvider(new AvailablePermissionsLabelProvider());
		availableEpTreeViewer.getSwtTreeViewer().setSorter(new PermissionsNodeSorter());
		// ---- DOCRolePermissionsDualListbox

		// Composite with buttons
		final IEpLayoutComposite buttonsComposite = CompositeFactory.createGridLayoutComposite(controlPane.getSwtComposite(), 1, true);
		addButton = buttonsComposite.addPushButton(" > ", EpState.EDITABLE, //$NON-NLS-1$
				buttonsComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER, true, false));
		addButton.setToolTipText(CoreMessages.get().button_Add);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(final SelectionEvent event) {
				widgetSelected(event);
			}

			public void widgetSelected(final SelectionEvent event) {
				LOG.debug("ADD button pressed"); //$NON-NLS-1$
				// We can cast to IStructuredSelection since we're using a Structured Viewer.
				// Add the selection to the model
				RolePermissionsDualListbox.this.assignToModel((IStructuredSelection) availableEpTreeViewer.getSwtTreeViewer().getSelection());
				// Reset the input to the contentProvider.
				assignedEpTableViewer.setInput(RolePermissionsDualListbox.this.getAssigned());
			}
		});

		buttonsComposite.addEmptyComponent(controlPane.createLayoutData(IEpLayoutData.CENTER, IEpLayoutData.CENTER));
		removeButton = buttonsComposite.addPushButton("<", EpState.EDITABLE, //$NON-NLS-1$
				buttonsComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER, true, false));
		removeButton.setToolTipText(CoreMessages.get().button_Remove);
		removeButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent event) {
				widgetSelected(event);
			}

			public void widgetSelected(final SelectionEvent event) {
				LOG.debug("REMOVE button pressed"); //$NON-NLS-1$
				// Remove the selection from the model
				RolePermissionsDualListbox.this.removeFromModel((IStructuredSelection) assignedEpTableViewer.getSwtTableViewer().getSelection());
				// Reset the input to the contentProvider.
				assignedEpTableViewer.setInput(RolePermissionsDualListbox.this.getAssigned());
			}
		});

		// List box
		assignedEpTableViewer = controlPane.addTableViewer(true, EpState.EDITABLE, fillData, ROLE_PERMISSIONS_TABLE);
		assignedEpTableViewer.getSwtTable().setHeaderVisible(false);
		assignedEpTableViewer.getSwtTable().setLinesVisible(false);
		assignedEpTableViewer.setContentProvider(new AssignedPermissionsContentProvider());
		assignedEpTableViewer.setLabelProvider(new AssignedPermissionsLabelProvider());
		assignedEpTableViewer.getSwtTableViewer().setSorter(new PermissionsNodeSorter());
	}

	/**
	 * Initialize the viewers by setting their content.
	 */
	public void initialize() {
		availableEpTreeViewer.setInput(getAvailable());
		assignedEpTableViewer.setInput(getAssigned());
	}

	/**
	 * Add the "Available Listbox" selected objects from the UserRole model object.
	 * 
	 * @param selection the selection in the Available listbox
	 */
	protected void assignToModel(final IStructuredSelection selection) {
		for (final Iterator< ? > it = selection.iterator(); it.hasNext();) {
			final Object selectionObject = it.next();
			if (selectionObject instanceof PermissionsNode) {
				PermissionsNode node = (PermissionsNode) selectionObject;
				if (node.isRootNode()) {
					for (PermissionsNode childNode : node.getChildren()) {
						userRole.addUserPermission(childNode.getUserPermission());
					}
				} else {
					userRole.addUserPermission(node.getUserPermission());
				}
			}
		}
	}

	/**
	 * Remove the "Assigned Listbox" selected objects from the UserRole model object.
	 * 
	 * @param selection the selection in the Assigned listbox
	 */
	protected void removeFromModel(final IStructuredSelection selection) {
		for (final Iterator< ? > it = selection.iterator(); it.hasNext();) {
			UserPermission userPermission = ((PermissionsNode) it.next()).getUserPermission();
			userRole.removeUserPermission(userPermission);
		}
	}

	/**
	 * Gets all the Available Permissions in the form of a root Permissions Node.
	 * 
	 * @return available objects to assign
	 */
	public PermissionsNode getAvailable() {
		return getRootPermissionsNode();
	}

	/**
	 * Gets the current UserRole, to which UserPermissions may be assigned.
	 * 
	 * @return the current UserRole
	 */
	public UserRole getAssigned() {
		return userRole;
	}

	/**
	 * Get a root node for the tree, which contains other nodes, each of which contains Permission Strings.
	 * 
	 * @return The root node
	 */
	private PermissionsNode getRootPermissionsNode() {
		knownPermissions = PermissionsProvider.getInstance().getKnownPermissions();
		allExtensionPermissions = new ArrayList<>();
		final PermissionsNode rootNode = new PermissionsNode("root"); //$NON-NLS-1$
		for (final Activity activityName : knownPermissions.keySet()) {
			final PermissionsNode permissionsNode = new PermissionsNode(activityName.getName());
			for (final Permission permission : knownPermissions.get(activityName)) {
				// Convert permission to UserPermission
				final UserPermission userPermission = (UserPermission) ServiceLocator.getService(
						ContextIdNames.USER_PERMISSION);
				userPermission.setAuthority(permission.getKey());
				final PermissionsNode childNode = new PermissionsNode(permission.getName());
				childNode.setUserPermission(userPermission);
				// Add UserPermission to tree
				permissionsNode.addChild(childNode);
				allExtensionPermissions.add(permission);
			}
			rootNode.addChild(permissionsNode);
		}
		return rootNode;
	}

	/**
	 * This class represents a root node in the model that contains permissions.
	 */
	private class PermissionsNode {

		private final List<PermissionsNode> children;

		private final String name;

		private UserPermission userPermission;

		/**
		 * Constructor.
		 * 
		 * @param name the name of this root node.
		 */
		PermissionsNode(final String name) {
			this.name = name;
			children = new ArrayList<>();
		}

		/**
		 *
		 * @return
		 */
		public boolean isRootNode() {
			return userPermission == null;
		}

		/**
		 *
		 * @return
		 */
		public UserPermission getUserPermission() {
			return userPermission;
		}

		/**
		 *
		 * @param userPermission
		 */
		public void setUserPermission(final UserPermission userPermission) {
			this.userPermission = userPermission;
		}

		public String getName() {
			return name;
		}

		public void addChild(final PermissionsNode child) {
			children.add(child);
		}

		public void removeChild(final PermissionsNode child) {
			children.remove(child);
		}

		public PermissionsNode[] getChildren() {
			return children.toArray(new PermissionsNode[children.size()]);
		}

		public boolean hasChildren() {
			return !children.isEmpty();
		}
	}

	/** Provides labels for the Available Permissions. */
	protected class AvailablePermissionsLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		@Override
		public String getText(final Object element) {
			if (element instanceof PermissionsNode) {
				return ((PermissionsNode) element).getName();
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			if (element instanceof PermissionsNode) {
				return ((PermissionsNode) element).getName();
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}
	}

	/** Provides content to the Available Permissions tree. */
	protected class AvailablePermissionsContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof PermissionsNode) {
				return ((PermissionsNode) parentElement).getChildren();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(final Object element) {
			// Not used.
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof PermissionsNode) {
				return ((PermissionsNode) element).hasChildren();
			}
			return false;
		}

		@Override
		public Object[] getElements(final Object inputElement) {
			return this.getChildren(inputElement);
		}

		@Override
		public void dispose() {
			// Do nothing
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// Do nothing
		}
	}

	/**
	 * Provides content to the AssignedPermissions TableViewer.
	 */
	protected class AssignedPermissionsContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
			// Do nothing
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// Do nothing
		}

		/**
		 * Expects the inputElement to be a Collection of <code>UserPermission</code>s.
		 * 
		 * @param inputElement the input to the control
		 * @return the UserRole's Permission Strings
		 */
		public Object[] getElements(final Object inputElement) {
			if (inputElement instanceof UserRole) {
				List<PermissionsNode> wrappedPermissions = new ArrayList<>();
				Set<UserPermission> userPermissions = ((UserRole) inputElement).getUserPermissions();
				for (UserPermission permission : userPermissions) {
					createAndAddWrappedPermissionToList(permission, wrappedPermissions);
				}
				return wrappedPermissions.toArray();
			}
			return new Object[0];
		}

		/**
		 * Used to get the name of the permission as it exists only on the client side.
		 */
		private void createAndAddWrappedPermissionToList(final UserPermission userPermission, final List<PermissionsNode> wrappedPermissions) {
			for (Permission permission : allExtensionPermissions) {
				if (permission.getKey().equals(userPermission.getAuthority())) {
					PermissionsNode permissionsNode = new PermissionsNode(permission.getName());
					permissionsNode.setUserPermission(userPermission);
					wrappedPermissions.add(permissionsNode);
				}
			}
		}
	}

	/**
	 * Provides labels for the AssignedPermissions TableViewer.
	 */
	protected class AssignedPermissionsLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			return ((PermissionsNode) element).getName();
		}

		@Override
		public boolean isLabelProperty(final Object element, final String property) {
			return false;
		}
	}
	
	/**
	 * Sorter for the viewers.
	 */
	public class PermissionsNodeSorter extends ViewerSorter {

		@Override
		public int compare(final Viewer viewer, final Object object1, final Object object2) {
			if (object1 instanceof PermissionsNode && object2 instanceof PermissionsNode) {
				PermissionsNode node1 = (PermissionsNode) object1;
				PermissionsNode node2 = (PermissionsNode) object2;
				return node1.getName().compareTo(node2.getName()); 
			}
			return 0;
		}
	}

	/**
	 *
	 * @param width the width in pixels
	 * @param height the height in pixels
	 */
	public void setPreferredSize(final int width, final int height) {
		GridData gridData = (GridData) controlPane.getSwtComposite().getLayoutData();
		gridData.minimumWidth = width;
		gridData.heightHint = height;
		gridData.widthHint = width;
	}
}


