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
import com.elasticpath.domain.message.handler.attribute.handler.AttributeCreatedEventHandler;
import com.elasticpath.messaging.EventMessage;

/**
 * Test for {@link AttributeCreatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeCreatedEventHandlerTest {

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
	private AttributeCreatedEventHandler attributeCreatedEventHandler;

	@Before
	public void setUp() {
		when(attribute.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(attribute);
	}

	@Test
	public void testThatAttributeCreatedHandlerCallGetExchangedEntityMethod() {
		attributeCreatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void ensureThatAttributeCreatedHandlerCallMethodProcessAttributeCreated() {
		attributeCreatedEventHandler.handleMessage(eventMessage);

		verify(attributeUpdateProcessor).processAttributeCreated(attribute);
	}
}
