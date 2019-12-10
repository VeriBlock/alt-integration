// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockIntegrationLibraryManager;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class BitcoinStoreTest {

    private VeriBlockSecurity veriBlockSecurity;
    private BlockStore<StoredBitcoinBlock, Sha256Hash> store;

    private final BitcoinBlock block1 = new BitcoinBlock(766099456,
            Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
            Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
            1572336145, 0x17148edf, 790109764);
    private final StoredBitcoinBlock storedBlock1 = new StoredBitcoinBlock(block1, BigInteger.TEN, 0);

    private final BitcoinBlock block2 = new BitcoinBlock(1073733632,
            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
            Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
            1572336708, 0x17148edf, 344118374);
    private final StoredBitcoinBlock storedBlock2 = new StoredBitcoinBlock(block2, BigInteger.ONE, 0);

    @Before
    public void setUp() throws Exception {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();
        store = veriBlockSecurity.getContext().getBitcoinStore();

        Assert.assertEquals(block1.getHash(),
                Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"));
        Assert.assertEquals(block2.getHash(),
                Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"));
    }

    @After
    public void tearDown() throws Exception {
        veriBlockSecurity.shutdown();
    }

    @Test
    public void eraseCantSplitBlockchainTest() throws SQLException, IOException {
        store.put(storedBlock2);
        store.put(storedBlock1);

        try {
            store.erase(block1.getHash());
            Assert.fail("Expected BlockStoreException");
        } catch (BlockStoreException e) {
            Assert.assertTrue(e.getMessage().startsWith("Cannot erase a block"));
        }

        Assert.assertEquals(store.erase(block2.getHash()), storedBlock2);
        Assert.assertEquals(store.erase(block1.getHash()), storedBlock1);
    }

    @Test
    public void nonexistingBlockStoreTest() throws SQLException, IOException {
        Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash("123".getBytes()));
        StoredBitcoinBlock storedBitcoinBlock = store.get(hash);
        Assert.assertTrue(storedBitcoinBlock == null);
    }

    @Test
    public void bitcoinBlockStoreTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock storedBitcoinBlockExpected = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
        store.put(storedBitcoinBlockExpected);
        StoredBitcoinBlock storedBitcoinBlockActual = store.get(storedBitcoinBlockExpected.getHash());

        Assert.assertEquals(storedBitcoinBlockExpected, storedBitcoinBlockActual);
    }

    @Test
    public void chainHeadStoreTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock storedBitcoinBlockExpected = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
        store.put(storedBitcoinBlockExpected);
        StoredBitcoinBlock replacedBlock = store.setChainHead(storedBitcoinBlockExpected);
        Assert.assertEquals(replacedBlock, null);

        StoredBitcoinBlock storedBitcoinBlockActual = store.getChainHead();
        Assert.assertEquals(storedBitcoinBlockExpected, storedBitcoinBlockActual);
    }

    @Test
    public void chainHeadNonExistingBlockStoreTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock storedBitcoinBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

        try {
            store.setChainHead(storedBitcoinBlock);
            Assert.fail("Should throw an exception");
        } catch (BlockStoreException e) {
            Assert.assertTrue(e.getMessage().startsWith("Chain head should reference existing block"));
        }

        StoredBitcoinBlock storedBitcoinBlockActual = store.getChainHead();
        Assert.assertEquals(storedBitcoinBlockActual, null);
    }

    @Test
    public void multipleBlocksStoreTest() throws SQLException, IOException {
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
    }

    @Test
    public void multipleChainsStoreTest() throws SQLException, IOException {
        BitcoinBlock block1 = new BitcoinBlock(1, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 1, 1, 1);
        StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(block1, BigInteger.ONE, 1);
        store.put(storedBlock);

        BitcoinBlock block2 = new BitcoinBlock(1, block1.getHash(), Sha256Hash.ZERO_HASH, 1, 1, 1);
        storedBlock = new StoredBitcoinBlock(block2, BigInteger.ONE, 2);
        store.put(storedBlock);

        BitcoinBlock block3 = new BitcoinBlock(1, block1.getHash(), Sha256Hash.ZERO_HASH, 1, 1, 2);
        storedBlock = new StoredBitcoinBlock(block3, BigInteger.ONE, 3);
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
    }

    @Test
    public void eraseTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock expectedBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

        store.put(expectedBlock);
        StoredBitcoinBlock storedBlock = store.get(expectedBlock.getHash());
        Assert.assertEquals(expectedBlock, storedBlock);

        StoredBitcoinBlock erasedBlock = store.erase(expectedBlock.getHash());
        Assert.assertEquals(storedBlock, erasedBlock);

        storedBlock = store.get(expectedBlock.getHash());
        Assert.assertEquals(storedBlock, null);
    }

    @Test
    public void cantEraseChainHeadTest() throws SQLException, IOException {
        StoredBitcoinBlock chainHeadBlock = storedBlock1;

        store.put(chainHeadBlock);
        store.setChainHead(chainHeadBlock);

        Assert.assertEquals(chainHeadBlock, store.get(chainHeadBlock.getHash()));
        Assert.assertEquals(chainHeadBlock, store.getChainHead());

        try {
            store.erase(chainHeadBlock.getHash());
            Assert.fail("Should throw a BlockStoreException");
        } catch (BlockStoreException e) {
            Assert.assertEquals("Cannot erase the chain head block", e.getMessage());
        }

        // the block is still in the store
        Assert.assertEquals(chainHeadBlock, store.get(chainHeadBlock.getHash()));
    }

    @Test
    public void eraseNonexistentTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock expectedBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

        StoredBitcoinBlock storedBlock = store.get(expectedBlock.getHash());
        Assert.assertEquals(storedBlock, null);

        StoredBitcoinBlock erasedBlock = store.erase(expectedBlock.getHash());
        Assert.assertEquals(erasedBlock, null);
    }

    @Test
    public void replaceBlockTest() throws SQLException, IOException {
        StoredBitcoinBlock newBlock = new StoredBitcoinBlock(block1, BigInteger.ONE, 0);
        StoredBitcoinBlock oldBlock = storedBlock1;

        // StoredBitcoinBlock.work should differ
        Assert.assertNotEquals(newBlock, oldBlock);

        store.put(oldBlock);
        StoredBitcoinBlock storedBlock = store.get(oldBlock.getHash());
        Assert.assertEquals(storedBlock, oldBlock);

        StoredBitcoinBlock replacedBlock = store.replace(oldBlock.getHash(), newBlock);
        Assert.assertEquals(replacedBlock, oldBlock);

        storedBlock = store.get(oldBlock.getHash());
        Assert.assertEquals(newBlock, storedBlock);
    }

    @Test
    public void replaceReferencedBlockTest() throws SQLException, IOException {
        Assert.assertEquals(block2.getPreviousBlock(), block1.getHash());

        StoredBitcoinBlock newBlock = new StoredBitcoinBlock(block1, BigInteger.ONE, 0);
        // StoredBitcoinBlock.work should differ
        Assert.assertNotEquals(newBlock, storedBlock1);

        store.put(storedBlock2);
        store.put(storedBlock1);

        StoredBitcoinBlock replacedBlock = store.replace(storedBlock1.getHash(), newBlock);
        Assert.assertEquals(replacedBlock, storedBlock1);

        StoredBitcoinBlock storedBlock = store.get(storedBlock1.getHash());
        Assert.assertEquals(newBlock, storedBlock);
    }

    @Test
    public void replaceNonexistentTest() throws SQLException, IOException {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock newBlock = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);
        StoredBitcoinBlock oldBlock = null;

        StoredBitcoinBlock storedBlock = store.get(newBlock.getHash());
        Assert.assertEquals(storedBlock, oldBlock);

        StoredBitcoinBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
        Assert.assertEquals(replacedBlock, oldBlock);

        storedBlock = store.get(newBlock.getHash());
        Assert.assertEquals(newBlock, storedBlock);
    }

    @Test
    public void replaceWithDifferentHashTest() throws SQLException, IOException {
        store.put(storedBlock1);

        try {
            store.replace(storedBlock1.getHash(), storedBlock2);
            Assert.fail("Should throw a BlockStoreException");
        } catch (BlockStoreException e) {
            Assert.assertEquals(e.getMessage(), "The original and replacement block hashes must match");
        }

        StoredBitcoinBlock storedBlock = store.get(storedBlock1.getHash());
        Assert.assertEquals(storedBlock, storedBlock1);

        storedBlock = store.get(storedBlock2.getHash());
        Assert.assertEquals(storedBlock, null);
    }
}
