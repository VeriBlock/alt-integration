// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.protoconverters.AltPublicationProtoConverter;
import org.veriblock.protoconverters.BitcoinBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockPoPTransactionProtoConverter;
import org.veriblock.protoconverters.VeriBlockPublicationProtoConverter;
import org.veriblock.protoconverters.VeriBlockTransactionProtoConverter;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;

import integration.api.grpc.ValidationServiceGrpc;
import io.grpc.Channel;

public class ValidationProtoClient {

    private final ValidationServiceGrpc.ValidationServiceBlockingStub service;

    public ValidationProtoClient(Channel channel) {
        service = ValidationServiceGrpc.newBlockingStub(channel);
    }

    public ValidationResult verifyVeriBlockPoPTx(VeriBlockPoPTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyVeriBlockPoPTx(VeriBlockPoPTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult checkSignatureVeriBlockPoPTx(VeriBlockPoPTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkSignatureVeriBlockPoPTx(VeriBlockPoPTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult checkBitcoinTransactionForPoPData(VeriBlockPoPTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkBitcoinTransactionForPoPData(VeriBlockPoPTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult checkBitcoinMerklePathVeriBlockPoPTx(VeriBlockPoPTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkBitcoinMerklePathVeriBlockPoPTx(VeriBlockPoPTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult checkBitcoinBlocksVeriBlockPoPTx(VeriBlockPoPTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkBitcoinBlocksVeriBlockPoPTx(VeriBlockPoPTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult verifyVeriBlockPublication(VeriBlockPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyVeriBlockPublication(VeriBlockPublicationProtoConverter.toProto(request)));
    }

    public ValidationResult checkBlocksVeriBlockPublication(VeriBlockPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkBlocksVeriBlockPublication(VeriBlockPublicationProtoConverter.toProto(request)));
    }

    public ValidationResult checkMerklePathVeriBlockPublication(VeriBlockPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkMerklePathVeriBlockPublication(VeriBlockPublicationProtoConverter.toProto(request)));
    }

    public ValidationResult verifyVeriBlockTransaction(VeriBlockTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyVeriBlockTransaction(VeriBlockTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult checkSignatureVeriBlockTransaction(VeriBlockTransaction request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkSignatureVeriBlockTransaction(VeriBlockTransactionProtoConverter.toProto(request)));
    }

    public ValidationResult verifyVeriBlockBlock(VeriBlockBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyVeriBlockBlock(VeriBlockBlockProtoConverter.toProto(request)));
    }

    public ValidationResult checkProofOfWorkVeriBlockBlock(VeriBlockBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkProofOfWorkVeriBlockBlock(VeriBlockBlockProtoConverter.toProto(request)));
    }

    public ValidationResult checkMaximumDriftVeriBlockBlock(VeriBlockBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkMaximumDriftVeriBlockBlock(VeriBlockBlockProtoConverter.toProto(request)));
    }

    public ValidationResult verifyBitcoinBlock(BitcoinBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyBitcoinBlock(BitcoinBlockProtoConverter.toProto(request)));
    }

    public ValidationResult checkProofOfWorkBitcoinBlock(BitcoinBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkProofOfWorkBitcoinBlock(BitcoinBlockProtoConverter.toProto(request)));
    }

    public ValidationResult checkMaximumDriftBitcoinBlock(BitcoinBlock request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkMaximumDriftBitcoinBlock(BitcoinBlockProtoConverter.toProto(request)));
    }

    public ValidationResult verifyAltPublication(AltPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.verifyAltPublication(AltPublicationProtoConverter.toProto(request)));
    }

    public ValidationResult checkMerklePathAltPublication(AltPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkMerklePathAltPublication(AltPublicationProtoConverter.toProto(request)));
    }

    public ValidationResult checkBlocksAltPublication(AltPublication request) {
        return VeriBlockServiceCommon.validationResultFromProto(service.checkBlocksAltPublication(AltPublicationProtoConverter.toProto(request)));
    }

}
