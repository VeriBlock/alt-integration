// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import org.veriblock.sdk.models.VBlakeHash;

import java.util.ArrayList;
import java.util.List;

public final class VBlakeHashProtoConverter {

    private VBlakeHashProtoConverter() {} //never
    
    public static List<VBlakeHash> fromProto(List<ByteString> protoData) {
        ArrayList<VBlakeHash> result = new ArrayList<>(protoData.size());

        for (ByteString hash : protoData)
            result.add(VBlakeHash.wrap(hash.toByteArray()));
                
        return result;
    }

    public static List<ByteString> toProto(List<VBlakeHash> data) {
        ArrayList<ByteString> result = new ArrayList<>(data.size());

        for (VBlakeHash hash : data)
            result.add(ByteString.copyFrom(hash.getBytes()));
                
        return result;
    }
}
