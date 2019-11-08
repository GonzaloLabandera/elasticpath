/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tags.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.tags.domain.Condition;
import com.elasticpath.tags.domain.LogicalOperator;
import com.elasticpath.tags.domain.LogicalOperatorType;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.validation.domain.ValidationError;
import com.elasticpath.validation.domain.ValidationResult;
import com.elasticpath.validation.service.ValidationService;

/**
 * Test that condition validation facade provides a robust service for validating
 * conditions and condition trees.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConditionValidationFacadeImplTest  {

	private static final String ERROR2 = "error2";

	private static final String ERROR = "error";

	private ConditionValidationFacadeImpl validationFacade;

	@Mock
	private ValidationService validationService;

	/**
	 * Setups test.
	 */
	@Before
	public void setUp() {
		validationFacade = new ConditionValidationFacadeImpl();
		validationFacade.setValidationService(validationService);
	}

	/**
	 * Test that ensures that null condition as argument is not allowed.
	 */
	@Test
	public void testValidateSingleNullCondition() {
		final Condition condition = null;
		assertThatThrownBy(() -> validationFacade.validate(condition))
			.as("Must not evaluate on null conditions")
			.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test that ensures that null as tag definition of condition as argument is not allowed.
	 */
	@Test
	public void testValidateSingleNullTagDefinitionOfCondition() {
		final Condition condition = new Condition(null, "", "", "");
		assertThatThrownBy(() -> validationFacade.validate(condition))
			.as("Must not evaluate on conditions with null tag definition")
			.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test that ensures that null as tag value type of a tag definition of condition as argument
	 * is not allowed.
	 */
	@Test
	public void testValidateSingleNullTagValueTypeOfTagDefinitionOfCondition() {
		final TagDefinition tagDefinition = mock(TagDefinition.class, "tagDefinition");
		when(tagDefinition.getValueType()).thenReturn(null);

		final Condition condition = new Condition(tagDefinition, "", "", "");
		assertThatThrownBy(() -> validationFacade.validate(condition))
			.as("Must not evaluate on conditions with null tag definition")
			.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test that ensures that a valid condition will return a valid validation result.
	 */
	@Test
	public void testValidateSingleConditionSuccess() {
		final TagDefinition tagDefinition = mock(TagDefinition.class, "tagDefinition");
		final TagValueType tagValueType = mock(TagValueType.class, "tagValueType");
		final ValidationResult result = mock(ValidationResult.class, "validationResult");
		final Condition condition = new Condition(tagDefinition, "", "", "");

		when(tagDefinition.getValueType()).thenReturn(tagValueType);
		when(tagValueType.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition, null)).thenReturn(result);

		assertThat(validationFacade.validate(condition)).isEqualTo(result);
		verify(validationService).validate(condition, null);
	}

	/**
	 * Test that ensures that null logical trees as argument is not allowed.
	 */
	@Test
	public void testValidateTreeNullOperator() {
		final LogicalOperator tree = null;
		assertThatThrownBy(() -> validationFacade.validateTree(tree))
			.as("Must not evaluate on null root nodes")
			.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test that a valid condition tree will be traversed by the service and return a
	 * valid result.
	 */
	@Test
	public void testValidateTreeSuccess() {

		final TagDefinition tagDefinition11 = mock(TagDefinition.class, "tagDefinition11");
		final TagValueType tagValueType11 = mock(TagValueType.class, "tagValueType11");
		final ValidationResult result11 = mock(ValidationResult.class, "validationResult11");
		final Condition condition11 = new Condition(tagDefinition11, "", "", "");

		final TagDefinition tagDefinition12 = mock(TagDefinition.class, "tagDefinition12");
		final TagValueType tagValueType12 = mock(TagValueType.class, "tagValueType12");
		final ValidationResult result12 = mock(ValidationResult.class, "validationResult12");
		final Condition condition12 = new Condition(tagDefinition12, "", "", "");

		final TagDefinition tagDefinition21 = mock(TagDefinition.class, "tagDefinition21");
		final TagValueType tagValueType21 = mock(TagValueType.class, "tagValueType21");
		final ValidationResult result21 = mock(ValidationResult.class, "validationResult21");
		final Condition condition21 = new Condition(tagDefinition21, "", "", "");

		final TagDefinition tagDefinition31 = mock(TagDefinition.class, "tagDefinition31");
		final TagValueType tagValueType31 = mock(TagValueType.class, "tagValueType31");
		final ValidationResult result31 = mock(ValidationResult.class, "validationResult31");
		final Condition condition31 = new Condition(tagDefinition31, "", "", "");

		final LogicalOperator root = new LogicalOperator(LogicalOperatorType.AND);
		root.addCondition(condition11);
		root.addCondition(condition12);

		final LogicalOperator sub1 = new LogicalOperator(LogicalOperatorType.OR);
		sub1.addCondition(condition21);
		sub1.setParentLogicalOperator(root);

		final LogicalOperator sub2 = new LogicalOperator(LogicalOperatorType.OR);
		sub2.addCondition(condition31);
		sub2.setParentLogicalOperator(sub1);

		when(tagDefinition11.getValueType()).thenReturn(tagValueType11);
		when(tagValueType11.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition11, null)).thenReturn(result11);
		when(result11.isValid()).thenReturn(true);

		when(tagDefinition12.getValueType()).thenReturn(tagValueType12);
		when(tagValueType12.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition12, null)).thenReturn(result12);
		when(result12.isValid()).thenReturn(true);

		when(tagDefinition21.getValueType()).thenReturn(tagValueType21);
		when(tagValueType21.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition21, null)).thenReturn(result21);
		when(result21.isValid()).thenReturn(true);

		when(tagDefinition31.getValueType()).thenReturn(tagValueType31);
		when(tagValueType31.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition31, null)).thenReturn(result31);
		when(result31.isValid()).thenReturn(true);

		final ValidationResult result = validationFacade.validateTree(root);
		assertThat(result.isValid()).isTrue();

		assertThat(result.getErrors())
			.isNotNull()
			.isEmpty();

		verify(validationService).validate(condition11, null);
		verify(validationService).validate(condition12, null);
		verify(validationService).validate(condition21, null);
		verify(validationService).validate(condition31, null);

	}

	/**
	 * Test that a valid condition tree with an invalid condition will produce an invalid result
	 * with error being captured.
	 */
	@Test
	public void testValidateTreeFailureWithTwoFailingConditions() {

		final TagDefinition tagDefinition11 = mock(TagDefinition.class, "tagDefinition11");
		final TagValueType tagValueType11 = mock(TagValueType.class, "tagValueType11");
		final ValidationResult result11 = mock(ValidationResult.class, "validationResult11");
		final Condition condition11 = new Condition(tagDefinition11, "", "", "");

		final TagDefinition tagDefinition12 = mock(TagDefinition.class, "tagDefinition12");
		final TagValueType tagValueType12 = mock(TagValueType.class, "tagValueType12");
		final ValidationResult result12 = mock(ValidationResult.class, "validationResult12");
		final Condition condition12 = new Condition(tagDefinition12, "", "", "");

		final TagDefinition tagDefinition21 = mock(TagDefinition.class, "tagDefinition21");
		final TagValueType tagValueType21 = mock(TagValueType.class, "tagValueType21");
		final ValidationResult result21 = mock(ValidationResult.class, "validationResult21");
		final Condition condition21 = new Condition(tagDefinition21, "", "", "");

		final TagDefinition tagDefinition31 = mock(TagDefinition.class, "tagDefinition31");
		final TagValueType tagValueType31 = mock(TagValueType.class, "tagValueType31");
		final ValidationResult result31 = mock(ValidationResult.class, "validationResult31");
		final Condition condition31 = new Condition(tagDefinition31, "", "", "");

		final LogicalOperator root = new LogicalOperator(LogicalOperatorType.AND);
		root.addCondition(condition11);
		root.addCondition(condition12);

		final LogicalOperator sub1 = new LogicalOperator(LogicalOperatorType.OR);
		sub1.addCondition(condition21);
		sub1.setParentLogicalOperator(root);

		final LogicalOperator sub2 = new LogicalOperator(LogicalOperatorType.OR);
		sub2.addCondition(condition31);
		sub2.setParentLogicalOperator(sub1);

		when(tagDefinition11.getValueType()).thenReturn(tagValueType11);
		when(tagValueType11.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition11, null)).thenReturn(result11);
		when(result11.isValid()).thenReturn(true);

		when(tagDefinition12.getValueType()).thenReturn(tagValueType12);
		when(tagValueType12.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition12, null)).thenReturn(result12);
		when(result12.isValid()).thenReturn(true);

		when(tagDefinition21.getValueType()).thenReturn(tagValueType21);
		when(tagValueType21.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition21, null)).thenReturn(result21);
		when(result21.isValid()).thenReturn(false);
		when(result21.getMessage()).thenReturn(ERROR);

		when(tagDefinition31.getValueType()).thenReturn(tagValueType31);
		when(tagValueType31.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition31, null)).thenReturn(result31);
		when(result31.isValid()).thenReturn(false);
		when(result31.getMessage()).thenReturn(ERROR2);

		final ValidationResult result = validationFacade.validateTree(root);
		assertThat(result.isValid()).isFalse();

		assertThat(result.getErrors())
			.hasSize(2)
			.extracting(ValidationError::getMessage).containsExactly(ERROR, ERROR2);

		verify(validationService).validate(condition11, null);
		verify(validationService).validate(condition12, null);
		verify(validationService).validate(condition21, null);
		verify(validationService).validate(condition31, null);

	}

	/**
	 * Test that a valid condition tree with an invalid condition will produce an invalid result
	 * with error being captured.
	 */
	@Test
	public void testValidateTreeFailure() {

		final TagDefinition tagDefinition11 = mock(TagDefinition.class, "tagDefinition11");
		final TagValueType tagValueType11 = mock(TagValueType.class, "tagValueType11");
		final ValidationResult result11 = mock(ValidationResult.class, "validationResult11");
		final Condition condition11 = new Condition(tagDefinition11, "", "", "");

		final TagDefinition tagDefinition12 = mock(TagDefinition.class, "tagDefinition12");
		final TagValueType tagValueType12 = mock(TagValueType.class, "tagValueType12");
		final ValidationResult result12 = mock(ValidationResult.class, "validationResult12");
		final Condition condition12 = new Condition(tagDefinition12, "", "", "");

		final TagDefinition tagDefinition21 = mock(TagDefinition.class, "tagDefinition21");
		final TagValueType tagValueType21 = mock(TagValueType.class, "tagValueType21");
		final ValidationResult result21 = mock(ValidationResult.class, "validationResult21");
		final Condition condition21 = new Condition(tagDefinition21, "", "", "");

		final TagDefinition tagDefinition31 = mock(TagDefinition.class, "tagDefinition31");
		final TagValueType tagValueType31 = mock(TagValueType.class, "tagValueType31");
		final ValidationResult result31 = mock(ValidationResult.class, "validationResult31");
		final Condition condition31 = new Condition(tagDefinition31, "", "", "");

		final LogicalOperator root = new LogicalOperator(LogicalOperatorType.AND);
		root.addCondition(condition11);
		root.addCondition(condition12);

		final LogicalOperator sub1 = new LogicalOperator(LogicalOperatorType.OR);
		sub1.addCondition(condition21);
		sub1.setParentLogicalOperator(root);

		final LogicalOperator sub2 = new LogicalOperator(LogicalOperatorType.OR);
		sub2.addCondition(condition31);
		sub2.setParentLogicalOperator(sub1);

		when(tagDefinition11.getValueType()).thenReturn(tagValueType11);
		when(tagValueType11.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition11, null)).thenReturn(result11);
		when(result11.isValid()).thenReturn(true);

		when(tagDefinition12.getValueType()).thenReturn(tagValueType12);
		when(tagValueType12.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition12, null)).thenReturn(result12);
		when(result12.isValid()).thenReturn(true);

		when(tagDefinition21.getValueType()).thenReturn(tagValueType21);
		when(tagValueType21.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition21, null)).thenReturn(result21);
		when(result21.isValid()).thenReturn(false);
		when(result21.getMessage()).thenReturn(ERROR);

		when(tagDefinition31.getValueType()).thenReturn(tagValueType31);
		when(tagValueType31.getValidationConstraints()).thenReturn(null);
		when(validationService.validate(condition31, null)).thenReturn(result31);
		when(result31.isValid()).thenReturn(true);

		final ValidationResult result = validationFacade.validateTree(root);
		assertThat(result.isValid()).isFalse();

		final int numberOfErrors = 1;
		assertThat(result.getErrors())
			.hasSize(numberOfErrors)
			.extracting(ValidationError::getMessage)
			.containsExactly(ERROR);

		verify(validationService).validate(condition11, null);
		verify(validationService).validate(condition12, null);
		verify(validationService).validate(condition21, null);
		verify(validationService).validate(condition31, null);

	}


}
