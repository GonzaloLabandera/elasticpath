package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;

@RunWith(MockitoJUnitRunner.class)
public class DataPolicyTagSetPopulatorTest {

	private static final String EXPECTED_DATA_POLICY_DOES_NOT_MATCH_RESULT = "Expected data policy does not match result.";
	private static final String SUBJECT_ATTRIBUTE_KEY = "DATA_POLICY_SEGMENTS";
	private static final String DATA_POLICY_HEADER = "x-ep-data-policy-segments";

	@Mock
	private XPFHttpTagSetContext mockContext;

	@Mock
	private HttpServletRequest httpRequest;

	@InjectMocks
	private DataPolicyPopulator dataPolicyPopulator;

	@Before
	public void setUp() {
		when(mockContext.getHttpRequest()).thenReturn(httpRequest);
	}

	@Test
	public void testTagValues() {
		when(httpRequest.getHeader(DATA_POLICY_HEADER)).thenReturn("data_policy1, data_policy2");

		Map<String, String> actualAttributes = dataPolicyPopulator.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals(EXPECTED_DATA_POLICY_DOES_NOT_MATCH_RESULT, "[data_policy1, data_policy2]",
				actualAttributes.get(SUBJECT_ATTRIBUTE_KEY));
	}

	@Test
	public void testMissingTagValues() {
		when(httpRequest.getHeader(DATA_POLICY_HEADER)).thenReturn(null);

		Map<String, String> actualAttributes = dataPolicyPopulator.collectTagValues(mockContext);

		assertEquals(0, actualAttributes.size());
	}
}
