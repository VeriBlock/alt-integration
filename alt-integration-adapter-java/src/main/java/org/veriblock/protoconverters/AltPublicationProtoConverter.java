// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockTransaction;

import java.util.ArrayList;
import java.util.List;

public final class AltPublicationProtoConverter {

    private AltPublicationProtoConverter() {} //never

    public static AltPublication fromProto(VeriBlockMessages.AltPublication protoData) {
        VeriBlockTransaction transaction = VeriBlockTransactionProtoConverter.fromProto(protoData.getTransaction());
        VeriBlockMerklePath merklePath = VeriBlockMerklePathProtoConverter.fromProto(protoData.getMerklePath());
        VeriBlockBlock containingBlock = VeriBlockBlockProtoConverter.fromProto(protoData.getContainingBlock());
        List<VeriBlockBlock> context = VeriBlockBlockProtoConverter.fromProto(protoData.getContextList());
        AltPublication result = new AltPublication(transaction, merklePath, containingBlock, context);
        return result;
    }
    
    public static List<AltPublication> fromProto(List<VeriBlockMessages.AltPublication> protoData) {
        List<AltPublication> result = new ArrayList<AltPublication>();
        for(VeriBlockMessages.AltPublication output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.AltPublication toProto(AltPublication data) {
        VeriBlockMessages.VeriBlockTransaction transaction = VeriBlockTransactionProtoConverter.toProto(data.getTransaction());
        String merklePath = data.getMerklePath().toCompactString();
        VeriBlockMessages.VeriBlockBlock containingBlock = VeriBlockBlockProtoConverter.toProto(data.getContainingBlock());
        List<VeriBlockMessages.VeriBlockBlock> context = VeriBlockBlockProtoConverter.toProto(data.getContext());
        VeriBlockMessages.AltPublication.Builder result = VeriBlockMessages.AltPublication.newBuilder();
        result = result.setTransaction(transaction)
                .setMerklePath(merklePath)
                .setContainingBlock(containingBlock)
                .addAllContext(context);
        return result.build();
    }
    
    public static List<VeriBlockMessages.AltPublication> toProto(List<AltPublication> data) {
        List<VeriBlockMessages.AltPublication> result = new ArrayList<>();
        for(AltPublication output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
