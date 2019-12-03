// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.AltChainBlock;

import java.util.ArrayList;
import java.util.List;

public class AltChainBlockProtoConverter {

    private AltChainBlockProtoConverter() {} // never

    public static AltChainBlock fromProto(VeriBlockMessages.AltChainBlock protoData) {
        AltChainBlock block = new AltChainBlock(protoData.getBlockIndex().getHash(), protoData.getBlockIndex().getHeight(), protoData.getTimestamp());

        return block;
    }

    public static VeriBlockMessages.AltChainBlock toProto(AltChainBlock block) {
        VeriBlockMessages.BlockIndex.Builder blockIndexBuilder = VeriBlockMessages.BlockIndex.newBuilder();
        blockIndexBuilder.setHash(block.getHash()).setHeight(block.getHeight());

        VeriBlockMessages.AltChainBlock.Builder result = VeriBlockMessages.AltChainBlock.newBuilder();
        result.setBlockIndex(blockIndexBuilder.build());
        result.setTimestamp(block.getTimestamp());

        return result.build();
    }

    public static List<AltChainBlock> fromProto(List<VeriBlockMessages.AltChainBlock> protoData) {
        List<AltChainBlock> result = new ArrayList<AltChainBlock>();
        for(VeriBlockMessages.AltChainBlock protoBlock: protoData)
        {
            result.add(fromProto(protoBlock));
        }
        return result;
    }

    public static List<VeriBlockMessages.AltChainBlock> toProto(List<AltChainBlock> blocks) {
        List<VeriBlockMessages.AltChainBlock> result = new ArrayList<VeriBlockMessages.AltChainBlock>();
        for(AltChainBlock block : blocks)
        {
            result.add(toProto(block));
        }
        return result;
    }
}
