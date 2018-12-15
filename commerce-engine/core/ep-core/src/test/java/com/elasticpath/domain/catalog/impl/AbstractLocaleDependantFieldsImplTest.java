/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test <code>AbstractLocaleDependantFieldsImpl</code>.
 */
public class AbstractLocaleDependantFieldsImplTest {

	private AbstractLocaleDependantFieldsImpl localeDependantFields;

	/**
	 * Prepare for tests.
	 * 
	 * @throws Exception in case of error happens
	 */
	@Before
	public void setUp() throws Exception {
		this.localeDependantFields = new AbstractLocaleDependantFieldsImpl() {
			private static final long serialVersionUID = 8573857602031136445L;

			@Override
			public long getUidPk() {
				// Not Testing
				return 0;
			}

			@Override
			public void setUidPk(final long uidPk) {
				// Not Testing
			}

		};
	}

	/**
	 * Test that setUrl converts to lowercase using the locale that's set.
	 */
	@Test
	public void testSetUrl() {
		AbstractLocaleDependantFieldsImpl aldf = new AbstractLocaleDependantFieldsImpl() {
			private static final long serialVersionUID = 3877467599445017179L;

			@Override
			public long getUidPk() {
				// not testing
				return 0;
			}

			@Override
			public void setUidPk(final long uidPk) {
				//not testing
			}
			
			@Override
			public Locale getLocale() {
				return Locale.CANADA;
			}
		};
		final String url = "TestUrl";
		aldf.setUrl(url);
		assertThat(aldf.getUrl()).isEqualTo(url.toLowerCase(Locale.CANADA));
	}
	
	/**
	 * Test that setUrl works with a null value.
	 */
	@Test
	public void testSetUrlNull() {
		AbstractLocaleDependantFieldsImpl aldf = new AbstractLocaleDependantFieldsImpl() {
			private static final long serialVersionUID = 4020829479614729780L;

			@Override
			public long getUidPk() {
				// not testing
				return 0;
			}

			@Override
			public void setUidPk(final long uidPk) {
				//not testing
			}
			
			@Override
			public Locale getLocale() {
				return Locale.CANADA;
			}
		};
		aldf.setUrl(null);
		assertThat(aldf.getUrl()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.LocaleDependantFieldsImpl.setLocale(Locale)'.
	 */
	@Test
	public void testSetLocale() {
		final Locale locale = Locale.CANADA;
		localeDependantFields.setLocale(locale);
		
		assertThat(localeDependantFields.getLocale())
			.isEqualTo(locale)
			.isSameAs(locale);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.LocaleDependantFieldsImpl.setKeyWords(String)'.
	 */
	@Test
	public void testSetKeyWords() {
		final String keyWords = "test key words";
		localeDependantFields.setKeyWords(keyWords);
		assertThat(localeDependantFields.getKeyWords()).isEqualTo(keyWords);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.LocaleDependantFieldsImpl.setDescription(String)'.
	 */
	@Test
	public void testSetDescription() {
		final String desc = "test desc";
		localeDependantFields.setDescription(desc);
		assertThat(localeDependantFields.getDescription()).isEqualTo(desc);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.LocaleDependantFieldsImpl.setTitle(String)'.
	 */
	@Test
	public void testSetTitle() {
		final String title = "test title";
		localeDependantFields.setTitle(title);
		assertThat(localeDependantFields.getTitle()).isEqualTo(title);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.LocaleDependantFieldsImpl.setDisplayName(String)'.
	 */
	@Test
	public void testSetDisplayName() {
		final String displayName = "test title";
		localeDependantFields.setDisplayName(displayName);
		assertThat(localeDependantFields.getDisplayName()).isEqualTo(displayName);
	}
}
