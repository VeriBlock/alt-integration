// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import integration.api.grpc.SerializeServiceGrpc;
import integration.api.grpc.VeriBlockMessages;
import io.grpc.Channel;
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
import org.veriblock.sdk.Address;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BitcoinTransaction;
import org.veriblock.sdk.MerklePath;
import org.veriblock.sdk.Output;
import org.veriblock.sdk.PublicationData;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;

public class VeriBlockSerializeProtoClient implements IVeriBlockSerialize {

    private final SerializeServiceGrpc.SerializeServiceBlockingStub service;

    public VeriBlockSerializeProtoClient(Channel channel) {
        service = SerializeServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public byte[] serializeAltPublication(AltPublication request) {
        return service.serializeAltPublication(AltPublicationProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializePublicationData(PublicationData request) {
        return service.serializePublicationData(PublicationDataProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeBitcoinTransaction(BitcoinTransaction request) {
        return service.serializeBitcoinTransaction(BitcoinTransactionProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeVeriBlockBlock(VeriBlockBlock request) {
        return service.serializeVeriBlockBlock(VeriBlockBlockProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeVeriBlockTransaction(VeriBlockTransaction request) {
        return service.serializeVeriBlockTransaction(VeriBlockTransactionProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeVeriBlockPublication(VeriBlockPublication request) {
        return service.serializeVeriBlockPublication(VeriBlockPublicationProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeVeriBlockPopTx(VeriBlockPoPTransaction request) {
        return service.serializeVeriBlockPopTx(VeriBlockPoPTransactionProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeOutput(Output request) {
        return service.serializeOutput(OutputsProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeAddress(Address request) {
        return service.serializeAddress(AddressProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeBitcoinBlock(BitcoinBlock request) {
        return service.serializeBitcoinBlock(BitcoinBlockProtoConverter.toProto(request)).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeVeriBlockMerklePath(VeriBlockMerklePath request) {
        VeriBlockMessages.VeriBlockMerklePath veriBlockMerklePath = VeriBlockMerklePathProtoConverter.toProto(request);
        return service.serializeVeriBlockMerklePath(veriBlockMerklePath).getData().toByteArray();
    }
    
    @Override
    public byte[] serializeMerklePath(MerklePath request) {
        VeriBlockMessages.MerklePath vMerklePath = MerklePathProtoConverter.toProto(request);
        return service.serializeMerklePath(vMerklePath).getData().toByteArray();
    }

}
