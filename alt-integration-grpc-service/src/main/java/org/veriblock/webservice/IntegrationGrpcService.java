// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.protoservice.VeriBlockSecurityProtoService;

import integration.api.grpc.IntegrationServiceGrpc.IntegrationServiceImplBase;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import io.grpc.stub.StreamObserver;

public class IntegrationGrpcService extends IntegrationServiceImplBase {
    
    public IntegrationGrpcService(VeriBlockSecurity security) {
        super();
        VeriBlockSecurityProtoService.setVeriBlockSecurity(security);
    }
    
    @Override
    public void resetSecurity(VeriBlockMessages.EmptyRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.resetSecurity();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void addGenesisVeriBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.addGenesisVeriBlock(request);        
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void addGenesisBitcoin(VeriBlockMessages.BitcoinBlock request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.addGenesisBitcoin(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void addPayloads(VeriBlockMessages.AddPayloadsRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.addPayloads(request.getBlockIndex(),
                request.getAltPublicationsList(),
                request.getVeriblockPublicationsList());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void removePayloads(VeriBlockMessages.RemovePayloadsRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.removePayloads(request.getBlockIndex());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void addTemporaryPayloads(VeriBlockMessages.AddTemporaryPayloadsRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.addTemporaryPayloads(request.getAltPublicationsList(),
                request.getVeriblockPublicationsList());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void clearTemporaryPayloads(VeriBlockMessages.EmptyRequest request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.clearTemporaryPayloads();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void simplifyVTBs(VeriBlockMessages.SimplifyVTBsRequest request, StreamObserver<VeriBlockMessages.SimplifyVTBsReply> responseObserver) {
        VeriBlockMessages.SimplifyVTBsReply reply = VeriBlockSecurityProtoService.simplifyVTBs(request.getVeriblockPublicationsList());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void checkATVAgainstView(VeriBlockMessages.AltPublication request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.checkATVAgainstView(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void checkVTBInternally(VeriBlockMessages.VeriBlockPublication request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.checkVTBInternally(request);        
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void checkATVInternally(VeriBlockMessages.AltPublication request, StreamObserver<GeneralReply> responseObserver) {
        GeneralReply reply = VeriBlockSecurityProtoService.checkATVInternally(request);        
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
    
    @Override
    public void getMainVBKHeightOfATV(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.GetMainVBKHeightOfATVReply> responseObserver) {
        VeriBlockMessages.GetMainVBKHeightOfATVReply reply = VeriBlockSecurityProtoService.getMainVBKHeightOfATV(request);        
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void setAltChainParametersConfig(VeriBlockMessages.AltChainConfigRequest request, StreamObserver<GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = VeriBlockSecurityProtoService.setAltChainParametersConfig(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void savePoPTransactionData(VeriBlockMessages.SavePoPTransactionDataRequest request, StreamObserver<GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = VeriBlockSecurityProtoService.savePoPTransactionData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getLastKnownVBKBlocks(VeriBlockMessages.GetLastKnownBlocksRequest request, StreamObserver<VeriBlockMessages.GetLastKnownVBKBlocksReply> responseObserver) {
        VeriBlockMessages.GetLastKnownVBKBlocksReply reply = VeriBlockSecurityProtoService.getLastKnownVBKBlocks(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getLastKnownBTCBlocks(VeriBlockMessages.GetLastKnownBlocksRequest request, StreamObserver<VeriBlockMessages.GetLastKnownBTCBlocksReply> responseObserver) {
        VeriBlockMessages.GetLastKnownBTCBlocksReply reply = VeriBlockSecurityProtoService.getLastKnownBTCBlocks(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
