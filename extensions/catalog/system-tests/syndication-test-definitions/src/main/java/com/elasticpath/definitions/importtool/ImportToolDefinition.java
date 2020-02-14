package com.elasticpath.definitions.importtool;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import cucumber.api.java.en.When;

import com.elasticpath.definitions.api.helpers.Constants;
import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.importtool.database.QueryBuilder;

/**
 * Syndication import tool steps.
 */
public class ImportToolDefinition {

	private List<String> objectCodes;
	private String objectType;

	/**
	 * Retrieves specified amount of randomly chosen domain objects of specified type.
	 *
	 * @param objectsAmount amount of the domain objects to retrieve from database
	 * @param objectType    a type of the domain objects to select
	 * @throws SQLException if SQL query failed
	 */
	@When("^I randomly select (\\d+) (.+) domain objects$")
	public void validateResponseCode(final int objectsAmount, final String objectType) throws SQLException {
		this.objectType = objectType;
		QueryBuilder builder = new QueryBuilder();
		List<String> codes = builder.selectObjects(objectType);
		assertThat(objectsAmount)
				.as("There are less domain objects in Db than specified amount: " + objectsAmount)
				.isLessThanOrEqualTo(codes.size());
		if (objectsAmount == codes.size()) {
			objectCodes = codes;
		} else {
			objectCodes = new ArrayList<>();
			for (int i = 0; i < objectsAmount; i++) {
				objectCodes.add(codes.get(ThreadLocalRandom.current().nextInt(0, codes.size())));
			}
		}
	}

	/**
	 * Verifies that selected previously domain objects have corresponding projections.
	 *
	 * @throws SQLException if SQL query failed
	 */
	@When("^corresponding projections were created in the system$")
	public void validateResponseCode() throws SQLException {
		QueryBuilder builder = new QueryBuilder();
		StepsHelper.sleep(Constants.API_SLEEP_TIME);
		for (String code : objectCodes) {
			assertThat(builder.ifProjectionExists(code, this.objectType))
					.as("There are no projections for required domain object: " + this.objectType + " - " + code)
					.isTrue();
		}
	}
}
