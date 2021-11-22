package com.elasticpath.definitions.data;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.definitions.api.helpers.StepsHelper;
import com.elasticpath.definitions.data.database.QueryBuilder;
import com.elasticpath.definitions.stateobjects.ObjectsContext;
import com.elasticpath.definitions.stateobjects.Projection;
import com.elasticpath.definitions.stateobjects.content.BrandProjectionContent;
import com.elasticpath.definitions.stateobjects.content.OptionProjectionContent;
import com.elasticpath.definitions.utils.DataHelper;
import com.elasticpath.selenium.util.Utility;

/**
 * Syndication test data population steps.
 */
public class PopulationDefinition {

	private static final String KEY_DELETED = "deleted";
	private static final String KEY_LOCALE = "languageLocale";
	private static final String KEY_VERSION = "version";
	private static final String KEY_DATE_OFFSET = "projectionDateTimePastOffset";
	private final ObjectsContext objectsContext;
	private final Projection projection;
	private final OptionProjectionContent optionProjectionContent;
	private final BrandProjectionContent brandProjectionContent;

	/**
	 * Constructor.
	 *
	 * @param objectContext           objects context state object
	 * @param projection              projection state object
	 * @param optionProjectionContent option projection content state object
	 * @param brandProjectionContent  brand projection content state object
	 */
	public PopulationDefinition(final ObjectsContext objectContext, final Projection projection,
								final OptionProjectionContent optionProjectionContent, final BrandProjectionContent brandProjectionContent) {
		this.objectsContext = objectContext;
		this.projection = projection;
		this.brandProjectionContent = brandProjectionContent;
		this.optionProjectionContent = optionProjectionContent;
	}

	/**
	 * Creates a new option projection which has generated code (randomly), store code (randomly), projection date time (now),
	 * content hash (calculated based on content).
	 *
	 * @param projection new projection parameters
	 */
	@Then("^I have option projection with generated content$")
	public void createOptionProjection(final Map<String, String> projection) {
		this.projection.setStore(Utility.getRandomUUID());
		createOptionProjectionWithoutStore(projection);
	}

	/**
	 * Creates new option projections which have generated code (randomly), one for all store code (randomly), projection date time (now),
	 * content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I have option projections for one store with the following parameters$")
	public void createOptionProjectionsRandomCodes(final Map<String, String> parameters) {
		createOptionProjections(parameters);
	}

	/**
	 * Creates new brand projections which have generated code (randomly), one for all store code (randomly), projection date time (now),
	 * content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I have brand projection(?:s) for one store with the following parameters$")
	public void createBrandProjectionsRandomCodes(final Map<String, String> parameters) {
		List<String> codes = new ArrayList<>();
		List<String> projectionDateOffsets = new ArrayList<>();
		String store = Optional.ofNullable(parameters.get("store"))
				.filter(StringUtils::isNotEmpty)
				.orElse(Utility.getRandomUUID());
		String unparsedCodes = Optional.ofNullable(parameters.get("codes"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		if (!"".equals(unparsedCodes)) {
			codes = StepsHelper.parseByComma(unparsedCodes);
		}
		List<String> brandsDisplayNames = StepsHelper.parseByComma(parameters.get("displayNames"));
		List<String> versions = StepsHelper.parseByComma(parameters.get("versions"));
		List<String> deletedFlags = StepsHelper.parseByComma(parameters.get(KEY_DELETED));
		List<String> schemaVersions = StepsHelper.parseByComma(parameters.get("schemaVersions"));
		String unparsedDateOffsets = Optional.ofNullable(parameters.get("projectionDateTimePastOffsets"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		if (!"".equals(unparsedDateOffsets)) {
			projectionDateOffsets = StepsHelper.parseByComma(unparsedDateOffsets);
		}
		Map<String, String> projectionMetadata = new HashMap<>();
		projection.setStore(store);
		int projectionIndex = 0;
		for (String name : brandsDisplayNames) {
			this.brandProjectionContent.setContent(parameters.get(KEY_LOCALE), name);
			this.projection.setContent(this.brandProjectionContent.getContent());
			this.projection.setContentHash(DigestUtils.sha256Hex(this.brandProjectionContent.getContent()));
			projectionMetadata.put(KEY_VERSION, versions.get(projectionIndex));
			projectionMetadata.put(KEY_DELETED, deletedFlags.get(projectionIndex));
			projectionMetadata.put("schemaVersion", schemaVersions.get(projectionIndex));
			if (projectionDateOffsets.isEmpty()) {
				projectionMetadata.put(KEY_DATE_OFFSET, "");
			} else {
				projectionMetadata.put(KEY_DATE_OFFSET, projectionDateOffsets.get(projectionIndex));
			}
			if (codes.isEmpty()) {
				this.projection.setCode(Utility.getRandomUUID());
				createProjectionWithoutCode(projectionMetadata, Projection.BRAND_TYPE);
			} else {
				projection.setCode(codes.get(projectionIndex) + Utility.getRandomUUID());
				createProjectionWithoutCode(projectionMetadata, Projection.BRAND_TYPE);
			}
			projectionIndex++;
		}
	}

	/**
	 * Creates new option projections which have generated code (randomly, but starting from specified string), one for all store code (randomly),
	 * projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I (?:have|add) option projections for one store with codes starting from specified parameter")
	public void createOptionProjectionsCodesProvided(final Map<String, String> parameters) {
		createOptionProjections(parameters);
	}

	/**
	 * Creates new brand projections which have generated code (randomly, but starting from specified string), one for all store code (randomly),
	 * projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I (?:have|add) brand projections for one store with codes starting from specified parameter")
	public void createBrandProjectionsCodesProvided(final Map<String, String> parameters) {
		createBrandProjectionsRandomCodes(parameters);
	}

	/**
	 * Creates new option projections which have generated code (randomly, but starting from specified string), uses created previously store code,
	 * projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I (?:have|add) option projections for previously generated store with codes starting from specified parameter")
	public void createOptionProjectionsCodesProvidedWithoutStore(final Map<String, String> parameters) {
		createOptionProjectionsWithoutStore(parameters, projection.getStore());
	}

	/**
	 * Creates new brand projections which have generated code (randomly, but starting from specified string), uses created previously store code,
	 * projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	@Then("^I (?:have|add) brand projections for previously generated store with codes starting from specified parameter")
	public void createBrandProjectionsCodesProvidedWithoutStore(final Map<String, String> parameters) {
		Map<String, String> completeParameters = new HashMap<>(parameters);
		completeParameters.put("store", projection.getStore());
		createBrandProjectionsRandomCodes(completeParameters);
	}

	/**
	 * Creates new option projections which have generated code (randomly, but starting from specified string if provided),
	 * one for all store code (randomly), projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	private void createOptionProjections(final Map<String, String> parameters) {
		createOptionProjectionsWithoutStore(parameters, Utility.getRandomUUID());
	}

	/**
	 * Creates new option projections which have generated code (randomly, but starting from specified string if provided),
	 * uses provided store code, projection date time (now), content hash (calculated based on content).
	 *
	 * @param parameters new projections parameters
	 */
	private void createOptionProjectionsWithoutStore(final Map<String, String> parameters, final String store) {
		List<String> codes = new ArrayList<>();
		List<String> projectionDateOffsets = new ArrayList<>();
		String unparsedCodes = Optional.ofNullable(parameters.get("codes"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		if (!"".equals(unparsedCodes)) {
			codes = StepsHelper.parseByComma(unparsedCodes);
		}
		List<String> optionsDisplayNames = StepsHelper.parseByComma(parameters.get("displayNames"));
		List<String> optionsValues = StepsHelper.parseByComma(parameters.get("optionValues"));
		List<String> optionsDisplayValues = StepsHelper.parseByComma(parameters.get("optionValueNames"));
		List<String> versions = StepsHelper.parseByComma(parameters.get("versions"));
		List<String> deletedFlags = StepsHelper.parseByComma(parameters.get(KEY_DELETED));
		List<String> schemaVersions = StepsHelper.parseByComma(parameters.get("schemaVersions"));
		String unparsedDateOffsets = Optional.ofNullable(parameters.get("projectionDateTimePastOffsets"))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		if (!"".equals(unparsedDateOffsets)) {
			projectionDateOffsets = StepsHelper.parseByComma(unparsedDateOffsets);
		}
		Map<String, String> projectionMetadata = new HashMap<>();
		int projectionIndex = 0;
		for (String name : optionsDisplayNames) {
			this.optionProjectionContent.setContent(
					parameters.get(KEY_LOCALE),
					name,
					optionsValues.get(projectionIndex),
					optionsDisplayValues.get(projectionIndex)
			);
			projection.setStore(store);
			this.projection.setContent(optionProjectionContent.getContent());
			this.projection.setContentHash(DigestUtils.sha256Hex(optionProjectionContent.getContent()));
			projectionMetadata.put(KEY_VERSION, versions.get(projectionIndex));
			projectionMetadata.put(KEY_DELETED, deletedFlags.get(projectionIndex));
			projectionMetadata.put("schemaVersion", schemaVersions.get(projectionIndex));
			if ("".equals(unparsedDateOffsets)) {
				projectionMetadata.put(KEY_DATE_OFFSET, unparsedDateOffsets);
			} else {
				projectionMetadata.put(KEY_DATE_OFFSET, projectionDateOffsets.get(projectionIndex));
			}
			if (codes.isEmpty()) {
				createOptionProjectionWithoutStore(projectionMetadata);
			} else {
				projection.setCode(codes.get(projectionIndex) + Utility.getRandomUUID());
				createProjectionWithoutCode(projectionMetadata, Projection.OPTION_TYPE);
			}
			projectionIndex++;
		}
	}

	/**
	 * Creates a new option projection which has generated previously store code, new generated code (randomly), projection date time (now),
	 * content hash (calculated based on content).
	 *
	 * @param projection new projection parameters
	 */
	@Then("^I have option projection for previously generated store with generated content$")
	public void createOptionProjectionWithoutStore(final Map<String, String> projection) {
		this.projection.setCode(Utility.getRandomUUID());
		this.projection.setContent(optionProjectionContent.getContent());
		this.projection.setContentHash(DigestUtils.sha256Hex(optionProjectionContent.getContent()));
		createProjectionWithoutCode(projection, Projection.OPTION_TYPE);
	}

	/**
	 * Creates a new option projection which has generated previously code and store code, projection date time (now),
	 * content hash (calculated based on content).
	 *
	 * @param projection new projection parameters
	 */
	private void createProjectionWithoutCode(final Map<String, String> projection, final String type) {
		QueryBuilder executor = new QueryBuilder();
		this.projection.setVersion(projection.get(KEY_VERSION));
		String projectionDateOffset = Optional.ofNullable(projection.get(KEY_DATE_OFFSET))
				.filter(StringUtils::isNotEmpty)
				.orElse("");
		if ("".equals(projectionDateOffset)) {
			this.projection.setProjectionDateTime(Calendar.getInstance().getTime());
		} else {
			this.projection.setProjectionDateTime(DataHelper.getDateWithOffset(projectionDateOffset));
		}
		this.projection.setDeleted(Integer.parseInt(projection.get(KEY_DELETED)));
		this.projection.setSchemaVersion(projection.get("schemaVersion"));
		objectsContext.addProjection(this.projection);
		executor.createProjection(
				type,
				this.projection.getStore(),
				this.projection.getCode(),
				this.projection.getVersion(),
				DataHelper.DATE_FORMAT.format(this.projection.getProjectionDateTime()),
				this.projection.getDeleted(),
				this.projection.getSchemaVersion(),
				this.projection.getContentHash(),
				this.projection.getContent(),
				UUID.randomUUID().toString()
		);
	}

	/**
	 * Generates a new option projection content JSON.
	 *
	 * @param projection new projection content parameters
	 */
	@Then("^I have option projection content with one language and one value$")
	public void createOptionProjectionContent(final Map<String, String> projection) {
		this.optionProjectionContent.setContent(
				projection.get(KEY_LOCALE),
				projection.get("displayName"),
				projection.get("optionValue"),
				projection.get("optionValueName")
		);
	}

	/**
	 * Updates option projection with provided values.
	 *
	 * @param projection new projection parameters
	 */
	@Then("^I edit generated option projection with the following parameters$")
	public void editOptionProjection(final Map<String, String> projection) {
		setNewProjectionMetadata(projection);
		setNewOptionContent(projection);
		objectsContext.updateProjection(this.projection);
		updateProjectionDb(Projection.OPTION_TYPE);
	}

	/**
	 * Updates brand projection with provided values.
	 *
	 * @param projection new projection parameters
	 */
	@Then("^I edit generated brand projection with the following parameters$")
	public void editBrandProjection(final Map<String, String> projection) {
		setNewProjectionMetadata(projection);
		setNewBrandContent(projection);
		objectsContext.updateProjection(this.projection);
		updateProjectionDb(Projection.BRAND_TYPE);
	}

	/**
	 * Updates Projection object using new provided version, deleted, date values.
	 *
	 * @param projection new projection parameters
	 */
	private void setNewProjectionMetadata(final Map<String, String> projection) {
		String version = Optional.ofNullable(projection.get(KEY_VERSION))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(this.projection::getVersion);
		String deleted = Optional.ofNullable(projection.get(KEY_DELETED))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(this.projection.getDeleted()));
		this.projection.setVersion(version);
		this.projection.setProjectionDateTime(Calendar.getInstance().getTime());
		this.projection.setDeleted(Integer.parseInt(deleted));
	}

	/**
	 * Updates Content and Projection objects using new provided content values.
	 *
	 * @param projection new projection parameters
	 */
	private void setNewOptionContent(final Map<String, String> projection) {
		String deleted = Optional.ofNullable(projection.get(KEY_DELETED))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(this.projection.getDeleted()));
		if ("1".equals(deleted)) {
			optionProjectionContent.setEmptyContent();
			this.projection.setSchemaVersion("");
			this.projection.setContentHash("");
		} else {
			optionProjectionContent.setContent(
					projection.get(KEY_LOCALE),
					projection.get("displayName"),
					projection.get("optionValue"),
					projection.get("optionValueName")
			);
			this.projection.setContentHash(DigestUtils.sha256Hex(optionProjectionContent.getContent()));
		}
		this.projection.setContent(optionProjectionContent.getContent());
	}

	/**
	 * Updates Content and Projection objects using new provided content values.
	 *
	 * @param projection new projection parameters
	 */
	private void setNewBrandContent(final Map<String, String> projection) {
		String deleted = Optional.ofNullable(projection.get(KEY_DELETED))
				.filter(StringUtils::isNotEmpty)
				.orElseGet(() -> String.valueOf(this.projection.getDeleted()));
		if ("1".equals(deleted)) {
			brandProjectionContent.setEmptyContent();
			this.projection.setSchemaVersion("");
			this.projection.setContentHash("");
		} else {
			brandProjectionContent.setContent(
					projection.get(KEY_LOCALE),
					projection.get("displayName")
			);
			this.projection.setContentHash(DigestUtils.sha256Hex(brandProjectionContent.getContent()));
		}
		this.projection.setContent(brandProjectionContent.getContent());
	}

	private void updateProjectionDb(final String type) {
		QueryBuilder executor = new QueryBuilder();
		executor.updateOptionProjection(
				type,
				this.projection.getStore(),
				this.projection.getCode(),
				this.projection.getVersion(),
				DataHelper.DATE_FORMAT.format(this.projection.getProjectionDateTime()),
				this.projection.getDeleted(),
				this.projection.getSchemaVersion(),
				this.projection.getContentHash(),
				this.projection.getContent()
		);
	}

	/**
	 * Removes populated option projection using projection state object parameters.
	 */
	@After(value = "@cleanUpOptionProjection")
	public void deleteOptionProjection() {
		QueryBuilder executor = new QueryBuilder();
		executor.deleteOptionProjection(this.projection.getStore(), this.projection.getCode(), this.projection.getVersion());
	}

}
