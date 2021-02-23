/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesEntity;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.service.permissions.RoleToPermissionsMappingService;

/**
 * Account buyer roles entity repository.
 * @param <E> the entity type.
 */
@Component
public class AccountBuyerRolesEntityRepositoryImpl<E extends AccountBuyerRolesEntity> implements
		Repository<AccountBuyerRolesEntity, AccountBuyerRolesIdentifier> {

	private RoleToPermissionsMappingService roleToPermissionsMappingService;

	@Override
	public Single<AccountBuyerRolesEntity> findOne(final AccountBuyerRolesIdentifier identifier) {
		final AccountBuyerRolesEntity.Builder builder = AccountBuyerRolesEntity.builder();
		builder.addingRoles(String.valueOf(roleToPermissionsMappingService.getDefinedRoleKeys()));
		return Single.just(builder.build());
	}

	@Reference
	public void setRoleToPermissionsMappingService(final RoleToPermissionsMappingService roleToPermissionsMappingService) {
		this.roleToPermissionsMappingService = roleToPermissionsMappingService;
	}
}
