// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;

import java.util.List;

public final class VeriBlockPoPTransactionProtoConverter {

    private VeriBlockPoPTransactionProtoConverter() {} //never

    public static VeriBlockPoPTransaction fromProto(VeriBlockMessages.VeriBlockPoPTransaction protoData) {
        Address address = AddressProtoConverter.fromProto(protoData.getAddress());
        VeriBlockBlock publishedBlock = VeriBlockBlockProtoConverter.fromProto(protoData.getPublishedBlock());
        BitcoinTransaction bitcoinTransaction = BitcoinTransactionProtoConverter.fromProto(protoData.getBitcoinTransaction());
        MerklePath merklePath = MerklePathProtoConverter.fromProto(protoData.getMerklePath());
        BitcoinBlock blockOfProof = BitcoinBlockProtoConverter.fromProto(protoData.getBlockOfProof());
        List<BitcoinBlock> blockOfProofContext = BitcoinBlockProtoConverter.fromProto(protoData.getBlockOfProofContextList());
        byte[] signature = protoData.getSignature().toByteArray();
        byte[] publicKey = protoData.getPublicKey().toByteArray();
        Byte networkByte = NetworkByteConverter.fromProto(protoData.getNetworkByte());

        VeriBlockPoPTransaction result = new VeriBlockPoPTransaction(address, publishedBlock, bitcoinTransaction, merklePath, blockOfProof,
                blockOfProofContext, signature, publicKey, networkByte);
        return result;
    }

    public static VeriBlockMessages.VeriBlockPoPTransaction toProto(VeriBlockPoPTransaction data) {
        VeriBlockMessages.Address address = AddressProtoConverter.toProto(data.getAddress());
        VeriBlockMessages.VeriBlockBlock publishedBlock = VeriBlockBlockProtoConverter.toProto(data.getPublishedBlock());
        VeriBlockMessages.BitcoinTransaction bitcoinTransaction = BitcoinTransactionProtoConverter.toProto(data.getBitcoinTransaction());
        String merklePath = data.getMerklePath().toCompactString();
        VeriBlockMessages.BitcoinBlock blockOfProof = BitcoinBlockProtoConverter.toProto(data.getBlockOfProof());
        List<VeriBlockMessages.BitcoinBlock> blockOfProofContext = BitcoinBlockProtoConverter.toProto(data.getBlockOfProofContext());
        byte[] signature = data.getSignature();
        byte[] publicKey = data.getPublicKey();        
        VeriBlockMessages.NetworkByte networkByte = NetworkByteConverter.toProto(data.getNetworkByte());

        VeriBlockMessages.VeriBlockPoPTransaction.Builder result = VeriBlockMessages.VeriBlockPoPTransaction.newBuilder();
        result = result.setAddress(address)
                .setPublishedBlock(publishedBlock)
                .setBitcoinTransaction(bitcoinTransaction)
                .setMerklePath(merklePath)
                .setBlockOfProof(blockOfProof)
                .addAllBlockOfProofContext(blockOfProofContext)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(ByteString.copyFrom(publicKey))
                .setNetworkByte(networkByte);
        return result.build();
    }
}
