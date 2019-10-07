// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.veriblock.integrations.rewards.PopPayoutRound;
import org.veriblock.integrations.rewards.PopRewardCalculatorConfig;
import org.veriblock.integrations.rewards.PopRewardEndorsements;
import org.veriblock.integrations.rewards.PopRewardOutput;
import org.veriblock.protoconverters.CalculatorConfigProtoConverter;
import org.veriblock.protoconverters.RewardEndorsementProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.Pair;
import org.veriblock.sdk.ValidationResult;

import integration.api.grpc.RewardsServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.EmptyRequest;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import integration.api.grpc.RewardsServiceGrpc.RewardsServiceBlockingStub;
import io.grpc.Channel;

public class VeriBlockRewardsProtoClient {    
    private final RewardsServiceBlockingStub service;
    
    public VeriBlockRewardsProtoClient(Channel channel) {
        service = RewardsServiceGrpc.newBlockingStub(channel);
    }
    
    public ValidationResult resetRewards() {
        GeneralReply reply = service.resetRewards(EmptyRequest.newBuilder().build());
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public Pair<ValidationResult, PopRewardCalculatorConfig> getCalculator() {
        VeriBlockMessages.GetCalculatorReply reply = service.getCalculator(EmptyRequest.newBuilder().build());
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, null);
        
        PopRewardCalculatorConfig config = CalculatorConfigProtoConverter.fromProto(reply.getCalculator());
        return new Pair<>(resultValid, config);
    }
    
    public ValidationResult setCalculator(PopRewardCalculatorConfig config) {
        VeriBlockMessages.SetCalculatorRequest request = VeriBlockMessages.SetCalculatorRequest.newBuilder()
                .setCalculator(CalculatorConfigProtoConverter.toProto(config))
                .build();
        GeneralReply reply = service.setCalculator(request);
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public Pair<ValidationResult, BigDecimal> rewardsCalculateScore(PopRewardEndorsements endorsements) {
        List<VeriBlockMessages.RewardEndorsement> endorsementsProto = RewardEndorsementProtoConverter.toProto(endorsements);
        VeriBlockMessages.RewardsCalculateScoreRequest request = VeriBlockMessages.RewardsCalculateScoreRequest.newBuilder()
                .addAllEndorsementsForBlock(endorsementsProto)
                .build();
        
        VeriBlockMessages.RewardsCalculateScoreReply reply = service.rewardsCalculateScore(request);
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, BigDecimal.ZERO);
        
        BigDecimal score = new BigDecimal(reply.getScore());
        return new Pair<>(resultValid, score);
    }
    
    public Pair<ValidationResult, PopPayoutRound> rewardsCalculateOutputs(int blockNumber, PopRewardEndorsements endorsements, BigDecimal popDifficulty) {
        List<VeriBlockMessages.RewardEndorsement> endorsementsProto = RewardEndorsementProtoConverter.toProto(endorsements);        
        VeriBlockMessages.RewardsCalculateOutputsRequest request = VeriBlockMessages.RewardsCalculateOutputsRequest.newBuilder()
                .setBlockAltHeight(blockNumber)
                .addAllEndorsementsForBlock(endorsementsProto)
                .setDifficulty(popDifficulty.toPlainString())
                .build();
        VeriBlockMessages.RewardsCalculateOutputsReply reply = service.rewardsCalculateOutputs(request);
        
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, new PopPayoutRound(0, 0, Collections.emptyList()));
        
        List<PopRewardOutput> outputsToPopMiners = RewardOutputProtoConverter.fromProto(reply.getOutputsList());
        
        PopPayoutRound payout = new PopPayoutRound(Long.parseUnsignedLong(reply.getTotalReward()),
                Long.parseUnsignedLong(reply.getBlockReward()),
                outputsToPopMiners);
        return new Pair<>(resultValid, payout);
    }
}
