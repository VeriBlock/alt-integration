// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.VeriBlockMerklePath;

public final class VeriBlockMerklePathProtoConverter {

    private VeriBlockMerklePathProtoConverter() {} //never
    
    public static VeriBlockMerklePath fromProto(VeriBlockMessages.VeriBlockMerklePath protoData) {
        VeriBlockMerklePath result = new VeriBlockMerklePath(protoData.getMerklePath());
        return result;
    }
    
    public static VeriBlockMerklePath fromProto(String protoData) {
        VeriBlockMerklePath result = new VeriBlockMerklePath(protoData);
        return result;
    }
    
    public static VeriBlockMessages.VeriBlockMerklePath toProto(VeriBlockMerklePath data) {
        VeriBlockMessages.VeriBlockMerklePath.Builder result = VeriBlockMessages.VeriBlockMerklePath.newBuilder();
        result = result.setMerklePath(data.toCompactString());
        return result.build();
    }
}
