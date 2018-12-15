/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.domain.contentspace.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.contentspace.ContentWrapper;
import com.elasticpath.domain.contentspace.ContentWrapperLoader;
import com.elasticpath.settings.test.support.SimpleSettingValueProvider;

/**
 * Tests the content wrapper repository to ensure that it functions as intended.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentWrapperRepositoryImplTest {

	/** Constant for a template name #1. */
	private static final String TEMPLATE1 = "template1";

	/** Constant for a template name #2. */
	private static final String TEMPLATE2 = "template2";
	
	/** Constant for a template name #3. */
	private static final String TEMPLATE3 = "template3";

	/** Constant for a wrapper Id #1. */
	private static final String CS1 = "cs1";

	/** Constant for a wrapper Id #2. */
	private static final String CS2 = "cs2";
	
	/** Constant for a wrapper Id #3. */
	private static final String CS3 = "cs3";

	/** Constant for a wrapper Id #4. */
	private static final String CS4 = "cs4";

	private ContentWrapperRepositoryImpl repository;
	
	/** Used in the conversion to milliseconds. **/
	private static final long MILLI = 1000;
	
	/** The interval that must elapse before a load is done in seconds. **/
	private static final int LOAD_INTERVAL = 10;

	@Mock
	private ContentWrapperLoader mockLoader;

	/**
	 * Mock missing content wrappers cache.
	 **/
	@Mock
	private Map<String, Long> mockMissingContentWrappers;

	/**
	 * The set up that is required before each test is run.
	 */
	@Before
	public void setUp() {
		repository = new ContentWrapperRepositoryImpl();
		repository.setLoadIntervalProvider(new SimpleSettingValueProvider<>(LOAD_INTERVAL));
		
		//Set the content wrapper repository to use the mock missed cache, and the mock loader
		repository.setMissingContentWrappers(mockMissingContentWrappers);
		repository.setContentWrapperLoader(mockLoader);
	}
	
	/**
	 * Creates a content wrapper Id to content wrapper map for two content wrappers.
	 * @return mapping of content wrapper Id to content wrapper for two objects
	 */
	private Map<String, ContentWrapper> setupTwoContentWrappers() {
		
		//Create two content wrapper objects
		ContentWrapper wrapper1 = new ContentWrapperImpl();
		wrapper1.setWrapperId(CS1);
		wrapper1.setTemplateName(TEMPLATE1);
		ContentWrapper wrapper2 = new ContentWrapperImpl();
		wrapper2.setWrapperId(CS2);
		wrapper2.setTemplateName(TEMPLATE2);
		
		//Add the two content wrapper objects to the map
		Map<String, ContentWrapper> wrappers = new HashMap<>();
		wrappers.put(wrapper1.getWrapperId(), wrapper1);
		wrappers.put(wrapper2.getWrapperId(), wrapper2);
		return wrappers;
	}
	
	/**
	 * Found the content wrapper Id in the cache of previously loaded content wrapper.
	 * Should be removed from the missing content cache if for some reason it is present there.
	 */
	@Test
	public void testFindById() {
		//Create a map that will have two content wrappers within it (with mapped wrapper Id's)
		final Map<String, ContentWrapper> wrappers = setupTwoContentWrappers();

		//The mock loader expects to load the content wrappers, which will return the two wrappers
		//we have already set up, the missed cache will attempt to remove the content wrappers 
		//from within the cache as they are not "missing" (this is to ensure that if previously
		//they were recorded missing that they are not still considered missing).
		when(mockLoader.loadContentWrappers()).thenReturn(wrappers);

		//Invoke the find content wrapper by Id method to find the wrapper with Id "cs1"
		//and ensure that the returned wrapper contains the correct information
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS1);
		assertThat(returnedWrapper.getTemplateName()).isEqualTo(TEMPLATE1);
		verify(mockMissingContentWrappers).remove(CS1);

		//Invoke the find content wrapper by Id method to find the wrapper with Id "cs2"
		//and ensure that the returned wrapper contains the correct information
		returnedWrapper = repository.findContentWrapperById(CS2);
		assertThat(returnedWrapper.getTemplateName()).isEqualTo(TEMPLATE2);
		verify(mockMissingContentWrappers).remove(CS2);

	}

	
	/**
	 * The content wrapper with the Id is not found in the cache of previously loaded content wrappers.
	 * Should first check if the missing cache contains the Id and if it does then check to see if the
	 * entry is expired. If the both of these conditions are true, then will return a null value.
	 */
	@Test
	public void testFindByIdInMissedCacheNotExpired() {
		//Obtain a map of two content wrappers (cs1 and cs2)
		final Map<String, ContentWrapper> twoWrappers = setupTwoContentWrappers();
		//Ensure that when loading content wrappers, two are loaded
		when(mockLoader.loadContentWrappers()).thenReturn(twoWrappers);

		//The missing cache will contain the key for (cs3) and will be a valid entry, thus marking
		//the content wrapper as missing
		when(mockMissingContentWrappers.containsKey(CS3)).thenReturn(true);

		when(mockMissingContentWrappers.get(CS3)).thenReturn(System.currentTimeMillis() + LOAD_INTERVAL * MILLI + 1);

		//If a content wrapper is in the missing cache, and the entry is valid then we should be returned
		//a null content wrapper
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS3);
		assertThat(returnedWrapper).isNull();

		verify(mockLoader).loadContentWrappers();
		verify(mockMissingContentWrappers).containsKey(CS3);
		verify(mockMissingContentWrappers).get(CS3);
	}
	
	/**
	 * The content wrapper with the Id is not found in the cache of previously loaded content wrappers.
	 * Should first check if the missing cache contains the Id and if it does then check to see if the
	 * entry is expired. In the case where the missing cache contains the wrapper Id but the entry has
	 * expired we must reload the content wrappers. If the freshly loaded content wrappers do contain the 
	 * content wrapper Id, then return this wrapper and ensure that if there is a reference to the wrapper 
	 * Id in the missing cache, remove it.
	 */
	@Test
	public void testFindByIdInMissedCacheExpiredFoundOnReload() {
		//Obtain a map of two content wrapper objects
		final Map<String, ContentWrapper> twoWrappers = setupTwoContentWrappers();

		//Obtain a map of two content wrapper objects and add a third
		//content wrapper to it (cs3 in addition to cs1 and cs2)
		final Map<String, ContentWrapper> threeWrappers = setupTwoContentWrappers();
		ContentWrapper wrapper3 = new ContentWrapperImpl();
		wrapper3.setWrapperId(CS3);
		wrapper3.setTemplateName(TEMPLATE3);
		threeWrappers.put(wrapper3.getWrapperId(), wrapper3);

		//The content wrapper will return two wrappers the first time loadContentWrappers method is called,
		//to simulate when a third content wrapper xml file was not present but on the subsequent call it will
		//return three content wrappers simulating a reload where another file was found
		when(mockLoader.loadContentWrappers()).thenReturn(twoWrappers).thenReturn(threeWrappers);

		//The missed cache should contain the wrapper Id for cs3 but it will be an expired entry, this
		//means that a reload of the content wrappers will have to take place
		when(mockMissingContentWrappers.containsKey(CS3)).thenReturn(true);

		when(mockMissingContentWrappers.get(CS3)).thenReturn(System.currentTimeMillis() - LOAD_INTERVAL * MILLI - 1);

		//Invoke the findContentWrapperById method to try to find the content wrapper with Id cs3, since
		//it will be found when the reload occurs it should not be null and contain all of the pertinent
		//information
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS3);
		assertThat(returnedWrapper).isNotNull();
		assertThat(returnedWrapper.getWrapperId()).isEqualTo(CS3);
		assertThat(returnedWrapper.getTemplateName()).isEqualTo(TEMPLATE3);

		verify(mockMissingContentWrappers).containsKey(CS3);
		verify(mockMissingContentWrappers).get(CS3);

		//Since the new content wrapper will be found, the method should be trying to remove the entry
		//if it exists for cs3 from the missed cache
		verify(mockMissingContentWrappers).remove(CS3);
	}
	
	/**
	 * The content wrapper with the Id is not found in the cache of previously loaded content wrappers.
	 * Should first check if the missing cache contains the Id and if it does then check to see if the
	 * entry is expired. In the case where the missing cache contains the wrapper Id but the entry has
	 * expired we must reload the content wrappers. If the freshly loaded content wrappers does NOT 
	 * contain the content wrapper Id, then add the wrapper Id to the cache of missing wrappers and return 
	 * a null content wrapper.
	 */
	@Test
	public void testFindByIdInMissedCacheExpiredNotFoundOnReload() {
		//Obtain a map of two content wrapper objects
		final Map<String, ContentWrapper> twoWrappers = setupTwoContentWrappers();

		//The mock content wrapper loader should on consecutive calls to the loaderContentWrappers method
		//return both times a map of two content wrappers, simulating a case where no additional content
		//wrapper xml files were found
		when(mockLoader.loadContentWrappers()).thenReturn(twoWrappers).thenReturn(twoWrappers);

		//The missing cache should contain the wrapper Id for the desired content wrapper (cs3) and the 
		//entry should be expired meaning that a reload of the content wrappers should occur
		when(mockMissingContentWrappers.containsKey(CS3)).thenReturn(true);

		when(mockMissingContentWrappers.get(CS3)).thenReturn(System.currentTimeMillis() - LOAD_INTERVAL * MILLI - 1);
		//Invoking the findContentWrapperId to find the cs3 content wrapper will return null since the content wrapper was not found
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS3);
		assertThat(returnedWrapper).isNull();

		verify(mockMissingContentWrappers).containsKey(CS3);
		verify(mockMissingContentWrappers).get(CS3);

		//Since the new content wrapper will not be found, the missed cache should insert itself with
		//the content wrapper Id, overriding any old one since it is a map along with a current time stamp (not expired now)
		verify(mockMissingContentWrappers).put(eq(CS3), any(Long.class));

	}
	
	/**
	 * The content wrapper with the Id is not found in the cache of previously loaded content wrappers.
	 * Should first check if the missing cache contains the Id, and if it does then check to see if the
	 * entry is expired. In the case where the missing cache does not contain the content wrapper Id, the
	 * content wrappers are reloaded. If the freshly loaded content wrappers do contain the content wrapper Id,
	 * then return this wrapper and ensure that if there is a reference to the wrapper Id in the missing cache, remove it.
	 */
	@Test
	public void testFindByIdNotInMissedCacheFoundOnReload() {
		//Obtain a map of two content wrapper objects
		final Map<String, ContentWrapper> twoWrappers = setupTwoContentWrappers();

		//Obtain a map of two content wrapper objects and add a third
		//content wrapper to it (cs3 in addition to cs1 and cs2)
		final Map<String, ContentWrapper> wrappers = setupTwoContentWrappers();
		ContentWrapper wrapper3 = new ContentWrapperImpl();
		wrapper3.setWrapperId(CS3);
		wrapper3.setTemplateName(TEMPLATE3);
		wrappers.put(wrapper3.getWrapperId(), wrapper3);

		//The content wrapper will return two wrappers the first time loadContentWrappers method is called,
		//to simulate when a third content wrapper xml file was not present but on the subsequent call it will
		//return three content wrappers simulating a reload where another file was found
		when(mockLoader.loadContentWrappers()).thenReturn(twoWrappers).thenReturn(wrappers);

		//The missing cache should NOT contain the wrapper Id for the desired content wrapper (cs3), and
		//since the new content wrapper file will be found we try to try to remove any stale references to
		//the content wrapper Id in the missing cache if they exist
		when(mockMissingContentWrappers.containsKey(CS3)).thenReturn(false);

		//Invoke the findContentWrapperById to find the content wrapper Id cs3, since it will be found it
		//should not be null and the information should be the same as what we had put in.
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS3);
		assertThat(returnedWrapper).isNotNull();
		assertThat(returnedWrapper.getWrapperId()).isEqualTo(wrapper3.getWrapperId());
		assertThat(returnedWrapper.getTemplateName()).isEqualTo(wrapper3.getTemplateName());

		verify(mockMissingContentWrappers).containsKey(CS3);
		verify(mockMissingContentWrappers).remove(CS3);
	}
	
	/**
	 * The content wrapper with the Id is not found in the cache of previously loaded content wrappers.
	 * Should first check if the missing cache contains the Id, and if it does then check to see if the
	 * entry is expired. In the case where the missing cache does not contain the content wrapper Id, the
	 * content wrappers are reloaded. If the freshly loaded content wrappers does NOT contain the content wrapper Id,
	 * then add the wrapper Id to the cache of missing wrappers and return a null content wrapper.
	 */
	@Test
	public void testFindByIdNotInMissedCacheNotFoundOnReload() {
		//Obtain a map of two content wrapper objects
		final Map<String, ContentWrapper> wrappers = setupTwoContentWrappers();

		//The content wrapper will on consecutive calls return two wrappers
		when(mockLoader.loadContentWrappers()).thenReturn(wrappers);

		//The missing cache will not contain the wrapper Id for cs4 but since that content wrapper will not be found
		//(only cs1 and cs2) will be found, then we must put the cs4 wrapper Id into the missed cache along
		//with the time the load was attempted 
		when(mockMissingContentWrappers.containsKey(CS4)).thenReturn(false);
		//Invoke the findContentWrapperById method that will attempt to find the cs4 wrapper but since
		//this will not be found it will return null
		ContentWrapper returnedWrapper = repository.findContentWrapperById(CS4);
		assertThat(returnedWrapper).isNull();

		verify(mockMissingContentWrappers).containsKey(CS4);
		verify(mockMissingContentWrappers).put(eq(CS4), any(Long.class));
	}

	/**
	 * Test method for hasLoadingIntervalElapsed, which should only return true if the spell 
	 * checking interval has elapsed since the previous rebuild.
	 */
	@Test
	public void testHasLoadingIntervalElapsed() {
		
		// First call to load/reload, lastLoadTime is 0
		assertThat(repository.hasLoadingIntervalElapsed(repository.getLastLoadTime()))
			.as("Loading interval should have elapsed.")
			.isTrue();
		
		// Second, and immediate, call should not trigger reload since interval has not elapsed
		repository.setLastLoadTime(System.currentTimeMillis());
		assertThat(repository.hasLoadingIntervalElapsed(repository.getLastLoadTime()))
			.as("Loading interval should not have elapsed.")
			.isFalse();
		
		// third delayed call should trigger spell checking rebuild
		long delayedTime = System.currentTimeMillis() - LOAD_INTERVAL * MILLI - 1;
		repository.setLastLoadTime(delayedTime);
		assertThat(repository.hasLoadingIntervalElapsed(repository.getLastLoadTime()))
			.as("Loading interval should have elasped.")
			.isTrue();
	}
}
