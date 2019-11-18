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
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;

public class VeriBlockBlockchainTest {
    private VeriBlockBlockchain blockchain;
    private VeriBlockStore store;
    private VeriBlockSecurity veriBlockSecurity;

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        store = Context.getVeriblockStore();
        store.clear();
        
        BitcoinStore btcStore = Context.getBitcoinStore();
        btcStore.clear();
        
        blockchain = new VeriBlockBlockchain(Context.getNetworkParameters(), store, btcStore);
    }

    @After
    public void teardown() throws SQLException {
        veriBlockSecurity.shutdown();
    }

    @Test
    public void rewindTest() throws SQLException, IOException {
        byte[] raw =  Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
        VeriBlockBlock block = SerializeDeserializeService.parseVeriBlockBlock(raw);

        byte[] raw2 = Utils.decodeHex("000199840002A69BF9FE9B06E641B61699A9654A16FCCE708F3F0DEED25E1D2513D05A3B7D7F80EB5E94D01C6B3796DDE5647F135DB7F1DD040098960EA12045");
        VeriBlockBlock block2 = SerializeDeserializeService.parseVeriBlockBlock(raw2);

        byte[] raw3 = Utils.decodeHex("000199850002461DB458CD6258D3571D4A2A654A16FCCE708F3F0DEED25E1D2513D05A3BB0B8A658CBFFCFBE9185AFDE789841EC5DB7F2360400989610B1662B");
        VeriBlockBlock block3 = SerializeDeserializeService.parseVeriBlockBlock(raw3);

        blockchain.add(block);

        Changeset changeset = new Changeset(BlockIdentifier.wrap(block.getHash().getBytes()));
        
        changeset.addChanges(blockchain.add(block2));
        changeset.addChanges(blockchain.add(block3));

        Assert.assertEquals(store.get(block.getHash()).getBlock(), block);
        Assert.assertEquals(store.get(block2.getHash()).getBlock(), block2);
        Assert.assertEquals(store.get(block3.getHash()).getBlock(), block3);

        Iterator<Change> changeIterator = changeset.reverseIterator();
        while (changeIterator.hasNext()) {
            Change change = changeIterator.next();
            blockchain.rewind(Collections.singletonList(change));
        }

        Assert.assertEquals(store.get(block.getHash()).getBlock(), block);
        Assert.assertEquals(store.get(block2.getHash()), null);
        Assert.assertEquals(store.get(block3.getHash()), null);        
    }
}
