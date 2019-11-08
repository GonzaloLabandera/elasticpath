package com.elasticpath.definitions.api.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class which has methods which are used by API step definition classes.
 */
public final class StepsHelper {

	private StepsHelper() {
	}

	/**
	 * Parses given string by comma separator and converts it to the list of strings.
	 *
	 * @param stringToParse strin to parse
	 * @return list of strings which consists of elements created from given string parsed by comma separator
	 */
	public static List<String> parseByComma(final String stringToParse) {
		return Arrays.asList(stringToParse.split(","));
	}

	/**
	 * @param codes      list of codes
	 * @param startAfter brand code which is used to find {code}startIndex{code}
	 * @return index of {code}cedes{code} element which follows an element which is equal to {code}startAfter{code}. If {code}cedes{code} doesn't
	 * contain {code}startAfter{code} function returns index of the first element which is alphabetically greater than {code}startAfter{code} and
	 * -1 if all the elements are alphabetically less than {code}startAfter{code}
	 */
	public static int getStartIndex(final List<String> codes, final String startAfter) {
		int startIndex = -1;
		if (codes.indexOf(startAfter) == -1) {
			for (String code : codes) {
				if (startAfter.compareTo(code) < 0) {
					startIndex = codes.indexOf(code);
					break;
				}
			}
		} else {
			startIndex = codes.indexOf(startAfter) + 1;
			assertThat(startIndex)
					.as("There are less pages in the response than provided value. Thus response page start index is out of range")
					.isLessThan(codes.size());
		}
		assertThat(startIndex)
				.as("There are less pages in the response than provided value. Thus response page start index is out of range")
				.isNotEqualTo(-1);
		return startIndex;
	}
}
