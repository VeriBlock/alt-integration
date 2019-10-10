package org.veriblock.protoconverters;

import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.VBlakeHash;

import com.google.protobuf.ByteString;

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
