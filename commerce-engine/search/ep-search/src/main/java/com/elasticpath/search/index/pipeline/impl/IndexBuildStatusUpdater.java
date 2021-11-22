/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.pipeline.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;

import com.elasticpath.domain.search.IndexBuildStatus;
import com.elasticpath.domain.search.IndexStatus;
import com.elasticpath.persistence.dao.IndexBuildStatusDao;
import com.elasticpath.service.search.IndexType;

/**
 * Utility that manages the updating of IndexBuildStatus objects in the context of a multi-threaded indexing environmnent.
 * 
 * During Indexing there will be a single IndexBuildStatus instance for each IndexType. These are shared between indexer threads and used to update
 * the statistics for a particular index job as it runs. The intention of this class is to prevent issues that might occur in this multi-threaded
 * environment, particularly around IndexBuildStatus objects becoming stale. There was a particularly nasty openjpa error occurring previously.
 * 
 * IndexBuildStatus instances get passed to this class, they are put on a queue, there is a worker thread that periodically checks this queue
 * for any objects to update. We manage the copying 
 * of data from the passed in IndexBuildStatus ourselves, after an update we always save back to the internal map.
 * 
 */
public class IndexBuildStatusUpdater {

	private static final Logger LOG = LogManager.getLogger(IndexBuildStatusUpdater.class);
	
	private static final long POLLING_INTERVAL_IN_MILLISECONDS = 500L;
	
	private static final long MAX_TERMINATION_TIME_IN_SECONDS = 30L;

	private final BlockingQueue<IndexBuildStatus> waitingQueue = new LinkedBlockingDeque<>();
	
	private ScheduledExecutorService executorService;
	
	private GuardedIndexBuildStatusDao guardedIndexBuildStatusDao;

	private ObjectFactory<IndexBuildStatus> indexBuildStatusFactory;


	/**
	 * Add the <code>IndexBuildStatus</code> to the waiting queue to be processed later!
	 * @param buildStatus the status update to be processed
	 */
	public void enqueue(final IndexBuildStatus buildStatus) {
		waitingQueue.add(buildStatus);
	}

	/**
	 * Initializes a background process that manages the retrieval of <code>IndexBuildStatus</code> from 
	 * internal <code>Queue</code> and passes it to the internal <code>IndexBuildStatusDao</code>.
	 */
	public void initialize() {
		if (executorService == null) {
			waitingQueue.clear();  // this supports test scenarios
			executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleWithFixedDelay(new UpdateProcessor(),
					0, POLLING_INTERVAL_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
		} else {
			LOG.debug("Scheduled executor service is already initialized");
		}
	}

	/**
	 * The runnable Thread that has been scheduled.
	 * <b/>
	 * It will consume the waiting queue of <code>IndexBuildStatus</code> persisting them.
	 */
	protected class UpdateProcessor implements Runnable {
		@Override
		public void run() {
			if (!waitingQueue.isEmpty()) {
				LinkedList<IndexBuildStatus> processingQueue = new LinkedList<>();
				waitingQueue.drainTo(processingQueue);
				if (LOG.isDebugEnabled()) {
					LOG.debug(String.format("Processing: %s. Waiting: %s", processingQueue.size(), waitingQueue.size()));
				}
				saveOrUpdateMostRecentStatusUpdatesPerType(processingQueue);
			}
		}

		private void saveOrUpdateMostRecentStatusUpdatesPerType(final Queue<IndexBuildStatus> batchOfStatusUpdates) {
			// process to a Map so we have unique IndexBuildStatus by IndexType
			// as every IndexBuildStatus by type will point to the same instance anyway
			// so we only want to hit the database once for each.
			Map<IndexType, IndexBuildStatus> statusesByType = batchOfStatusUpdates.stream()
					.filter(indexBuildStatus -> indexBuildStatus != null && indexBuildStatus.getIndexType() != null)
					.collect(Collectors.toMap(IndexBuildStatus::getIndexType, Function.identity(), (oldElement, newElement) -> newElement));

			for (IndexBuildStatus indexBuildStatus : statusesByType.values()) {
				prepareInstanceForSaveOrUpdate(indexBuildStatus);
			}
		}

		/**
		 * We always want to get a fresh instance from the db and do a soft copy of the data
		 * from the passed in <code>IndexBuildStatus</code> as we dont want OpenJpa to get in a twist
		 * in a multi threaded environment.
		 *
		 * All exception handling is managed by caller.
		 *
		 * @param indexBuildStatus buildStatus
		 * @return an updated Entity
		 */
		private void prepareInstanceForSaveOrUpdate(final IndexBuildStatus indexBuildStatus) {
			IndexBuildStatus freshInstance = guardedIndexBuildStatusDao.get(indexBuildStatus.getIndexType());

			if (freshInstance == null) {
				freshInstance = indexBuildStatus;
			} else {
				freshInstance.setLastBuildDate(indexBuildStatus.getLastBuildDate());
				freshInstance.setIndexStatus(indexBuildStatus.getIndexStatus());
				freshInstance.setLastModifiedDate(indexBuildStatus.getLastModifiedDate());
				freshInstance.setOperationStartDate(indexBuildStatus.getOperationStartDate());
				freshInstance.setProcessedRecords(indexBuildStatus.getProcessedRecords());
				freshInstance.setTotalRecords(indexBuildStatus.getTotalRecords());
				freshInstance.setIndexType(indexBuildStatus.getIndexType());
			}
			guardedIndexBuildStatusDao.saveOrUpdate(freshInstance);
		}
	}
	
	/**
	 * Indicates that the internal thread executor is eligible to stop.
	 */
	public void shutdown() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Shutting down IndexBuildStatusUpdater");
		}
		if (executorService == null) {
			LOG.debug("Scheduled executor service is already shutdown");
		} else {
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(MAX_TERMINATION_TIME_IN_SECONDS, TimeUnit.SECONDS)) {
					executorService.shutdownNow(); // Cancel currently executing tasks
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				executorService.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			} finally {
				executorService = null;
			}
		}
	}

	/**
	 * In case of IndexBuildStatusUpdater is in degraded mode, it has to compare the received <code>IndexType</code>
	 * with the same <code>IndexType</code> persisted in the database.
	 *
	 * @param indexType indexType to be checked.
	 * @return <code>true<code/> if rebuild was in progress.
	 */
	public boolean wasRebuildInProgress(final IndexType indexType) {
		if (guardedIndexBuildStatusDao.isInDegradedMode()) {
			IndexBuildStatus status = guardedIndexBuildStatusDao.get(indexType);
			return status != null && IndexStatus.REBUILD_IN_PROGRESS.equals(status.getIndexStatus());
		}
		return false;
	}

	/**
	 * Use this method to refresh a instance of <code>IndexBuildStatus</code> by <code>IndexType</code>.
	 *
	 * @param indexType the <code>IndexType</code> you want to refresh.
	 * @return a fresh instance of <code>IndexBuildStatus</code> from Database or from 'indexBuildStatusFactory'.
	 */
	public IndexBuildStatus getInitialIndexStatus(final IndexType indexType) {
		IndexBuildStatus buildStatus = guardedIndexBuildStatusDao.get(indexType);
		if (buildStatus == null) {
			buildStatus = indexBuildStatusFactory.getObject();
			buildStatus.setIndexType(indexType);
		}
		return buildStatus;
	}

	/**
	 * Sets the  <code>IndexBuildStatusDao</code>.
	 * @param indexBuildStatusDao indexBuildStatusDao.
	 */
	public void setIndexBuildStatusDao(final IndexBuildStatusDao indexBuildStatusDao) {
		this.guardedIndexBuildStatusDao = new GuardedIndexBuildStatusDao(indexBuildStatusDao);
	}

	public void setIndexBuildStatusFactory(final ObjectFactory<IndexBuildStatus> indexBuildStatusFactory) {
		this.indexBuildStatusFactory = indexBuildStatusFactory;
	}

	/**
	 * Act as a circuit breaker guarding IndexBuildStatusDao in order to do not persist in cases of Network/DB failures.
	 */
	static class GuardedIndexBuildStatusDao implements IndexBuildStatusDao {

		/**
		 * Number of operations to be consider by the circuit breaker as failing.
		 */
		static final int ERRORS_TO_CONSIDER_AS_FAILING = 2;

		/**
		 * Number of operations to be consider by the circuit breaker as recovering.
		 */
		static final int SUCCESS_TO_CONSIDER_AS_RECOVERING = 4;

		/**
		 * State to record a failed transaction.
		 */
		static final boolean ERROR = false;

		/**
		 *  State to record a successful transaction.
		 */
		static final boolean SUCCESS = true;

		/**
		 * The number of attempts to skip when the breaker is tripped.
		 */
		private static final int ATTEMPTS_TO_SKIP_THRESHOLD = 100;

		/**
		 * The buffer used by the circuit breaker.
		 */
		private final OperationHistory operationHistory = new OperationHistory();

		/**
		 * Consider two sequence failures as error - # fffffftt.
		 */
		private final boolean[] errorPattern = new boolean [ERRORS_TO_CONSIDER_AS_FAILING];

		/**
		 * Consider four successful sequence operations as in recover mode - # tttttttffff.
		 */
		private final boolean[] recoveryPattern = new boolean [SUCCESS_TO_CONSIDER_AS_RECOVERING];

		private final AtomicInteger skippedAttempts = new AtomicInteger();
		private final IndexBuildStatusDao wrapped;
		private boolean degradedMode;

		/**
		 * Default Constructor.
		 *
		 * @param indexBuildStatusDao the object to be guarded.
		 */
		GuardedIndexBuildStatusDao(final IndexBuildStatusDao indexBuildStatusDao) {
			wrapped = indexBuildStatusDao;

			Arrays.fill(errorPattern, ERROR);
			Arrays.fill(recoveryPattern, SUCCESS);
		}

		/**
		 * To consult if <code>IndexBuildSatusUpdater</code> has turn into degraded mode after any exception.
		 *
		 * @return <code>true</code> if it is in degraded mode.
		 */
		public boolean isInDegradedMode() {
			return degradedMode;
		}

		/**
		 * Record a failed/successful transaction to the Circuit Breaker.
		 * <p/>
		 * This circuit breaker implementation, controls the action of tripping off or reconnecting in case of
		 * network/database failures.
		 *
		 * @param value <code>True</code> to report a failed transaction and <code>False</code> for a successful.
		 */
		public void record(final boolean value) {
			operationHistory.shoveLeft(value);
			if (value == ERROR) {
				if (operationHistory.matchesPattern(errorPattern) && !degradedMode) {
					LOG.warn("Entering degraded mode!");
					degradedMode = true;
				}
			} else {
				if (operationHistory.matchesPattern(recoveryPattern) && degradedMode) {
					LOG.warn("Resuming normal operation.");
					degradedMode = false;
				}
			}
		}

		private boolean attemptRecovery() {
			int attempts = skippedAttempts.incrementAndGet();
			if (attempts > ATTEMPTS_TO_SKIP_THRESHOLD) {
				skippedAttempts.set(0);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Degraded Mode, attempting recovery");
				}
				return true;
			}
			return false;
		}

		/**
		 * It catches Throwable exception due to the particularity of ScheduledExecutorService,
		 * the block can never throw an exception otherwise it will stop ScheduledExecutorService.
		 * <p/>
		 * The PMD warning it's been suppressed on purpose to allow the above requirement.
		 *
		 * @param runnable the <code>IndexBuildStatusDao</code> method called
		 * @param theDefault the default object to be returned in case of Exception
		 * @param <T> the type of Object to be returned
		 * @return the returned of the <code>IndexBuildStatusDao</code> method that has been guarded
		 */
		@SuppressWarnings("PMD.AvoidCatchingThrowable")
		public <T> T guard(final Supplier<T> runnable, final T theDefault) {
			try {
				if (!degradedMode || attemptRecovery()) {
					T obj =  runnable.get();
					record(SUCCESS);
					return obj;
				}
				skippedAttempts.incrementAndGet();
				return theDefault;

			} catch (Throwable e) {
				record(ERROR);
				LOG.error("Schedule updater task failed returning the default value");
				LOG.debug("Schedule updater task failed exception", e);
				return theDefault;
			}
		}

		@Override
		public IndexBuildStatus saveOrUpdate(final IndexBuildStatus indexBuildStatus) {
			return guard(() -> wrapped.saveOrUpdate(indexBuildStatus), indexBuildStatus);
		}

		@Override
		public IndexBuildStatus get(final IndexType indexType) {
			return guard(() -> wrapped.get(indexType), null);
		}

		@Override
		public List<IndexBuildStatus> list() {
			return guard(() -> wrapped.list(), Collections.emptyList());
		}

		/**
		 * Wrapper of a Boolean Vector to abstract its manipulation to manage the Operation History.
		 */
		static class OperationHistory {

			/**
			 * It defines the number maximum of operation to be tracked.
			 * <p/>
			 * This number must be greater than the 'patterns' that will be check during its operation.
			 */
			static final int MAX_NUMBER_OF_OPERATIONS_TO_TRACK = 20;
			private boolean[] operations = new boolean[MAX_NUMBER_OF_OPERATIONS_TO_TRACK];

			/**
			 * @param value shove the elements to left and insert new 'value' in the last position of the array
			 */
			public void shoveLeft(final boolean value) {
				boolean[] destination = new boolean[MAX_NUMBER_OF_OPERATIONS_TO_TRACK];
				System.arraycopy(operations, 1, destination, 0, MAX_NUMBER_OF_OPERATIONS_TO_TRACK - 1);
				destination[MAX_NUMBER_OF_OPERATIONS_TO_TRACK - 1] = value;
				operations = destination;
			}

			/**
			 * Matches given pattern to the most recent operations. Given pattern may be a recoveryPattern or an errorPattern.
			 * Once we shoveLeft to input, the most recent are found from right to left.
			 *
			 * @param pattern to be compared.
			 * @return <code>True<code/> if the pattern was found if instead returns <code>False<code/>.
			 */
			public boolean matchesPattern(final boolean[] pattern) {
				if (pattern.length <= operations.length) {
					for (int i = 1; i <= pattern.length; i++) {
						if (pattern[pattern.length - i] != operations[operations.length - i]) {
							return false;
						}
					}
					return true;
				}
				throw new IllegalStateException("The pattern to be matched cannot be greater than operations vector.");
			}

		}

	}

}