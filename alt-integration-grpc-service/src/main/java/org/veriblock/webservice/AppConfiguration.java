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

public class AppConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
	
    private static final String packageName = Application.packageName;
    
    private static final String DEFAULT_PROPERTIES = packageName + "-default.properties";
    private static final String MAIN_PROPERTIES = packageName + ".properties";
    
    private String mainPropertiesFileName = MAIN_PROPERTIES;

    private final Properties defaultProperties = new Properties();
    private final Properties properties;
    
    public AppConfiguration() {
        loadDefaults();
        properties = new Properties(defaultProperties);
        String overridePropertiesPath = System.getenv("ALT_INT_PROPERTIES_FILE_PATH");
        if (overridePropertiesPath != null) {
        	mainPropertiesFileName = overridePropertiesPath;
        }
        
        loadFromFile(mainPropertiesFileName);
    }
    
    public AppConfiguration(String mainPropertiesFileName) {
        properties = new Properties(defaultProperties);
        loadFromFile(mainPropertiesFileName);
    }

    // to mock tests.
    public AppConfiguration(Properties properties) {
        this.properties = properties;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public String getPropertiesPath() {
        return mainPropertiesFileName;
    }

    private void loadFromFile(String path) {
        try {
        	try (InputStream stream = ClassLoader.getSystemResourceAsStream(path)) {
        		loadFromFile(stream);
            }
        } catch (FileNotFoundException e) {
            throw new AltConfigurationException(String.format("Unable to load custom properties file: File '{}' does not exist. Using default properties.", path));
        } catch (IOException e) {
            throw new AltConfigurationException(String.format("Unable to load custom properties file. Using default properties.", e));
        }
    }

    private void loadFromFile(InputStream inputStream) {
        try {
            if(inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            throw new AltConfigurationException(String.format("Unhandled exception in AppConfiguration.loadFromFile", e));
        }
    }

    private void loadDefaults() {
        try
        {
            InputStream stream = AppConfiguration.class
                    .getClassLoader()
                    .getResourceAsStream(DEFAULT_PROPERTIES);
            try {
                defaultProperties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException | NullPointerException e) {
            logger.error("Unable to load default properties", e);
        }
    }
}
