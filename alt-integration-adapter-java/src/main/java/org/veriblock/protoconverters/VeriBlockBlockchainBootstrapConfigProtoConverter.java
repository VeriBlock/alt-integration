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
import org.veriblock.sdk.blockchain.VeriBlockBlockchainBootstrapConfig;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class VeriBlockBlockchainBootstrapConfigProtoConverter {

    private VeriBlockBlockchainBootstrapConfigProtoConverter () {} // never

    public static VeriBlockBlockchainBootstrapConfig fromProto(VeriBlockMessages.VeriBlockBootstrapConfig protoData) {
        VeriBlockBlockchainBootstrapConfig config = new VeriBlockBlockchainBootstrapConfig();

        for (ByteString block : protoData.getBlocksList())
            config.blocks.add(SerializeDeserializeService.parseVeriBlockBlock(block.toByteArray()));
            
        return config;
    }

    public static VeriBlockMessages.VeriBlockBootstrapConfig toProto(VeriBlockBlockchainBootstrapConfig config) {
        VeriBlockMessages.VeriBlockBootstrapConfig.Builder result = VeriBlockMessages.VeriBlockBootstrapConfig.newBuilder();

        for (VeriBlockBlock block : config.blocks) {
            result.addBlocks(ByteString.copyFrom(block.getRaw()));
        }

        return result.build();
    }
}
