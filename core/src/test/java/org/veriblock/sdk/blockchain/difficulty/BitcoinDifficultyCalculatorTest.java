// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.difficulty;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.blockchain.BitcoinBlockchain;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinTestNetParameters;
import org.veriblock.sdk.conf.BitcoinRegTestParameters;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

public class BitcoinDifficultyCalculatorTest {
    private BlockStore<StoredBitcoinBlock, Sha256Hash> store;
    private VeriBlockSecurity veriBlockSecurity;

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        store = veriBlockSecurity.getContext().getBitcoinStore();
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

    private void assertDifficultyValidationFailure(VerificationException e) {
        Assert.assertTrue(e.getMessage().equals("Block does not match difficulty of previous block")
                       || e.getMessage().equals("Block does not match computed difficulty adjustment"));
    }

    @Test
    public void regtestTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinRegTestParameters(), store);
        addBlockDataToBlockchain(BitcoinRegtestBlockData.headers,
                                 BitcoinRegtestBlockData.firstBlockHeight,
                                 blockchain);
    }

    @Test
    public void testnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinTestNetParameters(), store);
        addBlockDataToBlockchain(BitcoinTestnetBlockData.headers,
                                 BitcoinTestnetBlockData.firstBlockHeight,
                                 blockchain);
    }

    @Test
    public void mainnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinMainNetParameters(), store);
        addBlockDataToBlockchain(BitcoinMainnetBlockData.headers,
                                 BitcoinMainnetBlockData.firstBlockHeight,
                                 blockchain);
    }

    @Test
    public void regtestFailsOnTestnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinRegTestParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinTestnetBlockData.headers,
                                     BitcoinTestnetBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

    @Test
    public void regtestFailsOnMainnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinRegTestParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinMainnetBlockData.headers,
                                     BitcoinMainnetBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

    @Test
    public void mainnetFailsOnTestnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinMainNetParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinTestnetBlockData.headers,
                                     BitcoinTestnetBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

    @Test
    public void mainnetFailsOnRegtestTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinMainNetParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinRegtestBlockData.headers,
                                     BitcoinRegtestBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

    @Test
    public void testnetFailsOnRegtestTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinTestNetParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinRegtestBlockData.headers,
                                     BitcoinRegtestBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

    @Test
    public void testnetFailsOnMainnetTest() throws SQLException, IOException {
        BitcoinBlockchain blockchain = new BitcoinBlockchain(new BitcoinTestNetParameters(), store);
        try {
            addBlockDataToBlockchain(BitcoinMainnetBlockData.headers,
                                     BitcoinMainnetBlockData.firstBlockHeight,
                                     blockchain);
            Assert.fail();
        } catch (VerificationException e) {
            assertDifficultyValidationFailure(e);
        }
    }

}
