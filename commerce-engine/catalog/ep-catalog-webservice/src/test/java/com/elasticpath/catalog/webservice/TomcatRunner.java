/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice;

import java.io.File;
import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;

/**
 * Runner for embedded Tomcat.
 */
public class TomcatRunner {

	private final String webXmlPath;
	private final int port;
	private final Tomcat tomcat = new Tomcat();

	/**
	 * Constructor.
	 *
	 * @param webXmlPath path to web.xml.
	 * @param port Tomcat port.
	 */
	public TomcatRunner(final String webXmlPath, final int port) {
		this.webXmlPath = webXmlPath;
		this.port = port;
	}

	/**
	 * Start up Tomcat server and open http connection.
	 *
	 * @throws LifecycleException Tomcat {@link LifecycleException}.
	 * @throws ServletException Tomcat {@link ServletException}.
	 */
	public void startUpTomcat() throws LifecycleException, ServletException {
		tomcat.setPort(port);

		final StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webXmlPath).getAbsolutePath());
		final StandardJarScanner standardJarScanner = new StandardJarScanner();
		standardJarScanner.setScanManifest(false);
		ctx.setJarScanner(standardJarScanner);

		tomcat.start();

		final Connector connector = new Connector("HTTP/1.1");
		connector.setPort(port);
		tomcat.setConnector(connector);
	}

	/**
	 * Stop and destroy Tomcat server.
	 *
	 * @throws LifecycleException Tomcat {@link LifecycleException}.
	 */
	public void shutDownTomcat() throws LifecycleException {
		tomcat.stop();
		tomcat.destroy();
	}

}
