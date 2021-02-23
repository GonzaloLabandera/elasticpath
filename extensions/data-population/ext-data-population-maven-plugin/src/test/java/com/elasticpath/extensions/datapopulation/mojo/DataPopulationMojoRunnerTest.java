package com.elasticpath.extensions.datapopulation.mojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class DataPopulationMojoRunnerTest {

	private DataPopulationMojoRunner runner = new DataPopulationMojoRunner();
	private MavenProject mavenProject = new MavenProject();
	private Map<String, MavenProject> pluginContext = new HashMap<>();

	private static final String PATH_TYPE = "dataDirectory";
	private static final String SPRING_PROFILES_ACTIVE_KEY = "spring.profiles.active";

	@Before
	public void setUp() {
		System.clearProperty(SPRING_PROFILES_ACTIVE_KEY);
		pluginContext.put("project", mavenProject);
		runner.setPluginContext(pluginContext);

	}
	
	@Test(expected = MojoExecutionException.class)
	public void testValidationNullPathForRequiredDirectory() throws MojoExecutionException {
		runner.validatePath(null, PATH_TYPE, true);
	}

	@Test(expected = MojoExecutionException.class)
	public void testValidationNullPathForNonrequiredDirectory() throws MojoExecutionException {
		runner.validatePath(null, PATH_TYPE, false);
	}
	
	@Test
	public void testValidationRelativePathForRequiredDirectory() throws MojoExecutionException {
		File file = new File("./tmp");
		file.mkdir();
		runner.validatePath(file, PATH_TYPE, true);
		file.delete();
	}
	
	@Test
	public void testValidationRelativePathForNonrequiredDirectory() throws MojoExecutionException {
		File file = new File("./tmp");
		runner.validatePath(file, PATH_TYPE, false);
	}

	@Test
	public void testActiveSpringProfilesAreOverridenWithSystemProperty() throws MojoExecutionException {
		String activeSpringProfiles = System.getProperty(SPRING_PROFILES_ACTIVE_KEY);
		assertNull(activeSpringProfiles);

		String expectedProfiles = "profile1, profile2";
		System.setProperty(SPRING_PROFILES_ACTIVE_KEY, expectedProfiles);

		runner.setSpringProfilesAsSystemPropertyIfPossible();
		assertEquals(expectedProfiles, System.getProperty(SPRING_PROFILES_ACTIVE_KEY));
	}

	@Test
	public void testActiveSpringProfilesAreOverridenWithMavenProjectProperty() throws MojoExecutionException {
		String activeSpringProfiles = System.getProperty(SPRING_PROFILES_ACTIVE_KEY);
		assertNull(activeSpringProfiles);

		String expectedProfiles = "profile1, profile2";
		((MavenProject) runner.getPluginContext().get("project")).getProperties().setProperty(SPRING_PROFILES_ACTIVE_KEY, expectedProfiles);

		runner.setSpringProfilesAsSystemPropertyIfPossible();
		assertEquals(expectedProfiles, System.getProperty(SPRING_PROFILES_ACTIVE_KEY));
	}
}
