// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import org.veriblock.sdk.BitcoinTransaction;

import com.google.protobuf.ByteString;

import integration.api.grpc.VeriBlockMessages;

public final class BitcoinTransactionProtoConverter {

    private BitcoinTransactionProtoConverter() {} //never
    
    public static BitcoinTransaction fromProto(VeriBlockMessages.BitcoinTransaction protoData) {
        BitcoinTransaction result = new BitcoinTransaction(protoData.getRaw().toByteArray());
        return result;
    }
    
    public static VeriBlockMessages.BitcoinTransaction toProto(BitcoinTransaction data) {
        VeriBlockMessages.BitcoinTransaction.Builder result = VeriBlockMessages.BitcoinTransaction.newBuilder();
        result = result.setRaw(ByteString.copyFrom(data.getRawBytes()));
        return result.build();
    }
}
