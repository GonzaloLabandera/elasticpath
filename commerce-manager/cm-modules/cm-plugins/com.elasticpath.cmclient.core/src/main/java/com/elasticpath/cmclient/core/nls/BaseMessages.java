/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.nls;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.commons.util.extenum.ExtensibleEnum;
import com.elasticpath.cmclient.core.util.InitializationGuard;

import org.apache.log4j.Logger;



/**
 * A Messages class with commonly used extensions for NLS messages.
 */
public class BaseMessages {


    /**
     * LOG logger.
     */
    private static final Logger LOG = Logger.getLogger(BaseMessages.class);


    private final InitializationGuard enumGuard = new InitializationGuard();

    // Map of enum constants to localized names
    protected final Map<Enum<?>, String> localizedEnums = new HashMap<>();

    // Map of enum constants to localized names
    protected final Map<ExtensibleEnum, String> localizedExtensibleEnums = new HashMap<>();

    /**
     * Returns the localized name of the given enum constant.
     *
     * @param enumValue the enum to be localized
     * @return the localized string for the enum
     */
    public String getLocalizedName(final Enum<?> enumValue) {
        enumGuard.await();
        String val = localizedEnums.get(enumValue);
        if (val == null) {
            LOG.warn(String.format("Localized enum value was returned as null for enum %s", enumValue.toString()));
        }
        return val;
    }

    /**
     * Returns the localized name of the given ExtensibleEnum constant.
     *
     * @param enumValue the enum to be localized
     * @return the localized string for the enum
     */
    public String getLocalizedName(final ExtensibleEnum enumValue) {
        enumGuard.await();
        String val =  localizedExtensibleEnums.get(enumValue);
        if (val == null) {
            LOG.warn(String.format("Localized extensible enum value was returned as null for %s", enumValue.toString()));
        }
        return val;
    }


    /**
     * Add a localized valur for a specific enum value.
     * @param enumValue the value to provide a localized translation for.
     * @param localizedName the localized value.
     */
    protected void putLocalizedName(final Enum<?> enumValue, final String localizedName) {
        localizedEnums.put(enumValue, localizedName);
    }

    /**
     * Add a localized valur for a specific enum value.
     * @param extensibleEnumValue the value to provide a localized translation for.
     * @param localizedName the localized value.
     */
    protected void putLocalizedName(final ExtensibleEnum extensibleEnumValue, final String localizedName) {
        localizedExtensibleEnums.put(extensibleEnumValue, localizedName);
    }

    /**
     * Call to initialize this messages instance - to be called once the localized values have been populated
     * on the instance.
     */
    public void initialize() {
        enumGuard.initialize(() -> { instantiateEnums(); });
    }

    /**
     * Override to initialize the localized values for the enumerations.
     */
    protected void instantiateEnums() {
        // implement to initialize enumerations with lcoalized values.
    }


}