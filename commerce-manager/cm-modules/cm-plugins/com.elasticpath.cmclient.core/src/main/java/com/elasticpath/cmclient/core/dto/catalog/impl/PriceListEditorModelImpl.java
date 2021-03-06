/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.cmclient.core.dto.catalog.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.elasticpath.cmclient.core.dto.catalog.PriceListEditorModel;
import com.elasticpath.common.dto.ChangeSetObjects;
import com.elasticpath.common.dto.category.ChangeSetObjectsImpl;
import com.elasticpath.common.dto.pricing.BaseAmountDTO;
import com.elasticpath.common.dto.pricing.PriceListDescriptorDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Default implementation of PriceListEditorModel. The model manages a snapshot of a Price List retrieved from the database, along
 * with a change set capturing any modifications a client might want to make to the Price List. The model should have no
 * dependencies on any service or view.
 */
public class PriceListEditorModelImpl implements PriceListEditorModel, Serializable {

    private static final long serialVersionUID = 1L;

    private PriceListDescriptorDTO priceListDescriptor;

    /*
     * The list of base amount DTOs should remain unchanged once created, as it is meant to be a snapshot.
     */
    private final List<BaseAmountDTO> baseAmountDtos = new ArrayList<>();

    /*
     * The change set contains required modifications to the snapshot list of base amounts. Management of items in this ChangeSet
     * is required when adding new items. All items in resultant change set should only be in one of the states. i.e. either add
     * or remove, not both.
     */
    private ChangeSetObjects<BaseAmountDTO> changeSet = new ChangeSetObjectsImpl<>();

    /**
     * Constructs the model.
     *
     * @param priceListDescriptor PriceListDescriptor of this Price List
     * @param baseAmounts         collection for this Price List
     */
    public PriceListEditorModelImpl(final PriceListDescriptorDTO priceListDescriptor, final Collection<BaseAmountDTO> baseAmounts) {
        this.priceListDescriptor = priceListDescriptor;
        this.baseAmountDtos.addAll(baseAmounts);
    }

    /**
     * Get the BaseAmounts in this model. This list is generated by a combination/subtraction of the changes with the snapshot
     * list of BaseAmounts.
     *
     * @return full list of BaseAmounts managed by this Price List model.
     */
    public List<BaseAmountDTO> getBaseAmounts() {
        List<BaseAmountDTO> fullBaseAmount = new ArrayList<>();
        fullBaseAmount.addAll(baseAmountDtos);
        for (BaseAmountDTO toRemove : changeSet.getRemovalList()) {
            removeFromListIfExisting(fullBaseAmount, toRemove);
        }
        for (BaseAmountDTO toUpdate : changeSet.getUpdateList()) {
            updateExistingBaseAmount(fullBaseAmount, toUpdate);
        }
        fullBaseAmount.addAll(this.changeSet.getAdditionList());
        return fullBaseAmount;
    }


    /**
     * Get the BaseAmounts in this model. Removed objects are still kept as existing until save action.
     *
     * @return full list of BaseAmounts managed by this Price List model.
     */
    public Collection<BaseAmountDTO> getBaseAmountWithRemoved() {
        Set<BaseAmountDTO> fullBaseAmount = new HashSet<>();
        fullBaseAmount.addAll(baseAmountDtos);
        for (BaseAmountDTO toUpdate : changeSet.getUpdateList()) {
            updateExistingBaseAmount(fullBaseAmount, toUpdate);
        }
        fullBaseAmount.addAll(this.changeSet.getRemovalList());
        fullBaseAmount.addAll(this.changeSet.getAdditionList());
        return fullBaseAmount;
    }

    /**
     * Filters base amounts by given object type.
     *
     * @param businessObjectType business object type
     * @return list of base amounts with given object type
     */
    public List<BaseAmountDTO> filterBaseAmounts(final String businessObjectType) {
        return filterBaseAmounts(businessObjectType, getBaseAmounts());
    }

    /**
     * Filters given base amounts.
     *
     * @param businessObjectType business object type
     * @param baseAmountList     list of base amounts to filter
     * @return filtered list of base amounts
     */
    protected List<BaseAmountDTO> filterBaseAmounts(final String businessObjectType, final List<BaseAmountDTO> baseAmountList) {
        CollectionUtils.filter(baseAmountList, baseAmount -> ((BaseAmountDTO) baseAmount).getObjectType().equals(businessObjectType));

        return baseAmountList;
    }

    @Override
    public boolean hasBaseAmounts() {
        return getNumberOfBaseAmounts() != 0;
    }

    @Override
    public int getNumberOfBaseAmounts() {
        return baseAmountDtos.size() + changeSet.getAdditionList().size() - changeSet.getRemovalList().size();
    }

    /**
     * Update a BaseAmount entry in a list if it exists. If the item is already in the list, replace it with the updated entry.
     * Otherwise simply add.
     *
     * @param baseAmountList list of BaseAmountDTOs
     * @param toUpdate       the updated entry
     */
    protected void updateExistingBaseAmount(final Collection<BaseAmountDTO> baseAmountList, final BaseAmountDTO toUpdate) {
        for (Iterator<BaseAmountDTO> iterator = baseAmountList.iterator(); iterator.hasNext();) {
            BaseAmountDTO item = iterator.next();
            if (StringUtils.isNotEmpty(item.getGuid()) && item.getGuid().equals(toUpdate.getGuid())) {
                iterator.remove();
                continue;
            }
        }
        baseAmountList.add(toUpdate);
    }

    /**
     * Remove an entry from a list if it exists. We must use logical comparisons because items in the list might not have GUIDs.
     *
     * @param baseAmountList list of BaseAmountDTOs
     * @param baseAmountItem item to remove
     * @return boolean true if element was deleted, false in over case
     */
    protected boolean removeFromListIfExisting(final List<BaseAmountDTO> baseAmountList, final BaseAmountDTO baseAmountItem) {
        for (Iterator<BaseAmountDTO> iterator = baseAmountList.iterator(); iterator.hasNext();) {
            if (logicalEquals(iterator.next(), baseAmountItem)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void addBaseAmount(final BaseAmountDTO baseAmountDTO) {
        baseAmountDTO.setPriceListDescriptorGuid(priceListDescriptor.getGuid());
        this.changeSet.addToAdditionList(baseAmountDTO);
    }

    @Override
    public void removeBaseAmount(final BaseAmountDTO baseAmountDTO) {
        removeFromListIfExisting(changeSet.getAdditionList(), baseAmountDTO);
        removeFromListIfExisting(changeSet.getUpdateList(), baseAmountDTO);
        removeFromListIfExisting(changeSet.getRemovalList(), baseAmountDTO);
        this.changeSet.addToRemovalList(baseAmountDTO);
    }

    @Override
    public void removeAllBaseAmounts() {
        for (BaseAmountDTO baseAmountDTO : getBaseAmounts()) {
            removeBaseAmount(baseAmountDTO);
        }
    }

    @Override
    public void updateBaseAmount(final BaseAmountDTO oldBaseAmountDTO, final BaseAmountDTO newBaseAmountDTO) {

        List<BaseAmountDTO> baseAmountList = this.getChangeSet().getAdditionList();
        // check for add list. We should to check for any new created PL, new created PL can be edit also.
        boolean wasFoundAndRemoved = removeFromListIfExisting(baseAmountList, oldBaseAmountDTO);
        if (!wasFoundAndRemoved) { // if not found in add list, then try look in edit list
            baseAmountList = this.getChangeSet().getUpdateList();
            removeFromListIfExisting(baseAmountList, oldBaseAmountDTO);
        }
        baseAmountList.add(newBaseAmountDTO);
    }

    /**
     * Gets base amount by qty.
     * <p>
     * NOTICE: it's not recommended to reimplement this method by calling getBaseAmounts().
     * </p>
     *
     * @param qty the qty
     * @return base amount with given qty
     */
    public BaseAmountDTO getBaseAmountDTO(final int qty) {
        BaseAmountDTO addBaseAmountDTO = getBaseAmountDTO(changeSet.getAdditionList(), qty);
        if (addBaseAmountDTO != null) {
            return addBaseAmountDTO;
        }
        if (getBaseAmountDTO(changeSet.getRemovalList(), qty) != null) {
            return null; // return null if based amount removed but not persisted.
        }
        BaseAmountDTO persistedBaseAmount = getBaseAmountDTO(baseAmountDtos, qty);
        if (persistedBaseAmount != null) {
            return persistedBaseAmount;
        }
        return null;
    }

    /**
     * Returns base amount by quantity.
     *
     * @param baseAmounts list of base amounts
     * @param qty         qty
     * @return base amount with given qty or null if such base amount does not exist
     */
    protected BaseAmountDTO getBaseAmountDTO(final List<BaseAmountDTO> baseAmounts, final int qty) {
        for (BaseAmountDTO baseAmountDTO : baseAmounts) {
            if (baseAmountDTO.getQuantity().intValue() == qty) {
                return baseAmountDTO;
            }
        }
        return null;
    }

    @Override
    public boolean logicalEquals(final BaseAmountDTO dto1, final BaseAmountDTO dto2) {
        return new EqualsBuilder().append(dto1.getObjectGuid(), dto2.getObjectGuid())
                .append(dto1.getObjectType(), dto2.getObjectType())
                .append(dto1.getQuantity(), dto2.getQuantity()).isEquals();
    }

    @Override
    public PriceListDescriptorDTO getPriceListDescriptor() {
        return this.priceListDescriptor;
    }

    @Override
    public void setPriceListDescriptor(final PriceListDescriptorDTO dto) {
        this.priceListDescriptor = dto;
    }

    @Override
    public ChangeSetObjects<BaseAmountDTO> getChangeSet() {
        return this.changeSet;
    }

    @Override
    public boolean isNewlyAdded(final BaseAmountDTO baseAmountDto) {
        return changeSet.getAdditionList().contains(baseAmountDto);
    }

    @Override
    public boolean isDeleted(final BaseAmountDTO baseAmountDto) {
        return changeSet.getRemovalList().contains(baseAmountDto);
    }

    @Override
    public boolean isEdited(final BaseAmountDTO baseAmountDto) {
        return changeSet.getUpdateList().contains(baseAmountDto);
    }

    @Override
    public void setChangeSet(final ChangeSetObjects<BaseAmountDTO> changeSet) {
        this.changeSet = changeSet;
    }

    @Override
    public List<BaseAmountDTO> getRawBaseAmounts() {
        return baseAmountDtos;
    }

}
