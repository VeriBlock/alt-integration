// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;
import org.veriblock.protoconverters.AddressProtoConverter;
import org.veriblock.protoconverters.AltPublicationProtoConverter;
import org.veriblock.protoconverters.BitcoinBlockProtoConverter;
import org.veriblock.protoconverters.BitcoinTransactionProtoConverter;
import org.veriblock.protoconverters.MerklePathProtoConverter;
import org.veriblock.protoconverters.OutputsProtoConverter;
import org.veriblock.protoconverters.PublicationDataProtoConverter;
import org.veriblock.protoconverters.VeriBlockBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockMerklePathProtoConverter;
import org.veriblock.protoconverters.VeriBlockPoPTransactionProtoConverter;
import org.veriblock.protoconverters.VeriBlockPublicationProtoConverter;
import org.veriblock.protoconverters.VeriBlockTransactionProtoConverter;
import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.Output;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class VeriBlockDeserializeProtoService {
    public static VeriBlockMessages.AltPublicationReply parseAltPublication(VeriBlockMessages.BytesArrayRequest request) {
        AltPublication publication = null;
        
        try {
            publication = SerializeDeserializeService.parseAltPublication(request.getData().toByteArray());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.AltPublicationReply reply = VeriBlockMessages.AltPublicationReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.AltPublication publicationReply = AltPublicationProtoConverter.toProto(publication);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.AltPublicationReply reply = VeriBlockMessages.AltPublicationReply.newBuilder()
                .setPublication(publicationReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.PublicationDataReply parsePublicationData(VeriBlockMessages.BytesArrayRequest request) {
        PublicationData publication = null;
        
        try {
            publication = SerializeDeserializeService.parsePublicationData(request.getData().toByteArray());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.PublicationDataReply reply = VeriBlockMessages.PublicationDataReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.PublicationData publicationReply = PublicationDataProtoConverter.toProto(publication);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.PublicationDataReply reply = VeriBlockMessages.PublicationDataReply.newBuilder()
                .setPublication(publicationReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.BitcoinTransactionReply parseBitcoinTransaction(VeriBlockMessages.BytesArrayRequest request) {
        BitcoinTransaction transaction = null;
        
        try {
            transaction = SerializeDeserializeService.parseBitcoinTransaction(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.BitcoinTransactionReply reply = VeriBlockMessages.BitcoinTransactionReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.BitcoinTransaction transactionReply = BitcoinTransactionProtoConverter.toProto(transaction);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.BitcoinTransactionReply reply = VeriBlockMessages.BitcoinTransactionReply.newBuilder()
                .setTransaction(transactionReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.VeriBlockBlockReply parseVeriBlockBlock(VeriBlockMessages.BytesArrayRequest request) {
        VeriBlockBlock block = null;
        
        try {
            block = SerializeDeserializeService.parseVeriBlockBlock(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.VeriBlockBlockReply reply = VeriBlockMessages.VeriBlockBlockReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.VeriBlockBlock blockReply = VeriBlockBlockProtoConverter.toProto(block);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.VeriBlockBlockReply reply = VeriBlockMessages.VeriBlockBlockReply.newBuilder()
                .setBlock(blockReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.VeriBlockTransactionReply parseVeriBlockTransaction(VeriBlockMessages.BytesArrayRequest request) {
        VeriBlockTransaction transaction = null;
        
        try {
            transaction = SerializeDeserializeService.parseVeriBlockTransaction(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.VeriBlockTransactionReply reply = VeriBlockMessages.VeriBlockTransactionReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.VeriBlockTransaction transactionReply = VeriBlockTransactionProtoConverter.toProto(transaction);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.VeriBlockTransactionReply reply = VeriBlockMessages.VeriBlockTransactionReply.newBuilder()
                .setTransaction(transactionReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.VeriBlockPublicationReply parseVeriBlockPublication(VeriBlockMessages.BytesArrayRequest request) {
        VeriBlockPublication publication = null;
        
        try {
            publication = SerializeDeserializeService.parseVeriBlockPublication(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.VeriBlockPublicationReply reply = VeriBlockMessages.VeriBlockPublicationReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.VeriBlockPublication publicationReply = VeriBlockPublicationProtoConverter.toProto(publication);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.VeriBlockPublicationReply reply = VeriBlockMessages.VeriBlockPublicationReply.newBuilder()
                .setPublication(publicationReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.VeriBlockPoPTransactionReply parseVeriBlockPopTx(VeriBlockMessages.BytesArrayRequest request) {
        VeriBlockPoPTransaction transaction = null;
        
        try {
            transaction = SerializeDeserializeService.parseVeriBlockPoPTx(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.VeriBlockPoPTransactionReply reply = VeriBlockMessages.VeriBlockPoPTransactionReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.VeriBlockPoPTransaction transactionReply = VeriBlockPoPTransactionProtoConverter.toProto(transaction);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.VeriBlockPoPTransactionReply reply = VeriBlockMessages.VeriBlockPoPTransactionReply.newBuilder()
                .setTransaction(transactionReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.OutputReply parseOutput(VeriBlockMessages.BytesArrayRequest request) {
        Output output = null;
        
        try {
            output = SerializeDeserializeService.parseOutput(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.OutputReply reply = VeriBlockMessages.OutputReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.Output outputReply = OutputsProtoConverter.toProto(output);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.OutputReply reply = VeriBlockMessages.OutputReply.newBuilder()
                .setOutput(outputReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.AddressReply parseAddress(VeriBlockMessages.BytesArrayRequest request) {
        Address address = null;
        
        try {
            address = SerializeDeserializeService.parseAddress(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.AddressReply reply = VeriBlockMessages.AddressReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.Address addressReply = AddressProtoConverter.toProto(address);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.AddressReply reply = VeriBlockMessages.AddressReply.newBuilder()
                .setAddress(addressReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.BitcoinBlockReply parseBitcoinBlock(VeriBlockMessages.BytesArrayRequest request) {
        BitcoinBlock block = null;
        
        try {
            block = SerializeDeserializeService.parseBitcoinBlockWithLength(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.BitcoinBlockReply reply = VeriBlockMessages.BitcoinBlockReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.BitcoinBlock blockReply = BitcoinBlockProtoConverter.toProto(block);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.BitcoinBlockReply reply = VeriBlockMessages.BitcoinBlockReply.newBuilder()
                .setBlock(blockReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.VeriBlockMerklePathReply parseVeriBlockMerklePath(VeriBlockMessages.BytesArrayRequest request) {
        VeriBlockMerklePath merklePath = null;
        
        try {
            merklePath = SerializeDeserializeService.parseVeriBlockMerklePath(request.getData().asReadOnlyByteBuffer());
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.VeriBlockMerklePathReply reply = VeriBlockMessages.VeriBlockMerklePathReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.VeriBlockMerklePath merklePathReply = VeriBlockMerklePathProtoConverter.toProto(merklePath);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.VeriBlockMerklePathReply reply = VeriBlockMessages.VeriBlockMerklePathReply.newBuilder()
                .setMerklePath(merklePathReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
    
    public static VeriBlockMessages.MerklePathReply parseMerklePath(VeriBlockMessages.MerklePathRequest request) {
        MerklePath merklePath = null;
        
        try {
            merklePath = SerializeDeserializeService.parseMerklePath(request.getData().asReadOnlyByteBuffer(), Sha256Hash.wrap(request.getSubject().toByteArray()));
        } catch(Exception e) {
            ValidationResult result = ValidationResult.fail(e.getMessage());
            GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
            VeriBlockMessages.MerklePathReply reply = VeriBlockMessages.MerklePathReply.newBuilder()
                    .setResult(replyResult)
                    .build();
            return reply;
        }
        
        VeriBlockMessages.MerklePath merklePathReply = MerklePathProtoConverter.toProto(merklePath);
        ValidationResult result = ValidationResult.success();
        GeneralReply replyResult = VeriBlockServiceCommon.validationResultToProto(result);
        VeriBlockMessages.MerklePathReply reply = VeriBlockMessages.MerklePathReply.newBuilder()
                .setMerklePath(merklePathReply)
                .setResult(replyResult)
                .build();
        return reply;
    }
}
