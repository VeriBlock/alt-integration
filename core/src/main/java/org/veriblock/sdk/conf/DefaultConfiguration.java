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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

import org.veriblock.sdk.util.Utils;

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
            if(inputStream != null) {
                properties.load(inputStream);
            }
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

    public boolean isValidateVBBlockDifficulty() {
        Boolean validation = Boolean.valueOf(getPropertyOverrideOrDefault("validation.vb.block.difficulty"));
        return validation;
    }

    public boolean isValidateBTCBlockDifficulty() {
        Boolean validation = Boolean.valueOf(getPropertyOverrideOrDefault("validation.btc.block.difficulty"));
        return validation;
    }

    public String getPackageName() {
        return packageName;
    }

    public NetworkParameters getVeriblockNetworkParameters() {
        String minDifficulty = properties.getProperty("veriblock.blockchain.minimumDifficulty");
        String txMagicByte = properties.getProperty("veriblock.blockchain.transactionMagicByte");

        byte[] magicBytes = txMagicByte == null ? new byte[0] : Utils.decodeHex(txMagicByte);

        if (magicBytes.length > 1)
            throw new IllegalArgumentException("veriblock.blockchain.transactionMagicByte must be empty or 2 hex digits");

        Byte magicByte = magicBytes.length == 0 ? null : magicBytes[0];

        if (minDifficulty == null)
            throw new IllegalArgumentException("veriblock.blockchain.minimumDifficulty is required");

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

        if (powLimit == null)
            throw new IllegalArgumentException("bitcoin.blockchain.powLimit is required");

        if (powTargetTimespan == null)
            throw new IllegalArgumentException("bitcoin.blockchain.powTargetTimespan is required");

        if (powTargetSpacing == null)
            throw new IllegalArgumentException("bitcoin.blockchain.powTargetSpacing is required");

        if (allowMinDifficultyBlocks == null)
            throw new IllegalArgumentException("bitcoin.blockchain.allowMinDifficultyBlocks is required");

        if (powNoRetargeting == null)
            throw new IllegalArgumentException("bitcoin.blockchain.powNoRetargeting is required");

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

    private void loadDefaults() {
        try (InputStream stream = DefaultConfiguration.class
                .getClassLoader()
                .getResourceAsStream(defaultPropertiesPath)) {
            defaultProperties.load(stream);
        } catch (IOException e) {
            logger.error("Unable to load default properties", e);
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
