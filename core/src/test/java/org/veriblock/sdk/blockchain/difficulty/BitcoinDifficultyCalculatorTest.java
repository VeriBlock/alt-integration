// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.difficulty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.blockchain.BitcoinBlockchain;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitcoinDifficultyCalculatorTest {
    private BlockStore<StoredBitcoinBlock, Sha256Hash> store;
    private VeriBlockSecurity veriBlockSecurity;

    private static final BitcoinNetworkParameters bitcoinRegtestNetworkParameters = new BitcoinNetworkParameters() {
                public BigInteger getPowLimit() {
                    return new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
                }
                public int getPowTargetTimespan() {
                    return 1209600;
                }
                public int getPowTargetSpacing() {
                    return 600;
                }
                public boolean getAllowMinDifficultyBlocks() {
                    return true;
                }
                public boolean getPowNoRetargeting() {
                    return true;
                }
            };

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        store = Context.getBitcoinStore();
        store.clear();
    }

    @After
    public void teardown() throws SQLException {
        veriBlockSecurity.shutdown();
    }

    public static List<BitcoinBlock> toBlocks(List<String> headers) {
        List<BitcoinBlock> blocks = new ArrayList<BitcoinBlock>(headers.size());
        for (String header : headers) {
            blocks.add(SerializeDeserializeService.parseBitcoinBlock(Utils.decodeHex(header)));
        }
        return blocks;
    }
    
    public static void addBlockDataToBlockchain(List<String> headers, int firstBlockHeight,
                                                BitcoinBlockchain blockchain) throws SQLException {
        List<BitcoinBlock> blocks = toBlocks(headers);
        blockchain.bootstrap(Arrays.asList(blocks.get(0)), firstBlockHeight);
        
        for(BitcoinBlock block : blocks) {
            blockchain.add(block);
        }
    }

    @Test
    public void RegtestTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(bitcoinRegtestNetworkParameters, store);
        addBlockDataToBlockchain(BitcoinRegtestBlockData.headers,
                                 BitcoinRegtestBlockData.firstBlockHeight,
                                 blockchain);
    }
    
    // FIXME: test that the regtest config fails on testnet and mainnet data

}
