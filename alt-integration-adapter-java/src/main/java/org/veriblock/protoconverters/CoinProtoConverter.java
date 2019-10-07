// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import org.veriblock.sdk.Coin;

import integration.api.grpc.VeriBlockMessages;

public final class CoinProtoConverter {

    private CoinProtoConverter() {} //never
    
    public static Coin fromProto(VeriBlockMessages.Coin protoData) {
        Coin result = Coin.valueOf(protoData.getAtomicUnits());
        return result;
    }
    
    public static VeriBlockMessages.Coin toProto(Coin data) {
        VeriBlockMessages.Coin.Builder result = VeriBlockMessages.Coin.newBuilder();
        result = result.setAtomicUnits(data.getAtomicUnits());
        return result.build();
    }
}
