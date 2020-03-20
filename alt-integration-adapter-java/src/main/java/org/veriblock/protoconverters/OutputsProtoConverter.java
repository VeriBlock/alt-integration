// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.Output;

import java.util.ArrayList;
import java.util.List;

public final class OutputsProtoConverter {

    private OutputsProtoConverter() {} //never
    
    public static Output fromProto(VeriBlockMessages.Output protoData) {
        Output result = new Output(AddressProtoConverter.fromProto(protoData.getAddress()), CoinProtoConverter.fromProto(protoData.getAmount()));
        return result;
    }
    
    public static List<Output> fromProto(List<VeriBlockMessages.Output> protoData) {
        List<Output> result = new ArrayList<Output>();
        for(VeriBlockMessages.Output output : protoData) {
            result.add(fromProto(output));
        }
        return result;
    }
    
    public static VeriBlockMessages.Output toProto(Output data) {
        VeriBlockMessages.Output.Builder result = VeriBlockMessages.Output.newBuilder();
        result = result.setAddress(AddressProtoConverter.toProto(data.getAddress()))
                .setAmount(CoinProtoConverter.toProto(data.getAmount()));
        return result.build();
    }
    
    public static List<VeriBlockMessages.Output> toProto(List<Output> data) {
        List<VeriBlockMessages.Output> result = new ArrayList<>();
        for(Output output : data) {
            result.add(toProto(output));
        }
        return result;
    }
}
