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
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.message.handler.attribute.handler.AttributeUpdatedEventHandler;
import com.elasticpath.messaging.EventMessage;

@RunWith(MockitoJUnitRunner.class)
public class AttributeUpdatedEventHandlerTest {

	private static final String GUID = "someGuid";

	@Mock
	private EventMessageHandlerHelper<Attribute> eventMessageHandlerHelper;

	@Mock
	private AttributeUpdateProcessor attributeUpdateProcessor;

	@Mock
	private EventMessage eventMessage;

	@Mock
	private Attribute attribute;

	@InjectMocks
	private AttributeUpdatedEventHandler attributeUpdatedEventHandler;

	@Before
	public void setUp() {
		when(attribute.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(attribute);
	}

	@Test
	public void testThatAttributeUpdatedHandlerCallGetExchangedEntityMethod() {
		attributeUpdatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void ensureThatAttributeUpdatedHandlerCallMethodProcessAttributeUpdated() {
		attributeUpdatedEventHandler.handleMessage(eventMessage);

		verify(attributeUpdateProcessor).processAttributeUpdated(attribute);
	}

}
