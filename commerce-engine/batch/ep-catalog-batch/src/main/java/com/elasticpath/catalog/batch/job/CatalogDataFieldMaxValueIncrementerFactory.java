/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch.job;

import static org.springframework.batch.support.DatabaseType.MYSQL;

import java.util.Locale;
import javax.sql.DataSource;

import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

/**
 * Extends {@link DefaultDataFieldMaxValueIncrementerFactory} for overriding MySql {@link DataFieldMaxValueIncrementer}.
 */
public class CatalogDataFieldMaxValueIncrementerFactory extends DefaultDataFieldMaxValueIncrementerFactory {

	/**
	 * Constructor.
	 *
	 * @param dataSource dataSource.
	 */
	public CatalogDataFieldMaxValueIncrementerFactory(final DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public DataFieldMaxValueIncrementer getIncrementer(final String incrementerType, final String incrementerName) {
		final DataFieldMaxValueIncrementer incrementer = super.getIncrementer(incrementerType, incrementerName);
		if (MYSQL == DatabaseType.valueOf(incrementerType.toUpperCase(Locale.getDefault()))) {
			((MySQLMaxValueIncrementer) incrementer).setUseNewConnection(false);
		}

		return incrementer;
	}

}
