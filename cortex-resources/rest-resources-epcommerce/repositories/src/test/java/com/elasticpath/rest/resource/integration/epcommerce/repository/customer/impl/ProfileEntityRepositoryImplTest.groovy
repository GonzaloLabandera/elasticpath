/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl

import static org.mockito.Mockito.CALLS_REAL_METHODS
import static org.mockito.Mockito.mock

import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.core.convert.ConversionService

import com.elasticpath.domain.customer.Customer
import com.elasticpath.rest.ResourceOperationFailure
import com.elasticpath.rest.definition.profiles.ProfileEntity
import com.elasticpath.rest.definition.profiles.ProfileIdentifier
import com.elasticpath.rest.id.type.StringIdentifier
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository

@RunWith(MockitoJUnitRunner)
class ProfileEntityRepositoryImplTest {

	def profileEntity =
			[getFamilyName: {"Sage"},
			 getGivenName : {"Sau"}] as ProfileEntity
	def profileIdentifier =
			[getProfileId: {StringIdentifier.of("my-id")},
			 getScope    : {StringIdentifier.of("my-store")}] as ProfileIdentifier

	@Test
	void shouldFailUpdateProfileWhenCustomerNotFound() {
		def patchedCustomerRepository = [
				updateCustomerAsCompletable: { Customer c ->
					customerFirstName = c.firstName
					customerLastName = c.lastName
					return Completable.complete()
				}, getCustomer             : { String s -> Single.error(ResourceOperationFailure.notFound()) }] as CustomerRepository

		def profileRepository = new ProfileEntityRepositoryImpl([
				customerRepository: patchedCustomerRepository]
		)

		profileRepository.update(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class)
	}

	abstract class NamedCustomer implements Customer {
		String firstName
		String lastName
	}

	@Test
	void shouldUpdateProfile() {
		def testCustomer = mock(NamedCustomer, CALLS_REAL_METHODS)
		def customerFirstName = '', customerLastName = ''

		def patchedCustomerRepository = [
				updateCustomerAsCompletable: { Customer c ->
					customerFirstName = c.firstName
					customerLastName = c.lastName
					return Completable.complete()
				},
				getCustomer                : { String s -> Single.just(testCustomer) }] as CustomerRepository

		def profileRepository = new ProfileEntityRepositoryImpl([
				customerRepository: patchedCustomerRepository
		])

		profileRepository.update(profileEntity, profileIdentifier).subscribe()

		assert 'Sau' == customerFirstName
		assert 'Sage' == customerLastName
	}

	@Test
	void shouldFailGetProfileWhenCustomerNotFoundError() {
		def patchedCustomerRepository = [getCustomer: { String s -> Single.error(ResourceOperationFailure.notFound()) }] as CustomerRepository
		def profileRepository = new ProfileEntityRepositoryImpl([
				customerRepository: patchedCustomerRepository]
		)

		profileRepository.findOne(profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class)
	}

	@Test
	void shouldGetProfile() {
		def profile = proxy(ProfileEntity)
		def patchedCustomerRepository = [getCustomer: { String s -> Single.just(proxy(Customer)) }] as CustomerRepository
		def patchedConversionService = [convert: {Object o, Class c -> profile}] as ConversionService
		def profileRepository = new ProfileEntityRepositoryImpl([
				conversionService : patchedConversionService,
				customerRepository: patchedCustomerRepository]
		)

		assert profile == profileRepository.findOne(profileIdentifier).blockingGet()
	}

	def proxy(Class clazz) {
		new ProxyGenerator().instantiateAggregateFromInterface(clazz)
	}

}