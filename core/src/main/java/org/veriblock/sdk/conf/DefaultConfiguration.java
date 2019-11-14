// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Singleton
public class DefaultConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConfiguration.class);

    private Properties defaultProperties = new Properties();
    private Properties properties;

    private String packageName;
    private String defaultPropertiesPath;
    private String mainPropertiesPath;

    public DefaultConfiguration(String packageName) {
        this.packageName = packageName;
        this.defaultPropertiesPath = packageName + "-default.properties";
        this.mainPropertiesPath = packageName + ".properties";
        loadDefaults();
        properties = new Properties(defaultProperties);
        load();
    }

    /**
     * to mock tests.
     */
    public DefaultConfiguration(Properties properties) {
        this.properties = properties;
    }

    private void load() {
        try
        {
            try (InputStream stream = ClassLoader.getSystemResourceAsStream(mainPropertiesPath)) {
                load(stream);
            }
        } catch (FileNotFoundException e) {
            logger.info("Unable to load custom properties file: File '{}' does not exist. Using default properties.", mainPropertiesPath);
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
        Integer port = Integer.valueOf(getPropertyOverrideOrDefault("app.api.port"));
        return port;
    }

    public String getApiHost() {
        String host = getPropertyOverrideOrDefault("app.api.host");
        return host;
    }

    public boolean isVBBlockDifficultyValidate() {
        Boolean validation = Boolean.valueOf(getPropertyOverrideOrDefault("block.difficulty.validation"));
        return validation;
    }

    public String getPackageName() {
        return packageName;
    }

    public NetworkParameters getVeriblockNetworkParameters() {
        String network = getPropertyOverrideOrDefault("veriblockNetwork");
        if ( "main".equals(network)) {
            return new MainNetParameters();
        } else if ("test".equals(network)) {
            return new TestNetParameters();
        } else if ("alpha".equals(network)) {
            return new TestNetParameters();
        } else  {
            throw new IllegalArgumentException("Unknown Veriblock network name");
        }
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
                    .getResourceAsStream(defaultPropertiesPath);
            try {
                defaultProperties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("Unable to load default properties", e);
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
