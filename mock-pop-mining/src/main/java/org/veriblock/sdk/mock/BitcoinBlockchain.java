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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitcoinBlockchain extends org.veriblock.sdk.blockchain.BitcoinBlockchain {
    private final Map<Sha256Hash, BitcoinBlockData> blockDataStore = new HashMap<>();

    public BitcoinBlockchain(BitcoinNetworkParameters networkParameters,
                      BlockStore<StoredBitcoinBlock, Sha256Hash> store) {
        super(networkParameters, store);
    }


    // retrieve the blocks between lastKnownBlock and getChainHead()
    public List<BitcoinBlock> getContext(BitcoinBlock lastKnownBlock) throws SQLException {
        List<BitcoinBlock> context = new ArrayList<>();

        BitcoinBlock prevBlock = get(getChainHead().getPreviousBlock());
        while (prevBlock != null && !prevBlock.equals(lastKnownBlock)) {
            context.add(prevBlock);
            prevBlock = get(prevBlock.getPreviousBlock());
        }

        Collections.reverse(context);
        return context;
    }

    public BitcoinBlock mine(BitcoinBlockData blockData) throws SQLException {
        StoredBitcoinBlock chainHead = getStoredChainHead();

        int timestamp = Math.max(getNextEarliestTimestamp(chainHead.getHash()).orElse(0), Utils.getCurrentTimestamp());
        int difficulty = (int) getNextDifficulty(timestamp, chainHead).getAsLong();

        for (int nonce = 0; nonce < Integer.MAX_VALUE; nonce++) {
            try {

                BitcoinBlock newBlock = new BitcoinBlock(
                    chainHead.getBlock().getVersion(),
                    chainHead.getHash(),
                    blockData.getMerkleRoot().getReversed(),
                    timestamp,
                    difficulty,
                    nonce);
                                                            
                ValidationService.checkProofOfWork(newBlock);

                add(newBlock);
                blockDataStore.put(newBlock.getHash(), blockData);

                return newBlock; 
            } catch (VerificationException e) {
                // FIXME: refactoring checkProofOfWork() would make this less ugly
                // suppress this specific exception as it's just a signal
                // that the block hash does not match the block difficulty
                if (!e.getMessage().startsWith("Block hash is higher than target")) {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to mine the block due to too high difficulty");
    }
}
