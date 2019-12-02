// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;
import org.veriblock.sdk.rewards.PopRewardCurveConfig;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public final class CalculatorConfigProtoConverter {

    private CalculatorConfigProtoConverter() {} //never
    
    public static PopRewardCalculatorConfig fromProto(VeriBlockMessages.CalculatorConfig protoData) {
        PopRewardCalculatorConfig config = new PopRewardCalculatorConfig();
        config.basicReward = new BigInteger(protoData.getBasicReward());
        config.keystoneRound = protoData.getKeystoneRound();
        config.payoutRounds = protoData.getPayoutRounds();
        config.roundRatios = new ArrayList<>();
        for(String r : protoData.getRoundRatios().getRoundRatioList()) {
            config.roundRatios.add(new BigDecimal(r));
        }
        config.maxRewardThresholdNormal = new BigDecimal(protoData.getMaxRewardThresholdNormal());
        config.maxRewardThresholdKeystone = new BigDecimal(protoData.getMaxRewardThresholdKeystone());
        config.flatScoreRound = protoData.getFlatScoreRound().getRound();
        config.flatScoreRoundUse = protoData.getFlatScoreRound().getActive();
        
        config.curveConfig = new PopRewardCurveConfig();
        config.curveConfig.startOfDecreasingLine = new BigDecimal(protoData.getRewardCurve().getStartOfDecreasingLine());
        config.curveConfig.widthOfDecreasingLineNormal = new BigDecimal(protoData.getRewardCurve().getWidthOfDecreasingLineNormal());
        config.curveConfig.widthOfDecreasingLineKeystone = new BigDecimal(protoData.getRewardCurve().getWidthOfDecreasingLineKeystone());
        config.curveConfig.aboveIntendedPayoutMultiplierNormal = new BigDecimal(protoData.getRewardCurve().getAboveIntendedPayoutMultiplierNormal());
        config.curveConfig.aboveIntendedPayoutMultiplierKeystone = new BigDecimal(protoData.getRewardCurve().getAboveIntendedPayoutMultiplierKeystone());        
        
        config.relativeScoreLookupTable = new ArrayList<>();
        for(String r : protoData.getRelativeScoreLookupTable().getScoreList()) {
            config.relativeScoreLookupTable.add(new BigDecimal(r));
        }
        config.popDifficultyAveragingInterval = protoData.getPopDifficultyAveragingInterval();
        config.popRewardSettlementInterval = protoData.getPopRewardSettlementInterval();

        return config;
    }
    
    public static VeriBlockMessages.CalculatorConfig toProto(PopRewardCalculatorConfig data) {
        
        VeriBlockMessages.RoundRatioConfig.Builder roundRatiosConfig = VeriBlockMessages.RoundRatioConfig.newBuilder();
        for(BigDecimal r : data.roundRatios) {
            roundRatiosConfig = roundRatiosConfig.addRoundRatio(r.toPlainString());
        }
        
        VeriBlockMessages.FlatScoreRoundConfig.Builder flatScoreConfig = VeriBlockMessages.FlatScoreRoundConfig.newBuilder();
        flatScoreConfig = flatScoreConfig.setActive(data.flatScoreRoundUse);
        flatScoreConfig = flatScoreConfig.setRound(data.flatScoreRound);
        
        VeriBlockMessages.RewardCurveConfig.Builder curveConfig = VeriBlockMessages.RewardCurveConfig.newBuilder();
        curveConfig = curveConfig.setStartOfDecreasingLine(data.curveConfig.startOfDecreasingLine.toPlainString());
        curveConfig = curveConfig.setWidthOfDecreasingLineNormal(data.curveConfig.widthOfDecreasingLineNormal.toPlainString());
        curveConfig = curveConfig.setWidthOfDecreasingLineKeystone(data.curveConfig.widthOfDecreasingLineKeystone.toPlainString());
        curveConfig = curveConfig.setAboveIntendedPayoutMultiplierNormal(data.curveConfig.aboveIntendedPayoutMultiplierNormal.toPlainString());
        curveConfig = curveConfig.setAboveIntendedPayoutMultiplierKeystone(data.curveConfig.aboveIntendedPayoutMultiplierKeystone.toPlainString());
        
        VeriBlockMessages.RelativeScoreConfig.Builder relativeScoreConfig = VeriBlockMessages.RelativeScoreConfig.newBuilder();
        for(BigDecimal r : data.relativeScoreLookupTable) {
            relativeScoreConfig = relativeScoreConfig.addScore(r.toPlainString());
        }
        
        VeriBlockMessages.CalculatorConfig.Builder result = VeriBlockMessages.CalculatorConfig.newBuilder();
        
        result = result
                .setBasicReward(data.basicReward.toString())
                .setKeystoneRound(data.keystoneRound)
                .setPayoutRounds(data.payoutRounds)
                .setRoundRatios(roundRatiosConfig.build())
                .setMaxRewardThresholdNormal(data.maxRewardThresholdNormal.toPlainString())
                .setMaxRewardThresholdKeystone(data.maxRewardThresholdKeystone.toPlainString())
                .setFlatScoreRound(flatScoreConfig.build())
                .setRewardCurve(curveConfig.build())
                .setRelativeScoreLookupTable(relativeScoreConfig.build())
                .setPopDifficultyAveragingInterval(data.popDifficultyAveragingInterval)
                .setPopRewardSettlementInterval(data.popRewardSettlementInterval);
        return result.build();
    }
}
