/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.support.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test case for {@link FetchGroupLoadTunerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchGroupLoadTunerImplTest {

	private static final String FETCH_GROUP_1 = "fetchGroup1";
	private static final String FETCH_GROUP_2 = "fetchGroup2";

	@Mock
	private FetchPlan mockFetchPlan;

	private final FetchGroupLoadTunerImpl fetchGroupLoadTunerImpl = new FetchGroupLoadTunerImpl();
	
	/**
	 * Test method for {@link FetchGroupLoadTunerImpl#addFetchGroup(String...)}.
	 */
	@Test
	public void testAddFetchGroups() {
		final String fg1 = "234";
		final String fg2 = "sldjfsdklf";
		
		fetchGroupLoadTunerImpl.addFetchGroup(fg1, fg2);
		
		boolean containsFG1 = false;
		for (String fetchGroup : fetchGroupLoadTunerImpl) {
			if (fetchGroup.equals(fg1)) {
				containsFG1 = true;
			}
		}
		assertTrue(containsFG1);
		
		boolean containsFG2 = false;
		for (String fetchGroup : fetchGroupLoadTunerImpl) {
			if (fetchGroup.equals(fg2)) {
				containsFG2 = true;
			}
		}
		assertTrue(containsFG2);
	}
	
	/**
	 * Test method for {@link FetchGroupLoadTunerImpl#removeFetchGroup(String...)}.
	 */
	@Test
	public void testRemoveFetchGroups() {
		final String fg1 = "234";
		final String fg2 = "sldjfsdklf";
		
		fetchGroupLoadTunerImpl.addFetchGroup(fg1, fg2);
		fetchGroupLoadTunerImpl.removeFetchGroup(fg1);
		
		boolean containsFG1 = false;
		for (String fetchGroup : fetchGroupLoadTunerImpl) {
			if (fetchGroup.equals(fg1)) {
				containsFG1 = true;
			}
		}
		assertFalse(containsFG1);
		
		boolean containsFG2 = false;
		for (String fetchGroup : fetchGroupLoadTunerImpl) {
			if (fetchGroup.equals(fg2)) {
				containsFG2 = true;
			}
		}
		assertTrue(containsFG2);
	}

	@Test
	public void shouldConfigureWithFetchGroupWithoutCleaningFetchPlan() {
		fetchGroupLoadTunerImpl.setCleanExistingGroups(false);

		fetchGroupLoadTunerImpl.addFetchGroup(FETCH_GROUP_1);
		fetchGroupLoadTunerImpl.addFetchGroup(FETCH_GROUP_2);

		fetchGroupLoadTunerImpl.configure(mockFetchPlan);

		verify(mockFetchPlan).addFetchGroup(FETCH_GROUP_1);
		verify(mockFetchPlan).addFetchGroup(FETCH_GROUP_2);
		verify(mockFetchPlan, never()).clearFetchGroups();
		verify(mockFetchPlan, never()).removeFetchGroup(FetchGroup.NAME_DEFAULT);
	}

	@Test
	public void shouldConfigureWithFetchGroupWithCleaningFetchPlan() {
		fetchGroupLoadTunerImpl.addFetchGroup(FETCH_GROUP_1);
		fetchGroupLoadTunerImpl.addFetchGroup(FETCH_GROUP_2);

		fetchGroupLoadTunerImpl.configure(mockFetchPlan);

		verify(mockFetchPlan).addFetchGroup(FETCH_GROUP_1);
		verify(mockFetchPlan).addFetchGroup(FETCH_GROUP_2);
		verify(mockFetchPlan).clearFetchGroups();
		verify(mockFetchPlan).removeFetchGroup(FetchGroup.NAME_DEFAULT);
	}
}
