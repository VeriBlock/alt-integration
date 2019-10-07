// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.VeriBlockBlock;

import com.google.protobuf.ByteString;

import integration.api.grpc.VeriBlockMessages;

public final class VeriBlockBlockProtoConverter {

    private VeriBlockBlockProtoConverter() {} //never
    
    public static VeriBlockBlock fromProto(VeriBlockMessages.VeriBlockBlock protoData) {
        int height = protoData.getHeight();
        short version = (short) protoData.getVersion();
        VBlakeHash previousBlock = VBlakeHash.wrap(protoData.getPreviousBlock().toByteArray(), protoData.getPreviousBlock().size());
        VBlakeHash previousKeystone = VBlakeHash.wrap(protoData.getPreviousKeystone().toByteArray(), protoData.getPreviousKeystone().size());
        VBlakeHash secondPreviousKeystone = VBlakeHash.wrap(protoData.getSecondPreviousKeystone().toByteArray(), protoData.getSecondPreviousKeystone().size());
        Sha256Hash merkleRoot = Sha256Hash.wrap(protoData.getMerkleRoot().toByteArray(), protoData.getMerkleRoot().size());
        int timestamp = protoData.getTimestamp();
        int difficulty = protoData.getDifficulty();
        int nonce = protoData.getNonce();
        VeriBlockBlock result = new VeriBlockBlock(height, version, previousBlock, previousKeystone, secondPreviousKeystone, merkleRoot,
                timestamp, difficulty, nonce);
        return result;
    }
    
    public static List<VeriBlockBlock> fromProto(List<VeriBlockMessages.VeriBlockBlock> protoData) {
        List<VeriBlockBlock> result = new ArrayList<>();
        for(VeriBlockMessages.VeriBlockBlock output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.VeriBlockBlock toProto(VeriBlockBlock data) {
        int height = data.getHeight();
        short version = data.getVersion();
        byte[] previousBlock = data.getPreviousBlock().getBytes();
        byte[] previousKeystone = data.getPreviousKeystone().getBytes();
        byte[] secondPreviousKeystone = data.getSecondPreviousKeystone().getBytes();
        byte[] merkleRoot = data.getMerkleRoot().getBytes();
        int timestamp = data.getTimestamp();
        int difficulty = data.getDifficulty();
        int nonce = data.getNonce();
        
        VeriBlockMessages.VeriBlockBlock.Builder result = VeriBlockMessages.VeriBlockBlock.newBuilder();
        result = result.setHeight(height)
                .setVersion(version)
                .setPreviousBlock(ByteString.copyFrom(previousBlock))
                .setPreviousKeystone(ByteString.copyFrom(previousKeystone))
                .setSecondPreviousKeystone(ByteString.copyFrom(secondPreviousKeystone))
                .setMerkleRoot(ByteString.copyFrom(merkleRoot))
                .setTimestamp(timestamp)
                .setDifficulty(difficulty)
                .setNonce(nonce);
        return result.build();
    }
    
    public static List<VeriBlockMessages.VeriBlockBlock> toProto(List<VeriBlockBlock> data) {
        List<VeriBlockMessages.VeriBlockBlock> result = new ArrayList<>();
        for(VeriBlockBlock output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
