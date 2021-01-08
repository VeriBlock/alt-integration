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
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

public final class BitcoinBlockProtoConverter {

    private BitcoinBlockProtoConverter() {} //never
    
    public static BitcoinBlock fromProto(VeriBlockMessages.BitcoinBlock protoData) {
        int version = protoData.getVersion();
        Sha256Hash previousBlock = Sha256Hash.wrap(protoData.getPreviousBlock().toByteArray(), protoData.getPreviousBlock().size());
        Sha256Hash merkleRoot = Sha256Hash.wrap(protoData.getMerkleRoot().toByteArray(), protoData.getMerkleRoot().size());
        int timestamp = protoData.getTimestamp();
        int bits = protoData.getBits();
        int nonce = protoData.getNonce();

        BitcoinBlock result = new BitcoinBlock(version, previousBlock, merkleRoot, timestamp, bits, nonce);
        return result;
    }
    
    public static List<BitcoinBlock> fromProto(List<VeriBlockMessages.BitcoinBlock> protoData) {
        List<BitcoinBlock> result = new ArrayList<BitcoinBlock>();
        for(VeriBlockMessages.BitcoinBlock output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.BitcoinBlock toProto(BitcoinBlock data) {
        int version = data.getVersion();
        byte[] previousBlock = data.getPreviousBlock().getBytes();
        byte[] merkleRoot = data.getMerkleRoot().getBytes();
        int timestamp = data.getTimestamp();
        int bits = data.getBits();
        int nonce = data.getNonce();
        
        VeriBlockMessages.BitcoinBlock.Builder result = VeriBlockMessages.BitcoinBlock.newBuilder();
        result = result.setVersion(version)
                .setPreviousBlock(ByteString.copyFrom(previousBlock))
                .setMerkleRoot(ByteString.copyFrom(merkleRoot))
                .setTimestamp(timestamp)
                .setBits(bits)
                .setNonce(nonce);
        return result.build();
    }
    
    public static List<VeriBlockMessages.BitcoinBlock> toProto(List<BitcoinBlock> data) {
        List<VeriBlockMessages.BitcoinBlock> result = new ArrayList<>();
        for(BitcoinBlock output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
