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

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

public class VeriBlockBlockchain extends org.veriblock.sdk.blockchain.VeriBlockBlockchain {
    private final Map<VBlakeHash, VeriBlockBlockData> blockDataStore = new HashMap<>();

    VeriBlockBlockchain(VeriBlockNetworkParameters networkParameters,
                        BlockStore<StoredVeriBlockBlock, VBlakeHash> store,
                        BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStore) {
        super(networkParameters, store, bitcoinStore);
    }

    public VeriBlockBlock mine(VeriBlockBlockData blockData) throws SQLException {
        VeriBlockBlock chainHead = getChainHead();

        VeriBlockBlock newBlock = new VeriBlockBlock(chainHead.getHeight() + 1,
                                                     chainHead.getVersion(),
                                                     chainHead.getHash().trimToPreviousBlockSize(),
                                                     // FIXME: set the keystones correctly
                                                     VBlakeHash.EMPTY_HASH.trimToPreviousKeystoneSize(),
                                                     VBlakeHash.EMPTY_HASH.trimToPreviousKeystoneSize(),
                                                     blockData.getMerkleRoot(),
                                                     chainHead.getTimestamp() + 1,
                                                     // FIXME: use the difficulty calculator to set the correct difficulty
                                                     chainHead.getDifficulty(),
                                                     0);
        add(newBlock);
        blockDataStore.put(newBlock.getHash(), blockData);

        return newBlock;
    }
}
