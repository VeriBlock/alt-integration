// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain;

import org.junit.*;

import org.veriblock.integrations.auditor.BlockIdentifier;
import org.veriblock.integrations.auditor.Change;
import org.veriblock.integrations.auditor.Changeset;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BitcoinBlockchainTest {
    private BitcoinBlockchain blockchain;
    private BitcoinStore store;

    @Before
    public void init() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager.init();

        store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();
        store.clear();
        
        blockchain = new BitcoinBlockchain(store);
    }

    @After
    public void teardown() throws SQLException {
        VeriBlockIntegrationLibraryManager.shutdown();
    }

    @Test
    public void rewindTest() throws SQLException, IOException {
        BitcoinBlock block  = new BitcoinBlock(766099456,
                                    Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
                                    Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
                                    1572336145, 0x17148edf, 790109764);
        Assert.assertEquals(block.getHash(),
                            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"));

        BitcoinBlock block2 = new BitcoinBlock(1073733632,
                                    Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
                                    Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
                                    1572336708, 0x17148edf, 344118374);
        Assert.assertEquals(block2.getHash(),
                            Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"));
    
        BitcoinBlock block3 = new BitcoinBlock(536870912,
                                    Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"),
                                    Sha256Hash.wrap("2dfad61070eeea30ee035cc58ac20a325292802f9445851d14f23b4e71ddee61"),1572337243, 0x17148edf, 2111493782);
        Assert.assertEquals(block3.getHash(),
                            Sha256Hash.wrap("0000000000000000000e008052ab86a7b0c20e46b29c54658b066d471022503f"));

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
