package com.elasticpath.service.search.impl;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import org.jmock.Expectations;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.domain.search.FieldKeyType;
import com.elasticpath.domain.search.impl.FacetImpl;
import com.elasticpath.test.jmock.AbstractEPServiceTestCase;

/**
 * Test case for {@link FacetServiceImpl}.
 */
public class FacetServiceImplTest extends AbstractEPServiceTestCase {

	private FacetServiceImpl facetServiceImpl;

	@Override
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();
		facetServiceImpl = new FacetServiceImpl();
		facetServiceImpl.setPersistenceEngine(getPersistenceEngine());
	}

	@Test
	public void testSaveOrUpdate() {
		Facet facet = generateTestFacet();
		Facet updatedFacet = new FacetImpl();

		Collection<Facet> facetFromDb = new ArrayList<>();
		facetFromDb.add(updatedFacet);

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with("FACET_FIND_BY_GUID"),
						with(any(Object[].class)));
				will(returnValue(facetFromDb));

				oneOf(getMockPersistenceEngine()).saveOrUpdate(with(same(facet)));
				will(returnValue(updatedFacet));
			}
		});

		final Facet returnedFacet = facetServiceImpl.saveOrUpdate(facet);
		assertSame(returnedFacet, updatedFacet);
	}

	@Test
	public void testRemove() {
		final Facet facet = context.mock(Facet.class);
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).delete(with(same(facet)));
			}
		});
		facetServiceImpl.remove(facet);
	}

	@Test
	public void testGetFacet() {

		stubGetBean(ContextIdNames.FACET_SERVICE, FacetImpl.class);

		final long uid = 1234L;
		final Facet facet = context.mock(Facet.class);
		context.checking(new Expectations() {
			{
				allowing(getMockPersistenceEngine()).get(FacetImpl.class, uid);
				will(returnValue(facet));
			}
		});
		assertSame(facet, facetServiceImpl.getObject(uid));
		assertSame(facet, facetServiceImpl.getFacet(uid));

	}

	@Test
	public void testFindAllFacetsForStore() {
		final Collection<Facet> facetList = new ArrayList<>();
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with("FIND_ALL_FACETS_BY_STORE_CODE"), with(any(Object[].class)));
				will(returnValue(facetList));
			}
		});
		assertSame(facetList, facetServiceImpl.findAllFacetsForStore("storeCode", Locale.getDefault()));

	}

	private Facet generateTestFacet() {
		Facet facet = new FacetImpl();
		facet.setFacetGuid(UUID.randomUUID().toString());
		facet.setFacetName(UUID.randomUUID().toString());
		facet.setFieldKeyType(FieldKeyType.STRING.getOrdinal());
		facet.setStoreCode("testStore");
		facet.setDisplayName("{\"English\":\"Brand\"}");
		facet.setFacetType(FacetType.FACET.getOrdinal());
		facet.setSearchableOption(Boolean.TRUE);
		facet.setRangeFacetValues("{}");

		return facet;
	}
}