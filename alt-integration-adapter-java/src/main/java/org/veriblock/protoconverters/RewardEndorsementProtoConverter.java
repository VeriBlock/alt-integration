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

import org.veriblock.integrations.rewards.PopEndorsement;
import org.veriblock.integrations.rewards.PopRewardEndorsements;

import integration.api.grpc.VeriBlockMessages;

public final class RewardEndorsementProtoConverter {

    private RewardEndorsementProtoConverter() {} //never
    
    public static PopRewardEndorsements fromProto(PopRewardEndorsements input, VeriBlockMessages.RewardEndorsement protoData) {
        for(VeriBlockMessages.PopEndorsement e : protoData.getEndorsementsInBlockList()) {
            PopEndorsement endorsement = new PopEndorsement(e.getAddress(), e.getTxid());
            input.addEndorsement(protoData.getBlockVbkHeight(), endorsement);
        }
        return input;
    }
    
    public static PopRewardEndorsements fromProto(VeriBlockMessages.RewardEndorsement protoData) {
        return fromProto(new PopRewardEndorsements(), protoData);
    }
    
    public static PopRewardEndorsements fromProto(List<VeriBlockMessages.RewardEndorsement> protoData) {
        PopRewardEndorsements result = new PopRewardEndorsements();
        for(VeriBlockMessages.RewardEndorsement r : protoData) {
            result = fromProto(result, r);
        }
        return result;
    }
    
    public static VeriBlockMessages.RewardEndorsement toProto(int vbkHeight, List<PopEndorsement> data) {
        List<VeriBlockMessages.PopEndorsement> endorsementsProto = new ArrayList<>();
        for(PopEndorsement e : data) {
            VeriBlockMessages.PopEndorsement endorsementProto = VeriBlockMessages.PopEndorsement.newBuilder()
                    .setAddress(e.getMiner())
                    .setTxid(e.getTxid())
                    .build();
            endorsementsProto.add(endorsementProto);
        }
        
        VeriBlockMessages.RewardEndorsement.Builder result = VeriBlockMessages.RewardEndorsement.newBuilder();
        result = result
                .setBlockVbkHeight(vbkHeight)
                .addAllEndorsementsInBlock(endorsementsProto);
        return result.build();
    }
    
    public static List<VeriBlockMessages.RewardEndorsement> toProto(PopRewardEndorsements data) {
        List<VeriBlockMessages.RewardEndorsement> endorsementsProto = new ArrayList<>();
        for(int key : data.getBlocksWithEndorsements().keySet()) {
            VeriBlockMessages.RewardEndorsement endorsement = toProto(key, data.getBlocksWithEndorsements().get(key));
            endorsementsProto.add(endorsement);
        }
        return endorsementsProto;
    }
}
