// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import org.veriblock.sdk.models.Sha256Hash;

import java.util.ArrayList;
import java.util.List;

public final class Sha256HashProtoConverter {

    private Sha256HashProtoConverter() {} //never
    
    public static List<Sha256Hash> fromProto(List<ByteString> protoData) {
        ArrayList<Sha256Hash> result = new ArrayList<>(protoData.size());

        for (ByteString hash : protoData)
            result.add(Sha256Hash.wrap(hash.toByteArray()));
                
        return result;
    }

    public static List<ByteString> toProto(List<Sha256Hash> data) {
        ArrayList<ByteString> result = new ArrayList<>(data.size());

        for (Sha256Hash hash : data)
            result.add(ByteString.copyFrom(hash.getBytes()));
                
        return result;
    }
}
