// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.store;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class BitcoinStoreTest {

    @Test
    public void EraseCantSplitBlockchainTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();
            BitcoinBlock block1  = new BitcoinBlock(766099456,
                                        Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
                                        Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
                                        1572336145, 0x17148edf, 790109764);
            Assert.assertEquals(block1.getHash(),
                                Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"));

            StoredBitcoinBlock storedBlock1 = new StoredBitcoinBlock(block1, BigInteger.TEN, 0);

            BitcoinBlock block2 = new BitcoinBlock(1073733632,
                                        Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
                                        Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
                                        1572336708, 0x17148edf, 344118374);
            Assert.assertEquals(block2.getHash(),
                                Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"));
    
            StoredBitcoinBlock storedBlock2 = new StoredBitcoinBlock(block2, BigInteger.ONE, 0);

            store.put(storedBlock2);
            store.put(storedBlock1);

            try {
                store.erase(block1.getHash());
                Assert.fail("Expected BlockStoreException");
            } catch(BlockStoreException e) {
                Assert.assertTrue(e.getMessage().startsWith("Cannot erase a block"));
            }

            Assert.assertEquals(store.erase(block2.getHash()), storedBlock2);
            Assert.assertEquals(store.erase(block1.getHash()), storedBlock1);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void nonexistingBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash("123".getBytes()));
            StoredBitcoinBlock storedBitcoinBlock = store.get(hash);
            Assert.assertTrue(storedBitcoinBlock == null);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void bitcoinBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock storedBitcoinBlockExpected = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
            store.put(storedBitcoinBlockExpected);
            StoredBitcoinBlock storedBitcoinBlockActual = store.get(storedBitcoinBlockExpected.getHash());

            Assert.assertEquals(storedBitcoinBlockExpected, storedBitcoinBlockActual);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void chainHeadStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock storedBitcoinBlockExpected = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
            store.put(storedBitcoinBlockExpected);
            StoredBitcoinBlock replacedBlock = store.setChainHead(storedBitcoinBlockExpected);
            Assert.assertEquals(replacedBlock, null);
            
            StoredBitcoinBlock storedBitcoinBlockActual = store.getChainHead();
            Assert.assertEquals(storedBitcoinBlockExpected, storedBitcoinBlockActual);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void chainHeadNonExistingBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock storedBitcoinBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
            
            try {
                store.setChainHead(storedBitcoinBlock);
                Assert.fail("Should throw an exception");
            } catch(BlockStoreException e) {
                Assert.assertTrue(e.getMessage().startsWith("Chain head should reference existing block"));
            }
            
            StoredBitcoinBlock storedBitcoinBlockActual = store.getChainHead();
            Assert.assertEquals(storedBitcoinBlockActual, null);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void multipleBlocksStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();
            
            BitcoinBlock block1 = new BitcoinBlock(1, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 1, 1, 1);
            StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(block1, BigInteger.ONE, 0);
            store.put(storedBlock);
            
            BitcoinBlock block2 = new BitcoinBlock(1, block1.getHash(), Sha256Hash.ZERO_HASH, 1, 1, 1);
            storedBlock = new StoredBitcoinBlock(block2, BigInteger.ONE, 0);
            store.put(storedBlock);
            
            // we try to get 2 blocks starting from block1 but we get only one.
            List<StoredBitcoinBlock> blocks = store.get(block1.getHash(), 2);
            Assert.assertEquals(blocks.size(), 1);
            
            // we try to get 2 blocks starting from block2 and we get them.
            blocks = store.get(block2.getHash(), 2);
            Assert.assertEquals(blocks.size(), 2);
            
            StoredBitcoinBlock block2Previous = store.getFromChain(block2.getHash(), 1);
            Assert.assertEquals(block2Previous.getBlock(), block1);
            
            StoredBitcoinBlock block2PreviousPrevious = store.getFromChain(block2.getHash(), 2);
            Assert.assertEquals(block2PreviousPrevious, null);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void multipleChainsStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();
            
            BitcoinBlock block1 = new BitcoinBlock(1, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 1, 1, 1);
            StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(block1, BigInteger.ONE, 0);
            store.put(storedBlock);
            
            BitcoinBlock block2 = new BitcoinBlock(1, block1.getHash(), Sha256Hash.ZERO_HASH, 1, 1, 1);
            storedBlock = new StoredBitcoinBlock(block2, BigInteger.ONE, 0);
            store.put(storedBlock);
            
            BitcoinBlock block3 = new BitcoinBlock(1, block1.getHash(), Sha256Hash.ZERO_HASH, 1, 1, 2);
            storedBlock = new StoredBitcoinBlock(block3, BigInteger.ONE, 0);
            store.put(storedBlock);
            
            store.setChainHead(storedBlock);
            StoredBitcoinBlock block1Actual = store.scanBestChain(block1.getHash());
            Assert.assertNotEquals(block1Actual, null);
            
            // we can see that block2 does not belong to the main chain
            StoredBitcoinBlock block2Actual = store.scanBestChain(block2.getHash());
            Assert.assertEquals(block2Actual, null);
            
            // let's change the chain tip and see what happens
            store.setChainHead(new StoredBitcoinBlock(block2, BigInteger.ONE, 0));
            
            // now block2 belongs to the main chain
            block2Actual = store.scanBestChain(block2.getHash());
            Assert.assertNotEquals(block2Actual, null);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void EraseTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock expectedBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

            store.put(expectedBlock);
            StoredBitcoinBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(expectedBlock, storedBlock);

            StoredBitcoinBlock erasedBlock = store.erase(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, erasedBlock);

            storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, null);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void EraseNonexistentTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock expectedBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

            StoredBitcoinBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, null);

            StoredBitcoinBlock erasedBlock = store.erase(expectedBlock.getHash());
            Assert.assertEquals(erasedBlock, null);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    // This test is mostly useless at this moment, as BTC blocks
    // don't have any extra data(other than the header) associated
    // with them. Thus, a replacement block that has the same hash
    // also has identical data/metadata
    @Test
    public void ReplaceBlockTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock newBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
            StoredBitcoinBlock oldBlock = newBlock;

            store.put(oldBlock);
            StoredBitcoinBlock storedBlock = store.get(oldBlock.getHash());
            Assert.assertEquals(storedBlock, oldBlock);

            StoredBitcoinBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(newBlock, storedBlock);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void ReplaceNonexistentTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            BitcoinStore store = VeriBlockIntegrationLibraryManager.getContext().getBitcoinStore();

            byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock newBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
            StoredBitcoinBlock oldBlock = null;

            StoredBitcoinBlock storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(storedBlock, oldBlock);

            StoredBitcoinBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(newBlock, storedBlock);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
}
