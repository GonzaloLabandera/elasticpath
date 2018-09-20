# Chapter 11: Reporting

[TOC]

## Overview

Elastic Path Commerce contains reports that are useful for store operations. You can access these reports from the toolbar.

![](images/Ch11-01.png)

<!-- ## Customer Registration Report

The Customer Registration report is a summary of a store&#39;s customer registrations during a particular time frame. It is useful for gauging the effectiveness of promotions targeting new customers.

**Note:**         Anonymous registrations may also be included in the summary.

### Generating a Customer Registration Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Customer Registration**.

3. From the **Store** list, select the store for which you want to generate the report.

4. Specify the date range for the report using the calendar in the **From** and **To Date/Time** fields. The **From Date/Time** field is optional, but the **To Date/Time** field is required.

5. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

6. Click **Run Report**. A new pane containing the report appears on the right.

<!-- ## Gift Certificate Details Report

This report shows details about the gift certificates sold by a store in a given currency during a particular time frame.

### Generating a Gift Certificate Details Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Gift Certificate Details**.

3. From the **Store** list, select the store for which you want to generate the report.

4. From the **Currency** list, select the currency for which you want to generate the report.

5. Specify the purchase date range for the report using the calendar in the **Purchased From** and **To Date/Time** fields.  The **Purchased From Date/Time** field is optional, but the **To Date/Time** field  is required.

6. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

7. Click **Run Report**. A new pane containing the report appears on the right.

## Gift Certificate Summary Report

The Gift Certificate Summary report is a summary of the gift certificates in a given currency that were sold by a store (or all stores) during a specific time frame. You can use it for calculating outstanding liabilities.

### Generating a Gift Certificate Summary Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Gift Certificate Summary**.

3. From the **Store** list, select the store for which you want to generate the report, or select **All** to generate a report for all stores.

4. From the **Currency** list, select the currency for which you want to generate the report, or select **All** to generate a report for all currencies.

5. Specify the purchase date range for the report using the calendar in the **Purchased From** and **To Date/Time** fields.  The **Purchased From Date/Time** field is optional, but the **To Date/Time** field is required.

6. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

7. Click **Run Report**. A new pane containing the report appears on the right.

## Low Stock Report

This report helps determine the products to restock. If a certain product&#39;s Quantity on Hand is less than its Re-Order Minimum amount, it is included in this report.

You need to specify the warehouse the report is for. You can also specify a brand for further filtering. This report is useful when a warehouse has to submit an order proposal to a supplier.

If you want to check the low stock levels for a specific product, you can enter the product's SKU number at the input screen.

### Generating a Low Stock Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Low Stock**.

3. From the **Store** list, select the warehouse for which you want to generate the report.

4. If required, you can select a SKU Code and/or a brand to filter the results. Click the button next to the _SKU Code_ field to display the SKU code selector dialog box.

    ![](images/Ch12-05.png)

5. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

7. Click **Run Report**. A new pane containing the report appears on the right.

## Order Details Report

This report includes the same set of report parameters as the Order Summary Report, but it also includes other details on the individual orders.

You need to specify the store and subset of orders the report is generated for.

### Generating an Order Details Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Order Details**.

3. Enter values in the fields to specify what data the report should be generated on. Use the calendar icon to select the report&#39;s date range.

4. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

7. Click **Run Report**. A new pane containing the report appears on the right.
<!-- -->

## Order Status Report

This report provides a list of orders for a store and currency based on order status. By default all orders that are incomplete are included in the report.

### Generating an Order Status Report

1. On the toolbar, click the **Reporting** button. The **Reporting** pane appears.

2. From the **Report Type** list, select **Order Status**.

3. From the **Store** list, select the store for the report.

4. From the **Currency** list, select the currency for the report.

5. Specify the date range for the report using the calendar in the **From** and **To Date/Time** fields.

6. (Optional) To include only exchange orders, select the **Show Exchange Orders Only** option.

7. In the **Order Status** section, select the order statuses to include in the report.

8. Select the desired format from the **Report Format** list.  

9. Click **Run Report**. HTML reports are displayed in the pane on the right. CSV, PDF and Microsoft Excel reports are exported to files that can be saved.

## Order Summary Report

This report provides daily and monthly summaries of orders for a store and currency. By default all orders except for cancelled orders are included. Returns are not included in the report.

### Generating an Order Summary Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Order Summary**.

3. From the **Store** list, select the store for the report.

4. From the **Currency** list, select the currency for the report.

5. Specify the date range for the report using the calendar in the **From** and **To Date/Time** fields.

6. (Optional) To include only exchange orders, select the **Show Exchange Orders Only** option.

7. In the **Order Status** section, select the order statuses to include in the report.

8. Select the desired format from the **Report Format** list.  

9. Click **Run Report**. HTML reports are displayed in the pane on the right. CSV, PDF and Microsoft Excel reports are exported to files that can be saved.

<!-- ## Orders Awaiting Stock Allocation Report

This report provides a list of all products that are pre-ordered or back-ordered. You can use it for a variety of purposes, such as identifying the products to order.

You need to specify the store the report is generated for.
You can also filter the results by product status (preorder, backorder, or both).

If you want to check a specific product for its pre-order or back-order status, you can enter the corresponding SKU number at the input screen.

### Generating an Orders Awaiting Stock Allocation Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Orders Awaiting Stock Allocation**.

3. From the **Store** list, select the store for which you want to generate the report.

4. If required, you can also specify a SKU code to further filter the results in the report.

5. Select the Product Availability Rules you want the report generated for from the list.

6. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

7. Click **Run Report**. A new pane containing the report appears on the right

<!-- -->

## Returns and Exchanges Report

This report provides a summary of returns and exchanges for a store and currency based on return/exchange status.

### Generating a Returns and Exchanges Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Returns and Exchanges**.

3. From the **Store** list, select the store for the report.

4. From the **Currency** list, select the currency for the report.

5. Specify the date range for the report using the calendar in the **From** and **To Date/Time** fields.

6. (Optional) From the **RMA Type** list, select whether to include only returns, only exchanges, or both returns and exchanges in the report.

7. In the **Status** section, select the return and exchange statuses to include in the report.

8. Select the desired format from the **Report Format** list.  

9. Click **Run Report**. HTML reports are displayed in the pane on the right. CSV, PDF and Microsoft Excel reports are exported to files that can be saved.

## Shopping Cart Promotion Usage Report

The Shopping Cart Promotion Usage Report provides a summary of promotion usage by store.

### Generating a Shopping Cart Promotion Usage Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select **Shopping Cart Promotion Usage**.

3. From the **Store** list, select the store for the report.

4. Specify the date range for the report using the calendar in the **From** and **To Date/Time** fields.

5. (Optional) To exclude non-coupon promotions, select the **Include only Promotions with Coupon Codes** option.

6. Select the desired format from the **Report Format** list.  

7. Click **Run Report**. HTML reports are displayed in the pane on the right. CSV, PDF and Microsoft Excel reports are exported to files that can be saved.

<!-- ## Shopping Cart Promotion Details Report

The Shopping Cart Promotion Details Report contains information about orders where a specific shopping cart promotion was applied, including the order number, coupon code, order total, and customer email. This can be useful for determining the effectiveness of specific promotions.

### Generating a Shopping Cart Promotion Details Report

1. On the toolbar, click the **Reporting** button. The Reporting pane appears.

2. From the **Report Type** list, select _Shopping Cart Promotion Details_.

3. From the **Store** list, select the store for which you want to generate the report.

4. From the **Currency** list, select the currency for which you want to generate the report, or select **All** to generate a report for all currencies.

5.Specify the date range that contains orders you want to include in the report using the calendar in the **Purchased From** and **To Date/Time** fields. The **Purchased From Date/Time** field is optional, but the **To Date/Time** field is required.

6. From the **Promotion** list, select the promotion for which you want to generate the report.

7. If you want to filter the results to include a specific coupon code, enter the code in the **Coupon Code** field.

8. From the **Report Format** list, select the format to export the report to. For example, as a CSV, Microsoft Excel, or PDF file.

9. Click **Run Report**. A new pane containing the report appears on the right.
<!-- -->
