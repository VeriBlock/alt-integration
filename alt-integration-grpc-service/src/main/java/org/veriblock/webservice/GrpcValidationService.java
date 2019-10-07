// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import integration.api.grpc.ValidationServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.stub.StreamObserver;
import org.veriblock.protoservice.ValidationProtoService;

public class GrpcValidationService extends ValidationServiceGrpc.ValidationServiceImplBase {

    @Override
    public void verifyVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyVeriBlockPoPTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkSignatureVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkSignatureVeriBlockPoPTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBitcoinTransactionForPoPData(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkBitcoinTransactionForPoPData(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBitcoinMerklePathVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkBitcoinMerklePathVeriBlockPoPTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBitcoinBlocksVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkBitcoinBlocksVeriBlockPoPTx(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void verifyVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBlocksVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkBlocksVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkMerklePathVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkMerklePathVeriBlockPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void verifyVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkSignatureVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkSignatureVeriBlockTransaction(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void verifyVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkProofOfWorkVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkProofOfWorkVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkMaximumDriftVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkMaximumDriftVeriBlockBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void verifyBitcoinBlock(VeriBlockMessages.BitcoinBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkProofOfWorkBitcoinBlock(VeriBlockMessages.BitcoinBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkProofOfWorkBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkMaximumDriftBitcoinBlock(VeriBlockMessages.BitcoinBlock request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkMaximumDriftBitcoinBlock(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void verifyAltPublication(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.verifyAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkMerklePathAltPublication(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkMerklePathAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBlocksAltPublication(VeriBlockMessages.AltPublication request, StreamObserver<VeriBlockMessages.GeneralReply> responseObserver) {
        VeriBlockMessages.GeneralReply reply = ValidationProtoService.checkBlocksAltPublication(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
