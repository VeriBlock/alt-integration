// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import integration.api.grpc.VeriBlockMessages;

public final class NetworkByteConverter {

    private NetworkByteConverter() {} //never

    public static Byte fromProto(VeriBlockMessages.NetworkByte protoData) {
        Byte networkByte = null;
        if(protoData.getByteExists()) {
            networkByte = (byte) protoData.getNetworkByte();
        }
        return networkByte;
    }

    public static VeriBlockMessages.NetworkByte toProto(Byte data) {
        VeriBlockMessages.NetworkByte.Builder result = VeriBlockMessages.NetworkByte.newBuilder();
        result.setByteExists(false);
        
        if(data != null) {
            result.setByteExists(true);
            result.setNetworkByte(data);
        }
        return result.build();
    }
}
