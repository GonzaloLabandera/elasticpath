/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.contentspace.ContentSpace;
import com.elasticpath.domain.contentspace.DynamicContent;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.targetedselling.DynamicContentDelivery;
import com.elasticpath.service.contentspace.ContentSpaceService;
import com.elasticpath.service.contentspace.DynamicContentService;
import com.elasticpath.service.sellingcontext.SellingContextService;
import com.elasticpath.service.targetedselling.DynamicContentDeliveryService;
import com.elasticpath.tags.domain.ConditionalExpression;
import com.elasticpath.tags.domain.LogicalOperator;
import com.elasticpath.tags.domain.LogicalOperatorType;
import com.elasticpath.tags.service.ConditionDSLBuilder;
import com.elasticpath.tags.service.InvalidConditionTreeException;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test named queries for dynamic content delivery.
 */
public class DynamicContentDeliveryTest extends DbTestCase {

	@Autowired
	private DynamicContentService dynamicContentService;

	@Autowired
	private DynamicContentDeliveryService dynamicContentDeliveryService;

	@Autowired
	private SellingContextService sellingContextService;

	@Autowired
	private ConditionDSLBuilder conditionalBuilder;

	@Autowired
	private ContentSpaceService contentSpaceService;

	private static final String CONTENT_WRAPPER_ID   = "contentWrapperId";

	private static final String DYNAMIC_CONTENT_NAME = "Summer";

	private int idx;

	private int getIdx() {
		return ++idx;
	}

	/**
	 * Create single DC and persist it.
	 * @return instance of DynamicContent.
	 */
	private DynamicContent createSingleDynamicContent() {
		final DynamicContent dynamicContent = getBeanFactory().getPrototypeBean(ContextIdNames.DYNAMIC_CONTENT, DynamicContent.class);
		dynamicContent.setContentWrapperId(CONTENT_WRAPPER_ID);
		dynamicContent.setName(DYNAMIC_CONTENT_NAME);
		return dynamicContentService.add(dynamicContent);
	}

	/**
	 * Create conditional expression.
	 * @param tagDictionaryGuid the tag dictionary guid
	 * @return empty conditional expression.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	private ConditionalExpression  createConditionalExpression(final String tagDictionaryGuid) throws InvalidConditionTreeException {
		LogicalOperator logicalOperator = new LogicalOperator(LogicalOperatorType.AND);
		ConditionalExpression conditionalExpression = getBeanFactory().getPrototypeBean(ContextIdNames.TAG_CONDITION, ConditionalExpression.class);
		conditionalExpression.setTagDictionaryGuid(tagDictionaryGuid);
		conditionalExpression.setName("name_" + getIdx());
		conditionalExpression.setDescription(String.valueOf(getIdx()));
		conditionalExpression.setConditionString(conditionalBuilder.getConditionalDSLString(logicalOperator));
		return conditionalExpression;
	}

	private SellingContext createSellingContext() throws InvalidConditionTreeException {
		SellingContext sellingContext = getBeanFactory().getPrototypeBean(ContextIdNames.SELLING_CONTEXT, SellingContext.class);
		sellingContext.setPriority(1);
		sellingContext.setCondition("SHOPPER", createConditionalExpression("SHOPPER"));
		sellingContext.setCondition("STORES", createConditionalExpression("STORES"));
		sellingContext.setCondition("TIME" , createConditionalExpression("TIME"));
		sellingContext.setName("DCA SellingContext " + getIdx());
		sellingContext.setDescription(sellingContext.getName());
		return sellingContextService.saveOrUpdate(sellingContext);
	}

	private DynamicContentDelivery createDynamicContentDelivery() throws InvalidConditionTreeException {
		DynamicContentDelivery dynamicContentDelivery = getBeanFactory().getPrototypeBean(ContextIdNames.DYNAMIC_CONTENT_DELIVERY,
				DynamicContentDelivery.class);
		SellingContext sellingContext = createSellingContext();
		DynamicContent dynamicContentInstance = createSingleDynamicContent();
		dynamicContentDelivery.setName("Name" + getIdx());
		dynamicContentDelivery.setDescription("Description" + getIdx());
		dynamicContentDelivery.setDynamicContent(dynamicContentInstance);
		dynamicContentDelivery.setSellingContext(sellingContext);
		return dynamicContentDelivery;
	}

	/**
	 * Test for create Dynamic Content Delivery.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testAddDynamicContentDelivery() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery = createDynamicContentDelivery();

		DynamicContentDelivery dynamicContentDeliveryPersisted
			= getTxTemplate().execute(txStatus -> dynamicContentDeliveryService.add(dynamicContentDelivery));

		assertThat(dynamicContentDeliveryPersisted).isNotNull();
		assertThat(dynamicContentDeliveryPersisted.isPersisted()).isTrue();
	}

	/**
	 * Test DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME named query.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testFindByName() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery  = createDynamicContentDelivery();
		dynamicContentDelivery.setName("findMe");
		getTxTemplate().execute(txStatus -> {
			dynamicContentDeliveryService.add(dynamicContentDelivery);
			List<DynamicContentDelivery> allDynamicContent = getPersistenceEngine().retrieveByNamedQuery(
				"DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME", "findMe");
			assertThat(allDynamicContent).isNotEmpty();
			allDynamicContent = getPersistenceEngine().retrieveByNamedQuery("DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME", "canNotFound");
			assertThat(allDynamicContent).isEmpty();

			return null;
		});
	}

	/**
	 * Test DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME_LIKE named query.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testFindByNameLike() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery  = createDynamicContentDelivery();
		dynamicContentDelivery.setName("findMe");

		getTxTemplate().execute(txStatus -> {
			dynamicContentDeliveryService.add(dynamicContentDelivery);

			List<DynamicContentDelivery> allDynamicContent = getPersistenceEngine().retrieveByNamedQuery(
				"DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME_LIKE", "%indM%");
			assertThat(allDynamicContent).isNotEmpty();

			allDynamicContent = getPersistenceEngine().retrieveByNamedQuery("DYNAMIC_CONTENT_DELIVERY_FIND_BY_NAME_LIKE", "%canNotFound%");
			assertThat(allDynamicContent).isEmpty();

			return null;
		});
	}

	/**
	 * Test DYNAMIC_CONTENT_DELIVERY_FIND_ALL named query.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testFindAll() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery  = createDynamicContentDelivery();
		getTxTemplate().execute(txStatus -> {
			dynamicContentDeliveryService.add(dynamicContentDelivery);
			List<DynamicContentDelivery> allDynamicContent = getPersistenceEngine().retrieveByNamedQuery(
				"DYNAMIC_CONTENT_DELIVERY_FIND_ALL");
			assertThat(allDynamicContent).hasSize(1);

			return null;
		});
	}

	private ContentSpace createContentSpace(final String name) {
		ContentSpace contentSpace = getBeanFactory().getPrototypeBean(ContextIdNames.CONTENTSPACE, ContentSpace.class);
		contentSpace.setTargetId(name);
		contentSpace.setDescription("description");
		return contentSpace;
	}

	/**
	 * Test DYNAMIC_CONTENT_DELIVERY_FIND_BY_CONTENT_SPACE named query.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testFindDcaByContentSpace() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery  = createDynamicContentDelivery();
		getTxTemplate().execute(txStatus -> {
			final ContentSpace contentSpace = contentSpaceService.saveOrUpdate(createContentSpace("findMeByCS"));
			dynamicContentDelivery.getContentspaces().add(contentSpace);
			dynamicContentDeliveryService.add(dynamicContentDelivery);
			List<DynamicContentDelivery> allDynamicContent = getPersistenceEngine().retrieveByNamedQuery(
				"DYNAMIC_CONTENT_DELIVERY_FIND_BY_CONTENT_SPACE", "findMeByCS");
			assertThat(allDynamicContent).hasSize(1);

			return null;
		});
	}

	/**
	 * Test DYNAMIC_CONTENT_DELIVERY_FIND_BY_SELLING_CONTEXT_GUID named query.
	 * @throws InvalidConditionTreeException if the condition tree has an invalid format or data
	 */
	@DirtiesDatabase
	@Test
	public void testFindDcaBySellingContextGuid() throws InvalidConditionTreeException {
		final DynamicContentDelivery dynamicContentDelivery  = createDynamicContentDelivery();
		final String sellingContextGuid = dynamicContentDelivery.getSellingContextGuid();
		getTxTemplate().execute(txStatus -> {
			final ContentSpace contentSpace = contentSpaceService.saveOrUpdate(createContentSpace("notImportant"));
			dynamicContentDelivery.getContentspaces().add(contentSpace);
			dynamicContentDeliveryService.add(dynamicContentDelivery);
			List<DynamicContentDelivery> allDynamicContent = getPersistenceEngine().retrieveByNamedQuery(
				"DYNAMIC_CONTENT_DELIVERY_FIND_BY_SELLING_CONTEXT_GUID", sellingContextGuid);
			assertThat(allDynamicContent).hasSize(1);

			return null;
		});
	}
}
