/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.epcoretool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.domain.search.UpdateType;
import com.elasticpath.domain.search.impl.IndexNotificationImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceSessionFactory;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.rules.RecompilingRuleEngine;
import com.elasticpath.service.search.IndexBuildStatusService;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.SettingMetadataFactory;
import com.elasticpath.settings.SettingValueFactory;
import com.elasticpath.settings.SettingsService;

/**
 * Bootstrap enough of Elastic Path "core" to use what we need within Maven and provide access to various beans. Much of this is copied from
 * test.application's TestApplicationContext. We start by initializing log4j programatically, then loading our beans via a BeanFactory. BeanFactories
 * do not wire things together by default, so we then override most of the beans marking them as lazily initialized. After that we wire in the data
 * source as specified via Maven configurations.
 */
public class EmbeddedEpCore {

	/** The Spring application context that mimics the platform setup. */
	private ApplicationContext appCtx;

	private XmlBeanFactory beanFactory;

	private static final Logger LOGGER = (Logger) LogManager.getRootLogger();

	/**
	 * The constructor for this will fully initialize a Spring application context with the beans in epcore in it and get the database ready to go.
	 */
	public EmbeddedEpCore() {
		configureLog4j();
		initializeBeanFactory();
		wireDataSource();
		clearJPACache();
	}

	/**
	 * Gets the index search service.
	 *
	 * @return the index search service
	 */
	public IndexSearchService getIndexSearchService() {
		return appCtx.getBean(ContextIdNames.INDEX_SEARCH_SERVICE, IndexSearchService.class);
	}

	/**
	 * Gets the cm user service.
	 *
	 * @return the cm user service
	 */
	public CmUserService getCmUserService() {
		return appCtx.getBean(ContextIdNames.CMUSER_SERVICE, CmUserService.class);
	}

	/**
	 * Gets the index build status service.
	 *
	 * @return the index build status service
	 */
	public IndexBuildStatusService getIndexBuildStatusService() {
		return appCtx.getBean("indexBuildStatusService", IndexBuildStatusService.class);
	}

	/**
	 * Gets the settings service.
	 *
	 * @return the settings service
	 */
	public SettingsService getSettingsService() {
		return appCtx.getBean(ContextIdNames.SETTINGS_SERVICE, SettingsService.class);
	}

	/**
	 * Gets the store service.
	 *
	 * @return the store service
	 */
	public StoreService getStoreService() {
		return appCtx.getBean(ContextIdNames.STORE_SERVICE, StoreService.class);
	}

	/**
	 * Gets the setting value factory.
	 *
	 * @return the setting value factory
	 */
	public SettingValueFactory getSettingValueFactory() {
		return appCtx.getBean("settingValueFactory", SettingValueFactory.class);
	}

	/**
	 * Gets the setting metadata value factory.
	 *
	 * @return the setting metadata value factory
	 */
	public SettingMetadataFactory getSettingMetadataFactory() {
		return appCtx.getBean("settingMetadataFactory", SettingMetadataFactory.class);
	}

	/**
	 * Creates the index notification.
	 *
	 * @param indexType the index type
	 * @param updateType the update type
	 * @return the index notification
	 */
	public IndexNotification createIndexNotification(final IndexType indexType, final UpdateType updateType) {
		/* This is only available from the PrototypeBeanFactory which I do not want to initialize here. */
		IndexNotification notification = new IndexNotificationImpl();

		notification.setIndexType(indexType);
		notification.setUpdateType(updateType);
		return notification;
	}

	/**
	 * Gets the index notification service.
	 *
	 * @return the index notification service
	 */
	public IndexNotificationService getIndexNotificationService() {
		return appCtx.getBean(ContextIdNames.INDEX_NOTIFICATION_SERVICE, IndexNotificationService.class);

	}

	/**
	 * Gets the recompiling rule engine.
	 *
	 * @return the recompiling rule engine
	 */
	public RecompilingRuleEngine getRuleEngine() {
		return appCtx.getBean(ContextIdNames.EP_RULE_ENGINE, RecompilingRuleEngine.class);
	}

	/**
	 * Wire data source.
	 */
	private void wireDataSource() {
		PersistenceEngine persistenceEngine = getPersistenceEngine();
		PersistenceSessionFactory sessionFactory = persistenceEngine.getSessionFactory();

		sessionFactory.setConnectionFactoryName(null);
		sessionFactory.setConnectionDriverName(DataSourceFactory.class.getName());

	}

	/**
	 * Clear jpa cache.
	 */
	private void clearJPACache() {
		PersistenceEngine persistenceEngine = getPersistenceEngine();
		if (persistenceEngine.isCacheEnabled()) {
			persistenceEngine.clearCache();
		}
	}

	private PersistenceEngine getPersistenceEngine() {
		return appCtx.getBean("persistenceEngine", PersistenceEngine.class);
	}

	/**
	 * Lazily initialized beans that are ApplicationContextAware do not have their ApplicationContext injected in them. Therefore we skip over lazily
	 * initializing coreBeanFactory and elasticPath.
	 */
	private void overrideMostDefinitionsToLazy() {
		for (int i = 0; i < beanFactory.getBeanDefinitionNames().length; i++) {
			String name = beanFactory.getBeanDefinitionNames()[i];
			if (name != null) {
				if ("coreBeanFactory".equals(name) || "elasticPath".equals(name)) {
					AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanFactory.getBeanDefinition(name);
					beanDefinition.setLazyInit(false);
				} else if (!name.startsWith("beanRegistrar")) {
					AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanFactory.getBeanDefinition(name);
					beanDefinition.setLazyInit(true);
				}
			}
		}
	}

	/**
	 * Create and initialize the bean factory. Note, that when loading beans through a bean factory, one needs to call refresh after all the loading
	 * is done for Spring to take care of things like autowiring and alike. So after we override definitions to lazy, we call refresh to wire things
	 * up.
	 */
	private void initializeBeanFactory() {

		beanFactory = new XmlBeanFactory(new ClassPathResource("META-INF/spring/elasticpath-core-runtime.xml"));

		overrideMostDefinitionsToLazy();

		appCtx = new GenericApplicationContext(beanFactory);

		((AbstractApplicationContext) appCtx).refresh();

	}

	/**
	 * Configure log4j.
	 */
	private void configureLog4j() {
		
		if (!LOGGER.getAppenders().isEmpty()) {
			return;
		}
		
		PatternLayout patternLayout = PatternLayout.newBuilder().withPattern("%-5p [%t]: %m%n").build();
		ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().setLayout(patternLayout).build();
		LOGGER.addAppender(consoleAppender);
	}

	/**
	 * Release resources.
	 */
	public void close() {
		if (appCtx != null) {
			((GenericApplicationContext) appCtx).close();
		}
	}

}
