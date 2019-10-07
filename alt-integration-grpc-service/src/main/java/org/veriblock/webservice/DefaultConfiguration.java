// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConfiguration.class);
    
    private static final String packageName = Application.packageName;
    
    private static final String DEFAULT_PROPERTIES = packageName + "-default.properties";
    private static final String MAIN_PROPERTIES = packageName + ".properties";

    private final Properties defaultProperties = new Properties();
    private final Properties properties;

    public DefaultConfiguration() {
        loadDefaults();
        properties = new Properties(defaultProperties);
        load();
    }

    private void load() {
        try
        {
            try (InputStream stream = ClassLoader.getSystemResourceAsStream(MAIN_PROPERTIES)) {
                load(stream);
            }
        } catch (FileNotFoundException e) {
            logger.info("Unable to load custom properties file: File '{}' does not exist. Using default properties.", MAIN_PROPERTIES);
        } catch (IOException e) {
            logger.info("Unable to load custom properties file. Using default properties.", e);
        }
    }

    private void load(InputStream inputStream) {
        try {
            properties.load(inputStream);            
        } catch (Exception e) {
            logger.error("Unhandled exception in DefaultConfiguration.load", e);
        }
    }

    public int getApiPort() {
        Integer port = Integer.valueOf(getPropertyOverrideOrDefault("apiPort"));
        return port;
    }
    
    private String getPropertyOverrideOrDefault(final String name) {
        String value = properties.getProperty(name);
        if (value == null)
            return "";
        return value;
    }
    
    private void loadDefaults() {
        try
        {
            InputStream stream = DefaultConfiguration.class
                    .getClassLoader()
                    .getResourceAsStream(DEFAULT_PROPERTIES);
            try {
                defaultProperties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("Unable to load default properties", e);
        }
    }
}
