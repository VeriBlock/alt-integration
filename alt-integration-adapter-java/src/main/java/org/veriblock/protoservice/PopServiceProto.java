package org.veriblock.protoservice;

import integration.api.grpc.VeriBlockMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.protoconverters.AltChainBlockProtoConverter;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;

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



    }
}
