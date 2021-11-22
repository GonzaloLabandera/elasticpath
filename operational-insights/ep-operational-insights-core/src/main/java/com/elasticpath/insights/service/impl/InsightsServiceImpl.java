/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.service.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.jms.ConnectionFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.pool.impl.DefaultSizeOfEngine;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.jdbc.IsolationLevel;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.InfrastructureProxy;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import com.elasticpath.caching.core.CacheManagerWrapper;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.impl.ConverterUtils;
import com.elasticpath.commons.util.impl.DateUtils;
import com.elasticpath.commons.util.impl.VersionService;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.insights.OperationalInsightsConstants;
import com.elasticpath.insights.domain.RevenueDto;
import com.elasticpath.insights.report.InsightsReportBuilder;
import com.elasticpath.insights.service.InsightsService;
import com.elasticpath.insights.service.NativeDatabaseQueryService;
import com.elasticpath.insights.service.NativeDatabaseQueryServiceManager;
import com.elasticpath.insights.util.DataShapeUtil;
import com.elasticpath.insights.util.DatabaseUtil;
import com.elasticpath.insights.util.MxUtil;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;
import com.elasticpath.service.misc.TimeService;

/**
 * Service for generating a JSON report of operational insights into an Elastic Path Commerce environment.
 */
@SuppressWarnings({"PMD.GodClass"})
public class InsightsServiceImpl implements InsightsService {

	private static final Logger LOG = LoggerFactory.getLogger(InsightsServiceImpl.class);
	private static final long BYTES_PER_MB = 1024 * 1024L;
	private static final long HZ_PER_MHZ = 1024 * 1024L;
	private static final long SECONDS_PER_MINUTE = 60;
	private static final int ALL_LOAD_AVERAGES = 3;
	private static final int INDEX_LOAD_AVERAGE_15_MINS = 2;
	private static final int INDEX_LOAD_AVERAGE_5_MINS = 1;
	private static final int LOAD_AVERAGE_1_MIN = 0;
	private static final String TABLE_RULE = "TRULE";
	private static final int MAX_CACHE_SIZE_SAMPLES = 5;
	private static final long MS_PER_SECOND = 1000;
	private static final long MS_PER_MINUTE = 1000 * 60L;
	private static final String TATTRIBUTE_TABLENAME = "TATTRIBUTE";
	private static final String ATTRIBUTE_USAGE_WHERE_CLAUSE = "ATTRIBUTE_USAGE = ";
	private final String instanceGuid = UUID.randomUUID().toString();
	private final Map<String, String> cacheNameToMetricNameMap = new HashMap<>();
	private BeanFactory beanFactory;
	private CacheManagerWrapper cacheManager;
	private SystemInfo oshi;
	private NativeDatabaseQueryServiceManager nativeDatabaseQueryServiceManager;
	private PersistenceEngine persistenceEngine;
	private TimeService timeService;
	private ConnectionFactory connectionFactory;
	private List<String> sensitiveJvmArgKeyWords;

	/**
	 * Constructor.
	 */
	public InsightsServiceImpl() {
		cacheNameToMetricNameMap.put("skuOptionsCache", "sku-option");
		cacheNameToMetricNameMap.put("productByUidCache", "product");
		cacheNameToMetricNameMap.put("categoryByUidCache", "category");
		cacheNameToMetricNameMap.put("priceListAssignmentCache", "price-list-assignment");
		cacheNameToMetricNameMap.put("baseAmountCache", "base-amount");
		cacheNameToMetricNameMap.put("ruleCache", "rule");
		cacheNameToMetricNameMap.put("decomposedConditionCache", "rule-condition");
		cacheNameToMetricNameMap.put("storeCache", "store");
		cacheNameToMetricNameMap.put("tagDefinitionCache", "tag-definition");
		cacheNameToMetricNameMap.put("taxDocumentCache", "tax-document");
		cacheNameToMetricNameMap.put("cachingAttributeServicefindByKeyCache", "attribute-by-key");
		cacheNameToMetricNameMap.put("cachingAttributeServiceAttributesCache", "attribute-by-usage");
		cacheNameToMetricNameMap.put("attributeValueByAttributeUidCache", "attribute-value");
		cacheNameToMetricNameMap.put("groovyScriptCache", "groovy-script");
	}

	@Override
	public String getReport(final Set<String> zooms, final Set<JSONObject> configurationEpServiceReports,
							final Set<JSONObject> runtimeEpServiceReports) {
		LOG.warn("Creating System Information Report");

		InsightsReportBuilder report = new InsightsReportBuilder();
		report.withTimer();
		report.appendNode("epc-version", this::getEpcVersion);
		report.appendNode("report-date-time", this::getCurrentTime);
		if (zooms.contains(OperationalInsightsConstants.ZOOM_CONFIGURATION)) {
			report.appendNode("configuration", getConfiguration(configurationEpServiceReports));
		}
		if (zooms.contains(OperationalInsightsConstants.ZOOM_RUNTIME)) {
			report.appendNode("runtime", getRuntime(runtimeEpServiceReports));
		}
		if (zooms.contains(OperationalInsightsConstants.ZOOM_DATA_SHAPE)) {
			report.appendNode("data-shape", getDataShape());
		}
		if (zooms.contains(OperationalInsightsConstants.ZOOM_REVENUE)) {
			report.appendNodeArray("revenue", getRevenue());
		}

		return report.buildString();
	}

	private String getCurrentTime() {
		return ConverterUtils.date2String(timeService.getCurrentTime(), DateUtils.DATE_TIME_FORMAT_STRING_LOCAL_TIMEZONE, Locale.getDefault());
	}

	private InsightsReportBuilder getConfiguration(final Set<JSONObject> configurationEpServiceReports) {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("ep-services", getEpServicesReport(configurationEpServiceReports));
		section.appendNode("message-broker", getConfigurationMessageBroker());
		section.appendNode("database", getConfigurationDatabase());
		section.appendNode("settings", getConfigurationSettings());
		return section;
	}

	private InsightsReportBuilder getRuntime(final Set<JSONObject> runtimeEpServiceReports) {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("ep-services", getEpServicesReport(runtimeEpServiceReports));
		section.appendNode("message-broker", getRuntimeMessageBroker());
		section.appendNode("database", getRuntimeDatabase());
		return section;
	}

	@Override
	public String getThisEpServiceConfiguration() {
		LOG.info("Creating Configuration Report for this service");

		InsightsReportBuilder report = getThisEpServiceConfigurationReport();
		report.withTimer();
		return report.buildString();
	}

	@Override
	public String getThisEpServiceRuntime() {
		LOG.info("Creating Runtime Report for this service");

		InsightsReportBuilder report = getThisEpServiceRuntimeReport();
		report.withTimer();
		return report.buildString();
	}

	private String getEpcVersion() {
		VersionService service = beanFactory.getSingletonBean(ContextIdNames.VERSION_SERVICE, VersionService.class);
		return service.getApplicationVersion();
	}

	private boolean isHdsEnabled() {
		HDSSupportBean hdsSupportBean = beanFactory.getSingletonBean("hdsSupportBean", HDSSupportBean.class);
		return hdsSupportBean.isHdsSupportEnabled();
	}

	private InsightsReportBuilder getThisEpServiceConfigurationReport() {
		InsightsReportBuilder report = new InsightsReportBuilder();
		report.appendNode("identification", getServiceIdentification());
		report.appendNode("hardware", getServiceConfigurationHardware());
		report.appendNode("operating-system", getServiceConfigurationOperatingSystem());
		report.appendNode("jvm", getServiceConfigurationJVM());
		report.appendNode("application-server", getServiceConfigurationApplicationServer());
		report.appendNode("db-client", getServiceConfigurationDbClient());
		report.appendNode("jms-client", getServiceConfigurationJmsClient());
		report.appendNode("caching", getServiceConfigurationCaching());
		report.appendNode("search-indexing", getServiceConfigurationSearchIndexing());
		return report;
	}

	private InsightsReportBuilder getThisEpServiceRuntimeReport() {
		InsightsReportBuilder report = new InsightsReportBuilder();
		report.appendNode("identification", getServiceIdentification());
		report.appendNode("hardware", getServiceRuntimeHardware());
		report.appendNode("operating-system", getServiceRuntimeOperatingSystem());
		report.appendNode("jvm", getServiceRuntimeJVM());
		report.appendNode("gc", getServiceRuntimeGC());
		report.appendNode("application-server", getServiceRuntimeApplicationServer());
		report.appendNode("db-client", getServiceRuntimeDbClient());
		report.appendNode("jms-client", getServiceRuntimeJmsClient());
		report.appendNode("caching", getServiceRuntimeCaching());
		report.appendNode("search-indexing", getServiceRuntimeSearchIndexing());
		return report;
	}

	private InsightsReportBuilder getServiceIdentification() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("instance-guid", () -> instanceGuid);
		try {
			VersionService service = beanFactory.getSingletonBean(ContextIdNames.VERSION_SERVICE, VersionService.class);
			section.appendNode("service-type", service::getApplicationName);
			section.appendNode("hostname", () -> oshi.getOperatingSystem().getNetworkParams().getHostName());
			section.appendNode("ipv4-addresses", () -> oshi.getHardware().getNetworkIFs().stream()
					.flatMap(networkIF -> Arrays.stream(networkIF.getIPv4addr()))
					.collect(Collectors.joining(", ")));
			section.appendNode("ipv6-addresses", () -> oshi.getHardware().getNetworkIFs().stream()
					.flatMap(networkIF -> Arrays.stream(networkIF.getIPv6addr()))
					.collect(Collectors.joining(", ")));
		} catch (Exception exception) {
			LOG.error("Unable to complete service identification.", exception);
		}
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationHardware() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("system-manufacturer", () -> oshi.getHardware().getComputerSystem().getManufacturer());
		section.appendNode("system-model", () -> oshi.getHardware().getComputerSystem().getModel());
		section.appendNode("processors-logical", () -> oshi.getHardware().getProcessor().getLogicalProcessorCount());
		section.appendNode("processors-physical", () -> oshi.getHardware().getProcessor().getPhysicalProcessorCount());
		section.appendNode("processor-mhz", () -> oshi.getHardware().getProcessor().getMaxFreq() / HZ_PER_MHZ);
		section.appendNode("memory-total-mb", () -> oshi.getHardware().getMemory().getTotal() / BYTES_PER_MB);
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeHardware() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("memory-available-mb", () -> oshi.getHardware().getMemory().getAvailable() / BYTES_PER_MB);
		double[] systemLoadAverages = oshi.getHardware().getProcessor().getSystemLoadAverage(ALL_LOAD_AVERAGES);
		if (systemLoadAverages.length > LOAD_AVERAGE_1_MIN) {
			section.appendNode("load-average-1-minute", () -> systemLoadAverages[LOAD_AVERAGE_1_MIN]);
		}
		if (systemLoadAverages.length > INDEX_LOAD_AVERAGE_5_MINS) {
			section.appendNode("load-average-5-minutes", () -> systemLoadAverages[INDEX_LOAD_AVERAGE_5_MINS]);
		}
		if (systemLoadAverages.length > INDEX_LOAD_AVERAGE_15_MINS) {
			section.appendNode("load-average-15-minutes", () -> systemLoadAverages[INDEX_LOAD_AVERAGE_15_MINS]);
		}
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationOperatingSystem() {
		InsightsReportBuilder section = new InsightsReportBuilder();

		section.appendNode("os-manufacturer", () -> oshi.getOperatingSystem().getManufacturer());
		section.appendNode("os-family", () -> oshi.getOperatingSystem().getFamily());
		section.appendNode("os-version", () -> oshi.getOperatingSystem().getVersionInfo().getVersion());
		section.appendNode("os-bitness", () -> oshi.getOperatingSystem().getBitness());
		section.appendNode("os-max-files", () -> MxUtil.getSimpleAttribute("java.lang:type=OperatingSystem", "MaxFileDescriptorCount", null));
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeOperatingSystem() {
		InsightsReportBuilder section = new InsightsReportBuilder();

		section.appendNode("os-uptime-mins", () -> oshi.getOperatingSystem().getSystemUptime() / SECONDS_PER_MINUTE);
		section.appendNode("os-thread-count", () -> oshi.getOperatingSystem().getThreadCount());
		section.appendNode("os-process-count", () -> oshi.getOperatingSystem().getProcessCount());
		section.appendNode("os-open-files", () -> MxUtil.getSimpleAttribute("java.lang:type=OperatingSystem", "OpenFileDescriptorCount", null));
		OSProcess currentProcess = oshi.getOperatingSystem().getProcess(oshi.getOperatingSystem().getProcessId());
		section.appendNode("process-id", currentProcess::getProcessID);
		section.appendNode("process-uptime-mins", () -> currentProcess.getUpTime() / MS_PER_MINUTE);
		section.appendNode("process-thread-count", currentProcess::getThreadCount);
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationJVM() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("jvm-name", () -> System.getProperty("java.vm.name"));
		section.appendNode("jvm-vendor", () -> System.getProperty("java.vendor"));
		section.appendNode("jvm-version", () -> System.getProperty("java.version"));
		section.appendNode("jvm-vm-version", () -> System.getProperty("java.vm.version"));
		section.appendNode("jvm-arguments", () -> StringUtils.join(MxUtil.getJvmArguments(sensitiveJvmArgKeyWords), ", "));
		section.appendNode("heap-max-size-mb", () -> Runtime.getRuntime().maxMemory() / BYTES_PER_MB);
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeJVM() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("heap-current-size-mb", () -> Runtime.getRuntime().totalMemory() / BYTES_PER_MB);
		section.appendNode("heap-free-size-mb", () -> Runtime.getRuntime().freeMemory() / BYTES_PER_MB);
		section.appendNode("uptime-mins", () -> MxUtil.getUptimeSeconds() / SECONDS_PER_MINUTE);
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeGC() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		for (GarbageCollectorMXBean gc : MxUtil.getGarbageCollectors()) {
			InsightsReportBuilder gcSection = new InsightsReportBuilder();
			gcSection.appendNode("name", gc::getName);
			gcSection.appendNode("total-collections-count", gc::getCollectionCount);
			gcSection.appendNode("total-collection-time-sec", () -> gc.getCollectionTime() / MS_PER_SECOND);
			section.appendNode(gc.getName().replace(" ", "-").toLowerCase(), gcSection);
		}
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationApplicationServer() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		final String mxObjectName = "Tomcat:type=ThreadPool,name=*";

		if (MxUtil.isTomcat()) {
			section.appendNode("server-name", () -> "Tomcat");
			section.appendNode("server-version", () -> MxUtil.getSimpleAttribute("Tomcat:type=Server", "serverNumber", "unknown"));
			section.appendNode("tomcat-max-connections", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "maxConnections"));
			section.appendNode("tomcat-max-threads", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "maxThreads"));
			section.appendNode("tomcat-min-spare-threads", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "minSpareThreads"));
			section.appendNode("tomcat-accept-count", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "acceptCount"));
		} else {
			section.appendNode("name", () -> "Unknown");
			section.appendNode("version", () -> "Unknown");
		}
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeApplicationServer() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		final String mxObjectName = "Tomcat:type=ThreadPool,name=*";

		if (MxUtil.isTomcat()) {
			section.appendNode("tomcat-current-thread-count", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "currentThreadCount"));
			section.appendNode("tomcat-current-threads-busy", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "currentThreadsBusy"));
		}

		return section;
	}

	private InsightsReportBuilder getServiceConfigurationDbClient() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		final String mxObjectName = "Tomcat:type=DataSource,host=localhost,class=javax.sql.DataSource,name=\"jdbc/epjndi\",context=*";
		try (Connection databaseConnection = getDatabaseConnection()) {

			section.appendNode("driver-name", () -> DatabaseUtil.safeDb(conn -> conn.getMetaData().getDriverName(), databaseConnection));
			section.appendNode("driver-version", () -> DatabaseUtil.safeDb(conn -> conn.getMetaData().getDriverVersion(), databaseConnection));
			section.appendNode("connection-url", () -> DatabaseUtil.safeDb(conn -> conn.getMetaData().getURL(), databaseConnection));
			section.appendNode("connection-properties", () -> MxUtil.getFirstMatchingObjectAttributeString(mxObjectName, "connectionProperties"));

			section.appendNode("pool-max-active", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "maxActive"));
			section.appendNode("pool-min-idle", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "minIdle"));
			section.appendNode("pool-max-idle", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "maxIdle"));
			section.appendNode("pool-max-wait-ms", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "maxWait"));
			section.appendNode("pool-jdbc-interceptors", () -> MxUtil.getFirstMatchingObjectAttributeString(mxObjectName, "jdbcInterceptors"));
			section.appendNode("pool-remove-abandoned", () -> MxUtil.getFirstMatchingObjectAttributeBoolean(mxObjectName, "removeAbandoned"));
			section.appendNode("pool-remove-abandoned-timeout-sec", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName,
					"removeAbandonedTimeout"));
			section.appendNode("pool-log-abandoned", () -> MxUtil.getFirstMatchingObjectAttributeBoolean(mxObjectName, "logAbandoned"));
			section.appendNode("pool-test-while-idle", () -> MxUtil.getFirstMatchingObjectAttributeBoolean(mxObjectName, "testWhileIdle"));
			section.appendNode("pool-validation-interval-ms", () -> MxUtil.getFirstMatchingObjectAttributeLong(mxObjectName, "validationInterval"));
			section.appendNode("pool-validation-query", () -> MxUtil.getFirstMatchingObjectAttributeString(mxObjectName, "validationQuery"));
			section.appendNode("pool-validation-query-timeout-sec", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName,
					"validationQueryTimeout"));
			section.appendNode("pool-validation-login-timeout-sec", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "loginTimeout"));
		} catch (Exception exception) {
			LOG.error("Unable to determine database client configuration.", exception);
		}
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeDbClient() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		final String mxObjectName = "Tomcat:type=DataSource,host=localhost,class=javax.sql.DataSource,name=\"jdbc/epjndi\",context=*";
		section.appendNode("pool-active", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "numActive"));
		section.appendNode("pool-idle", () -> MxUtil.getFirstMatchingObjectAttributeInt(mxObjectName, "numIdle"));
		section.appendNode("remove-abandoned-count", () -> MxUtil.getFirstMatchingObjectAttributeLong(mxObjectName, "removeAbandonedCount"));
		section.appendNode("reconnected-count", () -> MxUtil.getFirstMatchingObjectAttributeLong(mxObjectName, "reconnectedCount"));

		return section;
	}

	private InsightsReportBuilder getServiceConfigurationJmsClient() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		PooledConnectionFactory jmsConnectionFactory = getJmsConnectionFactory();
		if (jmsConnectionFactory != null) {
			section.appendNode("connection-url", () -> jmsConnectionFactory.getProperties().getProperty("brokerURL"));
			section.appendNode("pool-max-connections", () -> jmsConnectionFactory.getProperties().getProperty("maxConnections"));
			section.appendNode("pool-max-threads", () -> jmsConnectionFactory.getProperties().getProperty("maxThreadPoolSize"));
			section.appendNode("pool-max-active-sessions-per-connection",
					() -> jmsConnectionFactory.getProperties().getProperty("maximumActiveSessionPerConnection"));
			section.appendNode("pool-idle-timeout-ms", jmsConnectionFactory::getIdleTimeout);
			section.appendNode("pool-expiry-timeout-ms", jmsConnectionFactory::getExpiryTimeout);
			section.appendNode("pool-connection-timeout-ms", jmsConnectionFactory::getConnectionTimeout);
			section.appendNode("prefetch-policy-queues", () -> jmsConnectionFactory.getProperties().getProperty("prefetchPolicy.queuePrefetch"));
			section.appendNode("redelivery-policy-max-redeliveries",
					() -> jmsConnectionFactory.getProperties().getProperty("redeliveryPolicy.maximumRedeliveries"));
			section.appendNode("redelivery-policy-redelivery-delay",
					() -> jmsConnectionFactory.getProperties().getProperty("redeliveryPolicy.redeliveryDelay"));
			section.appendNode("redelivery-policy-initial-redelivery-delay",
					() -> jmsConnectionFactory.getProperties().getProperty("redeliveryPolicy.initialRedeliveryDelay"));
		}
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeJmsClient() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		PooledConnectionFactory jmsConnectionFactory = getJmsConnectionFactory();
		if (jmsConnectionFactory != null) {
			section.appendNode("pool-current-connections", jmsConnectionFactory::getNumConnections);
		}
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationSearchIndexing() {
		InsightsReportBuilder section = new InsightsReportBuilder();

		section.appendNode("document-creator-max-pool-size", () -> MxUtil.getSimpleAttribute("com.elasticpath.search:bean=DocumentCreator",
				"MaximumPoolSize", null));
		section.appendNode("entity-loader-max-pool-size", () -> MxUtil.getSimpleAttribute("com.elasticpath.search:bean=EntityLoader",
				"MaximumPoolSize", null));
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeSearchIndexing() {
		InsightsReportBuilder section = new InsightsReportBuilder();

		section.appendNode("document-creator-pool-size", () -> MxUtil.getSimpleAttribute("com.elasticpath.search:bean=DocumentCreator", "PoolSize",
				null));
		section.appendNode("entity-loader-pool-size", () -> MxUtil.getSimpleAttribute("com.elasticpath.search:bean=EntityLoader", "PoolSize", null));
		section.appendNode("index-build-status", () -> MxUtil.getSimpleAttribute("com.elasticpath.search:name=IndexingStats", "IndexBuildStatuses",
				null));
		return section;
	}

	private InsightsReportBuilder getDataShape() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		section.appendNode("counts", getDataShapeCounts());
		section.appendNode("sizes", getDataShapeSizes());
		return section;
	}

	private InsightsReportBuilder getDataShapeCounts() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		try (Connection databaseConnection = getDatabaseConnection()) {
			DataShapeUtil dataShapeUtil = new DataShapeUtil(databaseConnection);

			// Catalog Data
			section.appendNode("stores", dataShapeUtil.getTableRowCount("TSTORE"));
			section.appendNode("master-catalogs", dataShapeUtil.getTableRowCount("TCATALOG", "MASTER = TRUE;"));
			section.appendNode("virtual-catalogs", dataShapeUtil.getTableRowCount("TCATALOG", "MASTER = FALSE;"));
			section.appendNode("categories", dataShapeUtil.getTableRowCount("TCATEGORY"));
			section.appendNode("product-types", dataShapeUtil.getTableRowCount("TPRODUCTTYPE"));
			section.appendNode("products", dataShapeUtil.getTableRowCount("TPRODUCT"));
			section.appendNode("skus", dataShapeUtil.getTableRowCount("TPRODUCTSKU"));
			section.appendNode("sku-options", dataShapeUtil.getTableRowCount("TPRODUCTTYPESKUOPTION"));
			section.appendNode("product-associations", dataShapeUtil.getTableRowCount("TPRODUCTASSOCIATION"));
			section.appendNode("shipping-service-levels", dataShapeUtil.getTableRowCount("TSHIPPINGSERVICELEVEL"));
			section.appendNode("sku-attributes", dataShapeUtil.getTableRowCount(TATTRIBUTE_TABLENAME,
					ATTRIBUTE_USAGE_WHERE_CLAUSE + AttributeUsage.PRODUCT));
			section.appendNode("product-attributes", dataShapeUtil.getTableRowCount(TATTRIBUTE_TABLENAME,
					ATTRIBUTE_USAGE_WHERE_CLAUSE + AttributeUsage.SKU));
			section.appendNode("category-attributes", dataShapeUtil.getTableRowCount(TATTRIBUTE_TABLENAME,
					ATTRIBUTE_USAGE_WHERE_CLAUSE + AttributeUsage.CATEGORY));

			// Pricing & Promotions
			section.appendNode("base-amounts", dataShapeUtil.getTableRowCount("TBASEAMOUNT"));
			section.appendNode("price-lists", dataShapeUtil.getTableRowCount("TPRICELIST"));
			section.appendNode("price-list-assignments", dataShapeUtil.getTableRowCount("TPRICELISTASSIGNMENT"));
			section.appendNode("catalog-promotions", dataShapeUtil.getTableRowCount(TABLE_RULE, "CATALOG_UID IS NOT NULL;"));
			section.appendNode("cart-promotions-enabled", dataShapeUtil.getTableRowCount(TABLE_RULE, "STORE_UID IS NOT NULL AND ENABLED = TRUE;"));
			section.appendNode("cart-promotions", dataShapeUtil.getTableRowCount(TABLE_RULE, "STORE_UID IS NOT NULL;"));
			section.appendNode("cart-promotions-enabled", dataShapeUtil.getTableRowCount(TABLE_RULE, "STORE_UID IS NOT NULL AND ENABLED = TRUE;"));
			section.appendNode("coupons", dataShapeUtil.getTableRowCount("TCOUPON"));
			section.appendNode("coupon-usages", dataShapeUtil.getTableRowCount("TCOUPONUSAGE"));

			// Carts & Orders
			section.appendNode("carts", dataShapeUtil.getTableRowCount("TSHOPPINGCART"));
			section.appendNode("orders", dataShapeUtil.getTableRowCount("TORDER"));
			section.appendNode("modifier-fields", dataShapeUtil.getTableRowCount("TMODIFIERFIELD"));
			section.appendNode("order-locks", dataShapeUtil.getTableRowCount("TORDERLOCK"));
			section.appendNode("order-holds", dataShapeUtil.getTableRowCount("TORDERHOLD"));
			section.appendNode("inventory-journal", dataShapeUtil.getTableRowCount("TINVENTORYJOURNAL"));

			// Customers
			section.appendNode("users-registered", dataShapeUtil.getTableRowCount("TCUSTOMER", "CUSTOMER_TYPE = 'REGISTERED_USER'"));
			section.appendNode("users-single-session", dataShapeUtil.getTableRowCount("TCUSTOMER", "CUSTOMER_TYPE = 'SINGLE_SESSION_USER'"));
			section.appendNode("accounts", dataShapeUtil.getTableRowCount("TCUSTOMER", "CUSTOMER_TYPE = 'ACCOUNT'"));
			section.appendNode("data-policy-points", dataShapeUtil.getTableRowCount("TDATAPOLICY"));
			section.appendNode("data-policy-consents", dataShapeUtil.getTableRowCount("TCUSTOMERCONSENT"));
			section.appendNode("customer-groups", dataShapeUtil.getTableRowCount("TCUSTOMERGROUP"));
			section.appendNode("user-profile-attributes", dataShapeUtil.getTableRowCount(TATTRIBUTE_TABLENAME,
					ATTRIBUTE_USAGE_WHERE_CLAUSE + AttributeUsage.USER_PROFILE));
			section.appendNode("account-profile-attributes", dataShapeUtil.getTableRowCount(TATTRIBUTE_TABLENAME,
					ATTRIBUTE_USAGE_WHERE_CLAUSE + AttributeUsage.ACCOUNT_PROFILE));

			// Imports, changesets and audit
			section.appendNode("import-job-status", dataShapeUtil.getTableRowCount("TIMPORTJOBSTATUS"));
			section.appendNode("changesets", dataShapeUtil.getTableRowCount("TCHANGESET"));
			section.appendNode("changeset-objects", dataShapeUtil.getTableRowCount("TOBJECTGROUPMEMBER"));
			section.appendNode("audit-records", dataShapeUtil.getTableRowCount("TDATACHANGED"));

			// Misc
			section.appendNode("outbox-messages", dataShapeUtil.getTableRowCount("TOUTBOXMESSAGE"));
			section.appendNode("oauth-tokens", dataShapeUtil.getTableRowCount("TOAUTHACCESSTOKEN"));

		} catch (SQLException exception) {
			LOG.error("Unable to determine data shape counts.", exception);
		}
		return section;
	}

	private InsightsReportBuilder getDataShapeSizes() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		try (Connection databaseConnection = getDatabaseConnection()) {
			DataShapeUtil dataShapeUtil = new DataShapeUtil(databaseConnection);

			section.appendNode("compiled-rule-storage-kb", dataShapeUtil.getTableSizeKb("TRULESTORAGE"));
			section.appendNode("catalog-projections-storage-kb", dataShapeUtil.getTableSizeKb("TCATALOGPROJECTIONS"));
			section.appendNode("catalog-projections-history-storage-kb", dataShapeUtil.getTableSizeKb("TCATALOGHISTORY"));

		} catch (SQLException exception) {
			LOG.error("Unable to determine data shape sizes.", exception);
		}
		return section;
	}

	private List<InsightsReportBuilder> getRevenue() {
		List<InsightsReportBuilder> revenueSectionList = new ArrayList<>();
		LocalDateTime asOfDate =  LocalDateTime.now().minusYears(1);
		List<RevenueDto> revenueDtoList = getRevenueAsOfDate(asOfDate);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		revenueDtoList.forEach(revenueDto -> {
			InsightsReportBuilder section = new InsightsReportBuilder();
			section.appendNode("store-code", revenueDto::getStoreCode);
			section.appendNode("currency-code", revenueDto::getCurrencyCode);
			section.appendNode("date", () -> dateFormat.format(revenueDto.getOrderDate()));
			section.appendNode("booked-revenue", revenueDto::getBookedRevenue);
			section.appendNode("order-count", revenueDto::getOrderCount);
			revenueSectionList.add(section);
		});

		return revenueSectionList;
	}

	/**
	 * Get order revenue details as of given date.
	 * @param asOfDate the date as of which order revenue details are to be obtained.
	 * @return the order revenue details
	 */
	private List<RevenueDto> getRevenueAsOfDate(final LocalDateTime asOfDate) {
		Optional<NativeDatabaseQueryService> dataServiceOptional = nativeDatabaseQueryServiceManager.getDataService();
		if (dataServiceOptional.isPresent()) {
			return dataServiceOptional.get().getRevenueSinceDate(Date.from(asOfDate.atZone(ZoneId.systemDefault()).toInstant()));
		}

		LOG.warn("Revenue data is only supported for mysql, postgresql and oracle databases currently.");
		return Collections.emptyList();
	}

	private InsightsReportBuilder getEpServicesReport(final Set<JSONObject> configurationEpServiceReports) {
		InsightsReportBuilder section = new InsightsReportBuilder();
		configurationEpServiceReports.forEach(jsonObject -> section.appendNode(
				jsonObject.getJSONObject("identification").getString("instance-guid"), () -> jsonObject));
		return section;
	}

	private InsightsReportBuilder getConfigurationMessageBroker() {
		return new InsightsReportBuilder();
	}

	private InsightsReportBuilder getRuntimeMessageBroker() {
		return new InsightsReportBuilder();
	}

	private InsightsReportBuilder getConfigurationDatabase() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		try (Connection databaseConnection = getDatabaseConnection()) {

			section.appendNode("db-name", () -> DatabaseUtil.safeDb(conn -> conn.getMetaData().getDatabaseProductName(), databaseConnection));
			section.appendNode("db-version", () -> DatabaseUtil.safeDb(conn -> conn.getMetaData().getDatabaseProductVersion(), databaseConnection));

			section.appendNode("compatibility-type", () -> "not implemented");
			section.appendNode("instance-size", () -> "not implemented");
			section.appendNode("isolation-level", () ->
					DatabaseUtil.safeDb(conn -> IsolationLevel.fromConnectionConstant(conn.getTransactionIsolation()).name(), databaseConnection));
			section.appendNode("hds-enabled", this::isHdsEnabled);

			// MySQL Specific
			section.appendNode("adaptive-hash-index", () -> DatabaseUtil.getDatabaseString(databaseConnection,
					"SHOW GLOBAL VARIABLES LIKE 'innodb_adaptive_hash_index'", 2));
			section.appendNode("max-connections", () -> DatabaseUtil.getDatabaseInt(databaseConnection,
					"SHOW GLOBAL VARIABLES LIKE 'max_connections'", 2));
			section.appendNode("innodb-buffer-pool-size-mb", () -> DatabaseUtil.getDatabaseInt(databaseConnection,
					"SHOW GLOBAL VARIABLES LIKE 'innodb_buffer_pool_size'", 2) / BYTES_PER_MB);
			section.appendNode("global-timezone", () -> DatabaseUtil.getDatabaseString(databaseConnection,
					"SELECT @@global.time_zone", 1));
			section.appendNode("session-timezone", () -> DatabaseUtil.getDatabaseString(databaseConnection,
					"SELECT @@session.time_zone", 1));
			section.appendNode("schema-size-kb", () -> DatabaseUtil.getSchemaSizeInKb(databaseConnection));
		} catch (SQLException exception) {
			LOG.error("Unable to determine database configuration.", exception);
		}
		return section;
	}

	private InsightsReportBuilder getRuntimeDatabase() {
		return new InsightsReportBuilder();
	}

	private InsightsReportBuilder getConfigurationSettings() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		try (Connection databaseConnection = getDatabaseConnection()) {
			section.appendNode("commerce-manager-admin-user-enabled", () -> DatabaseUtil.getDatabaseResultPresent(databaseConnection,
					"SELECT STATUS FROM TCMUSER WHERE USER_NAME = 'admin'"));
			section.appendNode("mobee-data-present", () -> DatabaseUtil.getDatabaseResultPresent(databaseConnection,
					"SELECT STORECODE FROM TSTORE WHERE STORECODE = 'mobee'"));
			section.appendNode("changesets-enabled", () -> DatabaseUtil.getDatabaseBoolean(databaseConnection,
					"SELECT DEFAULT_VALUE FROM TSETTINGDEFINITION WHERE PATH = 'COMMERCE/SYSTEM/CHANGESETS/enable'", 1));
		} catch (SQLException exception) {
			LOG.error("Unable to determine configuration settings.", exception);
		}
		return section;
	}

	private InsightsReportBuilder getServiceConfigurationCaching() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		if (getCacheManager() != null) {
			for (Map.Entry<String, String> entry : cacheNameToMetricNameMap.entrySet()) {
				InsightsReportBuilder cacheSection = new InsightsReportBuilder();
				Cache cache = cacheManager.getCache(entry.getKey());
				if (cache == null) {
					LOG.debug("Service cache configuration not found for {}", entry.getKey());
				} else {
					CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
					cacheSection.appendNode("max-entries-local-heap", cacheConfiguration::getMaxEntriesLocalHeap);
					cacheSection.appendNode("time-to-live-sec", cacheConfiguration::getTimeToLiveSeconds);
					cacheSection.appendNode("time-to-idle-sec", cacheConfiguration::getTimeToIdleSeconds);
					section.appendNode(entry.getValue(), cacheSection);
				}
			}
		}
		return section;
	}

	private InsightsReportBuilder getServiceRuntimeCaching() {
		InsightsReportBuilder section = new InsightsReportBuilder();
		if (getCacheManager() != null) {
			for (Map.Entry<String, String> entry : cacheNameToMetricNameMap.entrySet()) {
				InsightsReportBuilder cacheSection = new InsightsReportBuilder();
				Cache cache = getCacheManager().getCache(entry.getKey());
				if (cache == null) {
					LOG.debug("Runtime cache configuration not found for {}", entry.getKey());
				} else {
					CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
					LiveCacheStatistics liveCacheStatistics = cache.getLiveCacheStatistics();
					cacheSection.appendNode("hit-ratio", liveCacheStatistics::getCacheHitRatio);
					cacheSection.appendNode("object-count", liveCacheStatistics::getLocalHeapSize);
					cacheSection.appendNode("expired-count", liveCacheStatistics::getExpiredCount);

					// NOTE: Calling liveCacheStatistics.getLocalHeapSizeInBytes() is a VERY expensive operation (expect each MB
					// to take 1 second to evaluate). Therefore we determine average element size by sizing a random sample
					// of objects from the cache.
					long randomElementSizeInBytes = getAverageElementSizeInBytes(cache);
					cacheSection.appendNode("average-element-heap-use-bytes", () -> randomElementSizeInBytes);
					cacheSection.appendNode("current-heap-use-mb",
							() -> liveCacheStatistics.getLocalHeapSize() == 0 ? 0
									: ((randomElementSizeInBytes * liveCacheStatistics.getLocalHeapSize()) / BYTES_PER_MB));
					cacheSection.appendNode("max-expected-heap-use-mb",
							() -> cacheConfiguration.getMaxEntriesLocalHeap() == 0 ? 0
									: ((randomElementSizeInBytes * cacheConfiguration.getMaxEntriesLocalHeap()) / BYTES_PER_MB));

					section.appendNode(entry.getValue(), cacheSection);
				}
			}
		}
		return section;
	}

	@SuppressWarnings({"rawtypes"})
	private long getAverageElementSizeInBytes(final Cache cache) {
		List keysWithExpiryCheck = cache.getKeysWithExpiryCheck();
		int numSamples = Math.min(keysWithExpiryCheck.size(), MAX_CACHE_SIZE_SAMPLES);
		if (numSamples == 0) {
			return 0;
		}
		long sumSampledCacheSizes = 0;
		DefaultSizeOfEngine defaultSizeOfEngine = new DefaultSizeOfEngine(SizeOfPolicyConfiguration.resolveMaxDepth(cache), false);
		for (int index = 0; index < numSamples; index++) {
			Element element;
			if (keysWithExpiryCheck.size() == 1) {
				element = cache.get(keysWithExpiryCheck.get(0));
			} else {
				int randomIndex = ThreadLocalRandom.current().nextInt(0, keysWithExpiryCheck.size() - 1);
				element = cache.get(keysWithExpiryCheck.get(randomIndex));
			}
			sumSampledCacheSizes += defaultSizeOfEngine.sizeOf(element.getObjectKey(), element, element.getObjectValue()).getCalculated();
		}
		return sumSampledCacheSizes / numSamples;
	}

	/**
	 * Get database connection.
	 *
	 * @return the database connection
	 */
	protected Connection getDatabaseConnection() {
		PersistenceEngine engine = getPersistenceEngine();
		if (engine != null) {
			return engine.getConnection();
		}
		return null;
	}

	/**
	 * Retrieves JMS Connection factory.
	 *
	 * @return the jmsConnectionFactory
	 */
	protected PooledConnectionFactory getJmsConnectionFactory() {
		ConnectionFactory actualConnectionFactory = connectionFactory;
		if (actualConnectionFactory instanceof InfrastructureProxy) {
			actualConnectionFactory = (ConnectionFactory) ((InfrastructureProxy) actualConnectionFactory).getWrappedObject();
		}
		if (actualConnectionFactory instanceof PooledConnectionFactory) {
			return (PooledConnectionFactory) actualConnectionFactory;
		}
		return null;
	}

	/**
	 * Retrieves defaultCacheManagerWrapper bean from beanFactory and assigns to cacheManager.
	 *
	 * @return the cacheManager bean
	 */
	protected CacheManagerWrapper getCacheManager() {
		if (cacheManager == null) {
			try {
				cacheManager = beanFactory.getSingletonBean("defaultCacheManagerWrapper", CacheManagerWrapper.class);
			} catch (NoSuchBeanDefinitionException ex) {
				LOG.debug("No ehCache cache manager detected.");
			}
		}
		return cacheManager;
	}

	public void setCacheManager(final CacheManagerWrapper cacheManager) {
		this.cacheManager = cacheManager;
	}

	protected SystemInfo getOshi() {
		return oshi;
	}

	public void setOshi(final SystemInfo oshi) {
		this.oshi = oshi;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setNativeDatabaseQueryServiceManager(final NativeDatabaseQueryServiceManager nativeDatabaseQueryServiceManager) {
		this.nativeDatabaseQueryServiceManager = nativeDatabaseQueryServiceManager;
	}

	protected NativeDatabaseQueryServiceManager getNativeDatabaseQueryServiceManager() {
		return nativeDatabaseQueryServiceManager;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	protected TimeService getTimeService() {
		return timeService;
	}

	public void setConnectionFactory(final ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	protected ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setSensitiveJvmArgKeyWords(final List<String> sensitiveJvmArgKeyWordList) {
		this.sensitiveJvmArgKeyWords = sensitiveJvmArgKeyWordList;
	}

	protected List<String> getSensitiveJvmArgKeyWords() {
		return sensitiveJvmArgKeyWords;
	}
}
