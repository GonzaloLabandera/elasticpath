package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.cucumber.util.CortexMacrosTestBase;
import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Report Pane.
 */
public class ReportPane extends AbstractPageObject {

	private String htmlStr;
	private int numberOfOrders;
	private static final int PROMO_ORDER_COLUMN_NUMBER = 2;
	private static final int ORDER_STATUS_TOTAL_COLUMN_NUMBER = 2;

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ReportPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Sets the htmlStr.
	 */
	public void getHtmBody() {

		waitTillElementDisappears(By.cssSelector("div[widget-id='Progress Information']"));

		boolean htmlBodyExists = false;
		htmlStr = getDriver().switchTo().frame(0).getPageSource();
		getDriver().switchTo().defaultContent();

		if (htmlStr.length() > 0) {
			htmlBodyExists = true;
		}

		assertThat(htmlBodyExists)
				.as("unable to find html 'body' tag")
				.isTrue();
	}

	/**
	 * Gets the row by column value.
	 *
	 * @param columnValue the column value
	 * @param tableNumber nested table number, parent starts with 0
	 * @return row or null
	 */
	public Element getRowByColumnValue(final String columnValue, final int tableNumber) {

		if (htmlStr == null) {
			getHtmBody();
		}

		Document document = Jsoup.parse(htmlStr);
		Element table = document.select("table").get(tableNumber);
		Elements rows = table.select("tr");

		for (Element row : rows) {
			Elements columns = row.select("td");
			for (Element column : columns) {
				if (column.text().equals(columnValue)) {
					return row;
				}
			}
		}

		assertThat(false)
				.as("column value '" + columnValue + "' doesn't exist")
				.isTrue();

		return null;
	}

	/**
	 * Returns the number of orders for promotion report.
	 *
	 * @param columnValue the column value
	 * @return numberOfOrders the number of orders
	 */
	public int getPromoReportNumberOfOrders(final String columnValue) {
		Element row = getRowByColumnValue(columnValue, 1);
		Elements columns = row.select("td");
		numberOfOrders = Integer.parseInt(columns.get(PROMO_ORDER_COLUMN_NUMBER).text());
		return numberOfOrders;
	}

	/**
	 * Verifies the number of orders for promotion report.
	 *
	 * @param columnValue    the column value
	 * @param numberOfOrders the number of orders
	 */
	public void verifyPromoReportNumberOfOrders(final String columnValue, final int numberOfOrders) {
		assertThat(getPromoReportNumberOfOrders(columnValue))
				.as("Number of orders are not as expected")
				.isGreaterThanOrEqualTo(numberOfOrders);
	}

	/**
	 * Returns the number of orders for order summary report.
	 *
	 * @return numberOfOrders the number of orders
	 */
	public int getOrderSummaryReportNumberOfOrders() {
		Element row = getRowByColumnValue(getFormattedDate(0), 2);
		Elements columns = row.select("td");
		numberOfOrders = Integer.parseInt(columns.get(2).text());
		return numberOfOrders;
	}

	/**
	 * Verifies the number of orders in order summary.
	 *
	 * @param numberOfOrders the number of orders
	 */
	public void verifyNumberOfOrdersInOrderSummaryReport(final int numberOfOrders) {
		assertThat(getOrderSummaryReportNumberOfOrders())
				.as("Number of orders are not as expected")
				.isGreaterThanOrEqualTo(numberOfOrders);
	}

	/**
	 * Formats the date (26-Sep-2017).
	 *
	 * @param numberOfDays number of days to add or subtract from current date
	 * @return formatted date String
	 */
	private String getFormattedDate(final int numberOfDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
		return new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(calendar.getTime());
	}

	/**
	 * Verifies the order number in returns and exchanges report.
	 */
	public void verifyOrderNumberInReturnAndExchangeReport() {
		Element row = getRowByColumnValue(CortexMacrosTestBase.PURCHASE_NUMBER, 1);
		assertThat(row.select("td").get(0).text())
				.as("Return or exchange order number is not as expected")
				.isEqualTo(CortexMacrosTestBase.PURCHASE_NUMBER);
	}

	/**
	 * Verifies the order total in order status report.
	 * @param orderTotal the order total
	 */
	public void verifyOrderTotalInOrderStatusReport(final String orderTotal) {
		Element row = getRowByColumnValue(CortexMacrosTestBase.PURCHASE_NUMBER, 1);
		//The html values are returning with &nbsp value at the end and jsoup is mapping &nbsp to u00a0.
		assertThat(row.select("td").get(ORDER_STATUS_TOTAL_COLUMN_NUMBER).text().replace("\u00a0", ""))
				.as("Order total in Order Status report is not as expected")
				.isEqualTo(orderTotal);
	}

}
