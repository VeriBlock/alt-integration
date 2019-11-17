// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.rewards.PopPayoutRound;
import org.veriblock.integrations.rewards.PopRewardCalculator;
import org.veriblock.integrations.rewards.PopRewardCalculatorConfig;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.protoconverters.CalculatorConfigProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.ValidationResult;

import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;

public class VeriBlockRewardsProtoService {
    private static final Logger log = LoggerFactory.getLogger(VeriBlockSecurityProtoService.class);

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
    
    public static VeriBlockMessages.RewardsCalculateScoreReply rewardsCalculateScore(VeriBlockMessages.RewardsCalculateScoreRequest request) {
        AltChainBlock endorsedBlock = AltChainBlockProtoConverter.fromProto(request.getEndorsedBlock());
        List<AltChainBlock> endorsementBlocks = AltChainBlockProtoConverter.fromProto(request.getEndorsmentBlocksList());
        ValidationResult result = null;
        BigDecimal score = BigDecimal.ZERO;

        try{
            score = PopRewardCalculator.calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
            result = ValidationResult.success();

        }
        catch (SQLException e) {
            result = ValidationResult.fail(e.getMessage());
            log.debug("Could not call RewardsCalculate service", e);
        }

        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.RewardsCalculateScoreReply reply = VeriBlockMessages.RewardsCalculateScoreReply.newBuilder()
                .setResult(replyResult)
                .setScore(score.toPlainString())
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.RewardsCalculateOutputsReply rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateOutputsRequest request) {
        AltChainBlock endorsedBlock = AltChainBlockProtoConverter.fromProto(request.getEndorsedBlock());
        List<AltChainBlock> endorsementBlocks = AltChainBlockProtoConverter.fromProto(request.getEndorsmentBlocksList());
        ValidationResult result = null;

        List<VeriBlockMessages.RewardOutput> outputsProto = null;
        PopPayoutRound payout = null;

        try{
            payout = PopRewardCalculator.calculatePopPayoutRound(request.getBlockAltHeight(),
                    endorsedBlock, endorsementBlocks,
                    new BigDecimal(request.getDifficulty()));
            outputsProto = RewardOutputProtoConverter.toProto(payout.getOutputsToPopMiners());
            result = ValidationResult.success();
        }
        catch (SQLException e) {
            result = ValidationResult.fail(e.getMessage());
            log.debug("Could not call RewardsCalculate service", e);
        }

        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.RewardsCalculateOutputsReply reply = VeriBlockMessages.RewardsCalculateOutputsReply.newBuilder()
                .setResult(replyResult)
                .addAllOutputs(outputsProto)
                .setBlockReward(Long.toString(payout.getPopBlockReward()))
                .setTotalReward(Long.toString(payout.getTotalRewardPaidOut()))
                .build();
        return reply;
    }

    public static VeriBlockMessages.RewardsCalculateScoreReply rewardsCalculatePopDifficulty(VeriBlockMessages.RewardsCalculatePopDifficultyRequest request) {
        List<AltChainBlock> blocks = AltChainBlockProtoConverter.fromProto(request.getBlocksList());
        ValidationResult result = null;
        BigDecimal difficulty = BigDecimal.ONE;

        try{
            difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(blocks);
            result = ValidationResult.success();
        } catch (SQLException e) {
            result = ValidationResult.fail(e.getMessage());
            log.debug("Could not call RewardsCalculate service", e);
        }

        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.RewardsCalculateScoreReply reply = VeriBlockMessages.RewardsCalculateScoreReply.newBuilder()
                .setResult(replyResult)
                .setScore(difficulty.toPlainString())
                .build();
        return reply;
    }
}
