/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static com.elasticpath.performance.mojo.utils.Constants.HEAVY_PLUS_SIGN_EMOJI;
import static com.elasticpath.performance.mojo.utils.Constants.MOTIVATIONAL_EMOJIS;
import static com.elasticpath.performance.mojo.utils.Constants.QUESTION_EMOJI;
import static com.elasticpath.performance.mojo.utils.Constants.X_EMOJI;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** Test class for {@link HTMLUtils}. */
public class HTMLUtilsTest {
	@Test
	public void shouldReplaceGitHubEmojisWithIconsInHTMLContent() {
		StringBuilder htmlContent = new StringBuilder("<html><body><table><tr><td>")
				.append(X_EMOJI)
				.append("</td><td>")
				.append(HEAVY_PLUS_SIGN_EMOJI)
				.append("</td><td>")
				.append(QUESTION_EMOJI)
				.append("</td>");

		for (String emoji : MOTIVATIONAL_EMOJIS) {
			htmlContent.append("<td>" + emoji + "</td>");
		}

		htmlContent.append("</tr></td></table></body></html>");

		String expectedHtmlContent = "<html><body><table><tr><td><img src='x.png' width=20 height=20/></td>"
				+ "<td><img src='heavy_plus_sign.png' width=20 height=20/></td>"
				+ "<td><img src='question.png' width=20 height=20/></td>"
				+ "<td><img src='heart_eyes.png' width=20 height=20/></td>"
				+ "<td><img src='star2.png' width=20 height=20/></td>"
				+ "<td><img src='boom.png' width=20 height=20/></td>"
				+ "<td><img src='fire.png' width=20 height=20/></td>"
				+ "<td><img src='raised_hands.png' width=20 height=20/></td>"
				+ "<td><img src='shipit.png' width=20 height=20/></td>"
				+ "<td><img src='racehorse.png' width=20 height=20/></td>"
				+ "</tr></td></table></body></html>";

		String actualHtmlContent = HTMLUtils.replaceGitHubEmojisWithIcons(htmlContent);
		assertThat(actualHtmlContent)
				.isEqualTo(expectedHtmlContent);
	}
}
