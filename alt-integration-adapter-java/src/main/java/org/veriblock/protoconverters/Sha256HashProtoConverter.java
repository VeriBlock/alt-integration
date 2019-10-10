package org.veriblock.protoconverters;

import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.Sha256Hash;

import integration.api.grpc.VeriBlockMessages;

import com.google.protobuf.ByteString;

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
