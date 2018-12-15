# Chapter 4: Commerce Configuration

[TOC]

## Overview

The topics in this chapter describe activities performed in the initial configuration and maintenance of store operations in Elastic Path Commerce. These include configuring stores and warehouses, payment gateways, shipping regions, tax codes, and customer profile attributes.

## Customer Profiles

### Customer Profile Attributes

Customer profile attributes store information about a customer (such as name, address, and email address) within the Elastic Path Commerce database. They are used by the  Customer Service Representatives (CSRs) to record information about customers during the customer creation and maintenance processes.

#### Tax Exemption Profile Attributes

Two attributes are available as Customer Profile Attributes that are used during tax exemption scenarios.  Values for these fields can be manually entered by the CSRs.  When integrating with a Tax Provider, these values are available to pass along to the tax provider, indicating taxes for the given order are not to be calculated.  There are two scenarios as to how or where exemption is indicated.

In the first scenario, companies supporting tax exemption order processing are required to collect, validate, and store values for their customers.  In this scenario, the following fields are available in the Customer Profile to collect and store the information.  Orders for these customers then pass the given customer information to the tax provider.  Generally, these numbers, if visible to the customer at the time of checkout would be non-editable.

- **Business Number**: Used to record the customer&#39;s business identification number.

- **Tax Exemption**: Used to record the number provided by the government for use by the given customers on orders where taxes are not to be charged or collected.

In the second scenario, a tax exemption number is provided by the customer at the time of checkout.  You must enter this value manually with each transaction, as there is no tie between a value provided at checkout and that stored within a Customer Profile.  Similarly, values available in the Customer Profile are not used to auto-populate any tax exemption fields presented at the time of checkout or purchase.

> **Note:**  The Elastic Path tax engine currently does not factor in exemption.  If you are using the Elastic Path Tax Tables, taxes are calculated regardless of entering exemption codes. The Tax Exemption functionality only applies when integrating with a Tax Provider.


#### Creating a Customer Profile Attribute

You can add customer profile attributes in Elastic Path Commerce. However, adding an attribute does not automatically cause it to appear during store customer account creation or editing. You need to customize the store for additional fields to appear within it.

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profile** section of the left pane, select **Profile Attributes**.

2. From the top right pane toolbar, click **Create Attribute**.

3. In the _Create Attribute_ dialog box, enter values in the following fields:

    | Field | Description |
    | --- | --- |
    | Attribute Key | The unique identifier for the attribute. |
    | Attribute Name | The display name of the attribute. |
    | Attribute Type | The format of the attribute&#39;s data (for example, Date, Text, Integer, or Decimal). |
    | Required | Specifies whether the new attribute is required or optional. |

4. Click **Save**.

#### Editing a Customer Profile Attribute

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profile** section of the left pane, select **Profile Attributes**.

3. In the top right pane, select the profile attribute you want to edit.

    > **Tip:** To find attributes more quickly, click a column header to sort the column by that header.

4. From the top right pane toolbar, click **Edit Attribute**.

5. In the _Edit Attribute_ dialog box, make the required changes and click **Save**.

#### Deleting a Customer Profile Attribute

> **Note:** If data is already entered for a profile attribute, the **Edit Attribute** and **Delete Attribute** buttons are disabled. This prevents corruption and loss of data.

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profile** section of the left pane, select **Profile Attributes**.

3. In the top right pane, select the profile attribute you want to delete.

4. On the top right pane toolbar, click **Delete Attribute**.

5. Click **OK**.

### Customer Segments

Customer Segments enable marketers to group customers together and create conditions for delivering price lists, dynamic content, and cart promotions. Customer Segment tags determine if the customer belongs to a segment and if they are entitled to different content, prices, and promotions for the storefront.

An example of a customer segment is where a store employee is assigned to an employee segment so they receive employee pricing when shopping.

#### Creating a Customer Segment

A default PUBLIC customer segment is available and applies to all customer accounts.

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profiles** section of the left pane, select **Customer Segments**. The **Customer Segments** tab appears in the top right pane.

3. Click **Create Customer Segment**.

4. In the **New Customer Segment** tab, enter the values for the following fields:

    | Field | Description |
    | --- | --- |
    | Customer Segment Name | The customer segment&#39;s unique identifier. |
    | Description | The customer segment&#39;s description. |
    | Enabled | Activates the segment. If disabled, conditions using this tag or the customer segment are not triggered. |

5. On the toolbar, click **Save**.

#### Editing a Customer Segment

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profile** section of the left pane, select **Customer Segments**. The **Customer Segments** tab appears in the top right pane.

3. Click **Edit Customer Segment**.

4. In the bottom right pane, make the required changes to the customer segment.

5. On the toolbar, click **Save**.

#### Deleting a Customer Segment

1. On the toolbar, click the **Configuration** button.

2. In the **Customer Profile** section of the left pane, select **Customer Segments**. The **Customer Segments** tab appears in the top right pane.

3. Select the customer you want to delete.

    > **Note:** You cannot delete the PUBLIC customer segment.

4. Click **Delete Customer Segment**.

5. Click **OK**.

    > **Note:** If the customer segment has a customer association or is used as a condition, then a _Customer Segment in Use_ message is displayed indicating the segment cannot be deleted.  Either remove the associations or condition or disable the customer segment.

## Data Policies

A data policy is a collection of data points that Elastic Path stores for a specific customer. A data point is a specific information about a customer. For example, when a customer orders an item, Elastic Path collects their name and address. The name is a data point, as is the address.

You can use data policies to maintain compliance with various data protection laws. Content management systems (CMS) use data policies to present the customer with the option to provide or revoke consent for the collection of their data for business purposes.

Elastic Path Commerce only records what data is stored for a data policy, as well as basic identifying information about the data policy. Presenting a data policy to a user, storing a data policy's legal description, and implementing how a data policy behaves is the responsibility of the CMS. The Elastic Path Commerce Admin Console only controls whether a data policy is active or inactive for a store, and what data points a particular policy collects.

### Data Policy States

Data policies can have three states.

| State | Description |
| --- | --- |
| Draft | Indicates that a data policy is editable and not collecting data. |
| Active | Indicates that a data policy is currently active for **all** stores which have enabled data policies. The end date of an active data policy is editable, but all other fields are not. |
| Disabled | Indicates that a data policy is no longer active. A disabled data policy may still have data points associated with it. Disabled data policies are not editable and **cannot** be re-enabled. |

### Data Retention and Deletion

This section covers data retention and automatic data deletion. For manual data deletion, see See Chapter 7, _Customer Service_.

#### Data Retention Types

You can specify the following data retention types for a data policy:

| Type | Description |
| --- | --- |
| From Created Date | Retains data from the date of its initial creation until the end of the retention period. |
| From Last Modified Date | Retains data from the date it was last modified by the end user or Elastic Path until the end of the retention period. Whenever data is modified, the retention period resets. |

#### Retention Type and Data Deletion

For data policies with the **From Created Date** retention type, data point values associated with the data policy are deleted automatically at the end of the data policy's retention period. For a data point created on the last day of an active data policy, the data point value is deleted at the end of the retention period.

For active data policies with the **From Last Modified Date** retention type, the retention period is calculated from the last modified date. For a disabled data policy with the From Last Modified Date retention type, the retention period is calculated either from the last modified date or the disabled date whichever is the earliest date. For example, if disabled date is January 3, 2018 and last modified date is January 8, 2018, the retention period is calculated from January 3, 2018. After the retention period of data policies, data is deleted.

#### Customer Consent and Data Deletion

If a customer revokes consent on a data policy, data is deleted within 24 hours by a clean-up job. Consent for a disabled data policy cannot be revoked.

When a data point is used in one or more data policies, data is automatically removed only if the following circumstances are met:

* The customer has not provided consent for the data point in any other data policy.
* The retention periods for all data policies using the data point have expired.

For example, if a customer provides consent for the collection of the first name for two data policies, the first name is deleted only at the end of the longest retention period.

For more information on removing data points manually, see _Chapter 7_.

### Creating a Data Policy

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. In the top right pane, click **Create Data Policy**. The **New Data Policy** tab appears in the bottom pane.

4. In the **New Data Policy** tab, on the **Summary** tab, enter values for the following fields:

    | Field | Description |
    | --- | --- |
    | Name | The display name of the data policy. |
    | Reference Key | The unique ID of a data policy as provided by an external system, such as a CMS. |
    | Retention Type | Indicates whether the data policy retains data from its creation date or its last modified date. |
    | Retention Period (Days) | Specifies how long data is stored in the system. Retention period can either be calculated from the creation date or from the last updated date. |
    | State | Specifies the current state of a data policy. For a new data policy, select **Draft**. |
    | Start Date | Specifies the date and time a data policy begins collecting data. |


5. Enter values for the following optional fields:

    | Field | Description |
    | --- | --- |
    | End Date | Specifies when a data policy no longer collects consent. A data policy's end date can be changed once active. |
    | Description | A description of the data policy. |
    | Activities | Activities that the data policy is associated with. |

6. To add data points to the data policy, click the **Data Points** tab. All available data points are listed in the **Data Points** window. You can select data points from the available data points and assign to the data policy. You can also create a new data point and assign to the data policy. For more information about creating a new data point, see _Data Points_.

7. To add data policy segments to the data policy, click the **Data Policy Segments** tab. To add a new data policy segment, enter a name in the **Add Segment** field, and click **Add Segment**. For more information see _Data Policy Segments_.

    > **Note**: You must create at least one data policy segment for a data policy.

8. On the toolbar, click **Save** then click **Refresh**. The data policy is added to the list in the **Data Policies** tab.

### Activating a Data Policy

Data policies are enabled for a store if the **Enable Data Policies** field in the store settings is selected. An activated data policy applies to all stores which have data policies enabled.

> **Warning**: The end date of an active data policy is editable, but all other fields are not.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. Double-click a data policy. The data policy appears in the bottom right pane.

4. In the **State** list, select **Active**.

5. In the toolbar, click **Save**, then click **Refresh**. The data policy's state updates in the **Data Policies** tab.

### Disabling a Data Policy

Disabling a data policy prevents Elastic Path Commerce from recording consent for the data policy, but does not truncate how long the data policy stores data. Data collected on the last day a data policy is active is retained for the full retention period of the policy, starting from that day or the data's last modified date.

> **Warning**: A disabled data policy **cannot** be re-activated.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. Select a data policy and click **Disable Data Policy**. A warning prompt to disable the data policy appears.

5. Click **OK**.

6. In the toolbar, click **Save**.

### Data Points

A data point refers to specific information about a customer that Elastic Path Commerce collects. A data point consists of customer data, the location of the data, creation and modification dates, and additional information about the data point itself.

#### Marking a Data Point as Removable

The data key value of a data point can be removed by a customer service representative for the customer by selecting **Removable** when creating a data point. You can mark any data point as removable, but removing the data key value for the data points that are required for the completion of purchase can cause errors in the system.

The following data point data keys should not be marked as removable unless otherwise specified by your system administrator:

| Data Location | Data Key | Result if Removed |
| --- | --- | --- |
| `CUSTOMER_PROFILE` | `CP_EMAIL` | The customer cannot add email address back to the profile and cannot complete the purchase. |
| `CUSTOMER_PROFILE` | `CP_FIRST_NAME` | The customer cannot complete the purchase and the system displays an error. |
| `CUSTOMER_PROFILE` | `CP_LAST_NAME` | The customer cannot complete the purchase and the system displays an error. |

For more information on removing customer data, see Chapter 7, _Customer Data Policies_.

#### Creating a Data Point

> **Warning**: A data policy must be in the draft state to add or create new data points.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. Double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Points** tab, click **Create Data Point**. The create data point pop-up appears.

5. Enter values for the following fields:

    | Field | Description |
    | --- | --- |
    | Name | The display name of the data point. |
    | Data Location | The location of the data point value. |
    | Data Key | The value of the data point. <br/><br/> **Note:** For the `ORDER_DATA`, `CUSTOMER_PROFILE` and `CART_GIFT_CERTIFICATE` data locations, the data key is a custom value, and must be provided to you by your system administrator. |
    | Description Key | The key or unique identifier used by the CMS or other system to describe the data point. |
    | Removable | Specifies whether a data point needs to be retained for auditing or legal reasons. |

6. Click **Save**. The new data point appears in the list of available data points.

Once a data point is created, it is available for use in all data policies in Elastic Path Commerce. You cannot create a data point for a data location-data key pair that already exists.

#### Adding a Data Point to a Data Policy

> **Warning**: A data policy must be in the draft state to add or create new data points.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. Double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Points** tab, in the **Available Data Points** list, select the data point(s) to add to the data policy.

    > **Note** To select multiple data points, hold the **Shift** key on the keyboard and select multiple data points in the list.

5. Use the **>** or **>>** buttons to add data points to a data policy.

6. On the toolbar, click **Save**.


#### Removing a Data Point from a Data Policy

> **Warning**: A data policy must be in the draft state to remove data points.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. Double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Points** tab, in the **Available Data Points** list, select the data point(s) to remove from the data policy.

    > **Note** To select multiple data points, hold the **Shift** key on the keyboard and select multiple data points in the list.

5. Use the **<** or **<<** buttons to add data points to a data policy.

6. On the toolbar, click **Save**.

### Data Policy Segments

Data policy segments indicate logical segments of customers for which a data policy can be applied. For example, you can create data segments for different geographical locations. Each data policy is associated with one or more data policy segments. A data segment can be associated with one or more data policies depending on the data protection laws in that segment. How a data policy functions is configured by the front-end developer in the CMS.

#### Creating a Data Policy Segment

> **Warning**: A data policy must be in the draft state to creating data policy segments.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. In the **Data Policies** tab, double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Policy Segments** tab, in the **Add Policy** field, enter a data policy name.

5. Click **Add Segment**. The data policy is added to the **Assigned Segments** list.

6. On the toolbar, click **Save**.

#### Editing an Existing Data Policy Segment

> **Warning**: A data policy must be in the draft state to edit data policy segments.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. In the **Data Policies** tab, double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Policy Segments** tab, in the **Assigned Segments** list, select a data policy segment to edit and click the ![](images/Ch04-01.png) icon.

5. Edit the name as needed.

6. On the toolbar, click **Save**.

#### Removing a Data Policy Segment

> **Warning**: A data policy must be in the draft state to remove data policy segments.

1. On the toolbar, click **Configuration**.

2. In the left pane, click **Data Policies**. The **Data Policies** tab appears in the top right pane.

3. In the **Data Policies** tab, double-click a data policy. The data policy appears in the bottom right pane.

4. On the **Data Policy Segments** tab, in the **Assigned Segments** list, select a data policy segment.

5. Click **Remove Segment**.

6. On the toolbar, click **Save**.

## Payment Gateways

A payment gateway processes and authorizes payments made from a customer to a retailer. Payment gateways encrypt sensitive information (such as credit card numbers) and ensure that the information is transferred securely.

Some payment gateways issue a certificate file to the merchant and require the merchant to use the certificate in all transactions. Define the certificate file&#39;s location using the steps outlined in the _Setting a Payment Gateway Certificate_ section.

> **Note**: Changes to payment gateway settings do not take effect until you restart the web applications.

### Creating a Payment Gateway

1. On the toolbar, click the **Configuration** button.

2. In the **Payment Methods** section of the left pane, select **Payment Gateways**. The **Payment Gateways** tab appears in the top right pane.

3. Click **Create Gateway**.

4. In the _Create Payment Gateway_ dialog box, enter a **Gateway Name** and a **Gateway Type**.

    > **Note:** You cannot change the **Gateway Type** setting after the gateway is created.The parameters in the **Properties** table vary depending on the Gateway Type, and can be configured by the developers.

5. Click **Save**.

### Editing a Payment Gateway

1. On the toolbar, click the **Configuration** button.

2. In the **Payment Methods** section of the left pane, select **Payment Gateways**. The **Payment Gateways** tab appears in the top right pane.

3. Select the payment gateway you want to change.

4. Click **Edit Gateway**.

5. Modify the gateway information.

6. Click **Save**.

### Deleting a Payment Gateway

1. On the toolbar, click the **Configuration** button.

2. In the **Payment Methods** section of the left pane, select **Payment Gateways**. The **Payment Gateways** tab appears in the top right pane.

3. Select the payment gateway you want to delete.

3. Click **Delete Gateway**.

4. In the _Delete Payment Gateway - Confirm_ dialog box, click **OK**.

### Setting a Payment Gateway Certificate

Some payment gateways provide the merchant with a certificate. Use the following System Configuration setting to define the certificate&#39;s location.

| Setting Name | Description |
| --- | --- |
| `COMMERCE/SYSTEM/PAYMENTGATEWAY/certificatesDirectory` | Defines the base directory where the payment gateway certificates are. |


When setting the payment gateway, the path to the certificate should be relative to the base directory. For example, in the case of PayPal, if the key resides in `/var/ep/payment/paypal/cert.p12`, set the `certificatesDirectory` setting to `/var/ep/payment/`, and set the `certificateFile` setting in the payment gateway to to `paypal/cert.p12`.

## Shipping Regions and Service Levels

In Elastic Path Commerce, a shipping region is a geographical entity (for example, country, state, or province), while a shipping service level determines what shipping services are available to customers at each store.
>Note: Elastic Path provides a default shipping calculation plug-in, which can be used to configure shipping regions and shipping service levels. However, if you want to use a custom shipping calculation plug-in, you must remove the default shipping calculation plug-in from Elastic Path Commerce Manager before you integrate the custom plug-in with the application.

### Shipping Regions

A shipping region is a geographical entity (for example, country, state, province) that your store ships to. Shipping service levels are the delivery services available in a given shipping region (for example, Ground Shipping, Overnight Shipping, etc.)

A customer&#39;s shipping cost depends on the shipping region they reside in and the shipping level they choose. A shipping region should use a single set of shipping service levels and costs. You must configure shipping regions before the store&#39;s checkout process can work.

> **Note**: If your shipping service levels and shipping costs are identical for all locations, you only need to create one shipping region. If you use non-global service levels (that is, they are available only in some parts of the world), or if shipping cost calculations vary between regions, you need to create multiple shipping regions.

#### Creating a Shipping Region

1. On the toolbar, click the **Configuration** button.

2. In the **Shipping** section of the left pane, select **Shipping Regions**. The **Shipping Regions** tab appears in the top right pane.

3. Click **Create Shipping Region**.

4. In the _Create Shipping Region_ dialog box, enter a name for the region.

5. In the **Available Countries/Sub Countries** list, select the countries or sub countries to include in the region.

    - To select multiple countries or sub-countries, hold the **CTRL** or **Shift** keys and click to select your choices.

6. Click **&gt;** to add the countries or sub-countries to the **Selected Countries/Sub Countries** list.

7. Click **Save**.

#### Editing a Shipping Region

1. On the toolbar, click the **Configuration** button.

2. In the **Shipping** section of the left pane, select **Shipping Regions**. The **Shipping Regions** tab appears in the top right pane.

3. Select the shipping region you want to modify.

4. Click **Edit Shipping Region**.

5. In the _Edit Shipping Region_ dialog box, make your changes.

6. Click **Save**.

#### Deleting a Shipping Region

> **Note:** If tax values are configured for a region, you cannot delete the region until you remove the tax values.

1. On the toolbar, click the **Configuration** button.

2. In the **Shipping** section of the left pane, select **Shipping Regions**. The **Shipping Regions** tab appears in the top right pane.

3. Select the shipping region you want to delete.

4. Click **Delete Shipping Region**.

5. Click **OK**.

### Shipping Service Levels

Shipping service levels determine the shipping services available to customers at each store. For example, customers for a store selling high-value items may be offered overnight shipping at a reduced rate, while customers shopping at a discounted storefront may be offered slower surface delivery options.

Shipping service levels are configured under **Promotions/Shipping**.

#### Searching for Service Levels

1. On the toolbar, click the **Promotions/Shipping** button.

2. On the left pane, click the **Shipping** tab.  

3. In the **Filters** section of the left pane, select a shipping service level state, shipping region, and store from the respective lists.

4. (Optional) In the **Sorting** section, select the sorting options for the returned search results.

5. Click **Search**.

#### Sorting Search Results

You can sort the shipping service level search results by clicking on a column header.

#### Pagination of Search Results

By default, the search results show 10 items per page. You can change this by clicking the **admin** list in the top right corner of the right pane, and selecting **Change Pagination Settings**. Choose a **Results Per Page** value from the list, then click **Save**.

### Creating a Service Level

1. On the toolbar, click the **Promotions/Shipping** button.

2. On the left pane, click the **Shipping** tab.  

3. (Optional) Perform a shipping service level search with default values.

4. On the top right pane toolbar, click **Create Service Level**.

5. In the _Create Shipping Service Level_ dialog box, set the values for your new shipping region.

    > **Note:** A _Unique Code_ is required for data importing and exporting.

6. Click **Save**.

### Editing a Service Level

1. On the toolbar, click the **Promotions/Shipping** button.

2. On the left pane, click the **Shipping** tab.  

3. (Optional) Perform a shipping service level search with default values.

4. Select the shipping service level you want to modify, and click **Open Service Level**.

5. In the _Edit Shipping Service Level_ dialog box, edit the values for your new shipping region.

6. Click **Save**.

### Deleting a Service Level

1. On the toolbar, click the **Promotions/Shipping** button.

2. On the left pane, click the **Shipping** tab.

3. (Optional) Perform a shipping service level search with default values.

4. Select the shipping service level you want to delete, and click **Delete Service Level**.

5. In the _Delete Shipment Service Level_ dialog box, click **OK**.

## Tax Configuration

Store administrators need to configure taxes for jurisdictions that are &quot;tax nexuses&quot; (jurisdictions with the right to impose a tax on taxpayers).

To configure a tax system for their store, administrators must configure:

- **Tax Codes**: Products are connected to taxes through _tax codes_. Tax codes define the type of tax that is applicable to a product (for example, Shipping vs Goods vs Digital taxes), and are visible to customers.

- **Tax Categories** : The _tax categories_ or taxes determine the taxes that are applicable to each _tax jurisdiction_. For example, the &quot;PST&quot; (the Provincial Sales Tax) or &quot;State Tax&quot; (the State tax) or &quot;VAT&quot; (Value Added Tax) rates may be different from one region to another.

- **Tax Jurisdictions**: The _tax jurisdictions_ are hierarchical, so it is possible to have different tax categories applied at various levels. These include country, sub-country (state or province), city, and zip code.  Tax Jurisdictions are either _inclusive_ or _exclusive_.

- **Tax Values**: The _tax rate_ applied to a tax category in a particular jurisdiction.

### Tax Configuration Hierarchy

You need to configure taxes in a specific hierarchy:

- First, configure the _tax jurisdictions_. These are the countries in which you need to calculate a tax for.

- Next, configure the _country level tax_, if required. For example the VAT in UK or GST in Canada.

    > **Note**: Some countries do not have country-level taxes. A country can have multiple country level taxes.

- Next, configure the _sub-country level tax_. For example, the PST in Canada, which is calculated at a different rate based on the province/region in the country.

- Finally, configure each sub-country's _tax values_. These are tax rates for specific types of goods (as defined by your _tax codes_) that can be different based on the sub-country.  

  > **Note**: A country (tax jurisdiction) may have multiple country-level taxes, but each country level tax cannot define the tax value. The tax values can only be assigned to sub-country taxes.


### Inclusive and Exclusive Tax Calculations

_Inclusive_ and _exclusive_ taxes indicate whether the taxes are included in the shown or advertised price of an item. Inclusive taxes are included in the price, while exclusive taxes are not.

For example, the advertised price for gasoline in Canada is inclusive; it includes the GST (Goods and Service Tax). However, most other goods sold in Canada are exclusive. The GST and other sales taxes are added to the advertised price of the item when the purchase is made. For more information, see the Tax Exemption section.

#### Inclusive Tax Calculation Example

The customer is buying 1 tennis racket that costs £75. The VAT tax rate is 15%; shipping charge is £5 and is also subject to VAT.

| **Line Item** | **Amount** | **Calculation** |
| --- | --- | --- |
| Subtotal | £75.00 | (sum of item prices) |
| Item tax | £9.78 | (VAT/1 + VAT)\*item price |
| Shipping tax | £0.65 | (VAT/1 + VAT)\*shipping price |
| Total tax | £10.43 | (item tax + shipping tax) |
| Total | £80.00 | (subtotal + shipping price) |

#### Inclusive Tax Calculation Example with Cart Discount

The customer is buying 1 tennis racket that costs £75. The VAT tax rate is 15%; shipping charge is £5 and is also subject to VAT. There is a discount of 10% on the subtotal.

| **Line Item** | **Amount** | **Calculation** |
| --- | --- | --- |
| Subtotal | £75.00 | (sum of item prices) |
| Discount | £7.50 | (subtotal \* discount percentage) |
| Item tax | £8.80 | (VAT/1 + VAT)\*(item price - discount) |
| Shipping tax | £0.65 | (VAT/1 + VAT)\*shipping price |
| Total tax | £9.45 | (item tax + shipping tax) |
| Total | £72.50 | (subtotal + shipping price) |

#### Exclusive Tax Calculation Example

The customer is buying 1 tennis racket that costs $75. The GST is 5% and PST is 7%; shipping charge is $5 and is also subject to GST and PST.

| **Line Item** | **Amount** | **Calculation** |
| --- | --- | --- |
| Subtotal | $75.00 | (sum of item prices) |
| Item tax | $9.00 | (item price \* (GST + PST)) |
| Shipping tax | $0.60 | (shipping price \* (GST + PST) |
| Total tax | $9.60 | (item tax + shipping tax) |
| Total | $89.60 | (subtotal + shipping price + total tax) |

#### Exclusive Tax Calculation Example with Cart Discount

The customer is buying 1 tennis racket that costs $75. The GST is 5% and PST is 7%; shipping charge is $5 and is also subject to GST and PST. There is a discount of 10% on the subtotal.

| **Line Item** | **Amount** | **Calculation** |
| --- | --- | --- |
| Subtotal | $75.00 | (sum of item prices) |
| Discount | $7.50 | (item price \* discount percentage) |
| Item tax | $8.10 | (item price - discount) \* (GST + PST) |
| Shipping tax | $0.60 | (shipping price \* (GST + PST) |
| Total tax | $8.70 | (item tax + shipping tax) |
| Total | $81.20 | (subtotal - discount + shipping price + total tax) |

### Calculating Multiple Taxes

For multiple taxes, there is a different formula for calculating the tax depending on whether the prices are tax inclusive or tax exclusive. In the tax exclusive case, the system multiplies the taxes against the price of each item and sums the results. In the tax inclusive case, the calculation is more complex, as shown below:

Calculating multiple inclusive taxes, for example VAT and an environmental tax (ENV):

```
vatTaxes = (VAT/(1 + VAT + ENV))\*price, envTaxes = (ENV/(1 + VAT + ENV))\*price
```

### Taxes and Discounts

Subtotal discounts apply to the sum of the line item prices. If the discount is given as a dollar amount (for example, $10 off on your cart), then that dollar amount is removed from the price sum. If the discount is given as a percentage (for example, 25% off on your cart subtotal), the system computes the dollar amount by taking the percentage of the sum of the prices.

- In the tax exclusive case, the discount is taken from the pre-tax price.

- In the tax inclusive case, the discount is taken from the tax inclusive price.

### Taxes and Order Returns

If the customer is returning the entire order, the system refunds all the taxes paid. If they are returning only a portion of the order, Elastic Path Commerce returns only a portion of the taxes. This is determined by looking at the total item taxes paid on the line item they are returning and then multiplying those taxes by the portion they are returning.

For example, a customer buys three tennis rackets having total item taxes of $29.34. They then return one tennis racket. The system returns (1/3)\*29.34= $9.78 in taxes. If they are returning two tennis rackets they get (2/3)\*29.34= $19.56 in taxes. The line item amounts are added to give the total taxes to return. By default, shipping costs and taxes are not refunded.

### Tax Codes

#### Creating a Tax Code

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Codes**. The **Tax Codes** tab appears in the top right pane.

3. On the top right pane toolbar, click **Create Tax Code**.

4. In the _Create Tax Code_ dialog box, enter the **Tax Code** value.

5. Click **Save**.

#### Editing a Tax Code

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Codes**. The **Tax Codes** tab appears in the top right pane.

3. Select the tax code you want to edit.

4. On the top right pane toolbar, click **Edit Tax Code**.

5. In the _Edit Tax Code_ dialog box, make your changes

6. Click **Save**.

#### Deleting a Tax Code

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Codes**. The **Tax Codes** tab appears in the top right pane.

3. Select the tax code you want to delete.

4. On the top right pane toolbar, click **Delete Tax Code**.

5. Click **OK**.

### Tax Jurisdictions

#### Creating a Tax Jurisdiction

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Jurisdictions**. The **Tax Jurisdictions** tab appears in the top right pane.

3. On the top right pane toolbar, click **Create Tax Jurisdiction**.

4. In the _Create Tax Jurisdiction_ dialog box, enter values in the following fields:

    | Field | Description |
    | --- | --- |
    | Jurisdiction Country | The country to which this tax applies. |
    | Tax Calculation Method | Specify whether the prices in this jurisdiction include taxes (inclusive), or whether they exclude taxes (exclusive). |
    | Configure Taxes | Specify the taxes that apply to this jurisdiction. In the _Create Tax_ dialog box, specify the **Tax Name** and **Address Field**. |

5. Click **Save**.

#### Editing a Tax Jurisdiction

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Jurisdictions**. The **Tax Jurisdictions** tab appears in the top right pane.

3. Select the tax jurisdiction you want to edit.

4. On the top right pane toolbar, click **Edit Tax Jurisdiction**.

5. In the _Edit Tax Jurisdiction_ dialog box, make your changes.

6. Click **Save**.

#### Deleting a Tax Jurisdiction

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Jurisdictions**. The **Tax Jurisdictions** tab appears in the top right pane.

3. Select the tax jurisdiction you want to delete.

4. On the top right pane toolbar, click **Delete Tax Jurisdiction**.

5. Click **OK**.

### Tax Values

#### Creating a Tax Value

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Values**. The _Manage Tax Values_ dialog box appears.

3. Configure the filter settings and click **Filter** to retrieve the tax values.

4. Click **Add**.

5. In the _Add Tax Rate_ dialog box, enter values in the fields.

6. Click **Save**.

#### Editing a Tax Value

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Values**. The _Manage Tax Values_ dialog box appears.

3. Configure the filter settings and click **Filter** to retrieve the tax values.

4. Click **Edit**.

5. In the _Edit Tax Rate_ dialog box, edit the values in the fields.

6. Click **Save**.

#### Deleting a Tax Value

1. On the toolbar, click the **Configuration** button.

2. In the **Taxes** section of the left pane, select **Tax Values**. The _Manage Tax Values_ dialog box appears.

3. Configure the filter settings and click **Filter** to retrieve the tax values.

4. Click **Remove**.

5. Click **OK**.

### Tax Exemption

Companies who sell in B2B (Business to Business) type scenarios, or who have non-profit, charity, or government customers often require the processing of sales where taxes are not calculated or collected.  In these scenarios, a tax exemption code, and possibly a business identification number, is collected and stored along with the order for audit purposes.  The selling company must be able to prove their due diligence in the non-collection of taxes for a given order.  The selling company may need to enable the tax exemption code and/or the business identification number to be either provided by the customer at the time of the sale or recorded in their Customer Profile.

When an exemption number is provided, the information can be passed along to any integrated Tax Provider service, so the request can be recorded and taxes are not calculated.

> **Note:** The Elastic Path tax engine currently does not factor in exemption.  When using Elastic Path tax tables, taxes are calculated regardless of entering exemption codes. The Tax Exemption functionality only applies when integrating with a tax provider.

## Warehouses

The Warehouse configuration settings in Elastic Path Commerce let you set up a warehouse entity that you can then associate to a store. After a warehouse is associated to a store, you can track the inventory of shipped and received products.

### Creating a Warehouse

1. On the toolbar, click the **Configuration** button.

2. In the **Warehouses** section of the left pane, select **Warehouses**. The **Warehouses** tab appears in the top right pane.

3. On the top right pane toolbar, click **Create Warehouse**.

4. In the _Create Warehouse_ dialog box, enter values for your new warehouse as described in the following table.

    | **Field** | **Description** |
    | --- | --- |
    | Warehouse Code | The unique identifier for the warehouse. You cannot change the code after the warehouse is created. |
    | Warehouse Name | The name of the warehouse. |
    | Address Line 1 | The address of the warehouse. |
    | Address Line 2 | If necessary, the remaining address information of the warehouse. |
    | City | The city the warehouse is located in. |
    | State/Province/Region | The state, province, or region the warehouse is located in, if applicable. |
    | Zip/Postal Code | The postal code of the warehouse. |
    | Country | The country the warehouse is located in. |

5. Click **Save**.

### Editing a Warehouse

1. On the toolbar, click the **Configuration** button.

2. In the **Warehouses** section of the left pane, select **Warehouses**. The **Warehouses** tab appears in the top right pane.

3. Select the warehouse you want to edit.

4. On the top right pane toolbar, click **Edit Warehouse**.

5. In the _Edit Warehouse_ dialog box, make your changes.

6. Click **Save**.

### Deleting a Warehouse

1. On the toolbar, click the **Configuration** button.

2. In the **Warehouses** section of the left pane, select **Warehouses**. The **Warehouses** tab appears in the top right pane.

3. Select the warehouse you want to delete.

4. On the top right pane toolbar, click **Delete Warehouse**.

5. Click **OK**.
