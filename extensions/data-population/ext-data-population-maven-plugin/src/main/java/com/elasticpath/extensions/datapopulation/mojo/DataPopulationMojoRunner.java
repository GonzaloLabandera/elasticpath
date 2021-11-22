/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.extensions.datapopulation.mojo;

import java.io.File;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import com.elasticpath.datapopulation.core.DataPopulationContextInitializer;
import com.elasticpath.datapopulation.core.DataPopulationCore;
import com.elasticpath.datapopulation.core.context.configurer.FilterActionConfiguration;

/**
 * The main maven plugin runner for data population. Provides validation on the directory inputs.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.INSTALL)
public class DataPopulationMojoRunner extends AbstractMojo {

	private static final String SPRING_PROFILES_ACTIVE_KEY = "spring.profiles.active";

    private BeanFactory beanFactory;
    
    @Parameter(property = "command", required = true)
    private String command;
	
    @Parameter(property = "data.directory", required = true)
    private File dataDirectory;

    @Parameter(property = "config.directory", required = true)
    private File configDirectory;

    @Parameter(property = "working.directory", required = true)
    private File workingDirectory;
    
    @Parameter(property = "output.directory")
    private File outputDirectory;
    
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Running Data Population Maven Plugin");
		
    	if (StringUtils.isEmpty(command)) {
    		throw new MojoExecutionException("No data population command specified.");
    	}
        validatePath(dataDirectory, "dataDirectory", true);
        validatePath(configDirectory, "configDirectory", true);

        if ("filter-data".equals(command)) {
        	validatePath(outputDirectory, "outputDirectory", false);
        }

		setSpringProfilesAsSystemPropertyIfPossible();

        configureLog4j();
        initializeBeanFactory();

        DataPopulationCore dpCore = (DataPopulationCore) beanFactory.getBean("dataPopulationCore");
        if ("filter-data".equals(command)) {
            FilterActionConfiguration actionConfigurer = new FilterActionConfiguration();
            actionConfigurer.setFilterOutputDirectory(outputDirectory);
            dpCore.getDataPopulationContext().setActionConfiguration(actionConfigurer);
        }
        dpCore.runActionExecutor(command);
        
        getLog().info("Data Population finished. Exiting.");
	}
	
	/**
	 * Helper method to precheck the directory is valid. The conditions are 
	 * 1) Not Null
	 * 2) Exists if the required boolean value is set to true
	 * 
	 * @param directory the directory examined
	 * @param pathType the referred pathname
	 * @param preExist true if the path needs to exist before running data population
	 * @throws MojoExecutionException are thrown when the directory does not pass validation
	 */
	protected void validatePath(final File directory, final String pathType, final boolean preExist) throws MojoExecutionException {
    	if (directory == null) {
    	    throw new MojoExecutionException("Invalid parameter - no '" + pathType + "' specified.");
    	} else if (preExist && !directory.exists()) {
    	    throw new MojoExecutionException("Invalid parameter - (" + directory + ") does not exist");
        }
    }

	/**
	 * Set spring.profiles.active system property if the same one is found in the pom.xml.
	 */
	protected void setSpringProfilesAsSystemPropertyIfPossible() {
		Stream.of(getActiveSpringProfilesEnvironmentProperty(),
				getActiveSpringProfilesSystemProperty(),
				getActiveSpringProfilesMavenProjectProperty())
				.filter(StringUtils::isNotBlank)
				.findFirst()
				.ifPresent(this::overridesActiveSpringProfiles);
	}

	private String getActiveSpringProfilesEnvironmentProperty() {
		return System.getenv("PROFILE");
	}

	private String getActiveSpringProfilesSystemProperty() {
		return System.getProperty(SPRING_PROFILES_ACTIVE_KEY);
	}

	private String getActiveSpringProfilesMavenProjectProperty() {
		return ((MavenProject) getPluginContext().get("project")).getProperties().getProperty(SPRING_PROFILES_ACTIVE_KEY);
	}

	private void overridesActiveSpringProfiles(final String activeSpringProfiles) {
		System.setProperty(SPRING_PROFILES_ACTIVE_KEY, activeSpringProfiles);
	}

    private void initializeBeanFactory() {
        getLog().info("Initializing spring context from conf/spring/ep-data-population-maven.xml");

        ApplicationContext context = DataPopulationContextInitializer.initializeContext(
                dataDirectory.getAbsolutePath(),
                configDirectory.getAbsolutePath(),
                workingDirectory.getAbsolutePath(),
                "conf/spring/ep-data-population-maven.xml"
        );
	    beanFactory = context;
    }

    private void configureLog4j() {
		// Cast to the concrete Logger so we can add the appender
		if (!((Logger) LogManager.getRootLogger()).getAppenders().isEmpty()) {
			return;
		}

		PatternLayout patternLayout = PatternLayout.newBuilder().withPattern("[%p] %m%n").build();
		ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().setLayout(patternLayout).build();
		((Logger) LogManager.getRootLogger()).addAppender(consoleAppender);
    }
}
