// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.rewards.PopRewardOutput;

import java.util.ArrayList;
import java.util.List;

public final class RewardOutputProtoConverter {

    private RewardOutputProtoConverter() {} //never
    
    public static PopRewardOutput fromProto(VeriBlockMessages.RewardOutput protoData) {
        PopRewardOutput result = new PopRewardOutput(protoData.getPayoutInfo().toByteArray(), Long.parseUnsignedLong(protoData.getReward()));
        return result;
    }
    
    public static List<PopRewardOutput> fromProto(List<VeriBlockMessages.RewardOutput> protoData) {
        List<PopRewardOutput> result = new ArrayList<PopRewardOutput>();
        for(VeriBlockMessages.RewardOutput output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.RewardOutput toProto(PopRewardOutput data) {
        VeriBlockMessages.RewardOutput.Builder result = VeriBlockMessages.RewardOutput.newBuilder();
        result = result.setPayoutInfo(ByteString.copyFrom(data.getPopMinerPayoutInfo()))
                .setReward(Long.toString(data.getReward()));
        return result.build();
    }
    
    public static List<VeriBlockMessages.RewardOutput> toProto(List<PopRewardOutput> data) {
        List<VeriBlockMessages.RewardOutput> result = new ArrayList<>();
        for(PopRewardOutput output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
