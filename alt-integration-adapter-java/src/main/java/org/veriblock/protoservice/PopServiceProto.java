package org.veriblock.protoservice;

import integration.api.grpc.VeriBlockMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.rewards.PopRewardCalculator;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;


public class PopServiceProto {
    private static final Logger log = LoggerFactory.getLogger(VeriBlockForkresolutionProtoService.class);
    private static VeriBlockSecurity security = null;

    private PopServiceProto() {}

    public static void setVeriBlockSecurity(VeriBlockSecurity security) {
        PopServiceProto.security = security;
    }

    static public VeriBlockMessages.CheckReply checkATVInternally(VeriBlockMessages.BytesArrayRequest request) throws Exception
    {
        AltPublication publication = SerializeDeserializeService.parseAltPublication(request.getData().toByteArray());
        ValidationResult result = security.checkATVInternally(publication);
        return VeriBlockServiceCommon.validationResultToCheckReplyProto(result);
    }

    static public VeriBlockMessages.CheckReply checkVTBInternally(VeriBlockMessages.BytesArrayRequest request) throws Exception
    {
        VeriBlockPublication publication = SerializeDeserializeService.parseVeriBlockPublication(request.getData().toByteArray());
        ValidationResult result = security.checkVTBInternally(publication);
        return VeriBlockServiceCommon.validationResultToCheckReplyProto(result);
    }

    static public VeriBlockMessages.RewardsOutputsReply rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateRequest request) throws Exception
    {
        AltChainBlock endorsedBlock = AltChainBlockProtoConverter.fromProto(request.getEndorsedBlock());
        List<AltChainBlock> endorsmentBlocks = AltChainBlockProtoConverter.fromProto(request.getEndorsmentBlocksList());
        List<AltChainBlock> difficultyBlocks = AltChainBlockProtoConverter.fromProto(request.getDifficultyBlocksList());

        BigDecimal difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(difficultyBlocks);
        PopPayoutRound payout = PopRewardCalculator.calculatePopPayoutRound(request.getBlockAltHeight(), endorsedBlock, endorsmentBlocks, difficulty);

        return VeriBlockMessages.RewardsOutputsReply.newBuilder()
                .addAllOutputs(RewardOutputProtoConverter.toProto(payout.getOutputsToPopMiners()))
                .setBlockReward(Long.toString(payout.getPopBlockReward()))
                .setTotalReward(Long.toString(payout.getTotalRewardPaidOut()))
                .build();
    }
}
