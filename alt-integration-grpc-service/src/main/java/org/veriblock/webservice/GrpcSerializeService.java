// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import integration.api.grpc.SerializeServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;
import org.veriblock.protoservice.SerializeProtoService;

public class GrpcSerializeService extends SerializeServiceGrpc.SerializeServiceImplBase {
    @Override
    public void serializeAltPublication(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializePublicationData(VeriBlockMessages.PublicationData request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializePublicationData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeBitcoinTransaction(VeriBlockMessages.BitcoinTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeBitcoinTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockPopTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeVeriBlockPopTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeOutput(VeriBlockMessages.Output request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeOutput(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeAddress(VeriBlockMessages.Address request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeAddress(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeBitcoinBlock(VeriBlockMessages.BitcoinBlock request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeVeriBlockMerklePath(VeriBlockMessages.VeriBlockMerklePath request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeVeriBlockMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void serializeMerklePath(VeriBlockMessages.MerklePath request, StreamObserver<VeriBlockMessages.BytesArrayReply> responseObserver) {
        VeriBlockMessages.BytesArrayReply reply = SerializeProtoService.serializeMerklePath(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
