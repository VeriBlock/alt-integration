// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import java.util.List;

import org.veriblock.sdk.*;

import com.google.protobuf.ByteString;

import integration.api.grpc.VeriBlockMessages;

public final class VeriBlockTransactionProtoConverter {

  private VeriBlockTransactionProtoConverter() {} //never

  public static VeriBlockTransaction fromProto(VeriBlockMessages.VeriBlockTransaction protoData) {
    byte type = (byte) protoData.getType();
    Address sourceAddress = AddressProtoConverter.fromProto(protoData.getSourceAddress());
    Coin sourceAmount = CoinProtoConverter.fromProto(protoData.getSourceAmount());
    List<Output> outputs = OutputsProtoConverter.fromProto(protoData.getOutputsList());
    long signatureIndex = protoData.getSignatureIndex();
    PublicationData publicationData = PublicationDataProtoConverter.fromProto(protoData.getPublicationData());
    byte[] signature = protoData.getSignature().toByteArray();
    byte[] publicKey = protoData.getPublicKey().toByteArray();
    Byte networkByte = NetworkByteConverter.fromProto(protoData.getNetworkByte());

    VeriBlockTransaction result = new VeriBlockTransaction(type, sourceAddress, sourceAmount, outputs, signatureIndex, publicationData,
        signature, publicKey, networkByte);
    return result;
  }

  public static VeriBlockMessages.VeriBlockTransaction toProto(VeriBlockTransaction data) {
      byte type = data.getType();
      VeriBlockMessages.Address sourceAddress = AddressProtoConverter.toProto(data.getSourceAddress());
      VeriBlockMessages.Coin sourceAmount = CoinProtoConverter.toProto(data.getSourceAmount());
      List<VeriBlockMessages.Output> outputs = OutputsProtoConverter.toProto(data.getOutputs());
      long signatureIndex = data.getSignatureIndex();
      VeriBlockMessages.PublicationData publicationData = PublicationDataProtoConverter.toProto(data.getPublicationData());
      byte[] signature = data.getSignature();
      byte[] publicKey = data.getPublicKey();
      VeriBlockMessages.NetworkByte networkByte = NetworkByteConverter.toProto(data.getNetworkByte());
  
      VeriBlockMessages.VeriBlockTransaction.Builder result = VeriBlockMessages.VeriBlockTransaction.newBuilder();
      result = result.setType(type)
          .setSourceAddress(sourceAddress)
          .setSourceAmount(sourceAmount)
          .addAllOutputs(outputs)
          .setSignatureIndex(signatureIndex)
          .setPublicationData(publicationData)
          .setSignature(ByteString.copyFrom(signature))
          .setPublicKey(ByteString.copyFrom(publicKey))
          .setNetworkByte(networkByte);
      return result.build();
  }
}
