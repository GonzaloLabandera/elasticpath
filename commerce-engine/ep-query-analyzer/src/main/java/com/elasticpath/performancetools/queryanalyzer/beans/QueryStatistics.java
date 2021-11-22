/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.beans;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CSV_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.JSON_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.DELETE_TABLE_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.INSERT_TABLE_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.JPA_ENTITY_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.QUERY_TABLE_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.TIMESTAMP_FORMAT_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.UPDATE_TABLE_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Utils.getOutputFileExtension;
import static com.elasticpath.performancetools.queryanalyzer.utils.Utils.getOutputFileIfEnabled;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static org.apache.commons.lang3.StringUtils.joinWith;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.performancetools.queryanalyzer.QueryAnalyzerConfigurator;
import com.elasticpath.performancetools.queryanalyzer.exceptions.QueryAnalyzerException;
import com.elasticpath.performancetools.queryanalyzer.parser.chain.UnableToSerializeStatisticsException;
import com.elasticpath.performancetools.queryanalyzer.utils.CollectionUtils;

/**
 * Query statistics representation.
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyFields", "PMD.GodClass", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public final class QueryStatistics implements Serializable {
	/**Serial version.*/
	public static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(QueryStatistics.class);

	private static final long MINIMUM_OPERATION_DURATION_THRESHOLD_MS = 2L;
	private static final int MILLIS_TOP_BOUNDARY = 999;
	private static final int ONE_SECOND_MILLIS = 1000;
	private static final int SECONDS_TOP_BOUNDARY = 59;
	private static final int ONE_MINUTE_SECONDS = 60;

	private Integer totalNumberOfStatements;
	private Integer totalDBQueries;
	private Integer totalDBUpdates;
	/*
		This counter counts only executed (real) insert statements.
	 */
	private Integer totalDBInserts;
	/*
		Unlike "totalDBInserts" counter, this one comprises executed and batched insert statements, but not executed batch statement.
		The OpenJPA may batch the statements differently based on internal algorithm affecting the total number of inserts
		used for comparison in e.g. performance tests.

		Unfortunately, OpenJPA doesn't enlist all insert values when executing a batch statement, only the last batched one.
	 */
	private transient int totalExecutedAndBatchedDBInserts;
	private Integer totalDBDeletes;
	private Integer totalJPACalls;
	private Integer totalDbExeTimeMs;

	//not used, but required by JSON serializer
	private String totalExecutionTimeFormatted;
	private String totalExecutionTimeWithoutDbCallsFormatted;
	private String totalExecutionTimeOfOperationsWithJpa;
	private String totalExecutionTimeOfOperationsWithoutJpa;

	private Integer totalNumberOfOperations;
	private Integer totalNumberOfOperationsWithJPA;
	private Integer totalNumberOfOperationsWithoutJPA;

	private transient Long totalExeTime;

	private final Map<String, Integer> totalDBQueriesPerTable = new LinkedHashMap<>();
	private final Map<String, Integer> totalDBUpdatesPerTable = new LinkedHashMap<>();
	private final Map<String, Integer> totalDBInsertsPerTable = new LinkedHashMap<>();
	private final Map<String, Integer> totalDBDeletesPerTable = new LinkedHashMap<>();

	private final Map<String, Integer> totalJPACallsPerEntity = new LinkedHashMap<>();

	private final Map<String, Integer> totalDBCallsPerOperation = new LinkedHashMap<>();
	private final Map<String, Integer> totalJPACallsPerOperation = new LinkedHashMap<>();

	private final Map<String, Integer> totalDBCallExeTimePerOperationMs = new LinkedHashMap<>();
	private final Map<Long, Integer> totalOperationsPerDuration = new LinkedHashMap<>();

	//all captured operations with JPA and SQL queries
	private final List<Operation> operations = new ArrayList<>();
	private final List<Operation> operationsWithJPADesc = new ArrayList<>();
	private final List<Operation> operationsWithoutJPA = new ArrayList<>();

	private transient ObjectMapper jsonMapper = new ObjectMapper();
	private transient QueryAnalyzerConfigurator qaConfigurator = QueryAnalyzerConfigurator.INSTANCE;

	/**
	 * Default constructor used for Jackson configuration as well as obtaining the reference to an
	 * output JSON file.
	 */
	public QueryStatistics() {
		init();
	}

	/**
	 * Init ObjectMapper and QueryAnalyzerConfigurator instances.
	 */
	public void init() {
		//both transient fields will be null when QueryStatistics is restored from the object stream
		//must be initialized here so the tests can pass
		if (jsonMapper == null) {
			jsonMapper = new ObjectMapper();

			jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
			jsonMapper.setDateFormat(new SimpleDateFormat(TIMESTAMP_FORMAT_PATTERN, Locale.getDefault()));
		}
		if (qaConfigurator == null) {
			qaConfigurator = QueryAnalyzerConfigurator.INSTANCE;
		}
	}

	@JsonIgnore
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * Add new operation to the collection of operations.
	 *
	 * @param operation a new operation.
	 */
	public void addOperation(final Operation operation) {
		this.operations.add(operation);
	}

	public List<Operation> getOperationsWithJPADesc() {
		return operationsWithJPADesc;
	}

	public List<Operation> getOperationsWithoutJPA() {
		return operationsWithoutJPA;
	}

	/**
	 * Add operation without JPA to the collection of operations without JPA queries.
	 *
	 * @param operationWithoutJPA an operation without JPA queries.
	 */
	public void addOperationWithoutJPA(final Operation operationWithoutJPA) {
		this.operationsWithoutJPA.add(operationWithoutJPA);
	}

	/**
	 * Add operation with JPA to the collection of operations with JPA queries.
	 *
	 * @param operationWithJPA an operation with JPA queries.
	 */
	public void addOperationWithJPA(final Operation operationWithJPA) {
		this.operationsWithJPADesc.add(operationWithJPA);
	}

	public Map<String, Integer> getTotalDBQueriesPerTable() {
		return totalDBQueriesPerTable;
	}
	public Map<String, Integer> getTotalDBUpdatesPerTable() {
		return totalDBUpdatesPerTable;
	}
	public Map<String, Integer> getTotalDBInsertsPerTable() {
		return totalDBInsertsPerTable;
	}
	public Map<String, Integer> getTotalDBDeletesPerTable() {
		return totalDBDeletesPerTable;
	}

	public Map<String, Integer> getTotalJPACallsPerEntity() {
		return totalJPACallsPerEntity;
	}

	public Map<String, Integer> getTotalDBCallsPerOperation() {
		return totalDBCallsPerOperation;
	}

	public Map<String, Integer> getTotalJPACallsPerOperation() {
		return totalJPACallsPerOperation;
	}

	public Map<String, Integer> getTotalDBCallExeTimePerOperationMs() {
		return totalDBCallExeTimePerOperationMs;
	}

	public Integer getTotalDBQueries() {
		return totalDBQueries;
	}

	public void setTotalDBQueries(final Integer totalDBQueries) {
		this.totalDBQueries = totalDBQueries;
	}

	public Integer getTotalDBUpdates() {
		return totalDBUpdates;
	}

	public void setTotalDBUpdates(final Integer totalDBUpdates) {
		this.totalDBUpdates = totalDBUpdates;
	}

	public Integer getTotalDBInserts() {
		return totalDBInserts;
	}

	public void setTotalDBInserts(final Integer totalDBInserts) {
		this.totalDBInserts = totalDBInserts;
	}

	public Integer getTotalDBDeletes() {
		return totalDBDeletes;
	}

	public void setTotalDBDeletes(final Integer totalDBDeletes) {
		this.totalDBDeletes = totalDBDeletes;
	}

	public Integer getTotalJPACalls() {
		return totalJPACalls;
	}

	public void setTotalJPACalls(final Integer totalJPACalls) {
		this.totalJPACalls = totalJPACalls;
	}

	public Integer getTotalDbExeTimeMs() {
		return totalDbExeTimeMs;
	}

	public void setTotalDbExeTimeMs(final Integer totalDbExeTimeMs) {
		this.totalDbExeTimeMs = totalDbExeTimeMs;
	}

	/**
	 * Return formatted total execution time of all operations (db execution time included).
	 *
	 * @return formatted total execution time.
	 */
	public String getTotalExecutionTimeFormatted() {
		return getFormattedTime(totalExeTime);
	}

	public void setTotalExecutionTime(final Long totalExecutionTime) {
		totalExeTime = totalExecutionTime;
	}

	public Integer getTotalNumberOfStatements() {
		return totalDBQueries + totalDBUpdates + totalDBInserts + totalDBDeletes;
	}


	/**
	 * Return formatted total execution time spent in execution of operations,
	 * without counting db execution time.
	 *
	 * @return formatted time.
	 */
	public String getTotalExecutionTimeWithoutDbCallsFormatted() {
		final long diffWithoutDbCalls = totalExeTime - totalDbExeTimeMs;
		return getFormattedTime(diffWithoutDbCalls);
	}

	/**
	 * Sum all operation (those that make JPA calls) duration times.
	 *
	 * @return Total execution time for all JPA-based operations
	 */
	public String getTotalExecutionTimeOfOperationsWithJpa() {
		return getFormattedTime(operationsWithJPADesc.stream()
				.mapToLong(Operation::getTotalOpDurationMs)
				.sum());
	}

	/**
	 * Sum all operation (those that do not make JPA calls) duration times.
	 *
	 * @return Total execution time for all non-JPA-based operations
	 */
	public String getTotalExecutionTimeOfOperationsWithoutJpa() {
		return getFormattedTime(operationsWithoutJPA.stream()
				.mapToLong(Operation::getTotalOpDurationMs)
				.sum());
	}

	/**
	 * Format given millis into more human-readable format
	 * MM minutes SS seconds MM millis.
	 *
	 * @param exeTime execution time in milliseconds
	 * @return formatted time
	 */
	public String getFormattedTime(final Long exeTime) {
		long seconds = 0L;
		long millisReminder = exeTime;
		long minutes = 0L;

		if (exeTime > MILLIS_TOP_BOUNDARY) {
			seconds = exeTime / ONE_SECOND_MILLIS;
			millisReminder = exeTime % ONE_SECOND_MILLIS;

			if (seconds > SECONDS_TOP_BOUNDARY) {
				minutes = seconds / ONE_MINUTE_SECONDS;
				seconds = seconds % ONE_MINUTE_SECONDS;
			}
		}
		return String.format("%d minute(s) %d second(s) %d millis", minutes, seconds, millisReminder);
	}

	public Integer getTotalNumberOfOperations() {
		return totalNumberOfOperations;
	}

	public void setTotalNumberOfOperations(final Integer totalNumberOfOperations) {
		this.totalNumberOfOperations = totalNumberOfOperations;
	}

	public Integer getTotalNumberOfOperationsWithJPA() {
		return totalNumberOfOperationsWithJPA;
	}

	public void setTotalNumberOfOperationsWithJPA(final Integer totalNumberOfOperationsWithJPA) {
		this.totalNumberOfOperationsWithJPA = totalNumberOfOperationsWithJPA;
	}

	public Map<Long, Integer> getTotalOperationsPerDuration() {
		return totalOperationsPerDuration;
	}

	public Integer getTotalNumberOfOperationsWithoutJPA() {
		return totalNumberOfOperationsWithoutJPA;
	}

	public void setTotalNumberOfOperationsWithoutJPA(final Integer totalNumberOfOperationsWithoutJPA) {
		this.totalNumberOfOperationsWithoutJPA = totalNumberOfOperationsWithoutJPA;
	}

	/**
	 * Generates db statistics and saves to JSON format.
	 * @return this instance
	 */
	public QueryStatistics generateStatistics() {
		final Map<String, Integer> innerDotalDBQueriesPerTable = getTotalDBQueriesPerTable();
		final Map<String, Integer> innerTotalDBUpdatesPerTable = getTotalDBUpdatesPerTable();
		final Map<String, Integer> innerTotalDBInsertsPerTable = getTotalDBInsertsPerTable();
		final Map<String, Integer> innerTotalDBDeletesPerTable = getTotalDBDeletesPerTable();

		final Map<String, Integer> innerTotalJPACallsPerEntity = getTotalJPACallsPerEntity();

		final Map<String, Integer> innerTotalDBCallsPerOperation = getTotalDBCallsPerOperation();
		final Map<String, Integer> innerTotalJPACallsPerOperation = getTotalJPACallsPerOperation();

		final Map<String, Integer> innerTotalDBCallExeTimePerOperation = getTotalDBCallExeTimePerOperationMs();
		final Map<Long, Integer> innerTotalOperationsPerDuration = getTotalOperationsPerDuration();

		final List<Operation> innerOperations = getOperations();

		setTotalNumberOfOperations(innerOperations.size());

		MutableLong totalExecutionTime = new MutableLong();

		for (Operation operation : innerOperations) {
			if (!(operation.getTotalOpDurationMs() < MINIMUM_OPERATION_DURATION_THRESHOLD_MS && operation.getJpaQueries().isEmpty())) {

				if (operation.getJpaQueries().isEmpty()) {
					addOperationWithoutJPA(operation);
				} else {
					addOperationWithJPA(operation);
				}

				innerTotalOperationsPerDuration.merge(operation.getTotalOpDurationMs(), 1, Integer::sum);
				totalExecutionTime.add(operation.getTotalOpDurationMs());

				final Map<String, Integer> totalDBQueriesPerTablePerOperation = operation.getTotalDBQueriesPerTable();
				final Map<String, Integer> totalDBUpdatesPerTablePerOperation = operation.getTotalDBUpdatesPerTable();
				final Map<String, Integer> totalDBInsertsPerTablePerOperation = operation.getTotalDBInsertsPerTable();
				final Map<String, Integer> totalDBDeletesPerTablePerOperation = operation.getTotalDBDeletesPerTable();

				final Map<String, Integer> totalJPACallsPerEntityPerOperation = operation.getTotalJPACallsPerEntity();

				String operationURI = operation.getUri();
				int totalDBCallExeTime = 0;

				for (JPAQuery jpaQuery : operation.getJpaQueries()) {
					CollectionUtils.updateTotalCallsPerOperation(JPA_ENTITY_PATTERN.matcher(jpaQuery.getQuery()),
							totalJPACallsPerEntityPerOperation,
							innerTotalJPACallsPerEntity);
					for (SQLQuery sqlQuery : jpaQuery.getSqlQueries()) {
						CollectionUtils.updateTotalCallsPerOperation(QUERY_TABLE_PATTERN.matcher(sqlQuery.getStatement()),
								totalDBQueriesPerTablePerOperation,
								innerDotalDBQueriesPerTable);
						totalDBCallExeTime += sqlQuery.getExeTimeMs();
					}

					for (DbStatement update : jpaQuery.getSqlUpdates()) {
						CollectionUtils.updateTotalCallsPerOperation(UPDATE_TABLE_PATTERN.matcher(update.getStatement()),
								totalDBUpdatesPerTablePerOperation,
								innerTotalDBUpdatesPerTable);
						totalDBCallExeTime += update.getExeTimeMs();
					}

					for (DbStatement insert : jpaQuery.getSqlInserts()) {
						CollectionUtils.updateTotalCallsPerOperation(INSERT_TABLE_PATTERN.matcher(insert.getStatement()),
								totalDBInsertsPerTablePerOperation,
								innerTotalDBInsertsPerTable);
						this.totalExecutedAndBatchedDBInserts += jpaQuery.getExecutedAndBatchedSqlInserts();
						totalDBCallExeTime += insert.getExeTimeMs();
					}

					if ("SQL INSERT".equals(jpaQuery.getQuery()) && jpaQuery.getSqlInserts().isEmpty()) {
						this.totalExecutedAndBatchedDBInserts += jpaQuery.getExecutedAndBatchedSqlInserts();
					}

					for (DbStatement delete : jpaQuery.getSqlDeletes()) {
						CollectionUtils.updateTotalCallsPerOperation(DELETE_TABLE_PATTERN.matcher(delete.getStatement()),
								totalDBDeletesPerTablePerOperation,
								innerTotalDBDeletesPerTable);
						totalDBCallExeTime += delete.getExeTimeMs();
					}

				}

				int sum = totalDBQueriesPerTablePerOperation.values().stream().mapToInt(Integer::intValue).sum();

				if (sum > 0) {
					innerTotalDBCallsPerOperation.put(operationURI, sum);
				}
				sum = totalJPACallsPerEntityPerOperation.values().stream().mapToInt(Integer::intValue).sum();
				if (sum > 0) {
					innerTotalJPACallsPerOperation.put(operationURI, sum);
				}
				if (totalDBCallExeTime > 0) {
					innerTotalDBCallExeTimePerOperation.put(operationURI, totalDBCallExeTime);
				}
			}
		}

		sortResults(innerDotalDBQueriesPerTable, innerTotalJPACallsPerEntity, innerTotalDBCallsPerOperation,
				innerTotalJPACallsPerOperation, innerTotalDBCallExeTimePerOperation);

		populateStatistics(innerDotalDBQueriesPerTable, innerTotalDBUpdatesPerTable, innerTotalDBInsertsPerTable,
				innerTotalDBDeletesPerTable, innerTotalJPACallsPerEntity,	innerTotalDBCallExeTimePerOperation, totalExecutionTime.longValue());

		saveStatistics();

		return this;
	}

	private void sortResults(final Map<String, Integer> totalDBQueriesPerTable,
							 final Map<String, Integer> totalJPACallsPerEntity,
							 final Map<String, Integer> totalDBCallsPerOperation,
							 final Map<String, Integer> totalJPACallsPerOperation,
							 final Map<String, Integer> totalDBCallExeTimePerOperation) {

		final Comparator<Map.Entry<String, Integer>> mapEntryComparator = Map.Entry.comparingByValue((Comparator.reverseOrder()));

		CollectionUtils.sortMapEntries(totalDBCallsPerOperation, mapEntryComparator);
		CollectionUtils.sortMapEntries(totalJPACallsPerOperation, mapEntryComparator);
		CollectionUtils.sortMapEntries(totalDBCallExeTimePerOperation, mapEntryComparator);

		CollectionUtils.sortMapEntries(totalDBQueriesPerTable, mapEntryComparator);
		CollectionUtils.sortMapEntries(totalJPACallsPerEntity, mapEntryComparator);
	}

	@SuppressWarnings("checkstyle:parameternumber")
	private void populateStatistics(final Map<String, Integer> totalDBQueriesPerTable,
									final Map<String, Integer> totalDBUpdatesPerTable,
									final Map<String, Integer> totalDBInsertsPerTable,
									final Map<String, Integer> totalDBDeletesPerTable,
									final Map<String, Integer> totalJPACallsPerEntity,
									final Map<String, Integer> totalDBCallExeTimePerOperation,
									final long totalExecutionTime) {
		setTotalDBQueries(totalDBQueriesPerTable.values().stream().mapToInt(Integer::intValue).sum());
		setTotalDBUpdates(totalDBUpdatesPerTable.values().stream().mapToInt(Integer::intValue).sum());
		setTotalDBInserts(totalDBInsertsPerTable.values().stream().mapToInt(Integer::intValue).sum());
		setTotalDBDeletes(totalDBDeletesPerTable.values().stream().mapToInt(Integer::intValue).sum());

		setTotalJPACalls(totalJPACallsPerEntity.values().stream().mapToInt(Integer::intValue).sum());
		setTotalDbExeTimeMs(totalDBCallExeTimePerOperation.values().stream().mapToInt(Integer::intValue).sum());

		getOperationsWithJPADesc().sort((op1, op2) -> op2.getTotalOpDurationMs().compareTo(op1.getTotalOpDurationMs()));

		setTotalExecutionTime(totalExecutionTime);
		setTotalNumberOfOperationsWithJPA(getOperationsWithJPADesc().size());
		setTotalNumberOfOperationsWithoutJPA(getOperationsWithoutJPA().size());
	}

	/**
	 * Save statistics.
	 */
	private void saveStatistics() {
		List<String> outputFileExtensions = qaConfigurator.getOutputFileExtensions();
		if (outputFileExtensions.isEmpty()) {
			String outputFileExtension = getOutputFileExtension();
			outputFileExtensions.add(outputFileExtension);
		}

		for (String outputFileExtension : outputFileExtensions) {
			MutableBoolean isAppend = new MutableBoolean();

			try (Writer writer = getWriter(outputFileExtension, isAppend)) {
				switch (outputFileExtension.toLowerCase(Locale.ENGLISH)) {
					case JSON_OUTPUT_FILE_EXTENSION:
						jsonMapper.writeValue(writer, this);
						break;
					case CSV_OUTPUT_FILE_EXTENSION:
						saveStatisticsAsCSV(writer, isAppend);
						break;
					default:
						throw new QueryAnalyzerException("Unsupported output format [" + outputFileExtension + "]");
				}
			} catch (IOException ex) {
				throw new UnableToSerializeStatisticsException(ex);
			}
		}
	}

	private void saveStatisticsAsCSV(final Writer writer, final MutableBoolean isAppend) throws IOException {
			String applicationName = qaConfigurator.getApplicationName();
			String testName = qaConfigurator.getTestName();
			String testId = qaConfigurator.getTestId();

			if (!isAppend.booleanValue()) {
				writer.write("Test Id, Application, Scenario, Total Selects,Total Inserts, Total Updates,Total Deletes, Total DB Time(ms)\n");
			}
			writer.write(joinWith(",", testId, applicationName, testName, totalDBQueries, totalExecutedAndBatchedDBInserts, totalDBUpdates,
					totalDBDeletes, totalDbExeTimeMs) + "\n");
			writer.flush();
	}

	// This warning had to suppressed because the code is correct as per
	// https://pmd.github.io/latest/pmd_rules_java_performance.html#optimizabletoarraycall
	//TODO remove @SuppressWarnings after upgrading the PMD to 6.x
	@SuppressWarnings("PMD.OptimizableToArrayCall")
	private Writer getWriter(final String outputFileExtension, final MutableBoolean isAppend) throws IOException {
		File outputFile = getOutputFileIfEnabled(outputFileExtension);

		if (outputFile == null) {
			LOG.debug("DB statistics will be printed in the console");
			return new OutputStreamWriter(System.out, UTF_8);
		}

		List<StandardOpenOption> writeOptions = new ArrayList<>(1);

		if (outputFile.exists() && outputFileExtension.equalsIgnoreCase(CSV_OUTPUT_FILE_EXTENSION)) {
			writeOptions.add(APPEND);
			isAppend.setTrue();
		}

		LOG.debug("DB statistics saved @ {}", outputFile);
		return Files.newBufferedWriter(outputFile.toPath(), UTF_8, writeOptions.toArray(new StandardOpenOption[0]));
	}

	@Override
	public boolean equals(final Object otherStatistics) {
		return EqualsBuilder.reflectionEquals(this, otherStatistics, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
