// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import org.veriblock.protoservice.VeriBlockDeserializeProtoService;

import integration.api.grpc.DeserializeServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;

public class GrpcDeserializeService  extends DeserializeServiceGrpc.DeserializeServiceImplBase {

    @Override
    public void parseAltPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.AltPublicationReply> responseObserver) {
        VeriBlockMessages.AltPublicationReply reply = VeriBlockDeserializeProtoService.parseAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parsePublicationData(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.PublicationDataReply> responseObserver) {
        VeriBlockMessages.PublicationDataReply reply = VeriBlockDeserializeProtoService.parsePublicationData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseBitcoinTransaction(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.BitcoinTransactionReply> responseObserver) {
        VeriBlockMessages.BitcoinTransactionReply reply = VeriBlockDeserializeProtoService.parseBitcoinTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockBlock(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockBlockReply> responseObserver) {
        VeriBlockMessages.VeriBlockBlockReply reply = VeriBlockDeserializeProtoService.parseVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockTransaction(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockTransactionReply> responseObserver) {
        VeriBlockMessages.VeriBlockTransactionReply reply = VeriBlockDeserializeProtoService.parseVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockPublicationReply> responseObserver) {
        VeriBlockMessages.VeriBlockPublicationReply reply = VeriBlockDeserializeProtoService.parseVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockPopTx(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockPoPTransactionReply> responseObserver) {
        VeriBlockMessages.VeriBlockPoPTransactionReply reply = VeriBlockDeserializeProtoService.parseVeriBlockPopTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseOutput(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.OutputReply> responseObserver) {
        VeriBlockMessages.OutputReply reply = VeriBlockDeserializeProtoService.parseOutput(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseAddress(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.AddressReply> responseObserver) {
        VeriBlockMessages.AddressReply reply = VeriBlockDeserializeProtoService.parseAddress(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseBitcoinBlock(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.BitcoinBlockReply> responseObserver) {
        VeriBlockMessages.BitcoinBlockReply reply = VeriBlockDeserializeProtoService.parseBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockMerklePath(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockMerklePathReply> responseObserver) {
        VeriBlockMessages.VeriBlockMerklePathReply reply = VeriBlockDeserializeProtoService.parseVeriBlockMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseMerklePath(VeriBlockMessages.MerklePathRequest request, StreamObserver<VeriBlockMessages.MerklePathReply> responseObserver) {
        VeriBlockMessages.MerklePathReply reply = VeriBlockDeserializeProtoService.parseMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
