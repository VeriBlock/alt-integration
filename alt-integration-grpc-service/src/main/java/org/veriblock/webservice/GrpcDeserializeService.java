// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import integration.api.grpc.DeserializeServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;
import org.veriblock.protoservice.DeserializeProtoService;

public class GrpcDeserializeService  extends DeserializeServiceGrpc.DeserializeServiceImplBase {

    @Override
    public void parseAltPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.AltPublicationReply> responseObserver) {
        VeriBlockMessages.AltPublicationReply reply = DeserializeProtoService.parseAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parsePublicationData(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.PublicationDataReply> responseObserver) {
        VeriBlockMessages.PublicationDataReply reply = DeserializeProtoService.parsePublicationData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseBitcoinTransaction(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.BitcoinTransactionReply> responseObserver) {
        VeriBlockMessages.BitcoinTransactionReply reply = DeserializeProtoService.parseBitcoinTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockBlock(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockBlockReply> responseObserver) {
        VeriBlockMessages.VeriBlockBlockReply reply = DeserializeProtoService.parseVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockTransaction(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockTransactionReply> responseObserver) {
        VeriBlockMessages.VeriBlockTransactionReply reply = DeserializeProtoService.parseVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockPublication(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockPublicationReply> responseObserver) {
        VeriBlockMessages.VeriBlockPublicationReply reply = DeserializeProtoService.parseVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockPopTx(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockPoPTransactionReply> responseObserver) {
        VeriBlockMessages.VeriBlockPoPTransactionReply reply = DeserializeProtoService.parseVeriBlockPopTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseOutput(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.OutputReply> responseObserver) {
        VeriBlockMessages.OutputReply reply = DeserializeProtoService.parseOutput(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseAddress(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.AddressReply> responseObserver) {
        VeriBlockMessages.AddressReply reply = DeserializeProtoService.parseAddress(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseBitcoinBlock(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.BitcoinBlockReply> responseObserver) {
        VeriBlockMessages.BitcoinBlockReply reply = DeserializeProtoService.parseBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseVeriBlockMerklePath(VeriBlockMessages.BytesArrayRequest request, StreamObserver<VeriBlockMessages.VeriBlockMerklePathReply> responseObserver) {
        VeriBlockMessages.VeriBlockMerklePathReply reply = DeserializeProtoService.parseVeriBlockMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void parseMerklePath(VeriBlockMessages.MerklePathRequest request, StreamObserver<VeriBlockMessages.MerklePathReply> responseObserver) {
        VeriBlockMessages.MerklePathReply reply = DeserializeProtoService.parseMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
