/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.cmclient.catalog.policy;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.policy.StateDeterminer;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.domain.modifier.ModifierField;

/**
 * Policy for the Catalog Cart Item Modifier Field Add/Edit dialog.
 */
public class CatalogAddEditCartItemModifierFieldDialogPolicy extends AbstractCatalogDeterminerStatePolicy {

	private ModifierField cartItemModifierField;

	private final Map<String, StateDeterminer> determinerMap = new HashMap<>();

	@Override
	public void init(final Object dependentObject) {
		if (dependentObject instanceof ModifierField) {
			cartItemModifierField = (ModifierField) dependentObject;
		}
	}


	@Override
	protected StateDeterminer getDefaultDeterminer() {
		return new DefaultManageModifierFieldAuthorizationDeterminer();
	}

	@Override
	protected Map<String, StateDeterminer> getDeterminerMap() {
		if (determinerMap.isEmpty()) {
			determinerMap.put("cartItemModifierFieldCodeField", new ModifierFieldCodeAuthorizationDeterminer()); //$NON-NLS-1$
		}
		return determinerMap;
	}

	/**
	 * Determiner for always editable controls.
	 */
	public class DefaultManageModifierFieldAuthorizationDeterminer implements StateDeterminer {

		@Override
		public EpControlFactory.EpState determineState(final PolicyActionContainer targetContainer) {
			return EpControlFactory.EpState.EDITABLE;
		}
	}

	/**
	 * Determines state based on persistent state.
	 */
	public class ModifierFieldCodeAuthorizationDeterminer implements StateDeterminer {

		@Override
		public EpControlFactory.EpState determineState(final PolicyActionContainer targetContainer) {
			if (cartItemModifierField.isPersisted()) {
				return EpControlFactory.EpState.READ_ONLY;
			}
			return EpControlFactory.EpState.EDITABLE;
		}
	}
}
