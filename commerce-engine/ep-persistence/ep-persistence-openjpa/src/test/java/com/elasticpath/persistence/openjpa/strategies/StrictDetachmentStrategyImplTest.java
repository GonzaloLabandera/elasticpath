package com.elasticpath.persistence.openjpa.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.persistence.openjpa.strategies.impl.StrictDetachmentStrategyImpl;

@RunWith(MockitoJUnitRunner.class)
public class StrictDetachmentStrategyImplTest {

	private final StrictDetachmentStrategyImpl fixture = new StrictDetachmentStrategyImpl();

	@Test
	public void shouldNullStateManagerAndDetachedStateWhenObjectIsPeristenceCapable() {
		PersistenceCapable persistable = mock(PersistenceCapable.class);

		fixture.detach(persistable);

		verify(persistable).pcReplaceStateManager(null);
		verify(persistable).pcSetDetachedState(null);
	}

	@Test
	public void shouldReturnSameObjectWhenObjectIsNotPeristenceCapable() {
		Object nonPersistable = new Object();
		Object result = fixture.detach(nonPersistable);

		assertThat(result)
				.isSameAs(nonPersistable);
	}
}
