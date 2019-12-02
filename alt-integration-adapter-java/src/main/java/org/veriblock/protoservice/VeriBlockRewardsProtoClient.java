// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import integration.api.grpc.RewardsServiceGrpc;
import integration.api.grpc.RewardsServiceGrpc.RewardsServiceBlockingStub;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.EmptyRequest;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import io.grpc.Channel;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.protoconverters.CalculatorConfigProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.Pair;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;
import org.veriblock.sdk.rewards.PopRewardOutput;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class VeriBlockRewardsProtoClient implements IVeriBlockRewards {    
    private final RewardsServiceBlockingStub service;
    
    public VeriBlockRewardsProtoClient(Channel channel) {
        service = RewardsServiceGrpc.newBlockingStub(channel);
    }
    
    @Override
    public ValidationResult resetRewards() {
        GeneralReply reply = service.resetRewards(EmptyRequest.newBuilder().build());
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    @Override
    public Pair<ValidationResult, PopRewardCalculatorConfig> getCalculator() {
        VeriBlockMessages.GetCalculatorReply reply = service.getCalculator(EmptyRequest.newBuilder().build());
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, null);
        
        PopRewardCalculatorConfig config = CalculatorConfigProtoConverter.fromProto(reply.getCalculator());
        return new Pair<>(resultValid, config);
    }
    
    @Override
    public Pair<ValidationResult, BigDecimal> rewardsCalculateScore(AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks) {
        VeriBlockMessages.AltChainBlock endorsedBlockProto = AltChainBlockProtoConverter.toProto(endorsedBlock);
        List<VeriBlockMessages.AltChainBlock> endorsementBlocksProto = AltChainBlockProtoConverter.toProto(endorsementBlocks);

        VeriBlockMessages.RewardsCalculateScoreRequest request = VeriBlockMessages.RewardsCalculateScoreRequest.newBuilder()
                .setEndorsedBlock(endorsedBlockProto)
                .addAllEndorsmentBlocks(endorsementBlocksProto)
                .build();

        VeriBlockMessages.RewardsCalculateScoreReply reply = service.rewardsCalculateScore(request);
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, BigDecimal.ZERO);
        
        BigDecimal score = new BigDecimal(reply.getScore());
        return new Pair<>(resultValid, score);
    }
    
    @Override
    public Pair<ValidationResult, PopPayoutRound> rewardsCalculateOutputs(int blockNumber, AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks, BigDecimal popDifficulty) {
        VeriBlockMessages.AltChainBlock endorsedBlockProto = AltChainBlockProtoConverter.toProto(endorsedBlock);
        List<VeriBlockMessages.AltChainBlock> endorsementBlocksProto = AltChainBlockProtoConverter.toProto(endorsementBlocks);
        VeriBlockMessages.RewardsCalculateOutputsRequest request = VeriBlockMessages.RewardsCalculateOutputsRequest.newBuilder()
                .setBlockAltHeight(blockNumber)
                .setEndorsedBlock(endorsedBlockProto)
                .addAllEndorsmentBlocks(endorsementBlocksProto)
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

    @Override
    public Pair<ValidationResult, BigDecimal> rewardsCalculatePopDifficulty(List<AltChainBlock> blocks) {
        VeriBlockMessages.RewardsCalculatePopDifficultyRequest request = VeriBlockMessages.RewardsCalculatePopDifficultyRequest.newBuilder()
                .addAllBlocks(AltChainBlockProtoConverter.toProto(blocks))
                .build();

        VeriBlockMessages.RewardsCalculateScoreReply reply = service.rewardsCalculatePopDifficulty(request);

        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, BigDecimal.ONE);

        BigDecimal difficulty = new BigDecimal(reply.getScore());
        return new Pair<>(resultValid, difficulty);
    }
}
