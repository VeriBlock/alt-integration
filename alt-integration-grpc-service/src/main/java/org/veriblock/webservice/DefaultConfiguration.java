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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.integrations.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.integrations.blockchain.VeriBlockBlockchainBootstrapConfig;
import org.veriblock.integrations.params.AlphaNetParameters;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.params.NetworkParameters;
import org.veriblock.integrations.params.TestNetParameters;
import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.forkresolution.ForkresolutionConfig;
import org.veriblock.integrations.rewards.PopRewardCalculatorConfig;
import org.veriblock.integrations.rewards.PopRewardCurveConfig;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

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

    public NetworkParameters getVeriblockNetworkParameters() {
        String network = getPropertyOverrideOrDefault("veriblockNetwork");
        if (network.equalsIgnoreCase("main")) {
            return new MainNetParameters();
        } else if (network.equalsIgnoreCase("test")) {
            return new TestNetParameters();
        } else if (network.equalsIgnoreCase("alpha")) {
            return new AlphaNetParameters();
        } else  {
            throw new IllegalArgumentException("Unknown Veriblock network name");
        }
    }

    public AltChainParametersConfig getAltChainParametersConfig() {
        String keystoneInterval = properties.getProperty("AltChainParameters.keystoneInterval");
        if (keystoneInterval == null)
            return null;

        AltChainParametersConfig config = new AltChainParametersConfig();
        config.keystoneInterval = Integer.parseInt(keystoneInterval);

        return config;
    }

    public ForkresolutionConfig getForkresolutionConfig() {
        String keystoneFinalityDelay = properties.getProperty("Forkresolution.keystoneFinalityDelay");
        String amnestyPeriod = properties.getProperty("Forkresolution.amnestyPeriod");
        if (keystoneFinalityDelay == null && amnestyPeriod == null)
            return null;

        if (keystoneFinalityDelay == null || amnestyPeriod == null)
            throw new IllegalArgumentException("Must specify either all or none of the Forkresolution config values");

        ForkresolutionConfig config = new ForkresolutionConfig();
        config.keystoneFinalityDelay = Integer.parseInt(keystoneFinalityDelay);
        config.amnestyPeriod = Integer.parseInt(amnestyPeriod);

        return config;
    }

    public PopRewardCurveConfig getPopRewardCurveConfig() {
        String startOfDecreasingLine = properties.getProperty("PopRewardCurveConfig.startOfDecreasingLine");
        String widthOfDecreasingLineNormal = properties.getProperty("PopRewardCurveConfig.widthOfDecreasingLineNormal");
        String widthOfDecreasingLineKeystone = properties.getProperty("PopRewardCurveConfig.widthOfDecreasingLineKeystone");
        String aboveIntendedPayoutMultiplierNormal = properties.getProperty("PopRewardCurveConfig.aboveIntendedPayoutMultiplierNormal");
        String aboveIntendedPayoutMultiplierKeystone = properties.getProperty("PopRewardCurveConfig.aboveIntendedPayoutMultiplierKeystone");
        if (startOfDecreasingLine == null && widthOfDecreasingLineNormal == null &&
            widthOfDecreasingLineKeystone == null && aboveIntendedPayoutMultiplierNormal == null &&
            aboveIntendedPayoutMultiplierKeystone == null)
            return null;

        if (startOfDecreasingLine == null || widthOfDecreasingLineNormal == null ||
            widthOfDecreasingLineKeystone == null || aboveIntendedPayoutMultiplierNormal == null ||
            aboveIntendedPayoutMultiplierKeystone == null)
            throw new IllegalArgumentException("Must specify either all or none of the PopRewardCurve config values");

        PopRewardCurveConfig config = new PopRewardCurveConfig();
        config.startOfDecreasingLine = new BigDecimal(startOfDecreasingLine);
        config.widthOfDecreasingLineNormal = new BigDecimal(widthOfDecreasingLineNormal);
        config.widthOfDecreasingLineKeystone = new BigDecimal(widthOfDecreasingLineKeystone);
        config.aboveIntendedPayoutMultiplierNormal = new BigDecimal(aboveIntendedPayoutMultiplierNormal);
        config.aboveIntendedPayoutMultiplierKeystone = new BigDecimal(aboveIntendedPayoutMultiplierKeystone);

        return config;
    }

    public PopRewardCalculatorConfig getPopRewardCalculatorConfig() {
        String basicReward = properties.getProperty("PopRewardCalculatorConfig.basicReward");
        String payoutRounds = properties.getProperty("PopRewardCalculatorConfig.payoutRounds");
        String keystoneRound = properties.getProperty("PopRewardCalculatorConfig.keystoneRound");
        String roundRatios = properties.getProperty("PopRewardCalculatorConfig.roundRatios");
        String maxRewardThresholdNormal = properties.getProperty("PopRewardCalculatorConfig.maxRewardThresholdNormal");
        String maxRewardThresholdKeystone = properties.getProperty("PopRewardCalculatorConfig.maxRewardThresholdKeystone");
        String flatScoreRound = properties.getProperty("PopRewardCalculatorConfig.flatScoreRound");
        String flatScoreRoundUse = properties.getProperty("PopRewardCalculatorConfig.flatScoreRoundUse");
        PopRewardCurveConfig curveConfig = getPopRewardCurveConfig();
        String relativeScoreLookupTable = properties.getProperty("PopRewardCalculatorConfig.relativeScoreLookupTable");
        String popDifficultyAveragingInterval = properties.getProperty("PopRewardCalculatorConfig.popDifficultyAveragingInterval");
        String popRewardSettlementInterval = properties.getProperty("PopRewardCalculatorConfig.popRewardSettlementInterval");
        if (basicReward == null && payoutRounds == null &&
            keystoneRound == null && roundRatios == null &&
            maxRewardThresholdNormal == null && maxRewardThresholdKeystone == null &&
            flatScoreRound == null && flatScoreRoundUse == null &&
            curveConfig == null && relativeScoreLookupTable == null &&
            popDifficultyAveragingInterval == null && popRewardSettlementInterval == null)
            return null;

        if (basicReward == null || payoutRounds == null ||
            keystoneRound == null || roundRatios == null ||
            maxRewardThresholdNormal == null || maxRewardThresholdKeystone == null ||
            flatScoreRound == null || flatScoreRoundUse == null ||
            curveConfig == null || relativeScoreLookupTable == null ||
            popDifficultyAveragingInterval == null || popRewardSettlementInterval == null)
            throw new IllegalArgumentException("Must specify either all of none of the PopRewardCalculator config values");

        PopRewardCalculatorConfig config = new PopRewardCalculatorConfig();
        config.basicReward = new BigInteger(basicReward);
        config.payoutRounds = Integer.parseInt(payoutRounds);
        config.keystoneRound = Integer.parseInt(keystoneRound);
        config.roundRatios = parseBigDecimalList(roundRatios);
        config.maxRewardThresholdNormal = new BigDecimal(maxRewardThresholdNormal);
        config.maxRewardThresholdKeystone = new BigDecimal(maxRewardThresholdKeystone);
        config.flatScoreRound = Integer.parseInt(flatScoreRound);
        config.flatScoreRoundUse = Boolean.parseBoolean(flatScoreRoundUse);
        config.curveConfig = curveConfig;
        config.relativeScoreLookupTable = parseBigDecimalList(roundRatios);
        config.popDifficultyAveragingInterval = Integer.parseInt(popDifficultyAveragingInterval);
        config.popRewardSettlementInterval = Integer.parseInt(popRewardSettlementInterval);

        return config;
    }

    private static List<BigDecimal> parseBigDecimalList(String str) {
        String[] items = str.split(",", 0);

        List<BigDecimal> result = new ArrayList<BigDecimal>(items.length);
        for(String item : items)
            result.add(new BigDecimal(item));

        return result;
    }

    public VeriBlockBlockchainBootstrapConfig getVeriBlockBlockchainBootstrapConfig() {
        String blocks = properties.getProperty("VeriBlockBlockchainBootstrap.blocks");
        if (blocks == null)
            return null;

        return new VeriBlockBlockchainBootstrapConfig(parseVeriBlockBlockList(blocks));
    }

    private static List<VeriBlockBlock> parseVeriBlockBlockList(String str) {
        String[] items = str.split(",", 0);

        List<VeriBlockBlock> result = new ArrayList<VeriBlockBlock>(items.length);
        for(String item : items)
            result.add(SerializeDeserializeService.parseVeriBlockBlock(Utils.decodeHex(item)));

        return result;
    }

    public BitcoinBlockchainBootstrapConfig getBitcoinBlockchainBootstrapConfig() {
        String blocks = properties.getProperty("BitcoinBlockchainBootstrapConfig.blocks");
        String firstBlockHeight = properties.getProperty("BitcoinBlockchainBootstrapConfig.firstBlockHeight");
        if (blocks == null && firstBlockHeight == null)
            return null;

        if (blocks == null || firstBlockHeight == null)
            throw new IllegalArgumentException("Must specify either all of none of the BitcoinBlockchainBootstrap config values");

        return new BitcoinBlockchainBootstrapConfig(parseBitcoinBlockList(blocks),
                                                    Integer.parseInt(firstBlockHeight));
    }

    private static List<BitcoinBlock> parseBitcoinBlockList(String str) {
        String[] items = str.split(",", 0);

        List<BitcoinBlock> result = new ArrayList<BitcoinBlock>(items.length);
        for(String item : items)
            result.add(SerializeDeserializeService.parseBitcoinBlock(Utils.decodeHex(item)));

        return result;
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
