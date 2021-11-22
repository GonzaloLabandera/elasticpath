package com.elasticpath.persistence.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleEventFilterTest {
	private static final String GUID_1 = "guid1";
	private static final String GUID_2 = "guid2";

	private final Class<PersistenceCapable> entityClass1 = PersistenceCapable.class;

	private final LifecycleEventFilter lifecycleEventFilter = new LifecycleEventFilter();

	@Before
	public void setup() {
		lifecycleEventFilter.beginTransaction();
	}

	@Test
	public void testThatSameEventsAreDuplicate() {
		boolean duplicate;

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isFalse();

		lifecycleEventFilter.trackProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isTrue();
	}

	@Test
	public void testThatCreatedAndUpdatedEventsAreDuplicate() {
		boolean duplicate;

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isFalse();

		lifecycleEventFilter.trackProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.CREATED, entityClass1, GUID_1);
		assertThat(duplicate).isTrue();
	}

	@Test
	public void testThatDifferentEventsAreNotDuplicates() {
		boolean duplicate;

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isFalse();

		lifecycleEventFilter.trackProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_2);
		assertThat(duplicate).isFalse();
	}

	@Test
	public void testThatSameEventsInDifferentTransactionsAreNotDuplicates() throws Exception {
		boolean duplicate;

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isFalse();

		lifecycleEventFilter.trackProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		lifecycleEventFilter.beginTransaction();

		duplicate = lifecycleEventFilter.wasPreviouslyProcessed(EventActionEnum.UPDATED, entityClass1, GUID_1);
		assertThat(duplicate).isFalse();
	}
}
