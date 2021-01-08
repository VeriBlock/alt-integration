// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.models.PublicationData;

public class PublicationDataProtoConverter {

    public static PublicationData fromProto(VeriBlockMessages.PublicationData data){
        return new PublicationData(data.getIdentifier(),
                data.getHeader().toByteArray(),
                data.getPayoutInfo().toByteArray(),
                data.getVeriblockContext().toByteArray());
    }



    public static VeriBlockMessages.PublicationData toProto(PublicationData data) {
        return VeriBlockMessages.PublicationData.newBuilder()
                .setIdentifier(data.getIdentifier())
                .setHeader(ByteString.copyFrom(data.getHeader()))
                .setPayoutInfo(ByteString.copyFrom(data.getPayoutInfo()))
                .setVeriblockContext(ByteString.copyFrom(data.getContextInfo()))
                .build();
    }
}
