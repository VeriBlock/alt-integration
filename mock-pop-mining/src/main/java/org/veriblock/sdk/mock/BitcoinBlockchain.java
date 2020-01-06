// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

public class BitcoinBlockchain extends org.veriblock.sdk.blockchain.BitcoinBlockchain {
    private final Map<Sha256Hash, BitcoinBlockData> blockDataStore = new HashMap<>();

    BitcoinBlockchain(BitcoinNetworkParameters networkParameters,
                      BlockStore<StoredBitcoinBlock, Sha256Hash> store) {
        super(networkParameters, store);
    }

    public BitcoinBlock mine(BitcoinBlockData blockData) throws SQLException {
        BitcoinBlock chainHead = getChainHead();

        for (int nonce = 0; nonce < Integer.MAX_VALUE; nonce++) {
            try {
                BitcoinBlock newBlock = new BitcoinBlock(
                    chainHead.getVersion(),
                    chainHead.getHash(),
                    blockData.getMerkleRoot().getReversed(),
                    Utils.getCurrentTimestamp(),
                    // FIXME: remove the hardcoded regtest difficulty adjustment
                    // use the difficulty calculator to set the correct difficulty
                    chainHead.getBits(),
                    nonce);
                                                            
                ValidationService.checkProofOfWork(newBlock);

                add(newBlock);
                blockDataStore.put(newBlock.getHash(), blockData);

                return newBlock; 
            } catch (VerificationException e) {
            }
        }
        throw new RuntimeException("Failed to mine the block due to too high difficulty");
    }
}
