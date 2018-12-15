# Store Configuration

[TOC]

## Overview

A _store_ in Elastic Path Commerce represents the user facing application that lets customers shop for products from a particular catalog. This user facing application could be a website, mobile application, or more.

### Configuration Options

> **Note**: If you are using a front-end CMS which allows for the configuration of settings on this list like language localization, Elastic Path recommends configuring those settings in the CMS.

Elastic Path allows you to configure the following settings for a store:

- General information, like the countries the store operates in.
- Language localization
- Data policy collection
- Store catalogs
- Store warehouses
- Taxes and tax codes
- Payment Gateways
- Customer accounts
- Marketing features
- HTML encoding
- Facet Configuration

### Prerequisites

Before creating and configuring a store, you must configure all of the following, as they are required during the store configuration process.

| Configuration | Configuration Instructions |
| :------------- | :------------- |
| Shipping Regions      | [Commerce Configuration](commerce-configuration.md) |
| Shipping Service Levels | [Shipping/Receiving](shipping.md) |
| Tax Codes | [Commerce Configuration](commerce-configuration.md) |
| Tax Jurisdictions |[Commerce Configuration](commerce-configuration.md) |
| Warehouses | [Warehouses](commerce-configuration.md#warehouses) |
| Catalogs | [Catalog Management](catalog-management.md) |
| Data Policies | [Commerce Configuration](commerce-configuration.md) |
| Faceted Search |[Facet Search](store-configuration.md#facet-search.md) |

### Creating a Store

1. On the toolbar, click the **Configuration** button.

2. In the left pane , click **Stores**.

3. From the top right pane toolbar, click **Create Store**.

1. In the bottom pane, select the store's **Summary** tab.
2. In the **Store Code** field, enter a unique identifier for the store.

     - The store code must only contain alphanumeric characters (A-Z, a-z, 0-9) and must not contain any spaces.

3. In the **Store Name** field, enter a name for the store.
4. If required, in the **Store Description** field enter a description for the store.
5. In the **Store URL** field, enter the URL which customers use to access the store.
6. In the **Store State** tab, set the store state to **Under Construction**.
7. In the **Operational Timezone** drop-down menu, select the time zone the store will operate in and will use for recording timestamps.
8. In the **Store Country** drop-down menu, select the country the store will operate out of.
9. In the **Store Sub-Country** drop-down menu, select the sub-country (province or state) the store will operate out of.
    - This option is only available if the store country selected has sub countries, such as the United States or Canada.
10. If required, select the **Enable Data Policies** checkbox to enable data policies for a store. For more information, see _Enabling Data Policies for a Store_.
    > **Warning**: Enabling data policies for a store enables **all** active data policies in Elastic Path Commerce for the store.

5. If required, in the **Localization** tab, complete all required fields. For more information, see _Localization Configuration_.

6. In the **Catalog** tab, select a catalog for the store.

7. In the **Warehouse** tab, select a warehouse for the store.

8. In the **Taxes** tab, select the following:
    - At least one Tax Jurisdiction from the **Enabled Tax Jurisdictions** list.
    - At least one Tax Code from the **Enabled Tax Codes** list.

9. In the **Payments** tab, configure the payment options for the store to support.

10. If required, in the **Shared Customer Accounts** tab, complete all required fields. For more information see _Shared Account Configuration_.

11. If required, in the **Marketing** tab, complete all required fields. For more information, see _Marketing Configuration_.

12. If required, configure the HTML encoding for a store. For more information, see _Configuring HTML encoding for a Store_.

17. In the **Configuration** tab of the left pane, click **System Configuration**.

18. In the **Setting Name** table of the top right pane, select `COMMERCE/STORE/storefrontUrl`

19. In the **Defined Values** section, click **New**.

20. In the _Add Configuration Value_ dialog box, enter the following:

    |Context|Value|
    |---|---|
    | The store code. For example, for the Snap It Up store, enter SNAPITUP. | The secure (HTTPS) storefront URL. If the store URL is [http://mystore.example.com:8080](http://mystore.example.com:8080), enter [https://mystore.example.com:8080/storefront](https://mystore.example.com:8080/storefront). |
21. Click **Save**.
22. In the **Facet Configuration** tab, enter the required configurations.
23. Click **Save**.

### Building the Store's Search Index

After a new store is created, the product search index must be rebuilt before any existing products become visible in the store. The index rebuild is scheduled automatically, but you can perform a manual rebuild as follows:

1. In the left pane, click **Search Indexes**.

2. Select the product index you want to rebuild.

3. From the top right pane toolbar, **Rebuild Index**.

### Store States

Every store has a state. The store&#39;s state determines who can access the store and what actions they can perform. Users with the appropriate permissions can change the state of a store.

You can assign the following states to a store:

- Under Construction
- Restricted Access
- Open

The following table describes the store states:

| **Store State**    | **Description** |
| ---                | ---             |
| Under Construction | A store in this state has the following characteristics:<br/>- You cannot view it in Elastic Path Commerce or a web browser.<br/>- You cannot reference it from objects in the system, including the Core API and Web Services.<br/>- It does not appear in any store lists in Elastic Path Commerce except the store list view.<br/>- It does not include store-related data when the search indexes are built.<br/>- It does not accept orders.<br/><br/>When the store is in this state, you cannot change it to another state until all the required settings are configured. <br/><br/>**Note**: After all the settings are configured, you cannot revert to the **Under Construction** state.  |
| Restricted Access | A store in this state has the following characteristics: <br/>- You cannot view it in Elastic Path Commerce or a web browser.<br/>- You cannot reference it from objects in the system, including the Core API and Web Services.<br/><br/>- It does not accept orders.<br/><br/> |
| Open | A store in this state has the following characteristics:<br/>- You can view it in Elastic Path Commerce and a web browser.<br/>- You can reference it from all the other objects in the system, including Cortex and Web Services.<br/>- It appears in all the store lists within Elastic Path Commerce.<br/>- It includes store-related data when the search indexes are built.<br/>- It accepts orders.<br/>You can change the state of a store in the &quot;Open&quot; state to the &quot;Restricted Access&quot; state at any time.  |

#### Changing a store's state

1. In any tab for a store in the bottom right pane, click the **Change Store State** button.

2. Select the store state to change.

3. Click **Save**.


### Editing a Store

> **Prerequisite**: The store must be in the **Under Construction** state.

1. On the toolbar, click the **Configuration** button.

2. In the left pane, click **Stores**.

3. From the top right pane, select the store you want to edit.

4. From the top right pane toolbar, click **Edit Store**.

5. Make the required changes in the **Summary** tab.

6. On the toolbar, click **Save**.

### Deleting a Store

> **Note**: You cannot delete a store that is in use.

1. On the toolbar, click the **Configuration** button.

2. In the left pane , click **Stores**.

3. From the top right pane, select the store you want to delete.

4. From the top right pane toolbar, click **Delete Store**.

5. In the _Delete Store - Confirm_ dialog box, click **OK**.

### Advanced Store Configuration

#### Configuring Localization

Elastic Path Admin Console supports the following localization options:

- A selection of available languages to display the store in.
- A default language to display the store in.
- A selection of available currencies for the store.
- A default currency for the store.

##### Configuring Available Store Languages

> **Prerequisite**: The store must be in the **Under Construction** state.

1. In the bottom pane, select a store's **Localization** tab.

2. In the **Language Selection** section locate the **Available Languages** list and select the language you want to use for your store.

3. Click **>** to add it to the list of selected languages.

4. Click **Save**.

##### Configuring a Default Store Language

If a shopper has specified a default language in their customer account and is logged in, the store will use their selected language. Otherwise, the default language is used.

1. In the bottom pane, select a store's **Localization** tab.

2. In the **Defaults** section, locate the **Default Language** drop down and select a default language.

3. Click **Save**.


##### Configuring Available Store Currencies

1. In the bottom pane, select a store's **Localization** tab.

2. In the **Currency Selection** section locate the **Available Currencies** list and select the currencies you want to use for your store.

3. Click **>** to add it to the list of selected currencies.

4. Click **Save**.

##### Configuring a Default Currency

If a shopper has specified a default currency in their customer account and is logged in, the store will use their selected currency. Otherwise, the default currency is used.

1. In the bottom pane, select a store's **Localization** tab.

2. In the **Defaults** section, locate the **Default Currency** drop down and select a default currency.

3. Click **Save**.

#### Sharing Customer Accounts Between Stores

Elastic Path allows allows you to share the registered customers&#39; profile information (for example, accounts or shipping addresses) with other stores. By default, customer profiles are not shared.

Account sharing is bi-directional: accounts created in one store can log on to another store and vice versa.

To share customer accounts between stores:

1. In the **Store** tab, select the **Shared Customer Accounts** tab.

2. Select the store to share customer accounts with from the **Available Stores** list and click **>**.

3. Click **Save**.

#### Configuring Marketing Settings

The **Marketing** tab configures marketing system settings and metadata for a store.

The following settings can be configured:


| **Field** | **Description** |
| --- | --- |
| `COMMERCE/STORE/CATALOG/CatalogSitemapPagination` | The maximum number of products listed on a page in the sitemap. |
| `COMMERCE/STORE/CATALOG/CatalogViewPagination` | The maximum number of items to display per page when browsing the catalog in the store. |
| `COMMERCE/STORE/CATALOG/featuredProductCountToDisplay` | The number of featured products to display when browsing a catalog with zero or one filter applied. |
| `COMMERCE/STORE/PRODUCTRECOMMENDATIONS/numberMaxRecommendations` | The maximum number of product recommendations to display. |
| `COMMERCE/STORE/PRODUCTRECOMMENDATIONS/number OrderHistoryDays` | The number of days of historical order data to use when calculating product recommendations. For example, if the value is 3, product recommendations are calculated based on all the orders created during the past three days. |
| `COMMERCE/STORE/SEARCH/searchCategoriesFirst` | Specifies whether searches in the store search categories before searching products. If a category match is found, the first matching category page is displayed instead of the search results page. |
| `COMMERCE/STORE/SEARCH/showBundlesFirst` | Specifies whether to display product bundles before other products in search results. |
| Display Out Of Stock Products | Required setting. Specifies whether to display products with 0 items in the inventory in the catalog and catalog searches. |
| Store Admin Email Address | Required setting. The store administrator&#39;s e-mail address.<br/><br/> Example:  administrator@snapitup.com |
| Store From Email (Friendly Name) | The sender name on all outgoing e-mails generated by the store. Example:  SnapItUp Sales &amp; Service |
| Store From Email (Sender Address) | Required setting. The e-mail account used to send out system-generated e-mails for the store. <br/><br/>Example:  Sales@snapitup.com |


##### Configuring a Marketing Setting

1. In the **Marketing** tab, select the setting you want to edit.

2. Click **Edit Value**.

3. Edit the value as required and click **Save**.

##### Removing a Setting Configuration

1. In the **Marketing** tab, select the setting you want to edit.

2. Click **Edit Value**.

3. Edit the value as required and click **Save**.

#### Configuring HTML Encoding for a store

> **Prerequisite:** The store must either be newly created or the store's state must be **Under Construction**.

Stores in Elastic Path use the UTF-8 character encoding by default. To change the default encoding:

1. In the store's **System** tab, select the **Store HTML Encoding** from the list.

2. Click the **Edit Value** button.

3. In the **Value** text field, enter a character encoding standard.

4. Click **Save**.

5. In the toolbar, click **Save**.

### Faceted Search

A store might be configured with product attributes, SKU attributes, SKU options, or other attributes for the entire product catalog in the store. These product details are called _facets_ and the options available in each facet are called _facet values_. A merchandiser can configure a facet as _facetable_ in Commerce Manager if the corresponding product attribute is configured as indexable. <br/>

The faceted offer search operation groups all similar SKUs and returns an offer with facets. For a store with huge catalogs, an offer search query might return a large number of results, and the shopper might have to navigate through the results to find the required item. With faceted offer search, the search engine processes a search query and returns relevant search results with facets and facet values depending on the facet configuration in Commerce Manager.<br/>
For example, if the size and color attributes in a clothing store are facetable, when a shopper searches for shirts, the search engine returns shirts in all available sizes and colors. A shopper can refine the search results by selecting the required size or color. In this example, size and color are facets and the options in each facet, such as large, small, blue, white, or red, are the facet values. A variant of a product corresponding to a specific SKU, such as a large blue shirt, large white shirt, or small blue shirt, represents an offer.

Commerce Manager provides options to configure facets for a store using the product attributes, SKU attributes, SKU options, and other attributes configured for the store. Each of these attributes represents a facet name. When you configure a store, Commerce Manager does not configure facets by default. You must configure attributes as facetable to display the available facets in search results, so that shoppers can refine the search results.

When a shopper searches for the keyword _shirt_:
 * For keywords search without facets: Cortex returns all results matching the keywords. All SKUs associated with a product are listed as separate products in the search result. The shopper must navigate through the results until the required item is found. Keywords search does not provide options to refine the search result.
 * For faceted offer search: Cortex returns all results matching the keyword along with the available facets. A shopper can refine the search results to view the exact match for the query. For example, a shopper can view only the blue shirts in small size from all brands by selecting small in size and blue in color facets.    

Elastic Path provides the following options depending on the facet value type:
  * **Facets**: Sets boolean or string type facets as facetable  
  * **Range facets**: Sets decimal or integer type facets as facetable

#### Configuring Facets

Use this procedure to configure boolean or string type facets, such as feature or brand.

1. Click **Configuration** > **Store** and select a store.
2. Click the **Facet Configuration** tab.
3. In the **Facet Configuration** table, select an attribute row.
4. In the **Facetable** field for the selected attribute, select **Facet**.<br>
5. (Optional) To change the default display name and locale for the facet, click **Edit**. <br/>You can only update the display name and locale.<br/>
5. Click **Save**.


#### Configuring Range Facets

Merchandisers can define ranges for decimal or integer type facets, such as price, weight, or length. A shopper can refine the search results by selecting appropriate ranges. When the shopper selects a range in the search query, the search engine returns the results within the selected range, excluding the upper bound value.<br/>

<b>Example</b> <br/>
**Store details**: For a laptop store, a merchandiser configures the price attribute as facetable and the offer price range from $500 to $5000. The ranges for this price facet are $500-$750, $750-$1000, $1000-$2000, $2000-$3000, and $3000-$4000. <br/>
**Action**: A shopper searches for a laptop in the range _$750-$1000_. <br/>
**Result**: The Search engine returns all laptops that cost $750 to $999.

1. Click **Configuration**>**Store** and select a store.
2. Click the **Facet Configuration** tab.
3. In the **Facet Configuration** table, select an attribute row.
4. In the **Facetable** field for the selected attribute, select **Range Facet**.
   Elastic Path provides default values for the ranges. <br>
5.(Optional) To change the default configurations, click **Edit**.<br/>
     i. In the **Display Name** field, enter the required name.<br/>
    ii. In the **Values** section, enter appropriate ranges for the facet.<br/>
  You can also add appropriate labels for the facet ranges and select a locale from the available locales. For example, for length, you can set inch, cm, or ft as the label.<br/>
6. Click **Save**.<br/>

**Note** After configuring facets in Commerce Manager, the merchandiser must save the changes to enable facets in the storefront. If the merchandiser does not save the changes, storefront returns the search results without facets for a search query.

#### Facet Configuration Settings

The following table lists the settings in the **Configuration** > **Stores** > **[store name]** > **Facet Configuration** tab:

| **Field** | **Description** |
| --- | ---|
|**Facet Name** | Specifies the name of the facet. The name can be a product attribute, a SKU attribute, a SKU option, or one of the `others` attributes. The `others` attributes are brand, length, width, weight, height, price, or category. Enter the facet name in the **Filter** field to get the details of a particular facet name. |
| **Facet Group** | Specifies the source from which the facet is populated, such as product attributes, SKU attributes, SKU options, or `others`. The `others` facet group represents brand, length, width, weight, height, price, or category facets. Use the drop-down menu at the  top-right in the **Facet Configuration** tab to view all facets in a specific facet group. |
| **Type** | Specifies the facet value type, such as string, integer, boolean, or decimal. |
| **Searchable** | Specifies whether the facet value is searchable. The default setting for this parameter is `true`. For example, if screen size is searchable and the shopper searches for _13inch laptop_, the search server returns all laptops with 13inch screen size. |
| **Facetable** | Specifies whether the facet is facetable. Depending on the facet value type, you can select facet or range facet option. With **range facet**, you can define ranges as facet values.|
| **Display Name** | Specifies how the facet name appears in the Cortex API search results. <br>For example, you can set Brand as the display name for a facet named `brand_name`. <br> |
