// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoconverters;

import com.google.protobuf.ByteString;
import integration.api.grpc.VeriBlockMessages;
import org.veriblock.sdk.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

public class BitcoinBlockchainBootstrapConfigProtoConverter {

    private BitcoinBlockchainBootstrapConfigProtoConverter () {} // never

    public static BitcoinBlockchainBootstrapConfig fromProto(VeriBlockMessages.BitcoinBootstrapConfig protoData) {
        BitcoinBlockchainBootstrapConfig config = new BitcoinBlockchainBootstrapConfig();

        config.firstBlockHeight = protoData.getFirstBlockHeight();

        for (ByteString block : protoData.getBlocksList())
            config.blocks.add(SerializeDeserializeService.parseBitcoinBlock(block.toByteArray()));
            
        return config;
    }

    public static VeriBlockMessages.BitcoinBootstrapConfig toProto(BitcoinBlockchainBootstrapConfig config) {
        VeriBlockMessages.BitcoinBootstrapConfig.Builder result = VeriBlockMessages.BitcoinBootstrapConfig.newBuilder();

        result.setFirstBlockHeight(config.firstBlockHeight);
        for (BitcoinBlock block : config.blocks) {
            result.addBlocks(ByteString.copyFrom(block.getRaw()));
        }

        return result.build();
    }
}
