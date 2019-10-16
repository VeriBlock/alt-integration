// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.util.Collections;
import java.util.List;

import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.sqlite.tables.PoPTransactionData;
import org.veriblock.protoconverters.*;
import org.veriblock.sdk.*;

import integration.api.grpc.IntegrationServiceGrpc;
import integration.api.grpc.IntegrationServiceGrpc.IntegrationServiceBlockingStub;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.EmptyRequest;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import io.grpc.Channel;

public class VeriBlockSecurityProtoClient {    
    private final IntegrationServiceBlockingStub service;
    
    public VeriBlockSecurityProtoClient(Channel channel) {
        service = IntegrationServiceGrpc.newBlockingStub(channel);
    }
    
    public ValidationResult resetSecurity() {
        GeneralReply reply = service.resetSecurity(EmptyRequest.newBuilder().build());
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult addGenesisVeriBlock(VeriBlockBlock block) {
        GeneralReply reply = service.addGenesisVeriBlock(VeriBlockBlockProtoConverter.toProto(block));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult addGenesisBitcoin(BitcoinBlock block) {
        GeneralReply reply = service.addGenesisBitcoin(BitcoinBlockProtoConverter.toProto(block));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult addPayloads(BlockIndex blockIndex, List<AltPublication> altPublications, List<VeriBlockPublication> vtbPublications) {        
        VeriBlockMessages.AddPayloadsRequest request = VeriBlockMessages.AddPayloadsRequest.newBuilder()
                .setBlockIndex(BlockIndexProtoConverter.toProto(blockIndex))
                .addAllAltPublications(AltPublicationProtoConverter.toProto(VeriBlockServiceCommon.nullToEmptyList(altPublications)))
                .addAllVeriblockPublications(VeriBlockPublicationProtoConverter.toProto(VeriBlockServiceCommon.nullToEmptyList(vtbPublications)))
                .build();
        GeneralReply reply = service.addPayloads(request);        
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult removePayloads(BlockIndex blockIndex) {
        VeriBlockMessages.RemovePayloadsRequest request = VeriBlockMessages.RemovePayloadsRequest.newBuilder()
                .setBlockIndex(BlockIndexProtoConverter.toProto(blockIndex))
                .build();
        GeneralReply reply = service.removePayloads(request);
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult addTemporaryPayloads(List<AltPublication> altPublications, List<VeriBlockPublication> vtbPublications) {
        VeriBlockMessages.AddTemporaryPayloadsRequest request = VeriBlockMessages.AddTemporaryPayloadsRequest.newBuilder()
                .addAllAltPublications(AltPublicationProtoConverter.toProto(VeriBlockServiceCommon.nullToEmptyList(altPublications)))
                .addAllVeriblockPublications(VeriBlockPublicationProtoConverter.toProto(VeriBlockServiceCommon.nullToEmptyList(vtbPublications)))
                .build();
        GeneralReply reply = service.addTemporaryPayloads(request);
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult clearTemporaryPayloads() {
        GeneralReply reply = service.clearTemporaryPayloads(EmptyRequest.newBuilder().build());
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public Pair<ValidationResult, List<VeriBlockPublication>> simplifyVTBs(List<VeriBlockPublication> vtbPublications) {
        VeriBlockMessages.SimplifyVTBsRequest request = VeriBlockMessages.SimplifyVTBsRequest.newBuilder()
                .addAllVeriblockPublications(VeriBlockPublicationProtoConverter.toProto(VeriBlockServiceCommon.nullToEmptyList(vtbPublications)))
                .build();
        VeriBlockMessages.SimplifyVTBsReply reply = service.simplifyVTBs(request);
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, Collections.emptyList());
        
        List<VeriBlockPublication> resultVtbPublications = VeriBlockPublicationProtoConverter.fromProto(reply.getVeriblockPublicationsList());
        return new Pair<>(resultValid, resultVtbPublications);
    }
    
    public ValidationResult checkATVAgainstView(AltPublication publication) {     
        GeneralReply reply = service.checkATVAgainstView(AltPublicationProtoConverter.toProto(publication));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult checkVTBInternally(VeriBlockPublication publication) {     
        GeneralReply reply = service.checkVTBInternally(VeriBlockPublicationProtoConverter.toProto(publication));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public ValidationResult checkATVInternally(AltPublication publication) {     
        GeneralReply reply = service.checkATVInternally(AltPublicationProtoConverter.toProto(publication));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
    
    public Pair<ValidationResult, Integer> getMainVBKHeightOfATV(AltPublication publication) {     
        VeriBlockMessages.GetMainVBKHeightOfATVReply reply = service.getMainVBKHeightOfATV(AltPublicationProtoConverter.toProto(publication));
        ValidationResult resultValid = VeriBlockServiceCommon.validationResultFromProto(reply.getResult());
        if(!resultValid.isValid()) return new Pair<>(resultValid, 0);
        
        return new Pair<>(resultValid, reply.getHeight());
    }

    public ValidationResult setAltChainParametersConfig(AltChainParametersConfig config)
    {
        VeriBlockMessages.GeneralReply reply = service.setAltChainParametersConfig(AltChainParametresConfigProtoConverter.toProto(config));
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }

    public ValidationResult savePoPTransactionData(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock) {
        VeriBlockMessages.SavePoPTransactionDataRequest request = VeriBlockMessages.SavePoPTransactionDataRequest.newBuilder()
                .setPopTx(PoPTransactionDataProtoConverter.toProto(popTx))
                .setContainingBlock(AltChainBlockProtoConverter.toProto(containingBlock))
                .setEndorsedBlock(AltChainBlockProtoConverter.toProto(endorsedBlock))
                .build();
        VeriBlockMessages.GeneralReply reply = service.savePoPTransactionData(request);
        return VeriBlockServiceCommon.validationResultFromProto(reply);
    }
}
