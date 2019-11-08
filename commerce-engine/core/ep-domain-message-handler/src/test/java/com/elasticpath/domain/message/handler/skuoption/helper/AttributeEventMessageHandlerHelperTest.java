package com.elasticpath.domain.message.handler.skuoption.helper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.message.handler.attribute.helper.AttributeEventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.attribute.AttributeService;

/**
 * Test for {@link AttributeEventMessageHandlerHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeEventMessageHandlerHelperTest {

	private static final String GUID = "someGuid";

	@Mock
	private AttributeService attributeService;

	@Mock
	private EventMessage eventMessage;

	@InjectMocks
	private AttributeEventMessageHandlerHelper attributeEventMessageHandlerHelper;

	@Test
	public void testThatAttributeEventMessageHandlerHelperCallMethodFindByIdMethodFromAttributeService() {
		when(eventMessage.getGuid()).thenReturn(GUID);

		attributeEventMessageHandlerHelper.getExchangedEntity(eventMessage);

		verify(attributeService).findByKey(GUID);
	}
}
