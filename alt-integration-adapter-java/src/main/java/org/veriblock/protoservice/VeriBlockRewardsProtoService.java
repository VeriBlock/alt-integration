// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.math.BigDecimal;
import java.util.List;

import org.veriblock.integrations.rewards.PopPayoutRound;
import org.veriblock.integrations.rewards.PopRewardCalculator;
import org.veriblock.integrations.rewards.PopRewardCalculatorConfig;
import org.veriblock.integrations.rewards.PopRewardEndorsements;
import org.veriblock.protoconverters.CalculatorConfigProtoConverter;
import org.veriblock.protoconverters.RewardEndorsementProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.ValidationResult;

import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;

public class VeriBlockRewardsProtoService { 
    private VeriBlockRewardsProtoService() { }
    
    public static GeneralReply resetRewards() {
        PopRewardCalculator.setCalculatorConfig(new PopRewardCalculatorConfig());
        ValidationResult result = ValidationResult.success();
        return VeriBlockServiceCommon.validationResultToProto(result);
    }
    
    public static VeriBlockMessages.GetCalculatorReply getCalculator() {
        VeriBlockMessages.CalculatorConfig config = CalculatorConfigProtoConverter.toProto(PopRewardCalculator.getCalculatorConfig());
        
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.GetCalculatorReply reply = VeriBlockMessages.GetCalculatorReply.newBuilder()
                .setCalculator(config)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static GeneralReply setCalculator(VeriBlockMessages.CalculatorConfig protoConfig) {
        PopRewardCalculatorConfig config = CalculatorConfigProtoConverter.fromProto(protoConfig);
        PopRewardCalculator.setCalculatorConfig(config);
        ValidationResult result = ValidationResult.success();
        return VeriBlockServiceCommon.validationResultToProto(result);
    }

    public static VeriBlockMessages.RewardsCalculateScoreReply rewardsCalculateScore(VeriBlockMessages.RewardsCalculateScoreRequest request) {
        PopRewardEndorsements endorsements = RewardEndorsementProtoConverter.fromProto(request.getEndorsementsForBlockList());
        BigDecimal score = PopRewardCalculator.calculatePopScoreFromEndorsements(endorsements);
        
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.RewardsCalculateScoreReply reply = VeriBlockMessages.RewardsCalculateScoreReply.newBuilder()
                .setResult(replyResult)
                .setScore(score.toPlainString())
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.RewardsCalculateOutputsReply rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateOutputsRequest request) {
        PopRewardEndorsements endorsements = RewardEndorsementProtoConverter.fromProto(request.getEndorsementsForBlockList());        
        PopPayoutRound payout = PopRewardCalculator.calculatePopPayoutRound(request.getBlockAltHeight(),
                endorsements,
                new BigDecimal(request.getDifficulty()));
        List<VeriBlockMessages.RewardOutput> outputsProto = RewardOutputProtoConverter.toProto(payout.getOutputsToPopMiners());
        
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.RewardsCalculateOutputsReply reply = VeriBlockMessages.RewardsCalculateOutputsReply.newBuilder()
                .setResult(replyResult)
                .addAllOutputs(outputsProto)
                .setBlockReward(Long.toString(payout.getPopBlockReward()))
                .setTotalReward(Long.toString(payout.getTotalRewardPaidOut()))
                .build();
        return reply;
    }
}
