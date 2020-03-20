// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import integration.api.grpc.VeriBlockMessages.GeneralReply;

import org.veriblock.protoconverters.AddressProtoConverter;
import org.veriblock.protoconverters.AltPublicationProtoConverter;
import org.veriblock.protoconverters.BitcoinBlockProtoConverter;
import org.veriblock.protoconverters.BitcoinTransactionProtoConverter;
import org.veriblock.protoconverters.MerklePathProtoConverter;
import org.veriblock.protoconverters.OutputsProtoConverter;
import org.veriblock.protoconverters.PublicationDataProtoConverter;
import org.veriblock.protoconverters.VeriBlockBlockProtoConverter;
import org.veriblock.protoconverters.VeriBlockMerklePathProtoConverter;
import org.veriblock.protoconverters.VeriBlockPoPTransactionProtoConverter;
import org.veriblock.protoconverters.VeriBlockPublicationProtoConverter;
import org.veriblock.protoconverters.VeriBlockTransactionProtoConverter;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class VeriBlockSerializeProtoService {

    public static VeriBlockMessages.BytesArrayReply serializeAltPublication(VeriBlockMessages.AltPublication request){
        byte[] array = SerializeDeserializeService.serialize(AltPublicationProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializePublicationData(VeriBlockMessages.PublicationData request){
        byte[] array = SerializeDeserializeService.serialize(PublicationDataProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeBitcoinTransaction(VeriBlockMessages.BitcoinTransaction request){
        byte[] array = SerializeDeserializeService.serialize(BitcoinTransactionProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeVeriBlockBlock(VeriBlockMessages.VeriBlockBlock request){
        byte[] array = SerializeDeserializeService.serialize(VeriBlockBlockProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeVeriBlockTransaction(VeriBlockMessages.VeriBlockTransaction request){
        byte[] array = SerializeDeserializeService.serialize(VeriBlockTransactionProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeVeriBlockPublication(VeriBlockMessages.VeriBlockPublication request){
        byte[] array = SerializeDeserializeService.serialize(VeriBlockPublicationProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeVeriBlockPopTx(VeriBlockMessages.VeriBlockPoPTransaction request){
        byte[] array = SerializeDeserializeService.serialize(VeriBlockPoPTransactionProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeOutput(VeriBlockMessages.Output request){
        byte[] array = SerializeDeserializeService.serialize(OutputsProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeAddress(VeriBlockMessages.Address request){
        byte[] array = SerializeDeserializeService.serialize(AddressProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeBitcoinBlock(VeriBlockMessages.BitcoinBlock request){
        byte[] array = SerializeDeserializeService.serialize(BitcoinBlockProtoConverter.fromProto(request));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeVeriBlockMerklePath(VeriBlockMessages.VeriBlockMerklePath request){
        byte[] array = SerializeDeserializeService.serialize(VeriBlockMerklePathProtoConverter.fromProto(request.getMerklePath()));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }
    public static VeriBlockMessages.BytesArrayReply serializeMerklePath(VeriBlockMessages.MerklePath request){
        byte[] array = SerializeDeserializeService.serialize(MerklePathProtoConverter.fromProto(request.getMerklePath()));
        return VeriBlockMessages.BytesArrayReply.newBuilder()
                .setData(ByteString.copyFrom(array))
                .setResult(GeneralReply.newBuilder().setResult(true))
                .build();
    }

}
