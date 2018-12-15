/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.email.util.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.util.StoreMessageSource;
import com.elasticpath.commons.util.VelocityEngineInstanceFactory;
import com.elasticpath.domain.store.Store;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.domain.impl.EmailPropertiesImpl;
import com.elasticpath.email.producer.spi.composer.util.EmailContextFactory;
import com.elasticpath.service.catalogview.impl.ThreadLocalStorageImpl;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Test cases for <code>EmailServiceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class LegacyEmailComposerImplTest {

	private LegacyEmailComposerImpl emailComposer;

	private EmailProperties emailProperties;

	@Mock
	private StoreService mockStoreService;
	
	@Mock
	private StoreMessageSource mockStoreMessageSource;

	@Mock
	private EmailContextFactory emailContextFactory;

	@Mock
	private SettingValueProvider<Boolean> emailTextTemplateEnabled;

	@Mock
	private SettingValueProvider<String> emailGlobalSenderAddress;

	@Mock
	private SettingValueProvider<String> emailGlobalSenderName;

	/**
	 * Prepare for the tests.
	 *
	 * @throws Exception on error
	 */
	@Before
	public void setUp() {
		// Create an email service that is stubbed for testing
		emailComposer = new LegacyEmailComposerImpl() {
			@Override
			protected String mergeTemplateIntoString(final String txtTemplate, final Map<String, Object> velocityTemplateResources,
													 final String storeCode) throws VelocityException {
				return "Processed Template";
			}
		};
		emailComposer.setEmailContextFactory(emailContextFactory);
		emailComposer.setStoreConfig(new ThreadLocalStorageImpl());
		emailComposer.setStoreService(mockStoreService);
		emailComposer.setStoreMessageSource(mockStoreMessageSource);
		emailComposer.setVelocityEngineFactory(new VelocityEngineInstanceFactory() {
			/**
			 * stubbed
			 **/
			@Override
			public VelocityEngine getVelocityEngine(final String storeCode) {
				return null;
			}
		});

		emailProperties = createAndInitializeEmailProperties();

		assertThat(emailProperties.getStoreCode()).isNotNull(); // can't be null for tests
	}

	private EmailProperties createAndInitializeEmailProperties() {
		EmailProperties emailProperties = new EmailPropertiesImpl();
		emailProperties.setTextTemplate("text template");
		emailProperties.setHtmlTemplate("html template");
		emailProperties.setDefaultSubject("defaultSubject");
		emailProperties.setStoreCode("test code");
		emailProperties.setTextOnly(true);
		emailProperties.setLocaleDependentSubjectKey(null);
		emailProperties.setEmailLocale(null);

		return emailProperties;
	}

	/**
	 * Test that if the given StoreCode does not map to a Store then an EpServiceException will be thrown.
	 */
	@Test
	public void testSendEmailListStoreSpecificBadStoreCode() {
		emailProperties.setStoreCode("some store");
		// expectations
		when(mockStoreService.findStoreWithCode(emailProperties.getStoreCode())).thenReturn(null);

		assertThatThrownBy(() -> emailComposer.composeMessage(emailProperties))
			.as("emailComposer should throw exception for non-existing store code")
			.isInstanceOf(EpServiceException.class)
			.isNotInstanceOf(EmailException.class);
		verify(mockStoreService).findStoreWithCode(emailProperties.getStoreCode());
	}

	/**
	 * Test method for 'com.elasticpath.service.misc.impl.emailComposerImpl.sendMail(EmailProperties)'.
	 */
	@Test
	public void testComposeTextEmail() {
		// lets change this to make sure we get the right results
		emailProperties.setHtmlTemplate(null);
		emailProperties.setRecipientAddress("whatever@asif.com");

		final Store mockStore = mock(Store.class);
		when(mockStore.getEmailSenderAddress()).thenReturn("test@elasticpath.com");

		when(mockStore.getEmailSenderName()).thenReturn("test");

		when(mockStore.getContentEncoding()).thenReturn("UTF-8");

		when(mockStoreService.findStoreWithCode(emailProperties.getStoreCode())).thenReturn(mockStore);

		when(emailContextFactory.createVelocityContext(mockStore, emailProperties)).thenReturn(Collections.emptyMap());
		assertThatCode(() -> emailComposer.composeMessage(emailProperties)).doesNotThrowAnyException();

		// test non-existing store
		emailProperties.setStoreCode("some store");
		// expectations
		when(mockStoreService.findStoreWithCode(emailProperties.getStoreCode())).thenReturn(null);

		assertThatThrownBy(() -> emailComposer.composeMessage(emailProperties))
			.as("emailComposer should throw exception for non-persisted store code")
			.isInstanceOf(EpServiceException.class)
			.isNotInstanceOf(EmailException.class);
		verify(mockStore).getEmailSenderAddress();
		verify(mockStore).getEmailSenderName();
		verify(mockStore).getContentEncoding();
		verify(mockStoreService).findStoreWithCode(emailProperties.getStoreCode());
		verify(mockStoreService).findStoreWithCode(emailProperties.getStoreCode());
	}

	/**
	 * Test method for 'com.elasticpath.service.misc.impl.emailComposerImpl.sendMail(EmailProperties)'.
	 */
	@Test
	public void testComposeHtmlEmail() throws EmailException {
		emailProperties.setRecipientAddress("whatever@asif.com");
		emailProperties.setTextOnly(false);

		when(emailTextTemplateEnabled.get()).thenReturn(false);

		emailComposer.setEmailTextTemplateEnabledProvider(emailTextTemplateEnabled);

		final Store mockStore = mock(Store.class);
		when(mockStore.getEmailSenderAddress()).thenReturn("test@elasticpath.com");

		when(mockStore.getEmailSenderName()).thenReturn("test");

		when(mockStoreService.findStoreWithCode(emailProperties.getStoreCode())).thenReturn(mockStore);

		when(emailContextFactory.createVelocityContext(mockStore, emailProperties)).thenReturn(Collections.emptyMap());
		emailComposer.composeMessage(emailProperties);
		verify(mockStore).getEmailSenderAddress();
		verify(mockStore).getEmailSenderName();
		verify(mockStoreService).findStoreWithCode(emailProperties.getStoreCode());
	}

	/**
	 * Test method for 'com.elasticpath.service.misc.impl.emailComposerImpl.composeMessage(EmailProperties)'.
	 */
	@Test
	public void testUseGlobalTemplates() throws EmailException {
		emailProperties.setRecipientAddress("whatever@asif.com");
		emailProperties.setStoreCode(null);

		when(emailGlobalSenderAddress.get()).thenReturn("customerService@SomeDomain.com");
		emailComposer.setEmailGlobalSenderAddressProvider(emailGlobalSenderAddress);

		when(emailGlobalSenderName.get()).thenReturn("Customer Service");
		emailComposer.setEmailGlobalSenderNameProvider(emailGlobalSenderName);

		when(emailContextFactory.createVelocityContext(null, emailProperties)).thenReturn(Collections.emptyMap());

		emailComposer.composeMessage(emailProperties);
	}
	/**
	 * Test method for 'com.elasticpath.service.misc.impl.emailComposerImpl.getEmailSubject(EmailProperties)'.
	 */
	@Test
	public void testGetEmailSubject() {
		final String defaultSubject = "my default subject";
		final EmailProperties emailProp = createAndInitializeEmailProperties();

		//only default message
		emailProp.setDefaultSubject(defaultSubject);
		assertThat(emailComposer.getEmailSubject(emailProp))
			.as("No message key, will get the default message")
			.isEqualTo(defaultSubject);

		//property key exists, but message not found
		emailProp.setLocaleDependentSubjectKey("my.english.key");
		emailProp.setEmailLocale(Locale.CANADA);
		when(mockStoreMessageSource.getMessage(emailProp.getStoreCode(), emailProp.getLocaleDependentSubjectKey(), emailProp.getEmailLocale()))
			.thenReturn(null);

		assertThat(emailComposer.getEmailSubject(emailProp))
			.as("Message key exists, but no message found in properties files")
			.isEqualTo(defaultSubject);

		//property key exists, and message found
		final String englishTranslationMessage = "my meesage for en locale";
		when(mockStoreMessageSource.getMessage(emailProp.getStoreCode(), emailProp.getLocaleDependentSubjectKey(), emailProp.getEmailLocale()))
			.thenReturn(englishTranslationMessage);

		assertThat(emailComposer.getEmailSubject(emailProp))
			.as("Message key exists, and message found in properties files")
			.isEqualTo(englishTranslationMessage);
		verify(mockStoreMessageSource, atLeastOnce())
			.getMessage(emailProp.getStoreCode(), emailProp.getLocaleDependentSubjectKey(), emailProp.getEmailLocale());

	}

}
