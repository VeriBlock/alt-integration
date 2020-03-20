// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.auditor.Change;
import org.veriblock.sdk.auditor.Changeset;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class VeriBlockBlockchainTest {
    private VeriBlockBlockchain blockchain;
    private BlockStore<StoredVeriBlockBlock, VBlakeHash> store;
    private VeriBlockSecurity veriBlockSecurity;

    private static final byte[] raw1 =  Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
    private static final VeriBlockBlock block1 = SerializeDeserializeService.parseVeriBlockBlock(raw1);

    private static final byte[] raw2 = Utils.decodeHex("000199840002A69BF9FE9B06E641B61699A9654A16FCCE708F3F0DEED25E1D2513D05A3B7D7F80EB5E94D01C6B3796DDE5647F135DB7F1DD040098960EA12045");
    private static final VeriBlockBlock block2 = SerializeDeserializeService.parseVeriBlockBlock(raw2);

    private static final byte[] raw3 = Utils.decodeHex("000199850002461DB458CD6258D3571D4A2A654A16FCCE708F3F0DEED25E1D2513D05A3BB0B8A658CBFFCFBE9185AFDE789841EC5DB7F2360400989610B1662B");
    private static final VeriBlockBlock block3 = SerializeDeserializeService.parseVeriBlockBlock(raw3);

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        store = veriBlockSecurity.getContext().getVeriblockStore();
        store.clear();
        
        BlockStore<StoredBitcoinBlock, Sha256Hash> btcStore = veriBlockSecurity.getContext().getBitcoinStore();
        btcStore.clear();
        
        blockchain = new VeriBlockBlockchain(veriBlockSecurity.getContext().getVeriBlockNetworkParameters(), store, btcStore);
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
        boolean bootstrapped = blockchain.bootstrap(Arrays.asList(block1, block2));
        Assert.assertTrue(bootstrapped);

        blockchain.add(block3);

        Assert.assertEquals(store.get(block1.getHash()).getBlock(), block1);
        Assert.assertEquals(store.get(block2.getHash()).getBlock(), block2);
        Assert.assertEquals(store.get(block3.getHash()).getBlock(), block3);

        Assert.assertEquals(blockchain.getChainHead(), block3);
    }

    @Test
    public void doubleBootstrapTest() throws SQLException, IOException {
        boolean bootstrapped = blockchain.bootstrap(Arrays.asList(block1, block2, block3));
        Assert.assertTrue(bootstrapped);

        bootstrapped = blockchain.bootstrap(Arrays.asList(block1));
        Assert.assertFalse(bootstrapped);
    }

    @Test
    public void bootstrapNonContiguousTest() throws SQLException, IOException {
        try {
            blockchain.bootstrap(Arrays.asList(block1, block3));
            Assert.fail("Expected VerificationException");
        } catch(VerificationException e) {
            Assert.assertEquals(e.getMessage(), "VeriBlock bootstrap blocks must be contiguous");
        }
    }
}
