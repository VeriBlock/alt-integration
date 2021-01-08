// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import integration.api.grpc.ValidationServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.protoconverters.AltPublicationProtoConverter;
import org.veriblock.protoconverters.BitcoinBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockPoPTransactionProtoConverter;
import org.veriblock.protoconverters.VeriBlockPublicationProtoConverter;
import org.veriblock.protoconverters.VeriBlockTransactionProtoConverter;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.ValidationService;

public class VeriBlockValidationProtoService extends ValidationServiceGrpc.ValidationServiceImplBase {

    public static VeriBlockMessages.GeneralReply verifyVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request) {
        try {
            ValidationService.verify(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkSignatureVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request) {
        try {
            ValidationService.checkSignature(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkBitcoinTransactionForPoPData(VeriBlockMessages.VeriBlockPoPTransaction request) {
        try {
            ValidationService.checkBitcoinTransactionForPoPData(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkBitcoinMerklePathVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request) {
        try {
            ValidationService.checkBitcoinMerklePath(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkBitcoinBlocksVeriBlockPoPTx(VeriBlockMessages.VeriBlockPoPTransaction request) {
        try {
            ValidationService.checkBitcoinBlocks(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply verifyVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request) {
        try {
            ValidationService.verify(VeriBlockPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkBlocksVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request) {
        try {
            ValidationService.checkBlocks(VeriBlockPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkMerklePathVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request) {
        try {
            ValidationService.checkMerklePath(VeriBlockPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply verifyVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request) {
        try {
            ValidationService.verify(VeriBlockTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkSignatureVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request) {
        try {
            ValidationService.checkSignature(VeriBlockTransactionProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply verifyVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request) {
        try {
            ValidationService.verify(VeriBlockBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkProofOfWorkVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request) {
        try {
            ValidationService.checkProofOfWork(VeriBlockBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkMaximumDriftVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request) {
        try {
            ValidationService.checkMaximumDrift(VeriBlockBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply verifyBitcoinBlock(VeriBlockMessages.BitcoinBlock request) {
        try {
            ValidationService.verify(BitcoinBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkProofOfWorkBitcoinBlock(VeriBlockMessages.BitcoinBlock request) {
        try {
            ValidationService.checkProofOfWork(BitcoinBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkMaximumDriftBitcoinBlock(VeriBlockMessages.BitcoinBlock request) {
        try {
            ValidationService.checkMaximumDrift(BitcoinBlockProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply verifyAltPublication(VeriBlockMessages.AltPublication request) {
        try {
            ValidationService.verify(AltPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkMerklePathAltPublication(VeriBlockMessages.AltPublication request) {
        try {
            ValidationService.checkMerklePath(AltPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }

    public static VeriBlockMessages.GeneralReply checkBlocksAltPublication(VeriBlockMessages.AltPublication request) {
        try {
            ValidationService.checkBlocks(AltPublicationProtoConverter.fromProto(request));
        } catch (VerificationException e){
            return VeriBlockMessages.GeneralReply.newBuilder()
                    .setResult(false)
                    .setResultMessage(e.getMessage())
                    .build();
        }
        return VeriBlockMessages.GeneralReply.newBuilder()
                .setResult(true)
                .build();
    }
}
