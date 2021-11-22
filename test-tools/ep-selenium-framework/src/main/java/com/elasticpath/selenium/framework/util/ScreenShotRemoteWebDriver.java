package com.elasticpath.selenium.framework.util;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

/**
 * Screenshot remote driver that enables a remote web driver to take a screenshot.
 */
public class ScreenShotRemoteWebDriver extends RemoteWebDriver {

	private static final Logger LOGGER = LogManager.getLogger(RemoteWebDriver.class);

	/**
	 * constructor.
	 *
	 * @param url          driver url.
	 * @param capabilities desired capabilities for driver of instance.
	 */
	public ScreenShotRemoteWebDriver(final URL url, final DesiredCapabilities capabilities) {
		super(url, capabilities);
	}

	@Override
	protected void log(final SessionId sessionId, final String commandName, final Object toLog, final When when) {
		if (when == When.BEFORE) {

			if (toLog == null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Executing: " + commandName);
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Executing: " + commandName + ", parameters: " + toLog);
				}
			}
		}
	}

	@Override
	public <X> X getScreenshotAs(final OutputType<X> target) throws WebDriverException {
		return target.convertFromBase64Png(execute(DriverCommand.SCREENSHOT).getValue().toString());
	}
}