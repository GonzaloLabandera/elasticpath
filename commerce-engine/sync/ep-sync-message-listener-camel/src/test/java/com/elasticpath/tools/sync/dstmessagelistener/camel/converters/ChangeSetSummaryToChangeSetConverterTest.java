/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.camel.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;

/**
 * Test class for {@link ChangeSetSummaryToChangeSetConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetSummaryToChangeSetConverterTest {

	private static final String CHANGE_SET_GUID = UUID.randomUUID().toString();

	@Mock
	private ChangeSetLoader changeSetLoader;

	@InjectMocks
	private ChangeSetSummaryToChangeSetConverter converter;

	@Test
	public void verifyConverterDelegates() throws Exception {
		final ChangeSet expectedChangeSet = mock(ChangeSet.class);

		final ChangeSetSummaryMessage changeSetSummaryMessage = mock(ChangeSetSummaryMessage.class);

		given(changeSetSummaryMessage.getChangeSetGuid())
				.willReturn(CHANGE_SET_GUID);

		given(changeSetLoader.load(CHANGE_SET_GUID))
				.willReturn(expectedChangeSet);

		final ChangeSet actualChangeSet = converter.convert(changeSetSummaryMessage);

		assertThat(actualChangeSet)
				.isSameAs(expectedChangeSet);
	}

}