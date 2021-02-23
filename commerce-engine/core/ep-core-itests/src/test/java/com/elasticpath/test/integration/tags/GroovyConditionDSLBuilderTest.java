/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.tags.domain.Condition;
import com.elasticpath.tags.domain.LogicalOperator;
import com.elasticpath.tags.domain.LogicalOperatorType;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.service.ConditionValidationFacade;
import com.elasticpath.tags.service.InvalidConditionTreeException;
import com.elasticpath.tags.service.TagDefinitionReader;
import com.elasticpath.tags.service.impl.GroovyConditionDSLBuilderImpl;
import com.elasticpath.tags.service.impl.TagDefinitionReaderImpl;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.validation.domain.ValidationResult;

/**
 * {@link GroovyConditionDSLBuilderImpl} integration test.
 */
public class GroovyConditionDSLBuilderTest extends BasicSpringContextTest {
	
	private static final Logger LOG = Logger.getLogger(GroovyConditionDSLBuilderTest.class);
	
	private GroovyConditionDSLBuilderImpl groovyDSLBuilder;

	/**
	 * Create a stub of the TagDefinition reader for the test.
	 */
	class TagDefinitionReaderMock extends TagDefinitionReaderImpl {
		@Override
		public List<TagDefinition> getTagDefinitions() {
			final List<TagDefinition> tagDefinitions = new ArrayList<>();
			String[] tags = {"refererUrl", "refererUrl", "location", "age", "memberType"};
			TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
			tagValueType.setJavaType(String.class.getCanonicalName());
			for (String tagGuid : tags) {
			TagDefinition tagDefinition = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
			tagDefinition.setGuid(tagGuid);
			tagDefinition.setValueType(tagValueType);
			tagDefinitions.add(tagDefinition);			
			}
			return tagDefinitions;
		}
	}
	
	/**
	 * Mocked validation.
	 */
	class TagConditionValidationMock implements ConditionValidationFacade {
		@Override
		public ValidationResult validate(final Condition condition,
				final Object newValue) throws IllegalArgumentException {
			return ValidationResult.VALID;
		}

		@Override
		public ValidationResult validate(final Condition condition)
				throws IllegalArgumentException {
			return ValidationResult.VALID;
		}

		@Override
		public ValidationResult validateTree(
				final LogicalOperator logicalOperatorTreeRootNode)
				throws IllegalArgumentException {
			return ValidationResult.VALID;
		}
	}
	
	/**
	 * Initialize test case.
	 */
	@Before
	public void initialize() {
		groovyDSLBuilder = new GroovyConditionDSLBuilderImpl();
		TagDefinitionReader tagDefinitionReader = new TagDefinitionReaderMock();
		groovyDSLBuilder.setTagDefinitionReader(tagDefinitionReader);
		groovyDSLBuilder.setValidationFacade(new TagConditionValidationMock());
	}
	
	/**
	 * Test getting LogicalOperator Tree by seeding empty condition.
	 */
	@Test
	public void testConditionalExpressionBySeedingEmptyCondition() {		
		StringBuilder conditionalExpression = new StringBuilder("");
		
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertNull(rootNode);
	}

	/**
	 * Test getting condition string by seeding null LogicalOperator Tree.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testConditionalExpressionBySeedingNullLogicalOperatorTree() throws InvalidConditionTreeException {		
		String conditionString = groovyDSLBuilder.getConditionalDSLString(null);
		
		assertEquals(StringUtils.EMPTY, conditionString);
	}

	/**
	 * Test getting LogicalOperator Tree by seeding only one condition.
	 *
	 * { AND { refererUrl.includes 'google' } }
	 */
	@Test
	public void testConditionalExpressionBySeedingOnlyOneCondition() {
		StringBuilder conditionalExpression = new StringBuilder("{ ");
		conditionalExpression.append(LogicalOperatorType.AND);

		// refererUrl contains google
		conditionalExpression.append("\n{ refererUrl.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'google' }\n}");

		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertNotNull(rootNode);
		assertEquals(1, rootNode.getConditions().size());
	}

	/**
	 * Test getting LogicalOperator Tree by seeding only one condition in several parallel threads.
	 *
	 * { AND { refererUrl.includes 'google' } }
	 */
	@Test
	public void testConditionalExpressionBySeedingOnlyOneConditionMultithreaded() throws ExecutionException, InterruptedException {
		final int threads = 5;
		final ExecutorService service = Executors.newFixedThreadPool(threads);
		final List<Future<LogicalOperator>> rootNodes = new ArrayList<>(threads);
		final String conditionalExpression = "{ " + LogicalOperatorType.AND
				+ "\n{ refererUrl."
				+ "includes"
				+ " 'google' }\n}";

		//CountDownLatch is used to make sure that all threads will start simultaneously.
		final CountDownLatch latch = new CountDownLatch(1);
		for (int i = 0; i < threads; i++) {
			rootNodes.add(service.submit(() -> {
				//makes thread stay on hold and wait until all of them are submitted.
				latch.await();
				return groovyDSLBuilder.getLogicalOperationTree(conditionalExpression);
			}));
		}
		//releases the latch after all threads are submitted.
		latch.countDown();

		for (Future<LogicalOperator> rootNode : rootNodes) {
			assertNotNull(rootNode.get());
			assertEquals(1, rootNode.get().getConditions().size());
		}
	}

	/**
	 * Test getting LogicalOperator Tree by seeding a simple AND operation.
	 */
	@Test
	public void testConditionalExpressionBySeedingSimpleANDOperation() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.AND);
		
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}");
		
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertNotNull(rootNode);
		assertEquals(2, rootNode.getConditions().size());
	}

	/**
	 * Add two conditions to the given expression.
	 * 
	 * @param conditionalExpression an expression that conditions to be added
	 */
	private void addSomeConditions(final StringBuffer conditionalExpression) {
		// refererUrl contains google
		conditionalExpression.append("\n{ refererUrl.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'google' }\n");
		
		// memberType equals to Gold
		conditionalExpression.append("{ memberType.");
		conditionalExpression.append("equalTo");
		conditionalExpression.append(" 'GOLD' }\n");
	}
	
	/**
	 * Test getting LogicalOperator Tree by seeding a simple OR operation.
	 */
	@Test
	public void testConditionalExpressionBySeedingSimpleOROperation() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.OR);
		
		addSomeConditions(conditionalExpression);				
		conditionalExpression.append("}");

		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());

		assertNotNull(rootNode);
		assertEquals(2, rootNode.getConditions().size());
	}
		
	/**
	 * Test getting LogicalOperator Tree by seeding a simple AND operation nested by a NOT operation.
	 */
	@Test
	public void testConditionalExpressionBySeedingSimpleAND() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.AND);
		
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		
		addSomeConditions(conditionalExpression);
		
		conditionalExpression.append("}\n}");			
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertNotNull(rootNode);
		assertEquals(1, rootNode.getLogicalOperators().size());
	}
	
	/**
	 * Test getting LogicalOperator Tree by seeding a simple OR operation nested by a NOT operation.
	 */
	@Test
	public void testConditionalExpressionBySeedingSimpleOROperationNestedByAND() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.AND);
		
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.OR);
		
		addSomeConditions(conditionalExpression);
		
		conditionalExpression.append("}\n}");				
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertNotNull(rootNode);
		assertEquals(1, rootNode.getLogicalOperators().size());
	}
	
	/**
	 * Test getting LogicalOperator Tree by seeding a nested operators.
	 * 
	 * AND
	 * 	{ refererUrl.includes 'google' }
	 * 	{ memberType.equalTo 'GOLD' }
	 * 	{
	 * 		OR
	 * 			{ refererUrl.includes 'google' }
	 * 			{ memberType.equalTo 'GOLD' }
	 * 			{
	 * 				AND
	 * 					{ refererUrl.includes 'google' }
	 * 					{ memberType.equalTo 'GOLD' }
	 * 			}
	 * 	}
	 * 	{
	 * 		AND {
	 * 			AND
	 * 				{ refererUrl.includes 'google' }
	 * 				{ memberType.equalTo 'GOLD' }
	 * 		}
	 * 	}
	 *
	 */
	@Test
	public void testConditionalExpressionBySeedingNestedOperators() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.OR);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}\n}\n");		

		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}\n}\n}");		

		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());

		assertNotNull(rootNode);

		// root level checks
		assertEquals(2, rootNode.getLogicalOperators().size());
		assertEquals(2, rootNode.getConditions().size());

		// level one checks
		List<LogicalOperator> levelOneOperators = new ArrayList<>(rootNode.getLogicalOperators());

		assertEquals(2, levelOneOperators.get(0).getConditions().size());
		assertEquals(1, levelOneOperators.get(0).getLogicalOperators().size());
		assertEquals(1, levelOneOperators.get(1).getLogicalOperators().size());
		assertEquals(0, levelOneOperators.get(1).getConditions().size());

		// third level checks
		List<LogicalOperator> levelTwoOperators = new ArrayList<>(levelOneOperators.get(0).getLogicalOperators());
		assertEquals(2, levelTwoOperators.get(0).getConditions().size());
		assertEquals(0, levelTwoOperators.get(0).getLogicalOperators().size());

		levelTwoOperators = new ArrayList<>(levelOneOperators.get(1).getLogicalOperators());
		assertEquals(2, levelTwoOperators.get(0).getConditions().size());
		assertEquals(0, levelTwoOperators.get(0).getLogicalOperators().size());
	}
	
	/**
	 * Test getting LogicalOperator Tree by seeding a nested operators.
	 * 
	 * AND
	 * 	{ refererUrl.includes 'google' }
	 * 	{ memberType.equalTo 'GOLD' }
	 * 	{
	 * 		OR
	 * 			{ refererUrl.includes 'google' }
	 * 			{ memberType.equalTo 'GOLD' }
	 * 			{
	 * 				AND
	 * 					{ refererUrl.includes 'google' }
	 * 					{ memberType.equalTo 'GOLD' }
	 * 			}
	 * 	}
	 * 	{
	 * 		AND {
	 * 			AND
	 * 				{ refererUrl.includes 'google' }
	 * 				{ memberType.equalTo 'GOLD' }
	 * 		}
	 * 	}
	 *  {
	 *      AND
	 * 			{ refererUrl.includes 'google' }
	 * 			{ memberType.equalTo 'GOLD' }
	 * 			{
	 * 				OR
	 * 					{ refererUrl.includes 'google' }
	 * 					{ memberType.equalTo 'GOLD' }
	 * 			}
	 *  }
	 */
	
	@Test
	public void testConditionalExpressionBySeedingMoreNestedOperators() {		
		StringBuffer conditionalExpression = new StringBuffer("{");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.OR);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}\n}\n");		

		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}\n}\n{\n");		
		conditionalExpression.append(LogicalOperatorType.AND);
		addSomeConditions(conditionalExpression);
		
		conditionalExpression.append("{\n");
		conditionalExpression.append(LogicalOperatorType.OR);
		addSomeConditions(conditionalExpression);
		conditionalExpression.append("}\n}\n}");		

		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());

		assertNotNull(rootNode);

		// root level checks
		assertEquals(Integer.parseInt("3"), rootNode.getLogicalOperators().size());
		assertEquals(2, rootNode.getConditions().size());

		// level one checks
		List<LogicalOperator> levelOneOperators = new ArrayList<>(rootNode.getLogicalOperators());

		assertEquals(2, levelOneOperators.get(0).getConditions().size());
		assertEquals(1, levelOneOperators.get(0).getLogicalOperators().size());
		assertEquals(1, levelOneOperators.get(1).getLogicalOperators().size());
		assertEquals(0, levelOneOperators.get(1).getConditions().size());
		assertEquals(1, levelOneOperators.get(2).getLogicalOperators().size());
		assertEquals(2, levelOneOperators.get(2).getConditions().size());

		// third level checks
		List<LogicalOperator> levelTwoOperators = new ArrayList<>(levelOneOperators.get(0).getLogicalOperators());
		assertEquals(2, levelTwoOperators.get(0).getConditions().size());
		assertEquals(0, levelTwoOperators.get(0).getLogicalOperators().size());

		levelTwoOperators = new ArrayList<>(levelOneOperators.get(1).getLogicalOperators());
		assertEquals(2, levelTwoOperators.get(0).getConditions().size());
		assertEquals(0, levelTwoOperators.get(0).getLogicalOperators().size());

		levelTwoOperators = new ArrayList<>(levelOneOperators.get(2).getLogicalOperators());
		assertEquals(2, levelTwoOperators.get(0).getConditions().size());
		assertEquals(0, levelTwoOperators.get(0).getLogicalOperators().size());
	}
	
	/**
	 * Test getting ConditionalExpression string by seeding a LogicalOperator which has just one condition.
	 * 
	 * { AND { refererUrl.includes 'google' }  }
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testConditionalExpressionBySeedingLogicalOperatorTreeThatHasOneCondition() throws InvalidConditionTreeException {		
		StringBuilder conditionalExpression = new StringBuilder(" { ");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append(" { refererUrl.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'google' }  } ");
		
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertEquals(conditionalExpression.toString(), groovyDSLBuilder.getConditionalDSLString(rootNode));
	}
	
	/**
	 * Test getting ConditionalExpression string by seeding a LogicalOperator which has two conditions.
	 * 
	 * { AND { refererUrl.includes 'google' }  { memberType.equalTo 'gold' } }
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testConditionalExpressionBySeedingLogicalOperatorTreeThatHasTwoConditions() throws InvalidConditionTreeException {		
		StringBuilder conditionalExpression = new StringBuilder(" { ");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append(" { refererUrl.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'google' }  { age.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'abc' }  } ");
		
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		assertEquals(conditionalExpression.toString(), groovyDSLBuilder.getConditionalDSLString(rootNode));
	}
	
	/**
	 * Test getting ConditionalExpression string by seeding a LogicalOperator which has nested structure.
	 * 
	 * { AND { refererUrl.includes 'google' }  { memberType.equalTo 'gold' } 
	 *       { OR { AND { memberType.equalTo 'basic' }  { location.equalTo 'ca' } } }
	 * }
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testConditionalExpressionBySeedingLogicalOperatorTreeThatHasNestedStructure() throws InvalidConditionTreeException {		
		StringBuilder conditionalExpression = new StringBuilder(" { ");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append(" { refererUrl.");
		conditionalExpression.append("includes");
		conditionalExpression.append(" 'google' }  { memberType.");
		conditionalExpression.append("equalTo");
		conditionalExpression.append(" 'gold' }  { ");
		conditionalExpression.append(LogicalOperatorType.OR);
		conditionalExpression.append(" { ");
		conditionalExpression.append(LogicalOperatorType.AND);
		conditionalExpression.append(" { memberType.");
		conditionalExpression.append("equalTo");
		conditionalExpression.append(" 'gold' }  { location.");
		conditionalExpression.append("equalTo");
		conditionalExpression.append(" 'ca' }  }  }  } ");
		
		LogicalOperator rootNode = groovyDSLBuilder.getLogicalOperationTree(conditionalExpression.toString());
		
		assertEquals(conditionalExpression.toString(), groovyDSLBuilder.getConditionalDSLString(rootNode));
	}
	
	/**
	 * Test a simple condition which right operand of the condition is not wrapped with quotes. The condition string
	 * should be automatically quoted since the type of the object is String.
	 * 
	 * { AND { memberType.includes google }  }
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testAddingRightOperandsWithoutQuotingThem() throws InvalidConditionTreeException {
		LogicalOperator rootNode = new LogicalOperator(LogicalOperatorType.AND);
		
		final TagDefinition memberType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
		memberType.setGuid("memberType");
		memberType.setName("memberType");
		TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
		tagValueType.setJavaType(String.class.getCanonicalName());
		memberType.setValueType(tagValueType);
		
		rootNode.addCondition(new Condition(memberType, "memberType", "includes", "google"));
		
		assertEquals(" { AND { memberType.includes 'google' }  } ", groovyDSLBuilder.getConditionalDSLString(rootNode));
	}
	
	/**
	 * Test a simple condition which right operand of the condition is an integer. The condition string
	 * should be created without quotes since the type of the object is integer.
	 * 
	 * { AND { age.equalTo 3 }  }
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test
	public void testAddingIntegerRightOperands() throws InvalidConditionTreeException {
		LogicalOperator rootNode = new LogicalOperator(LogicalOperatorType.AND);
		
		final TagDefinition age = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
		age.setGuid("age");
		age.setName("age");
		TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
		tagValueType.setJavaType(Integer.class.getCanonicalName());
		age.setValueType(tagValueType);
		
		rootNode.addCondition(new Condition(age, "age", "equalTo", Integer.parseInt("3")));

		final String condition = groovyDSLBuilder.getConditionalDSLString(rootNode);
		LOG.debug("-[act]->>" + condition);
		assertEquals(" { AND { age.equalTo (3i) }  } ", condition);
	}
	
	/**
	 * Test a simple condition which right operand of the condition is a collection. An unsupported operation exception
	 * should be thrown.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testAddingCollectionRightOperands() throws InvalidConditionTreeException {
		LogicalOperator rootNode = new LogicalOperator(LogicalOperatorType.AND);
		
		final TagDefinition age = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
		age.setGuid("age");
		age.setName("age");
		TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
		tagValueType.setJavaType(String.class.getCanonicalName());
		age.setValueType(tagValueType);

		rootNode.addCondition(new Condition(age, "age", "includes", Collections.singletonList("a")));

		groovyDSLBuilder.getConditionalDSLString(rootNode);
	}

	/**
	 * Test a simple condition which right operand of the condition is a map. An unsupported operation exception
	 * should be thrown.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testAddingMapRightOperands() throws InvalidConditionTreeException {
		LogicalOperator rootNode = new LogicalOperator(LogicalOperatorType.AND);
		
		final TagDefinition age = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
		age.setGuid("age");
		age.setName("age");
		TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
		tagValueType.setJavaType(String.class.getCanonicalName());
		age.setValueType(tagValueType);

		rootNode.addCondition(new Condition(age, "age", "equalTo", Collections.singletonMap("a", "b")));

		groovyDSLBuilder.getConditionalDSLString(rootNode);
	}

	/**
	 * Test a simple condition which right operand of the condition is an array. An unsupported operation exception
	 * should be thrown.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testAddingArrayRightOperands() throws InvalidConditionTreeException {
		LogicalOperator rootNode = new LogicalOperator(LogicalOperatorType.AND);
		
		final TagDefinition age = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
		age.setGuid("age");
		age.setName("age");
		TagValueType tagValueType = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
		tagValueType.setJavaType(String.class.getCanonicalName());
		age.setValueType(tagValueType);

		
		rootNode.addCondition(new Condition(age, "age", "equalTo", new String[] { "a" }));
		
		groovyDSLBuilder.getConditionalDSLString(rootNode);
	}
}
