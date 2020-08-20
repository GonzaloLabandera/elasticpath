package com.elasticpath.smoketests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpHeader;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that the applications start up without error.
 */
public class ApplicationStartupTest {
	
	private static final int HTTP_STATUS_OK = 200;

	final Properties settings = new Properties();

	private JMXClient jmxClient;

	@Before
	public void before() throws Exception {
		settings.load(ApplicationStartupTest.class.getResourceAsStream("/test.properties"));
		jmxClient = new JMXClient(settings.getProperty("remote.jmx.port"));
	}

	/**
	 * Test search server.
	 *
	 * @throws com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException the failing http status code exception
	 * @throws java.net.MalformedURLException the malformed url exception
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSearchServer() throws FailingHttpStatusCodeException, IOException {

		final WebClient webClient = new WebClient();
		final Page page = webClient.getPage(getStatusUrl("search.context"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof TextPage);

		webClient.close();
	}

	/**
	 * Test batch server.
	 *
	 * @throws com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException the failing http status code exception
	 * @throws java.net.MalformedURLException the malformed url exception
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testBatchServer() throws FailingHttpStatusCodeException, IOException {
		final String quartzInstanceMBeanName = "quartz:type=QuartzScheduler,name=BatchJmxScheduler,instanceId=NONE_CLUSTER";
		final WebClient webClient = new WebClient();
		final Page page = webClient.getPage(getStatusUrl("batch.context"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof TextPage);
		assertNotNull("Quartz instance not found", jmxClient.getObjectInstance(quartzInstanceMBeanName));

		webClient.close();
	}

	/**
	 * Test Commerce Manager.
	 *
	 * @throws com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException the failing http status code exception
	 * @throws java.net.MalformedURLException the malformed url exception
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testCM() throws FailingHttpStatusCodeException, IOException {

		WebClient webClient = new WebClient();
		final String cmUrl = getBaseUrl() + settings.getProperty("cm.context");

		Page page = webClient.getPage(cmUrl + settings.getProperty("cm.status.url"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof TextPage);

		webClient.close();

		webClient = new WebClient();

		String rwtBody="{\"head\":{\"requestCounter\":0},\"operations\":[]}";
		//verify that home page is loaded
		WebRequest postRequest = new WebRequest(new URL(cmUrl + "/"), HttpMethod.POST);

		postRequest.setEncodingType(FormEncodingType.TEXT_PLAIN);
		postRequest.setAdditionalHeader(HttpHeader.CONTENT_TYPE, "application/json");

		postRequest.setRequestBody(rwtBody);

		try {
			page = webClient.getPage(postRequest);

			assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		} catch (Exception e) {
			fail(e.getMessage());
		}

		webClient.close();
	}

	/**
	 * Test integration server.
	 *
	 * @throws FailingHttpStatusCodeException The failing http status code exception.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testIntegrationServer() throws FailingHttpStatusCodeException, IOException {

		final WebClient webClient = new WebClient();
		final Page page = webClient.getPage(getStatusUrl("integration.context"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof TextPage);

		webClient.close();
	}

	@Test
	public void testActiveMQWebConsole() throws FailingHttpStatusCodeException, IOException {

		final WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);

		final Page page = webClient.getPage(getBaseUrl() + settings.getProperty("activemq.context"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof HtmlPage);

		webClient.close();
	}

	/**
	 * Test cortex server.
	 *
	 * @throws com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException the failing http status code exception
	 * @throws java.net.MalformedURLException the malformed url exception
	 * @throws java.io.IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testCortexServer() throws FailingHttpStatusCodeException, IOException {
		final WebClient webClient = new WebClient();
		final Page page = webClient.getPage(getStatusUrl("cortex.context"));

		assertEquals(HTTP_STATUS_OK, page.getWebResponse().getStatusCode());
		assertTrue(page instanceof TextPage);

		webClient.close();
	}

	private String getBaseUrl() {
		return "http://localhost:" + settings.getProperty("cargo.port");
	}

	private String getStatusUrl(final String contextProperty) {
		return getBaseUrl() + settings.getProperty(contextProperty) + "/status";
	}

}
