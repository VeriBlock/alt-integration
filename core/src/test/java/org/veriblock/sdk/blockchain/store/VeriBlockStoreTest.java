// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
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
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class VeriBlockStoreTest {

    private VeriBlockSecurity veriBlockSecurity;
    private BlockStore<StoredVeriBlockBlock, VBlakeHash> store;

    private final byte[] raw1 =  Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
    private final VeriBlockBlock block1 = SerializeDeserializeService.parseVeriBlockBlock(raw1);
    private final StoredVeriBlockBlock storedBlock1 = new StoredVeriBlockBlock(block1, BigInteger.TEN);

    private final byte[] raw2 = Utils.decodeHex("000199840002A69BF9FE9B06E641B61699A9654A16FCCE708F3F0DEED25E1D2513D05A3B7D7F80EB5E94D01C6B3796DDE5647F135DB7F1DD040098960EA12045");
    private final VeriBlockBlock block2 = SerializeDeserializeService.parseVeriBlockBlock(raw2);
    private final StoredVeriBlockBlock storedBlock2 = new StoredVeriBlockBlock(block2, BigInteger.ONE);

    @Before
    public void setUp() throws Exception {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();
        store = veriBlockSecurity.getContext().getVeriblockStore();
    }

    @After
    public void tearDown() throws Exception {
        veriBlockSecurity.shutdown();
    }

    @Test
    public void eraseCantSplitBlockchainTest() throws SQLException, IOException {
            Assert.assertEquals(block2.getPreviousBlock(), block1.getHash().trimToPreviousBlockSize());

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
    }
    
    @Test
    public void nonexistingBlockStoreTest() throws SQLException, IOException {
            VBlakeHash hash = VBlakeHash.hash("123".getBytes());
            StoredVeriBlockBlock storedBlock = store.get(hash);
            Assert.assertTrue(storedBlock == null);
    }

    @Test
    public void veriBlockBlockStoreTest() throws SQLException, IOException {
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock storedVeriBlockBlockExpected = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            store.put(storedVeriBlockBlockExpected);
            StoredVeriBlockBlock storedVeriBlockBlockActual = store.get(storedVeriBlockBlockExpected.getHash());

            Assert.assertEquals(storedVeriBlockBlockExpected, storedVeriBlockBlockActual);
    }

    @Test
    public void putDoesNotUpdateTest() throws SQLException, IOException {
        StoredVeriBlockBlock updatedStoredBlock1 = new StoredVeriBlockBlock(block1, BigInteger.ONE);
        Assert.assertNotEquals(updatedStoredBlock1, storedBlock1);

        store.put(storedBlock1);

        try {
            store.put(storedBlock1);
            Assert.fail("Should throw BlockStoreException");
        } catch (BlockStoreException e) {
            Assert.assertEquals("A block with the same hash is already in the store", e.getMessage());
        }

        try {
            store.put(updatedStoredBlock1);
            Assert.fail("Should throw BlockStoreException");
        } catch (BlockStoreException e) {
            Assert.assertEquals("A block with the same hash is already in the store", e.getMessage());
        }
    }

    @Test
    public void chainHeadStoreTest() throws SQLException, IOException {
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock storedVeriBlockBlockExpected = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            store.put(storedVeriBlockBlockExpected);
            StoredVeriBlockBlock replacedBlock = store.setChainHead(storedVeriBlockBlockExpected);
            Assert.assertEquals(replacedBlock, null);
            
            StoredVeriBlockBlock storedVeriBlockBlockActual = store.getChainHead();
            Assert.assertEquals(storedVeriBlockBlockExpected, storedVeriBlockBlockActual);
    }
    
    @Test
    public void chainHeadNonExistingBlockStoreTest() throws SQLException, IOException {
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock storedVeriBlockBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            
            try {
                store.setChainHead(storedVeriBlockBlock);
                Assert.fail("Should throw an exception");
            } catch(BlockStoreException e) {
                Assert.assertTrue(e.getMessage().startsWith("Chain head should reference existing block"));
            }
            
            StoredVeriBlockBlock storedVeriBlockBlockActual = store.getChainHead();
            Assert.assertEquals(storedVeriBlockBlockActual, null);
    }
    
    @Test
    public void multipleBlocksStoreTest() throws SQLException, IOException {
            VeriBlockBlock block1 = new VeriBlockBlock(1, (short) 1, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                    Sha256Hash.ZERO_HASH, 1, 1, 1);
            StoredVeriBlockBlock storedBlock = new StoredVeriBlockBlock(block1, BigInteger.ONE);
            store.put(storedBlock);
            
            VeriBlockBlock block2 = new VeriBlockBlock(1, (short) 1, block1.getHash(), VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                    Sha256Hash.ZERO_HASH, 1, 1, 1);
            storedBlock = new StoredVeriBlockBlock(block2, BigInteger.ONE);
            store.put(storedBlock);
            
            // we try to get 2 blocks starting from block1 but we get only one.
            List<StoredVeriBlockBlock> blocks = store.get(block1.getHash(), 2);
            Assert.assertEquals(1, blocks.size());
            
            // we try to get 2 blocks starting from block2 and we get them.
            blocks = store.get(block2.getHash(), 2);
            Assert.assertEquals(2, blocks.size());
            
            StoredVeriBlockBlock block2Previous = store.getFromChain(block2.getHash(), 1);
            Assert.assertEquals(block2Previous.getBlock(), block1);
            
            StoredVeriBlockBlock block2PreviousPrevious = store.getFromChain(block2.getHash(), 2);
            Assert.assertEquals(block2PreviousPrevious, null);
    }
    
    @Test
    public void multipleChainsStoreTest() throws SQLException, IOException {
            VeriBlockBlock block1 = new VeriBlockBlock(1, (short) 1, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                    Sha256Hash.ZERO_HASH, 1, 1, 1);
            StoredVeriBlockBlock storedBlock = new StoredVeriBlockBlock(block1, BigInteger.ONE);
            store.put(storedBlock);
            
            VeriBlockBlock block2 = new VeriBlockBlock(1, (short) 1, block1.getHash(), VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                    Sha256Hash.ZERO_HASH, 1, 1, 1);
            storedBlock = new StoredVeriBlockBlock(block2, BigInteger.ONE);
            store.put(storedBlock);
            
            VeriBlockBlock block3 = new VeriBlockBlock(1, (short) 1, block1.getHash(), VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                    Sha256Hash.ZERO_HASH, 1, 1, 2);
            storedBlock = new StoredVeriBlockBlock(block3, BigInteger.ONE);
            store.put(storedBlock);
            
            store.setChainHead(storedBlock);
            StoredVeriBlockBlock block1Actual = store.scanBestChain(block1.getHash());
            Assert.assertNotEquals(block1Actual, null);
            
            // we can see that block2 does not belong to the main chain
            StoredVeriBlockBlock block2Actual = store.scanBestChain(block2.getHash());
            Assert.assertEquals(block2Actual, null);
            
            // let's change the chain tip and see what happens
            store.setChainHead(new StoredVeriBlockBlock(block2, BigInteger.ONE));
            
            // now block2 belongs to the main chain
            block2Actual = store.scanBestChain(block2.getHash());
            Assert.assertNotEquals(block2Actual, null);
    }

    @Test
    public void eraseTest() throws SQLException, IOException {
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock expectedBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

            store.put(expectedBlock);
            StoredVeriBlockBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(expectedBlock, storedBlock);

            StoredVeriBlockBlock erasedBlock = store.erase(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, erasedBlock);

            storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, null);
    }

    @Test
    public void cantEraseChainHeadTest() throws SQLException, IOException {
        StoredVeriBlockBlock chainHeadBlock = storedBlock1;

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
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock expectedBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

            StoredVeriBlockBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, null);

            StoredVeriBlockBlock erasedBlock = store.erase(expectedBlock.getHash());
            Assert.assertEquals(erasedBlock, null);
    }

    @Test
    public void replaceBlockTest() throws SQLException, IOException {
            byte[] rawBlockOfProof = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
            StoredBitcoinBlock blockOfProof = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(rawBlockOfProof), BigInteger.TEN, 0);

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");

            StoredVeriBlockBlock oldBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);;
            Assert.assertEquals(oldBlock.getBlockOfProof(), Sha256Hash.ZERO_HASH);

            StoredVeriBlockBlock newBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            newBlock.setBlockOfProof(blockOfProof.getHash());
            Assert.assertEquals(newBlock.getBlockOfProof(), blockOfProof.getHash());

            Assert.assertNotEquals(newBlock, oldBlock);

            store.put(oldBlock);
            StoredVeriBlockBlock storedBlock = store.get(oldBlock.getHash());
            Assert.assertEquals(storedBlock, oldBlock);

            StoredVeriBlockBlock replacedBlock = store.replace(oldBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(oldBlock.getHash());
            Assert.assertEquals(storedBlock.getBlockOfProof(), blockOfProof.getHash());
            Assert.assertEquals(newBlock, storedBlock);
    }

    @Test
    public void replaceNonexistentTest() throws SQLException, IOException {
            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock newBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            StoredVeriBlockBlock oldBlock = null;

            StoredVeriBlockBlock storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(storedBlock, oldBlock);

            StoredVeriBlockBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(newBlock, storedBlock);
    }

    @Test
    public void replaceReferencedBlockTest() throws SQLException, IOException {
            Assert.assertEquals(block2.getPreviousBlock(), block1.getHash().trimToPreviousBlockSize());

            store.put(storedBlock2);
            store.put(storedBlock1);

            StoredVeriBlockBlock newBlock = new StoredVeriBlockBlock(block1, BigInteger.ONE);
            // StoredVeriBlockBlock.work should differ
            Assert.assertNotEquals(newBlock, storedBlock1);

            StoredVeriBlockBlock replacedBlock = store.replace(storedBlock1.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, storedBlock1);

            StoredVeriBlockBlock storedBlock = store.get(storedBlock1.getHash());
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

        StoredVeriBlockBlock storedBlock = store.get(storedBlock1.getHash());
        Assert.assertEquals(storedBlock, storedBlock1);

        storedBlock = store.get(storedBlock2.getHash());
        Assert.assertEquals(storedBlock, null);
    }
}
