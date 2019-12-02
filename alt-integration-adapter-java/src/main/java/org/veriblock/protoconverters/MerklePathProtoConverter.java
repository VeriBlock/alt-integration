// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.MerklePath;

public final class MerklePathProtoConverter {

    private MerklePathProtoConverter() {} //never
    
    public static MerklePath fromProto(VeriBlockMessages.MerklePath protoData) {
        MerklePath result = new MerklePath(protoData.getMerklePath());
        return result;
    }
    
    public static MerklePath fromProto(String protoData) {
        MerklePath result = new MerklePath(protoData);
        return result;
    }
    
    public static VeriBlockMessages.MerklePath toProto(MerklePath data) {
        VeriBlockMessages.MerklePath.Builder result = VeriBlockMessages.MerklePath.newBuilder();
        result = result.setMerklePath(data.toCompactString());
        return result.build();
    }
}
