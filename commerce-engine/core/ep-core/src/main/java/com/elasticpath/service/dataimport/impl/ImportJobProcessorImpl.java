/**
 * Copyright (c) Elastic Path Software Inc., 2009
 */
package com.elasticpath.service.dataimport.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.util.AssetRepository;
import com.elasticpath.commons.util.csv.CSVFileUtil;
import com.elasticpath.core.messaging.dataimport.DataImportEventType;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.dataimport.ImportAction;
import com.elasticpath.domain.dataimport.ImportBadRow;
import com.elasticpath.domain.dataimport.ImportDataType;
import com.elasticpath.domain.dataimport.ImportJob;
import com.elasticpath.domain.dataimport.ImportJobState;
import com.elasticpath.domain.dataimport.ImportJobStatus;
import com.elasticpath.domain.dataimport.ImportNotification;
import com.elasticpath.domain.dataimport.ImportNotificationState;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.service.dataimport.ImportJobProcessor;
import com.elasticpath.service.dataimport.ImportJobRunner;
import com.elasticpath.service.dataimport.ImportJobStatusHandler;
import com.elasticpath.service.dataimport.ImportService;
import com.elasticpath.service.dataimport.dao.ImportJobStatusDao;
import com.elasticpath.service.dataimport.dao.ImportNotificationDao;

/**
 * The default implementation of {@link ImportJobProcessor}.
 */
public class ImportJobProcessorImpl implements ImportJobProcessor {
	
	/**
	 * The logger.
	 */
	private static final Logger LOG = Logger.getLogger(ImportJobProcessorImpl.class);

	private ImportNotificationDao importNotificationDao;
	
	private ImportService importService;
	
	private ImportJobStatusHandler importJobStatusHandler;
	
	private ImportJobStatusDao importJobStatusDao;
	
	private AssetRepository assetRepository;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private BeanFactory beanFactory;

	private String importAssetPath;

	/**
	 * Validates one import job in case there is a notification for that.
	 * 
	 * <p>
	 * Note: Only one job at a time will be processed. It is not clear what the 
	 * consequences will be in a clustered environment running different import jobs.
	 * All submitted import jobs are executed in sequence ordered by the date of submission.
	 */
	@Override
	public void launchImportJob() {

		if (hasRunningImportJob()) {
			LOG.info("Import Job. Another processor is running at the moment. Quiting operation.");
			return;
		}
		List<ImportNotification> notifications = importNotificationDao.
			findByActionAndState(ImportAction.LAUNCH_IMPORT, ImportNotificationState.NEW, 1);
		
		if (CollectionUtils.isEmpty(notifications)) {
			LOG.debug("Import Job. No notifications found. Quiting operation.");
			return;
		}

		final long startTime = System.currentTimeMillis();
		LOG.info("Start import job at: " + new Date(startTime));

		// grab the first notification. there should be only one.
		ImportNotification notification = notifications.get(0);

		if (!lock(notification)) {
			LOG.info("Import Job. Could not lock notification. Likely locked by another processor. " + notification);
			return;
		}
		
		try {
			if (isCancelled(notification.getProcessId())) {
				LOG.info("Import Job cancelled. Process ID: " + notification.getProcessId());
				return;
			}
			
			if (validateImportJob(notification)) {
				if (isCancelled(notification.getProcessId())) {
					LOG.info("Import Job cancelled. Process ID: " + notification.getProcessId());
					return;
				}
				ImportJobStatus importStatus;
				try {
					importStatus = executeImport(notification);
				} catch (Exception exc) {
					LOG.error("Import job failure.", exc);
					importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.FAILED);
					return;
				}
				sendEvent(importStatus, notification.getInitiator(), notification.getReportingLocale());
				LOG.info("Import job quartz job completed in (ms): " + (System.currentTimeMillis() - startTime));
			}
		} finally {
			unlock(notification);
		}
	}

	/**
	 *
	 * @param totalRows the total number of rows
	 * @param processId the process ID
	 */
	protected void updateTotalRowsNumber(final int totalRows, final String processId) {
		importJobStatusHandler.reportTotalRows(processId, totalRows);
	}

	/**
	 * Sets the notification in processed state.
	 * 
	 * @param notification the notification
	 */
	protected void unlock(final ImportNotification notification) {
		notification.setState(ImportNotificationState.PROCESSED);
		importNotificationDao.update(notification);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Import notification has been processed. Process ID: " + notification.getProcessId());
		}
	}

	/**
	 * Verifies if there is already a processor running an import job.
	 * 
	 * @return true if there is already an import job in process
	 */
	protected boolean hasRunningImportJob() {
		List<ImportNotification> result = importNotificationDao.
			findByActionAndState(ImportAction.LAUNCH_IMPORT, ImportNotificationState.IN_PROCESS);
		return CollectionUtils.isNotEmpty(result);
	}

	/**
	 * Tries to lock the notification by setting it into in_process state.
	 * 
	 * @param notification the notification to lock
	 * @return true if the lock has been gained successfully
	 */
	protected boolean lock(final ImportNotification notification) {
		if (Objects.equals(ImportNotificationState.IN_PROCESS, notification.getState())) {
			return false;
		}
		// set in process on notification so that no other parties should take it over
		notification.setState(ImportNotificationState.IN_PROCESS);
		try {
			// update the notification which makes sure that no other participants will take it for processing
			// could throw OptimisticLockException wrapped in EpPersistenceException when some other party
			// has updated the same object which means that it was already locked
			importNotificationDao.update(notification);
		} catch (EpPersistenceException exc) {
			LOG.debug("Cannot gain lock over notification: " + notification, exc);
			return false;
		}
		return true;
	}

	/**
	 * Verifies whether the import job process was cancelled and if yes reports it to the status.
	 * 
	 * @param importJobProcessId the process ID
	 * @return true if cancelled
	 */
	protected boolean isCancelled(final String importJobProcessId) {
		if (importJobStatusHandler.isImportJobCancelled(importJobProcessId)) {
			importJobStatusHandler.reportImportJobState(importJobProcessId, ImportJobState.CANCELLED);
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean validateImportJob(final ImportNotification notification) {
		importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.VALIDATING);
		
		List<ImportBadRow> importBadRows;
		try {
			importBadRows = processValidation(notification);
		} catch (Exception exc) {
			LOG.error("Validation failed.", exc);
			importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.VALIDATION_FAILED);
			return false;
		}
		
		if (CollectionUtils.isNotEmpty(importBadRows)) {
			importJobStatusHandler.reportBadRows(notification.getProcessId(), 
					importBadRows.toArray(new ImportBadRow[importBadRows.size()]));
			importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.VALIDATION_FAILED);
			return false;
		}

		importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.QUEUED_FOR_IMPORT);
		return true;
	}

	/**
	 * The method is made protected in order to give subclasses opportunity to alter remote file name.
	 * 
	 * @param csvFileName the file name
	 * @return remote file name
	 */
	protected String getRemoteCsvFileName(final String csvFileName) {
		return CSVFileUtil.getRemoteCsvFileName(getImportAssetPath(), csvFileName);
	}

	/**
	 * Process the validation by using the import job runner.
	 * 
	 * @param notification the notification
	 * @return a list of bad rows
	 */
	protected List<ImportBadRow> processValidation(final ImportNotification notification) {
		ImportJob importJob = notification.getImportJob();

		// update the CSV file path with the local file path
		notification.setImportSource(getRemoteCsvFileName(notification.getImportSource()));

		// Validate again
		final ImportDataType importDataType = importService.findImportDataType(importJob.getImportDataTypeName());
		if (importDataType == null) {
			throw new EpSystemException("ImportJob specifies unknown importDataType " + importJob.getImportDataTypeName());
		}

		ImportJobRunner importJobRunner = getImportJobRunner(importDataType.getImportJobRunnerBeanName());
		importJobRunner.init(notification, notification.getProcessId());

		return importJobRunner.validate(notification.getReportingLocale());
	}


	/**
	 * Executes an import by getting the parameters from the notification.
	 * 
	 * @param notification the notification
	 * @return the status of the import
	 */
	protected ImportJobStatus executeImport(final ImportNotification notification) {
		ImportJob importJob = notification.getImportJob();

		// update the CSV file path with the local file path
		notification.setImportSource(getRemoteCsvFileName(notification.getImportSource()));

		// Validate again
		final ImportDataType importDataType = importService.findImportDataType(importJob.getImportDataTypeName());
		if (importDataType == null) {
			throw new EpSystemException("ImportJob specifies unknown importDataType " + importJob.getImportDataTypeName());
		}

		ImportJobRunner importJobRunner = getImportJobRunner(importDataType.getImportJobRunnerBeanName());
		importJobRunner.init(notification, notification.getProcessId());

		updateTotalRowsNumber(importJobRunner.getTotalRows(), notification.getProcessId());
		
		importJobStatusHandler.reportImportJobState(notification.getProcessId(), ImportJobState.RUNNING);
		// Start the runner.
		LOG.info("Launch import job runner: " + importJobRunner);
		importJobRunner.run();
		
		return importJobStatusHandler.getImportJobStatus(notification.getProcessId());
	}

	/**
	 * Triggers a DataImport Event when the import job completes.
	 *
	 * @param importJobStatus the import job status
	 * @param cmUser the CM user
	 * @param locale the locale
	 */
	protected void sendEvent(final ImportJobStatus importJobStatus, final CmUser cmUser, final Locale locale) {
		try {
			final Map<String, Object> additionalData = new HashMap<>();
			if (cmUser != null) {
				additionalData.put("cmUserGuid", cmUser.getGuid());
			}
			additionalData.put("locale", locale.toString());

			final EventMessage eventMessage = getEventMessageFactory()
					.createEventMessage(DataImportEventType.IMPORT_JOB_COMPLETED,
										importJobStatus.getProcessId(),
										additionalData);
			getEventMessagePublisher().publish(eventMessage);
		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}

	}

	/**
	 * @param importJobRunnerBeanName the name of the import job runner
	 * @return the import job runner
	 */
	protected ImportJobRunner getImportJobRunner(final String importJobRunnerBeanName) {
		return beanFactory.getBean(importJobRunnerBeanName);
	}

	/**
	 * This implementation uses {@link AssetRepository}.
	 * @return the full path to the import assets directory
	 */
	String getImportAssetPath() {
		if (importAssetPath == null) {
			importAssetPath = getAssetRepository().getImportAssetPath();
		}

		return importAssetPath;
	}
	
	/**
	 * @return the assetRepository
	 */
	protected AssetRepository getAssetRepository() {
		return assetRepository;
	}

	/**
	 * @param assetRepository the assetRepository to set
	 */
	public void setAssetRepository(final AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
	}

	/**
	 *
	 * @return the importNotificationDao
	 */
	public ImportNotificationDao getImportNotificationDao() {
		return importNotificationDao;
	}

	/**
	 *
	 * @param importNotificationDao the importNotificationDao to set
	 */
	public void setImportNotificationDao(final ImportNotificationDao importNotificationDao) {
		this.importNotificationDao = importNotificationDao;
	}

	/**
	 *
	 * @return the importService
	 */
	protected ImportService getImportService() {
		return importService;
	}

	/**
	 *
	 * @param importService the importService to set
	 */
	public void setImportService(final ImportService importService) {
		this.importService = importService;
	}

	/**
	 *
	 * @return the importJobStatusHandler
	 */
	protected ImportJobStatusHandler getImportJobStatusHandler() {
		return importJobStatusHandler;
	}

	/**
	 *
	 * @param importJobStatusHandler the importJobStatusHandler to set
	 */
	public void setImportJobStatusHandler(final ImportJobStatusHandler importJobStatusHandler) {
		this.importJobStatusHandler = importJobStatusHandler;
	}

	/**
	 *
	 * @return the importJobStatusDao
	 */
	protected ImportJobStatusDao getImportJobStatusDao() {
		return importJobStatusDao;
	}

	/**
	 *
	 * @param importJobStatusDao the importJobStatusDao to set
	 */
	public void setImportJobStatusDao(final ImportJobStatusDao importJobStatusDao) {
		this.importJobStatusDao = importJobStatusDao;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return this.eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
