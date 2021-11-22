/**
 * Copyright (c) Elastic Path Software Inc., 2011.
 */
package com.elasticpath.inventory.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.elasticpath.commons.util.capabilities.Capabilities;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.inventory.CommandFactory;
import com.elasticpath.inventory.InventoryCommand;
import com.elasticpath.inventory.InventoryDto;
import com.elasticpath.inventory.InventoryFacade;
import com.elasticpath.inventory.InventoryKey;
import com.elasticpath.inventory.strategy.InventoryStrategy;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Out-of-the box implementation of the {@link InventoryFacade}.
 */
public class InventoryFacadeImpl implements InventoryFacade {

	/**
	 * The selected strategy.
	 */
	private InventoryStrategy selectedStrategy;

	/**
	 * The strategies.
	 */
	private Map<String, InventoryStrategy> strategies = new HashMap<>();

	/**
	 * Provides the ID of the selected strategy.
	 */
	private SettingValueProvider<String> inventoryStrategyIdProvider;

	/**
	 * Sets the strategies, that are mapped to their keys.
	 *
	 * @param strategies the strategies
	 */
	public void setStrategies(final Map<String, InventoryStrategy> strategies) {
		this.strategies = strategies;
	}

	/**
	 * Selects the {@link InventoryStrategy} by key, which should be provided through the Settings framework.
	 *
	 * @param strategyId The selected strategy ID, which is the key in the strategies map.
	 * @return the selected strategy
	 * @throws IllegalArgumentException If the given strategyId doesn't exist in the strategies map.
	 */
	public InventoryStrategy selectStrategy(final String strategyId) {
		InventoryStrategy inventoryStrategy = strategies.get(strategyId);
		if (inventoryStrategy == null) {
			throw new IllegalArgumentException("InventoryStrategy with id [" + strategyId + "] does not exist.");
		}
		selectedStrategy = inventoryStrategy;

		return inventoryStrategy;
	}

	@Override
	public void executeInventoryCommand(final InventoryCommand command) {
		getSelectedInventoryStrategy().executeCommand(command);
	}

	@Override
	public CommandFactory getInventoryCommandFactory() {
		return getSelectedInventoryStrategy().getCommandFactory();
	}

	@Override
	public void executeInventoryCommands(final Queue<InventoryCommand> commandQueue) {
		while (!commandQueue.isEmpty()) {
			final InventoryCommand command = commandQueue.poll();
			executeInventoryCommand(command);
		}
	}

	@Override
	public InventoryDto getInventory(final ProductSku productSku, final Long warehouseId) {
		return getSelectedInventoryStrategy().getInventory(productSku, warehouseId);
	}

	@Override
	public InventoryDto getInventory(final String skuCode, final Long warehouseId) {
		return getSelectedInventoryStrategy().getInventory(skuCode, warehouseId);
	}

	@Override
	public InventoryDto getInventory(final InventoryKey inventoryKey) {
		return getSelectedInventoryStrategy().getInventory(inventoryKey.getSkuCode(), inventoryKey.getWarehouseUid());
	}

	@Override
	public Map<InventoryKey, InventoryDto> getInventories(final Set<InventoryKey> inventoryKeys) {
		return getSelectedInventoryStrategy().getInventories(inventoryKeys);
	}

	@Override
	public Map<Long, InventoryDto> getInventoriesForSku(final String skuCode) {
		return getSelectedInventoryStrategy().getInventoriesForSku(skuCode);
	}

	@Override
	public Map<Long, InventoryDto> getInventoriesForSku(final ProductSku productSku) {
		return getSelectedInventoryStrategy().getInventoriesForSku(productSku);
	}

	@Override
	public Map<String, InventoryDto> getInventoriesForSkusInWarehouse(
			final Set<String> skuCodes, final long warehouseUid) {
		return getSelectedInventoryStrategy().getInventoriesForSkusInWarehouse(skuCodes, warehouseUid);
	}

	/**
	 * @return capabilities
	 */
	@Override
	public Capabilities getCapabilities() {
		return getSelectedInventoryStrategy().getCapabilities();
	}

	@Override
	public List<InventoryDto> findLowStockInventories(final Set<String> skuCodes, final long warehouseUid) {
		return getSelectedInventoryStrategy().findLowStockInventories(skuCodes, warehouseUid);
	}

	/**
	 * Gets default or selected inventory strategy.
	 *
	 * @return inventory strategy
	 */
	InventoryStrategy getSelectedInventoryStrategy() {
		synchronized (this) {
			if (selectedStrategy == null) {
				selectedStrategy = selectStrategy(getInventoryStrategyIdProvider().get());
			}
		}

		return selectedStrategy;
	}

	public void setInventoryStrategyIdProvider(final SettingValueProvider<String> inventoryStrategyIdProvider) {
		this.inventoryStrategyIdProvider = inventoryStrategyIdProvider;
	}

	protected SettingValueProvider<String> getInventoryStrategyIdProvider() {
		return inventoryStrategyIdProvider;
	}

}
