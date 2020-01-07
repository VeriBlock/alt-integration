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
import org.veriblock.sdk.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.BitcoinUtils;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;

public class VeriBlockBlockchain extends org.veriblock.sdk.blockchain.VeriBlockBlockchain {
    private final Map<VBlakeHash, VeriBlockBlockData> blockDataStore = new HashMap<>();
    private final BlockStore<StoredVeriBlockBlock, VBlakeHash> store;

    VeriBlockBlockchain(VeriBlockNetworkParameters networkParameters,
                        BlockStore<StoredVeriBlockBlock, VBlakeHash> store,
                        BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStore) {
        super(networkParameters, store, bitcoinStore);
        this.store = store;
    }

    private VBlakeHash getPreviousKeystoneForNewBlock() throws SQLException {
        VeriBlockBlock chainHead = getChainHead();
        int blockHeight = chainHead.getHeight() + 1;

        int keystoneBlocksAgo = blockHeight % 20;
        switch (keystoneBlocksAgo) {
            case 0:
                keystoneBlocksAgo = 20;
                break;
            case 1:
                keystoneBlocksAgo = 21;
        }

        List<StoredVeriBlockBlock> context = store.get(chainHead.getHash(), keystoneBlocksAgo);
        return context.size() == keystoneBlocksAgo
             ? context.get(keystoneBlocksAgo - 1).getBlock().getHash().trimToPreviousKeystoneSize()
             : VBlakeHash.EMPTY_HASH.trimToPreviousKeystoneSize();
    }

    private VBlakeHash getSecondPreviousKeystoneForNewBlock() throws SQLException {
        VeriBlockBlock chainHead = getChainHead();
        int blockHeight = chainHead.getHeight() + 1;

        int keystoneBlocksAgo = blockHeight % 20;
        switch (keystoneBlocksAgo) {
            case 0:
                keystoneBlocksAgo = 20;
                break;
            case 1:
                keystoneBlocksAgo = 21;
        }

        keystoneBlocksAgo += 20;

        List<StoredVeriBlockBlock> context = store.get(chainHead.getHash(), keystoneBlocksAgo);
        return context.size() == keystoneBlocksAgo
             ? context.get(keystoneBlocksAgo - 1).getBlock().getHash().trimToPreviousKeystoneSize()
             : VBlakeHash.EMPTY_HASH.trimToPreviousKeystoneSize();
    }

    public VeriBlockBlock mine(VeriBlockBlockData blockData) throws SQLException {
        VeriBlockBlock chainHead = getChainHead();
        int blockHeight = chainHead.getHeight() + 1;

        VBlakeHash previousKeystone = getPreviousKeystoneForNewBlock();
        VBlakeHash secondPreviousKeystone = getSecondPreviousKeystoneForNewBlock();

        int timestamp = 0;

        for (int nonce = 0; nonce < Integer.MAX_VALUE; nonce++) {
            try {
                timestamp = Math.max(timestamp, Utils.getCurrentTimestamp());

                VeriBlockBlock newBlock = new VeriBlockBlock(blockHeight,
                                                            chainHead.getVersion(),
                                                            chainHead.getHash().trimToPreviousBlockSize(),
                                                            previousKeystone,
                                                            secondPreviousKeystone,
                                                            blockData.getMerkleRoot(),
                                                            timestamp,
                                                            // FIXME: use the difficulty calculator to set the correct difficulty
                                                            chainHead.getDifficulty(),
                                                            nonce);

                ValidationService.checkProofOfWork(newBlock);

                add(newBlock);
                blockDataStore.put(newBlock.getHash(), blockData);

                return newBlock;

            } catch (VerificationException e) {
                // FIXME: refactoring checkTimestamp() would make this less ugly
                // if too many blocks are mined per second, we have to adjust the timestamp slightly
                if (e.getMessage().equals("Block is too far in the past")) {
                    timestamp++;

                // FIXME: refactoring checkProofOfWork() would make this less ugly
                // suppress this specific exception as it's just a signal
                // that the block hash does not match the block difficulty
                } else if (!e.getMessage().startsWith("Block hash is higher than target")) {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to mine the block due to too high difficulty");
    }
}
