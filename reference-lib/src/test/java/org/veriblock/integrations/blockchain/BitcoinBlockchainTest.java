// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.auditor.BlockIdentifier;
import org.veriblock.integrations.auditor.Change;
import org.veriblock.integrations.auditor.Changeset;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VerificationException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class BitcoinBlockchainTest {
    private BitcoinBlockchain blockchain;
    private BitcoinStore store;
    private VeriBlockSecurity veriBlockSecurity;

    private final static BitcoinBlock block1  = new BitcoinBlock(
            766099456,
            Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
            Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
            1572336145, 0x17148edf, 790109764);
    private final static int block1Height = 601491;

    private final static BitcoinBlock block2 = new BitcoinBlock(
            1073733632,
            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
            Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
            1572336708, 0x17148edf, 344118374);
    private final static int block2Height = block1Height + 1;

    private final static BitcoinBlock block3 = new BitcoinBlock(
            536870912,
            Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"),
            Sha256Hash.wrap("2dfad61070eeea30ee035cc58ac20a325292802f9445851d14f23b4e71ddee61"),
            1572337243, 0x17148edf, 2111493782);
    private final static int block3Height = block2Height + 1;

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        store = Context.getBitcoinStore();
        store.clear();
        
        blockchain = new BitcoinBlockchain(Context.getBitcoinNetworkParameters(), store);

        Assert.assertEquals(block1.getHash(),
                            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"));

        Assert.assertEquals(block2.getHash(),
                            Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"));

        Assert.assertEquals(block3.getHash(),
                            Sha256Hash.wrap("0000000000000000000e008052ab86a7b0c20e46b29c54658b066d471022503f"));

    }

    @After
    public void teardown() throws SQLException {
        veriBlockSecurity.shutdown();
    }

    @Test
    public void rewindTest() throws SQLException, IOException {
        blockchain.add(block1);

        Changeset changeset = new Changeset(BlockIdentifier.wrap(block1.getHash().getBytes()));
        
        changeset.addChanges(blockchain.add(block2));
        changeset.addChanges(blockchain.add(block3));

        Assert.assertEquals(store.get(block1.getHash()).getBlock(), block1);
        Assert.assertEquals(store.get(block2.getHash()).getBlock(), block2);
        Assert.assertEquals(store.get(block3.getHash()).getBlock(), block3);

        Iterator<Change> changeIterator = changeset.reverseIterator();
        while (changeIterator.hasNext()) {
            Change change = changeIterator.next();
            blockchain.rewind(Collections.singletonList(change));
        }

        Assert.assertEquals(store.get(block1.getHash()).getBlock(), block1);
        Assert.assertEquals(store.get(block2.getHash()), null);
        Assert.assertEquals(store.get(block3.getHash()), null);
    }

    @Test
    public void bootstrapTest() throws SQLException, IOException {
        boolean bootstrapped = blockchain.bootstrap(Arrays.asList(block1, block2), block1Height);
        Assert.assertTrue(bootstrapped);

        blockchain.add(block3);

        Assert.assertEquals(store.get(block1.getHash()).getBlock(), block1);
        Assert.assertEquals(store.get(block1.getHash()).getHeight(), block1Height);

        Assert.assertEquals(store.get(block2.getHash()).getBlock(), block2);
        Assert.assertEquals(store.get(block2.getHash()).getHeight(), block2Height);

        Assert.assertEquals(store.get(block3.getHash()).getBlock(), block3);
        Assert.assertEquals(store.get(block3.getHash()).getHeight(), block3Height);

        Assert.assertEquals(blockchain.getChainHead(), block3);
    }

    @Test
    public void doubleBootstrapTest() throws SQLException, IOException {
        boolean bootstrapped = blockchain.bootstrap(Arrays.asList(block1, block2, block3), block1Height);
        Assert.assertTrue(bootstrapped);

        bootstrapped = blockchain.bootstrap(Arrays.asList(block1), block1Height);
        Assert.assertFalse(bootstrapped);
    }

    @Test
    public void bootstrapNonContiguousTest() throws SQLException, IOException {
        try {
            blockchain.bootstrap(Arrays.asList(block1, block3), block1Height);
            Assert.fail("Expected VerificationException");
        } catch(VerificationException e) {
            Assert.assertEquals(e.getMessage(), "Bitcoin bootstrap blocks must be contiguous");
        }
    }
}
