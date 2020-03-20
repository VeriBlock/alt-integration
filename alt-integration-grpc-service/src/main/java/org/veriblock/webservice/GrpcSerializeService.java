// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import org.veriblock.protoservice.VeriBlockSerializeProtoService;

import integration.api.grpc.SerializeServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;

public class GrpcSerializeService extends SerializeServiceGrpc.SerializeServiceImplBase {
    @Override
    public void serializeAltPublication(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializePublicationData(VeriBlockMessages.PublicationData request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializePublicationData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeBitcoinTransaction(VeriBlockMessages.BitcoinTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeBitcoinTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockPopTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeVeriBlockPopTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeOutput(VeriBlockMessages.Output request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeOutput(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeAddress(VeriBlockMessages.Address request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeAddress(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeBitcoinBlock(VeriBlockMessages.BitcoinBlock request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockMerklePath(VeriBlockMessages.VeriBlockMerklePath request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeVeriBlockMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeMerklePath(VeriBlockMessages.MerklePath request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = VeriBlockSerializeProtoService.serializeMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
