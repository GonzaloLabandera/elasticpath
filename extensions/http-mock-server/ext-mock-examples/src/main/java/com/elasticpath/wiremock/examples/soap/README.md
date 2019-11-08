# DynamicSoapResponseDefinitionTransformer

Package: `com.elasticpath.wiremock.examples.soap`

This is an example ResponseDefinitionTransformer that takes a SOAP request and uses JAXB to unmarshall the request.  

A set of response POJOs are then populated with the same number of records as the request.  JAXB is used to 
marshall the POJOs back into XML and then they are wrapped with a SOAP envelope and the response is returned.

HTTP Verb: `POST`

Content-Type: `text/xml`

Response Code: `200` if successful, `400` if the request cannot be parsed


## Sample Request

	<?xml version="1.0"?>
	<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
		<soap:Body>
			<GetInventory>
				<InventoryLine>
					<sku>123abc</sku>
				</InventoryLine>
				<InventoryLine>
					<sku>456abc</sku>
				</InventoryLine>
				<InventoryLine>
					<sku>789abc</sku>
				</InventoryLine>
			</GetInventory>
		</soap:Body>
	</soap:Envelope>
	
## Sample Response

	<?xml version="1.0"?>
	<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
	  <SOAP-ENV:Header/>
	  <SOAP-ENV:Body>
		<GetInventoryResponse>
		  <InventoryLine>
			<sku>123abc</sku>
			<dcId>1</dcId>
			<quantity>100.0</quantity>
		  </InventoryLine>
		  <InventoryLine>
			<sku>456abc</sku>
			<dcId>1</dcId>
			<quantity>100.0</quantity>
		  </InventoryLine>
		  <InventoryLine>
			<sku>789abc</sku>
			<dcId>1</dcId>
			<quantity>100.0</quantity>
		  </InventoryLine>
		</GetInventoryResponse>
	  </SOAP-ENV:Body>
	</SOAP-ENV:Envelope>


