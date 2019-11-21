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
import org.veriblock.sdk.exceptions.AltConfigurationException;
import org.veriblock.sdk.util.FileUtils;
import org.veriblock.sdk.util.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

public class AppConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    private Properties defaultProperties;
    private Properties properties;

    private String defaultPropertiesFileName;
    private String defaultPropertiesPath;
    private String mainPropertiesPath;

    public AppConfiguration(String defaultPropertiesFileName) {
        this.defaultPropertiesFileName = defaultPropertiesFileName;
        this.defaultPropertiesPath = defaultPropertiesFileName + ".properties";
        this.mainPropertiesPath = System.getenv("ALT_INT_PROPERTIES_FILE_PATH");
        this.defaultProperties = loadDefaults();
        this.properties = new Properties(defaultProperties);
        if(this.mainPropertiesPath != null) {
            loadFromFile();
        }
    }

    /**
     * to mock tests.
     */
    public AppConfiguration(Properties properties) {
        this.properties = properties;
    }

    private void loadFromFile() {
        try {
            try (InputStream stream = new FileInputStream(mainPropertiesPath)) {
                loadFromFile(stream);
            }
        } catch (FileNotFoundException e) {
            throw new AltConfigurationException(String.format("Unable to load custom properties file: File '%1$s' does not exist. Using default properties.", mainPropertiesPath));
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
            throw new AltConfigurationException(String.format("Unhandled exception in DefaultConfiguration.load", e));
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

    public boolean isValidateVBBlockDifficulty() {
        Boolean validation = Boolean.valueOf(getPropertyOverrideOrDefault("validation.vb.block.difficulty"));
        return validation;
    }

    public boolean isValidateBTCBlockDifficulty() {
        Boolean validation = Boolean.valueOf(getPropertyOverrideOrDefault("validation.btc.block.difficulty"));
        return validation;
    }

    public String getDefaultPropertiesFileName() {
        return defaultPropertiesFileName;
    }

    public NetworkParameters getVeriblockNetworkParameters() {
        String minDifficulty = properties.getProperty("veriblock.blockchain.minimumDifficulty");
        String txMagicByte = properties.getProperty("veriblock.blockchain.transactionMagicByte");

        byte[] magicBytes = txMagicByte == null ? new byte[0] : Utils.decodeHex(txMagicByte);

        if (magicBytes.length > 1) {
            throw new AltConfigurationException("veriblock.blockchain.transactionMagicByte must be empty or 2 hex digits");
        }

        Byte magicByte = magicBytes.length == 0 ? null : magicBytes[0];

        if (minDifficulty == null) {
            throw new AltConfigurationException("veriblock.blockchain.minimumDifficulty is required");
        }

        return new NetworkParameters() {
                    public BigInteger getMinimumDifficulty() {
                        return new BigInteger(minDifficulty);
                    }
                    public Byte getTransactionMagicByte() {
                        return magicByte;
                    }
                };
    }

    public BitcoinNetworkParameters getBitcoinNetworkParameters() {
        String powLimit = properties.getProperty("bitcoin.blockchain.powLimit");
        String powTargetTimespan = properties.getProperty("bitcoin.blockchain.powTargetTimespan");
        String powTargetSpacing = properties.getProperty("bitcoin.blockchain.powTargetSpacing");
        String allowMinDifficultyBlocks = properties.getProperty("bitcoin.blockchain.allowMinDifficultyBlocks");
        String powNoRetargeting = properties.getProperty("bitcoin.blockchain.powNoRetargeting");

        if (powLimit == null){
            throw new AltConfigurationException("bitcoin.blockchain.powLimit is required");
        }

        if (powTargetTimespan == null){
            throw new AltConfigurationException("bitcoin.blockchain.powTargetTimespan is required");
        }

        if (powTargetSpacing == null){
            throw new AltConfigurationException("bitcoin.blockchain.powTargetSpacing is required");
        }

        if (allowMinDifficultyBlocks == null){
            throw new AltConfigurationException("bitcoin.blockchain.allowMinDifficultyBlocks is required");
        }

        if (powNoRetargeting == null){
            throw new AltConfigurationException("bitcoin.blockchain.powNoRetargeting is required");
        }

        return new BitcoinNetworkParameters() {
                    public BigInteger getPowLimit() {
                        return new BigInteger(powLimit, 16);
                    }
                    public int getPowTargetTimespan() {
                        return Integer.parseInt(powTargetTimespan);
                    }
                    public int getPowTargetSpacing() {
                        return Integer.parseInt(powTargetSpacing);
                    }
                    public boolean getAllowMinDifficultyBlocks() {
                        return Boolean.parseBoolean(allowMinDifficultyBlocks);
                    }
                    public boolean getPowNoRetargeting() {
                        return Boolean.parseBoolean(powNoRetargeting);
                    }
                };
    }

    private String getPropertyOverrideOrDefault(final String name) {
        String value = properties.getProperty(name);
        if (value == null)
            return "";
        return value;
    }

    private Properties loadDefaults() {
        Properties properties = FileUtils.loadProperty(defaultPropertiesPath);

        if(properties == null){
            throw new AltConfigurationException("Default property not initialised.");
        }

        return properties;
    }

    public Properties getProperties() {
        return properties;
    }
}
