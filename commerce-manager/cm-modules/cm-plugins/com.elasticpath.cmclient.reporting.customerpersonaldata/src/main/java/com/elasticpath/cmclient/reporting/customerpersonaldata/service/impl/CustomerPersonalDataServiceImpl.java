/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.reporting.customerpersonaldata.service.impl;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.extenders.PluginHelper;
import com.elasticpath.cmclient.reporting.ReportTypeManager;
import com.elasticpath.cmclient.reporting.ReportingPlugin;
import com.elasticpath.cmclient.reporting.customerpersonaldata.CustomerPersonalDataReportPlugin;
import com.elasticpath.cmclient.reporting.customerpersonaldata.CustomerPersonalDataReportSection;
import com.elasticpath.cmclient.reporting.customerpersonaldata.parameters.CustomerPersonalDataParameters;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.datapolicy.CustomerPersonalDataReportingService;

/**
* The service provides data required for the "Customer Personal Data" report.
*/
public class CustomerPersonalDataServiceImpl {

	private static final int CREATED_INDEX = 3;
	private static final int LAST_MODIFED_INDEX = 4;

	private CustomerPersonalDataParameters parameters;
	private static final String ATTR_NAME = "name";
	private static final String ATTR_TYPE_REPORT = "reports";

	/**
	 * The report rows are populated from provided data on the following way.
	 *
	 * row["Customer Full Name"] = data[0];
	 * row["Data Point Name"] = data[1];
	 * row["Data Point Value"] = data[2];
	 * row["Created"] = data[3];
	 * row["Last Updated"] = data[4];
	 *
	 * @return the list of rows.
	 */
	public Collection<Object[]> getData() {

		final CustomerPersonalDataParameters params = getParameters();
		final DateFormat dateFormat =  params.getDateFormat();

		Collection<Object[]> data = getCustomerPersonalDataReportingService().getData(params.getStore(), params.getUserId());
		data.forEach(datum ->  {
			datum[CREATED_INDEX] = (datum[CREATED_INDEX] == null ? "" : dateFormat.format((Date) datum[CREATED_INDEX]));
			datum[LAST_MODIFED_INDEX] = (datum[LAST_MODIFED_INDEX] == null ? "" : dateFormat.format((Date) datum[LAST_MODIFED_INDEX]));
		});
		return data;

	}

	private CustomerPersonalDataReportingService getCustomerPersonalDataReportingService() {
		return BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_PERSONAL_DATA_REPORTING_SERVICE, CustomerPersonalDataReportingService.class);
	}


	private CustomerPersonalDataParameters getParameters() {
		if (parameters == null) {
			loadParameters();
		}
		return parameters;
	}

	private void loadParameters() {
		Map<String, Object> params = getReportParameters();
		parameters = (CustomerPersonalDataParameters) params.get(CustomerPersonalDataReportSection.PARAMETER_PARAMETERS);
	}

	private Map<String, Object> getReportParameters() {

		return ReportTypeManager.getInstance().getReportTypes()
				.stream()
				.filter(reportType -> reportType.getName().equalsIgnoreCase(
					PluginHelper.getPluginAttribute(ReportingPlugin.PLUGIN_ID,
						CustomerPersonalDataReportPlugin.PLUGIN_ID, ATTR_TYPE_REPORT, ATTR_NAME)))
				.findFirst()
				.get()
				.getReport()
				.getParameters();
	}


}
