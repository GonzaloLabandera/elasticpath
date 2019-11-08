package com.elasticpath.domain.message.handler.skuoption.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.AttributeUpdateProcessor;
import com.elasticpath.domain.message.handler.attribute.handler.AttributeDeletedEventHandler;
import com.elasticpath.messaging.EventMessage;

@RunWith(MockitoJUnitRunner.class)
public class AttributeDeletedEventHandlerTest {

	private static final String GUID = "someGuid";

	@Mock
	private AttributeUpdateProcessor attributeUpdateProcessor;

	@Mock
	private EventMessage eventMessage;

	@InjectMocks
	private AttributeDeletedEventHandler attributeDeletedEventHandler;

	@Before
	public void setUp() {
		when(eventMessage.getGuid()).thenReturn(GUID);
	}

	@Test
	public void ensureThatAttributeDeletedHandlerCallMethodProcessAttributeDeleted() {
		attributeDeletedEventHandler.handleMessage(eventMessage);

		verify(attributeUpdateProcessor).processAttributeDeleted(GUID);
	}
}
