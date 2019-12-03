// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;

import java.util.ArrayList;
import java.util.List;

public final class VeriBlockPublicationProtoConverter {

    private VeriBlockPublicationProtoConverter() {} //never

    public static VeriBlockPublication fromProto(VeriBlockMessages.VeriBlockPublication protoData) {
        VeriBlockPoPTransaction transaction = VeriBlockPoPTransactionProtoConverter.fromProto(protoData.getTransaction());
        VeriBlockMerklePath merklePath = VeriBlockMerklePathProtoConverter.fromProto(protoData.getMerklePath());
        VeriBlockBlock containingBlock = VeriBlockBlockProtoConverter.fromProto(protoData.getContainingBlock());
        List<VeriBlockBlock> context = VeriBlockBlockProtoConverter.fromProto(protoData.getContextList());
        VeriBlockPublication result = new VeriBlockPublication(transaction, merklePath, containingBlock, context);
        return result;
    }
    
    public static List<VeriBlockPublication> fromProto(List<VeriBlockMessages.VeriBlockPublication> protoData) {
        List<VeriBlockPublication> result = new ArrayList<VeriBlockPublication>();
        for(VeriBlockMessages.VeriBlockPublication output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.VeriBlockPublication toProto(VeriBlockPublication data) {
        VeriBlockMessages.VeriBlockPoPTransaction transaction = VeriBlockPoPTransactionProtoConverter.toProto(data.getTransaction());
        String merklePath = data.getMerklePath().toCompactString();
        VeriBlockMessages.VeriBlockBlock containingBlock = VeriBlockBlockProtoConverter.toProto(data.getContainingBlock());
        List<VeriBlockMessages.VeriBlockBlock> context = VeriBlockBlockProtoConverter.toProto(data.getContext());
        VeriBlockMessages.VeriBlockPublication.Builder result = VeriBlockMessages.VeriBlockPublication.newBuilder();
        result = result.setTransaction(transaction)
                .setMerklePath(merklePath)
                .setContainingBlock(containingBlock)
                .addAllContext(context);
        return result.build();
    }
    
    public static List<VeriBlockMessages.VeriBlockPublication> toProto(List<VeriBlockPublication> data) {
        List<VeriBlockMessages.VeriBlockPublication> result = new ArrayList<>();
        for(VeriBlockPublication output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
