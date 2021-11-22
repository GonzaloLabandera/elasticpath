/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.TransactionException;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.Dto;
import com.elasticpath.commons.util.impl.JaxbUtils;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateAssociatedEntityRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateEntityRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.MarshallingRuntimeException;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.importexport.common.exception.runtime.PopulationRuntimeException;
import com.elasticpath.importexport.common.marshalling.DefaultValidationEventHandler;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.ImportProcessor;
import com.elasticpath.importexport.importer.importers.Importer;
import com.elasticpath.importexport.importer.importers.ImporterFactory;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Transaction;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * This class is responsible for import processing.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class ImportProcessorImpl extends AbstractEpPersistenceServiceImpl implements ImportProcessor {

	private static final Logger LOG = LogManager.getLogger(ImportProcessorImpl.class);

	private ImporterFactory importerFactory;

	private DefaultValidationEventHandler validationEventHandler;

	private SavingManager<? extends Persistable> savingManager;

	@Override
	public void process(final InputStream entryToImport, final ImportContext context) throws ConfigurationException {
		XMLStreamReader streamReader = null;
		try {
			final Summary summary = context.getSummary();
			streamReader = XMLInputFactory.newInstance().createXMLStreamReader(entryToImport);
			streamReader.nextTag();
			streamReader.require(XMLStreamConstants.START_ELEMENT, null, null);
			final JobType jobType = JobType.getJobTypeByTag(streamReader.getLocalName());

			LOG.info("Begin importing " + jobType);
			final Importer<? super Persistable, ? super Dto> importer = importerFactory.createImporter(streamReader.getLocalName(),
					context,
					savingManager);

			runImport(context, streamReader, summary, jobType, importer);

			importer.postProcessingImportHandling();

		} catch (XMLStreamException exception) {
			throw new ImportRuntimeException("IE-30403", exception);
		} finally {
			if (streamReader != null) {
				try {
					streamReader.close();
				} catch (XMLStreamException e) {
					LOG.error("Could not close xml stream reader", e);
				}
			}
		}
	}

	/**
	 * Runs an import of the given <code>jobType</code> from the given <code>streamReader</code>.
	 *
	 * @param context Import configuration settings
	 * @param streamReader XML stream reader setup with the file with data to import from
	 * @param summary Contains summary details of the import of <code>jobType</code>
	 * @param jobType Type of entity being imported
	 * @param importer The importer to use to do the import
	 * @throws XMLStreamException if any errors
	 */
	protected void runImport(final ImportContext context,
			final XMLStreamReader streamReader,
			final Summary summary,
			final JobType jobType,
			final Importer<? super Persistable, ? super Dto> importer) throws XMLStreamException {
		final Class<?>[] jaxbClasses = JaxbUtils.createClassArray(importer.getDtoClass(), importer.getAuxiliaryJaxbClasses());

		final XMLUnmarshaller unmarshaller = createUnmarshaller(jaxbClasses,
				context.getImportConfiguration().isXmlValidation(),
				importer.getSchemaPath());

		streamReader.nextTag();

		PersistenceSession session = getPersistenceEngine().getSharedPersistenceSession();
		List<Dto> unmarshalledObjects = Collections.emptyList();

		// Continue importing while there are still start tags left in the XML to process for import
		while (streamReader.getEventType() == XMLStreamConstants.START_ELEMENT || !unmarshalledObjects.isEmpty()) {
			final Transaction transaction = session.beginTransaction();
			final MutableInt transactionVolume = new MutableInt(0);
			final MutableInt processedVolume = new MutableInt(0);
			try {
				if (unmarshalledObjects.isEmpty()) {
					unmarshalledObjects = unmarshallObjects(unmarshaller, streamReader, jobType, importer);
				}
				importObjects(unmarshalledObjects, jobType, importer, summary, transactionVolume, processedVolume);
				commitTransaction(summary, jobType, importer.getStatusHolder().getImportStatus(), transaction, transactionVolume.intValue());
				unmarshalledObjects = Collections.emptyList();

			} catch (PopulationRollbackException exception) {
				unmarshalledObjects = unmarshalledObjects.subList(processedVolume.intValue(), unmarshalledObjects.size());
				LOG.error(exception.getIEMessage());
				summary.addToCounter(jobType, -transactionVolume.intValue());
				skipToNextStartElement(streamReader, importer.getImportedObjectName());
				try {
					transaction.rollback();
					// Report that the transaction was rolled back.
					// transactionVolume is 0 based so need to increment by 1
					LOG.error(new Message("IE-30407", Integer.toString(transactionVolume.intValue() + 1), jobType.toString()));
				} catch (TransactionException ex) {
					LOG.error(new Message("IE-30405", ex, Integer.toString(transactionVolume.intValue())));
				}
			} catch (Exception exception) {
				unmarshalledObjects = unmarshalledObjects.subList(processedVolume.intValue(), unmarshalledObjects.size());
				LOG.error(new Message("IE-30402", exception, jobType.toString(), importer.getStatusHolder().getImportStatus()));
				summary.addToCounter(jobType, -transactionVolume.intValue());
				skipToNextStartElement(streamReader, importer.getImportedObjectName());
				try {
					transaction.rollback();
					// Report that the transaction was rolled back.
					// transactionVolume is 0 based so need to increment by 1
					LOG.error(new Message("IE-30407", Integer.toString(transactionVolume.intValue() + 1), jobType.toString()));
				} catch (TransactionException ex) {
					LOG.error(new Message("IE-30405", ex, Integer.toString(transactionVolume.intValue())));
				}
			}
		}
		session.close();
	}

	/**
	 * Unmarshalls the objects.
	 *
	 * @param unmarshaller the unmarshaller
	 * @param streamReader the stream reader
	 * @param jobType      the job type
	 * @param importer     the importer
	 * @return the list of unmarshalled objects
	 * @throws XMLStreamException in case of any errors
	 */
	protected List<Dto> unmarshallObjects(final XMLUnmarshaller unmarshaller,
										  final XMLStreamReader streamReader,
										  final JobType jobType,
										  final Importer<? super Persistable, ? super Dto> importer) throws XMLStreamException {
		int unmarshalledObjectsQty = 0;
		final List<Dto> unmarshalledObjects = new ArrayList<>(importer.getCommitUnit());
		while (unmarshalledObjectsQty < importer.getCommitUnit() && streamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
			streamReader.require(XMLStreamConstants.START_ELEMENT, null, importer.getImportedObjectName());
			try {
				final Dto unmarshalledObject = unmarshaller.unmarshall(streamReader);
				unmarshalledObjects.add(unmarshalledObject);
				unmarshalledObjectsQty += Math.max(importer.getObjectsQty(unmarshalledObject), 1);
				skipNotStartElements(streamReader, importer.getImportedObjectName());
			} catch (MarshallingRuntimeException marshallingRuntimeException) {
				LOG.error(new Message("IE-30400", marshallingRuntimeException, jobType.toString(),
						validationEventHandler.getLastErrorStatus()));
				LOG.error(marshallingRuntimeException.getIEMessage());
				skipToNextStartElement(streamReader, importer.getImportedObjectName());
			}
		}
		return unmarshalledObjects;
	}

	/**
	 * Imports the list of unmarshalled objects.
	 *
	 * @param unmarshalledObjects the unmarshalled objects
	 * @param jobType             the job type
	 * @param importer            the importer
	 * @param summary             the summary
	 * @param transactionVolume   the transaction volume
	 * @param processedVolume     the processed volume
	 */
	protected void importObjects(final List<Dto> unmarshalledObjects, final JobType jobType,
								final Importer<? super Persistable, ? super Dto> importer,
								final Summary summary, final MutableInt transactionVolume, final MutableInt processedVolume) {
		for (final Dto unmarshalledObject : unmarshalledObjects) {
			try {
				processedVolume.increment();
				if (importer.executeImport(unmarshalledObject)) {
					transactionVolume.increment();
					summary.addToCounter(jobType, importer.getObjectsQty(unmarshalledObject));
				}
			} catch (ImportDuplicateEntityRuntimeException duplicateEntityRuntimeException) {
				LOG.error(new Message("IE-30401", duplicateEntityRuntimeException, jobType.toString(),
						importer.getStatusHolder().getImportStatus(), jobType.toString()));
				LOG.error(duplicateEntityRuntimeException.getIEMessage());
			} catch (ImportDuplicateAssociatedEntityRuntimeException ex) {
				LOG.error(new Message("IE-30408", ex, jobType.toString(), importer.getStatusHolder().getImportStatus(),
						jobType.toString()));
				LOG.error(ex.getIEMessage());
			} catch (PopulationRuntimeException exception) {
				LOG.error(exception.getIEMessage());
			}
		}
	}

	/**
	 * Attempts to commit the <code>transaction</code> and logs error information if an exception occurs during the commit.
	 *
	 * @param summary Contains summary details of the import of <code>jobType</code>
	 * @param jobType Type of entity that is in the transaction
	 * @param importStatus Identifier of any failing entity in the transaction
	 * @param transaction The batch of entity changes to commit
	 * @param transactionVolume The size of the batch to be committed
	 */
	private void commitTransaction(final Summary summary,
			final JobType jobType,
			final String importStatus,
			final Transaction transaction,
			final int transactionVolume) {

		try {
			LOG.debug("Attempting Transaction Commit");
			transaction.commit();
			LOG.info("Transaction of type " + jobType + " and size " + (transactionVolume + 1) + " was committed.");
		} catch (TransactionException exception) {
			summary.addToCounter(jobType, -transactionVolume);
			LOG.error(new Message("IE-30404", exception, Integer.toString(transactionVolume)));
		} catch (Exception exc) {
			LOG.error(new Message("IE-30402", exc, jobType.toString(), importStatus));
		}

	}

	private XMLUnmarshaller createUnmarshaller(final Class<?>[] jaxbClasses, final boolean xmlValidation,
			final String schemaPath) {
		XMLUnmarshaller unmarshaller = new XMLUnmarshaller(jaxbClasses);
		if (xmlValidation) {
			unmarshaller.initValidationParameters(schemaPath, validationEventHandler);
		}
		return unmarshaller;
	}

	/**
	 * Advance the stream reader to the start of the next start element with the passed expectedTagName.
	 * Will not advance at all if the stream reader is already on the expected start element.
	 *
	 * @param streamReader the stream reader
	 * @param expectedTagName the expected tag name of the start element
	 * @throws XMLStreamException if an exception occurs during XML processing
	 */
	private void skipNotStartElements(final XMLStreamReader streamReader, final String expectedTagName) throws XMLStreamException {
		while ((streamReader.getEventType() != XMLStreamConstants.START_ELEMENT || !streamReader.getLocalName().equals(expectedTagName))
				&& streamReader.hasNext()) {
			streamReader.next();
		}
	}

	/**
	 * Advance the stream reader to the start of the next start element with the passed expectedTagName.
	 * Will always advance past at least one XML element even if the stream reader is already on the expected start element.
	 *
	 * @param streamReader the stream reader
	 * @param expectedTagName the expected tag name of the start element
	 * @throws XMLStreamException if an exception occurs during XML processing
	 */
	private void skipToNextStartElement(final XMLStreamReader streamReader, final String expectedTagName) throws XMLStreamException {
		if (streamReader.hasNext()) {
			streamReader.next();
		}
		while ((streamReader.getEventType() != XMLStreamConstants.START_ELEMENT || !streamReader.getLocalName().equals(expectedTagName))
				&& streamReader.hasNext()) {
			streamReader.next();
		}
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		throw new UnsupportedOperationException("unsupported operation");
	}

	public ImporterFactory getImporterFactory() {
		return importerFactory;
	}

	public void setImporterFactory(final ImporterFactory importerFactory) {
		this.importerFactory = importerFactory;
	}

	public DefaultValidationEventHandler getValidationEventHandler() {
		return validationEventHandler;
	}

	public void setValidationEventHandler(final DefaultValidationEventHandler validationEventHandler) {
		this.validationEventHandler = validationEventHandler;
	}

	public SavingManager<? extends Persistable> getSavingManager() {
		return savingManager;
	}

	public void setSavingManager(final SavingManager<? extends Persistable> savingManager) {
		this.savingManager = savingManager;
	}
}
