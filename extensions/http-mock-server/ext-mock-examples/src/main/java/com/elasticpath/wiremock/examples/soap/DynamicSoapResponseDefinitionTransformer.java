/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.elasticpath.wiremock.examples.soap.dto.GetInventoryRequest;
import com.elasticpath.wiremock.examples.soap.dto.GetInventoryResponse;

/**
 * A custom extensions of WireMock {@link ResponseDefinitionTransformer} that reads
 * a SOAP XML request which is unmarshalled into POJO(s) using JAXB.
 *
 * It then uses those POJO's to build the response POJO(s)which are marshalled into a
 * SOAP response XML.
 *
 * The response will contain the same size of InventoryLine elements as the request and also the same
 * skus.  A dcId and quantity value are added to each element.
 */
public class DynamicSoapResponseDefinitionTransformer extends ResponseDefinitionTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSoapResponseDefinitionTransformer.class);
	private static final double QUANTITY_VALUE = 100.0;
	private static final int DC_ID_VALUE = 1;
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String TEXT_XML = "text/xml";

	@Override
	public boolean applyGlobally() {
		return false;
	}

	@Override
	public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition, final FileSource fileSource,
			final Parameters parameters) {

		String responseBody;
		int status = HttpURLConnection.HTTP_OK;

		try {
			GetInventoryRequest inventoryRequest = unmarshallRequest(request);

			GetInventoryResponse inventoryResponse = buildInventoryResponse(inventoryRequest);

			responseBody = marshallResponse(inventoryResponse);

		} catch (Exception e) {
			LOGGER.error("Exception in processing SOAP request", e);
			responseBody = buildSOAPFault(e);
			status = HttpURLConnection.HTTP_BAD_REQUEST;
		}

		return new ResponseDefinitionBuilder()
				.withStatus(status)
				.withHeader(CONTENT_TYPE, TEXT_XML)
				.withBody(responseBody)
				.build();

	}

	private String buildSOAPFault(final Exception origExec) {
		try {
			SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
			soapMessage.getMimeHeaders().addHeader(CONTENT_TYPE, TEXT_XML);

			SOAPFault fault = soapMessage.getSOAPBody().addFault();
			StringWriter exceptionWriter = new StringWriter();
			origExec.printStackTrace(new PrintWriter(exceptionWriter));
			fault.setFaultString(exceptionWriter.toString());

			ByteArrayOutputStream soapFaultByteStream = new ByteArrayOutputStream();
			soapMessage.writeTo(soapFaultByteStream);
			return new String(soapFaultByteStream.toByteArray(), Charset.defaultCharset());

		} catch (Exception e) {
			LOGGER.error("Unexpected error generating SOAPFault response", e);
		}

		return StringUtils.EMPTY;

	}

	private String marshallResponse(final GetInventoryResponse inventoryResponse)
			throws ParserConfigurationException, JAXBException, SOAPException, IOException {

		Document inventoryRespDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		Marshaller marshaller = JAXBContext.newInstance(GetInventoryResponse.class).createMarshaller();
		marshaller.marshal(inventoryResponse, inventoryRespDoc);

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		soapMessage.getMimeHeaders().addHeader(CONTENT_TYPE, TEXT_XML);
		soapMessage.getSOAPBody().addDocument(inventoryRespDoc);

		ByteArrayOutputStream invRespByteStream = new ByteArrayOutputStream();
		soapMessage.writeTo(invRespByteStream);

		return new String(invRespByteStream.toByteArray(), Charset.defaultCharset());
	}

	private GetInventoryResponse buildInventoryResponse(final GetInventoryRequest inventoryRequest) {
		GetInventoryResponse inventoryResponse = new GetInventoryResponse();
		inventoryRequest.getInventoryLine().forEach(invLine -> {
			invLine.setDcId(DC_ID_VALUE);
			invLine.setQuantity(QUANTITY_VALUE);
		});
		inventoryResponse.setInventoryLine(inventoryRequest.getInventoryLine());
		return inventoryResponse;
	}

	private GetInventoryRequest unmarshallRequest(final Request request) throws SOAPException, IOException, JAXBException {
		MessageFactory messageFactory = MessageFactory.newInstance();
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader(CONTENT_TYPE, TEXT_XML);

		InputStream inputStream = new ByteArrayInputStream(request.getBody());
		SOAPMessage soapMessage = messageFactory.createMessage(mimeHeaders, inputStream);
		SOAPBody soapBody = soapMessage.getSOAPBody();

		JAXBContext jaxbContext = JAXBContext.newInstance(GetInventoryRequest.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		return (GetInventoryRequest) jaxbUnmarshaller.unmarshal(soapBody.extractContentAsDocument());
	}

	@Override
	public String getName() {
		return "example-soap-transformer";
	}
}

