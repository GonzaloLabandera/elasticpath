/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.dto;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Data;

import com.elasticpath.xpf.exception.InvalidConfigurationException;

/**
 * Represents a XPF plugin/extension setting value.
 */
@Data
@SuppressWarnings({"PMD.ImmutableField", "PMD.UnusedPrivateField"})
public class PluginSettingValueDTO {
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private final int sequence;
	private final String mapKey;
	private String shortTextValue;
	private Integer integerValue;
	private BigDecimal decimalValue;
	private Boolean booleanValue;
	private Date dateValue;

	/**
	 * Constructor.
	 *
	 * @param sequence       the sequence
	 * @param mapKey         the map key
	 * @param shortTextValue the short text value
	 * @param integerValue   the integer value
	 * @param decimalValue   the decimal value
	 * @param booleanValue   the boolean value
	 * @param dateValue      the data value
	 */
	public PluginSettingValueDTO(final int sequence, final String mapKey, final String shortTextValue, final Integer integerValue,
								 final BigDecimal decimalValue, final Boolean booleanValue, final Date dateValue) {
		this.sequence = sequence;
		this.mapKey = mapKey;
		this.shortTextValue = shortTextValue;
		this.integerValue = integerValue;
		this.decimalValue = decimalValue;
		this.booleanValue = booleanValue;
		this.dateValue = dateValue;
	}

	/**
	 * Constructor.
	 *
	 * @param sequence the sequence
	 * @param mapKey   the map key
	 * @param value    the value
	 * @param dataType the data type of the value
	 */
	public PluginSettingValueDTO(final int sequence, final String mapKey, final Object value, final SettingDataTypeDTO dataType) {
		this.sequence = sequence;
		this.mapKey = mapKey;
		try {
			switch (dataType) {
				case BOOLEAN:
					booleanValue = (Boolean) value;
					break;
				case DATE:
					dateValue = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(value.toString());
					break;
				case DECIMAL:
					decimalValue = BigDecimal.valueOf((Double) value);
					break;
				case INTEGER:
					integerValue = Integer.valueOf(value.toString());
					break;
				case SHORT_TEXT:
					shortTextValue = (String) value;
					break;
				default:
					throw new InvalidConfigurationException("Unrecognized setting data type: " + dataType);

			}
		} catch (Exception e) {
			throw new InvalidConfigurationException("Unable to parse setting value [" + value + "]. Expected data type: " + dataType, e);
		}
	}

	/**
	 * Get Value.
	 *
	 * @param dataType the data type
	 * @return the value
	 */
	public Object getValue(final SettingDataTypeDTO dataType) {
		switch (dataType) {
			case BOOLEAN:
				return getBooleanValue();
			case DATE:
				return getDateValue();
			case DECIMAL:
				return getDecimalValue();
			case INTEGER:
				return getIntegerValue();
			case SHORT_TEXT:
				return getShortTextValue();
			default:
				throw new InvalidConfigurationException("Unrecognized setting data type: " + dataType);
		}
	}
}
