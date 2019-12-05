package org.veriblock.protoservice;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.protoconverters.RewardOutputProtoConverter;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.forkresolution.ForkresolutionComparator;
import org.veriblock.sdk.models.*;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.rewards.PopRewardCalculator;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    static public VeriBlockMessages.RewardsCalculateReply rewardsCalculateOutputs(VeriBlockMessages.RewardsCalculateRequest request) throws Exception {
        AltChainBlock endorsedBlock = AltChainBlockProtoConverter.fromProto(request.getEndorsedBlock());
        List<AltChainBlock> endorsmentBlocks = AltChainBlockProtoConverter.fromProto(request.getEndorsmentBlocksList());
        List<AltChainBlock> difficultyBlocks = AltChainBlockProtoConverter.fromProto(request.getDifficultyBlocksList());

        BigDecimal difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(difficultyBlocks);
        PopPayoutRound payout = PopRewardCalculator.calculatePopPayoutRound(request.getBlockAltHeight(), endorsedBlock, endorsmentBlocks, difficulty);

        return VeriBlockMessages.RewardsCalculateReply.newBuilder()
                .addAllOutputs(RewardOutputProtoConverter.toProto(payout.getOutputsToPopMiners()))
                .setBlockReward(Long.toString(payout.getPopBlockReward()))
                .setTotalReward(Long.toString(payout.getTotalRewardPaidOut()))
                .build();
    }

    static public VeriBlockMessages.Empty savePopTxToDatabase(VeriBlockMessages.SaveBlockPopTxRequest request) throws Exception {
        AltChainBlock containingBlock = AltChainBlockProtoConverter.fromProto(request.getContainingBlock());
        for(VeriBlockMessages.PopTxData popData : request.getpopDataList())
        {
            AltChainBlock endorsedBlock = AltChainBlockProtoConverter.fromProto(popData.getEndorsedBlock());
            AltPublication altPublication = SerializeDeserializeService.parseAltPublication(popData.getAltPublication());

            List<VeriBlockPublication> veriBlockPublications = new ArrayList<VeriBlockPublication>();
            for(ByteString veriBlockPublicationBytes : popData.getVeriblockPublicationsList())
            {
                veriBlockPublications.add(SerializeDeserializeService.parseVeriBlockPublication(veriBlockPublicationBytes.toByteArray()));
            }

            PoPTransactionData popTx = new PoPTransactionData(popData.getPopTxHash(), veriBlockPublications, altPublication);
            Context.getPopTxDBStore().addPoPTransaction(popTx, containingBlock, endorsedBlock);
        }

        return VeriBlockMessages.Empty.newBuilder().build();
    }

    public static VeriBlockMessages.Empty updateContext(VeriBlockMessages.UpdateContextRequest request) throws Exception {
        List<BitcoinBlock> bitcoinBlocks = new ArrayList<BitcoinBlock>(request.getBitcoinBlocksCount());
        List<VeriBlockBlock> veriBlockBlocks = new ArrayList<VeriBlockBlock>(request.getVeriBlockBlocksCount());
        for(ByteString encodedBlock : request.getBitcoinBlocksList()) {
            BitcoinBlock block = SerializeDeserializeService.parseBitcoinBlock(encodedBlock.toByteArray());
            bitcoinBlocks.add(block);
        }

        for(ByteString encodedBlock : request.getVeriBlockBlocksList()) {
            VeriBlockBlock block = SerializeDeserializeService.parseVeriBlockBlock(encodedBlock.toByteArray());
            veriBlockBlocks.add(block);
        }

        security.updateContext(bitcoinBlocks, veriBlockBlocks);
        return VeriBlockMessages.Empty.newBuilder().build();
    }

    public static VeriBlockMessages.CompareTwoBranchesReply compareTwoBranches(VeriBlockMessages.TwoBranchesRequest request) throws Exception {

        List<AltChainBlock> leftFork = AltChainBlockProtoConverter.fromProto(request.getLeftForkList());
        List<AltChainBlock> rightFork = AltChainBlockProtoConverter.fromProto(request.getRightForkList());
        int compareResult = ForkresolutionComparator.compareTwoBranches(leftFork, rightFork);

        return VeriBlockMessages.CompareTwoBranchesReply.newBuilder()
                .setComparingsResult(compareResult)
                .build();
    }
}