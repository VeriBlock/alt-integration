// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.transactions.VeriBlockTransactionsVtb;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSecurityVtbTest {
    
    private static VeriBlockSecurity security;
    
    @Before
    public void setUp() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        security = veriBlockIntegrationLibraryManager.init();
    }
    
    @After
    public void tearDown() throws SQLException {
        security.shutdown();
    }
    
    @Test
    public void dummyAddPayloadTest() throws SQLException, IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationNotAttached();
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        // adding a random publication should fail since it does not connect to any block
        try {
            security.addPayloads(blockIndex, vtbPublications, null);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("Publication does not connect to VeriBlock blockchain", e.getMessage());
        }
    }
    
    @Test
    public void normalAddPayloadTest() throws SignatureException, VerificationException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);
        BitcoinBlock bitcoinGenesisBlock = IntegrationLibraryGenesis.addBitcoinGenesisBlock(security);

        ///HACK: bits value is hardcoded in createVtbAttachedToBitcoinBlock()
        VeriBlockPoPTransaction tx = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(bitcoinGenesisBlock.getHash());
        
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationAttached(veriGenesisBlock, tx);
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        security.addPayloads(blockIndex, vtbPublications, null);
    }
    
    @Test
    public void temporalAddPayload() throws SignatureException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);
        BitcoinBlock bitcoinGenesisBlock = IntegrationLibraryGenesis.addBitcoinGenesisBlock(security);

        VeriBlockPoPTransaction tx = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(bitcoinGenesisBlock.getHash());
        VeriBlockPublication publication = VeriBlockTransactionsVtb.createVtbPublicationAttached(veriGenesisBlock, tx);
        ValidationService.verify(publication);

        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(publication);
        
        security.addTemporaryPayloads(vtbPublications, null);
        
        VeriBlockPoPTransaction tx2 = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(publication.getTransaction().getBlockOfProof().getHash());
        VeriBlockPublication publication2 = VeriBlockTransactionsVtb.createVtbPublicationAttached(publication.getContainingBlock(), tx2);
        ValidationService.verify(publication2);

        vtbPublications = new ArrayList<>();
        vtbPublications.add(publication2);
        
        security.addTemporaryPayloads(vtbPublications, null);
    }
    
    @Test
    public void addRemovePayloadTest() throws SignatureException, VerificationException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);
        BitcoinBlock bitcoinGenesisBlock = IntegrationLibraryGenesis.addBitcoinGenesisBlock(security);

        ///HACK: bits value is hardcoded in createVtbAttachedToBitcoinBlock()
        VeriBlockPoPTransaction tx = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(bitcoinGenesisBlock.getHash());
        
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationAttached(veriGenesisBlock, tx);
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        
        // only genesis block should exist
        List<Sha256Hash> btcBlocks = security.getLastKnownBTCBlocks(5);
        Assert.assertTrue(btcBlocks.size() == 1);

        security.addPayloads(blockIndex, vtbPublications, null);
        
        btcBlocks = security.getLastKnownBTCBlocks(5);
        // and now two blocks exist
        Assert.assertTrue(btcBlocks.size() == 2);
        
        security.removePayloads(blockIndex);
        
        btcBlocks = security.getLastKnownBTCBlocks(5);
        // blocks are rewinded with removePayloads so just genesis block stays
        Assert.assertTrue(btcBlocks.size() == 1);
    }
}
