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
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class VeriBlockStoreTest {
    
    @Test
    public void nonexistingBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            VBlakeHash hash = VBlakeHash.hash("123".getBytes());
            StoredVeriBlockBlock storedBlock = store.get(hash);
            Assert.assertTrue(storedBlock == null);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void veriBlockBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock storedVeriBlockBlockExpected = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            store.put(storedVeriBlockBlockExpected);
            StoredVeriBlockBlock storedVeriBlockBlockActual = store.get(storedVeriBlockBlockExpected.getHash());

            Assert.assertEquals(storedVeriBlockBlockExpected, storedVeriBlockBlockActual);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void chainHeadStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock storedVeriBlockBlockExpected = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            store.put(storedVeriBlockBlockExpected);
            StoredVeriBlockBlock replacedBlock = store.setChainHead(storedVeriBlockBlockExpected);
            Assert.assertEquals(replacedBlock, null);
            
            StoredVeriBlockBlock storedVeriBlockBlockActual = store.getChainHead();
            Assert.assertEquals(storedVeriBlockBlockExpected, storedVeriBlockBlockActual);
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void chainHeadNonExistingBlockStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

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
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void multipleBlocksStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();
            
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
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
    
    @Test
    public void multipleChainsStoreTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();
            
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
        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void EraseTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock expectedBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

            store.put(expectedBlock);
            StoredVeriBlockBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(expectedBlock, storedBlock);

            StoredVeriBlockBlock erasedBlock = store.erase(expectedBlock.getHash());
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
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock expectedBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

            StoredVeriBlockBlock storedBlock = store.get(expectedBlock.getHash());
            Assert.assertEquals(storedBlock, null);

            StoredVeriBlockBlock erasedBlock = store.erase(expectedBlock.getHash());
            Assert.assertEquals(erasedBlock, null);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void ReplaceBlockTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();
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

            StoredVeriBlockBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(storedBlock.getBlockOfProof(), blockOfProof.getHash());
            Assert.assertEquals(newBlock, storedBlock);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }

    @Test
    public void ReplaceNonexistentTest() throws SQLException, IOException {
        try {
            VeriBlockIntegrationLibraryManager.init();
            VeriBlockStore store = VeriBlockIntegrationLibraryManager.getContext().getVeriblockStore();

            byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
            StoredVeriBlockBlock newBlock = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);
            StoredVeriBlockBlock oldBlock = null;

            StoredVeriBlockBlock storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(storedBlock, oldBlock);

            StoredVeriBlockBlock replacedBlock = store.replace(newBlock.getHash(), newBlock);
            Assert.assertEquals(replacedBlock, oldBlock);

            storedBlock = store.get(newBlock.getHash());
            Assert.assertEquals(newBlock, storedBlock);

        } finally {
            VeriBlockIntegrationLibraryManager.shutdown();
        }
    }
}
