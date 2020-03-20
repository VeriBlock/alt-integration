// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.BlockIndex;

public final class BlockIndexProtoConverter {

    private BlockIndexProtoConverter() {} //never
    
    public static BlockIndex fromProto(VeriBlockMessages.BlockIndex protoData) {
        BlockIndex result = new BlockIndex(protoData.getHeight(), protoData.getHash());
        return result;
    }
    
    public static VeriBlockMessages.BlockIndex toProto(BlockIndex data) {
        VeriBlockMessages.BlockIndex.Builder result = VeriBlockMessages.BlockIndex.newBuilder();
        result = result
                .setHeight(data.getHeight())
                .setHash(data.getHash());
        return result.build();
    }
}
