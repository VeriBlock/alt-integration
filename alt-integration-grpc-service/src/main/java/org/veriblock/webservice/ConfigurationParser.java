// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.integrations.blockchain.VeriBlockBlockchainBootstrapConfig;
import org.veriblock.integrations.forkresolution.ForkresolutionConfig;
import org.veriblock.integrations.rewards.PopRewardCalculatorConfig;
import org.veriblock.integrations.rewards.PopRewardCurveConfig;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigurationParser {
    private final Properties properties;

    public ConfigurationParser(Properties properties) {
        this.properties = properties;
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

    public VeriBlockNetworkParameters getVeriblockNetworkParameters() {
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

        return new VeriBlockNetworkParameters() {
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

    public AltChainParametersConfig getAltChainParametersConfig() {
        String keystoneInterval = properties.getProperty("altChain.parameters.keystoneInterval");

        if (keystoneInterval == null) {
            return null;
        }

        AltChainParametersConfig config = new AltChainParametersConfig();
        config.keystoneInterval = Integer.parseInt(keystoneInterval);

        return config;
    }

    public ForkresolutionConfig getForkresolutionConfig() {
        String keystoneFinalityDelay = properties.getProperty("forkResolution.keystoneFinalityDelay");
        String amnestyPeriod = properties.getProperty("forkResolution.amnestyPeriod");

        if (keystoneFinalityDelay == null && amnestyPeriod == null) {
            return null;
        }

        if (keystoneFinalityDelay == null || amnestyPeriod == null) {
            throw new IllegalArgumentException("Must specify either all or none of the Forkresolution config values");
        }

        ForkresolutionConfig config = new ForkresolutionConfig();
        config.keystoneFinalityDelay = Integer.parseInt(keystoneFinalityDelay);
        config.amnestyPeriod = Integer.parseInt(amnestyPeriod);

        return config;
    }

    public PopRewardCurveConfig getPopRewardCurveConfig() {
        String startOfDecreasingLine = properties.getProperty("popReward.curve.startOfDecreasingLine");
        String widthOfDecreasingLineNormal = properties.getProperty("popReward.curve.widthOfDecreasingLineNormal");
        String widthOfDecreasingLineKeystone = properties.getProperty("popReward.curve.widthOfDecreasingLineKeystone");
        String aboveIntendedPayoutMultiplierNormal = properties.getProperty("popReward.curve.aboveIntendedPayoutMultiplierNormal");
        String aboveIntendedPayoutMultiplierKeystone = properties.getProperty("popReward.curve.aboveIntendedPayoutMultiplierKeystone");

        if (startOfDecreasingLine == null && widthOfDecreasingLineNormal == null &&
            widthOfDecreasingLineKeystone == null && aboveIntendedPayoutMultiplierNormal == null &&
            aboveIntendedPayoutMultiplierKeystone == null) {
            return null;
        }

        if (startOfDecreasingLine == null || widthOfDecreasingLineNormal == null ||
            widthOfDecreasingLineKeystone == null || aboveIntendedPayoutMultiplierNormal == null ||
            aboveIntendedPayoutMultiplierKeystone == null) {
            throw new IllegalArgumentException("Must specify either all or none of the PopRewardCurve config values");
        }

        PopRewardCurveConfig config = new PopRewardCurveConfig();
        config.startOfDecreasingLine = new BigDecimal(startOfDecreasingLine);
        config.widthOfDecreasingLineNormal = new BigDecimal(widthOfDecreasingLineNormal);
        config.widthOfDecreasingLineKeystone = new BigDecimal(widthOfDecreasingLineKeystone);
        config.aboveIntendedPayoutMultiplierNormal = new BigDecimal(aboveIntendedPayoutMultiplierNormal);
        config.aboveIntendedPayoutMultiplierKeystone = new BigDecimal(aboveIntendedPayoutMultiplierKeystone);

        return config;
    }

    public PopRewardCalculatorConfig getPopRewardCalculatorConfig() {
        String basicReward = properties.getProperty("popReward.calculator.basicReward");
        String payoutRounds = properties.getProperty("popReward.calculator.payoutRounds");
        String keystoneRound = properties.getProperty("popReward.calculator.keystoneRound");
        String roundRatios = properties.getProperty("popReward.calculator.roundRatios");
        String maxRewardThresholdNormal = properties.getProperty("popReward.calculator.maxRewardThresholdNormal");
        String maxRewardThresholdKeystone = properties.getProperty("popReward.calculator.maxRewardThresholdKeystone");
        String flatScoreRound = properties.getProperty("popReward.calculator.flatScoreRound");
        String flatScoreRoundUse = properties.getProperty("popReward.calculator.flatScoreRoundUse");
        PopRewardCurveConfig curveConfig = getPopRewardCurveConfig();
        String relativeScoreLookupTable = properties.getProperty("popReward.calculator.relativeScoreLookupTable");
        String popDifficultyAveragingInterval = properties.getProperty("popReward.calculator.popDifficultyAveragingInterval");
        String popRewardSettlementInterval = properties.getProperty("popReward.calculator.popRewardSettlementInterval");

        if (basicReward == null && payoutRounds == null &&
            keystoneRound == null && roundRatios == null &&
            maxRewardThresholdNormal == null && maxRewardThresholdKeystone == null &&
            flatScoreRound == null && flatScoreRoundUse == null &&
            curveConfig == null && relativeScoreLookupTable == null &&
            popDifficultyAveragingInterval == null && popRewardSettlementInterval == null) {
            return null;
        }

        if (basicReward == null || payoutRounds == null ||
            keystoneRound == null || roundRatios == null ||
            maxRewardThresholdNormal == null || maxRewardThresholdKeystone == null ||
            flatScoreRound == null || flatScoreRoundUse == null ||
            curveConfig == null || relativeScoreLookupTable == null ||
            popDifficultyAveragingInterval == null || popRewardSettlementInterval == null) {
            throw new IllegalArgumentException("Must specify either all or none of the PopRewardCalculator config values");
        }

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
        for(String item : items) {
            result.add(new BigDecimal(item));
        }

        return result;
    }

    public VeriBlockBlockchainBootstrapConfig getVeriBlockBlockchainBootstrapConfig() {
        String blocks = properties.getProperty("veriblock.blockchain.bootstrap.blocks");

        if (blocks == null) {
            return null;
        }

        return new VeriBlockBlockchainBootstrapConfig(parseVeriBlockBlockList(blocks));
    }

    private static List<VeriBlockBlock> parseVeriBlockBlockList(String str) {
        String[] items = str.split(",", 0);

        List<VeriBlockBlock> result = new ArrayList<VeriBlockBlock>(items.length);
        for(String item : items) {
            result.add(SerializeDeserializeService.parseVeriBlockBlock(Utils.decodeHex(item)));
        }

        return result;
    }

    public BitcoinBlockchainBootstrapConfig getBitcoinBlockchainBootstrapConfig() {
        String blocks = properties.getProperty("bitcoin.blockchain.bootstrap.blocks");
        String firstBlockHeight = properties.getProperty("bitcoin.blockchain.bootstrap.firstBlockHeight");

        if (blocks == null && firstBlockHeight == null) {
            return null;
        }

        if (blocks == null || firstBlockHeight == null) {
            throw new IllegalArgumentException("Must specify either all of none of the BitcoinBlockchainBootstrap config values");
        }

        return new BitcoinBlockchainBootstrapConfig(parseBitcoinBlockList(blocks), Integer.parseInt(firstBlockHeight));
    }

    private static List<BitcoinBlock> parseBitcoinBlockList(String str) {
        String[] items = str.split(",", 0);

        List<BitcoinBlock> result = new ArrayList<BitcoinBlock>(items.length);
        for(String item : items) {
            result.add(SerializeDeserializeService.parseBitcoinBlock(Utils.decodeHex(item)));
        }

        return result;
    }
}
