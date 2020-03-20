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
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.transactions.VeriBlockTransactionsAtv;
import org.veriblock.sdk.transactions.VeriBlockTransactionsVtb;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSecurityAtvTest {
    
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
        
        AltPublication altPublication = VeriBlockTransactionsAtv.createAtvPublicationNotAttached();
        List<AltPublication> altPublications = new ArrayList<>();
        altPublications.add(altPublication);
        
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationNotAttached();
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        // adding a random publication should fail since it does not connect to any block
        try {
            security.addPayloads(blockIndex, vtbPublications, altPublications);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("Publication does not connect to VeriBlock blockchain", e.getMessage());
        }
    }
    
    @Test
    public void normalAddPayloadTest() throws SignatureException, VerificationException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);

        VeriBlockTransaction tx = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication = VeriBlockTransactionsAtv.createAtvPublicationAttached(veriGenesisBlock, tx);

        ValidationService.verify(publication);

        List<AltPublication> altPublications = new ArrayList<>();
        altPublications.add(publication);

        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);

        security.addPayloads(blockIndex, null, altPublications);
    }
    
    @Test
    public void temporalAddPayload() throws SignatureException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);

        VeriBlockTransaction tx = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication = VeriBlockTransactionsAtv.createAtvPublicationAttached(veriGenesisBlock, tx);
        ValidationService.verify(publication);

        List<AltPublication> altPublications = new ArrayList<>();
        altPublications.add(publication);

        security.addTemporaryPayloads(null, altPublications);

        VeriBlockTransaction tx2 = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication2 = VeriBlockTransactionsAtv.createAtvPublicationAttached(publication.getContainingBlock(), tx2);
        ValidationService.verify(publication2);

        altPublications = new ArrayList<>();
        altPublications.add(publication2);

        security.addTemporaryPayloads(null, altPublications);

        // we have successfully stored two payloads in the temporal storage
        // now let's try to clean the storage

        security.clearTemporaryPayloads();

        // we recreate the first transaction again. It should be added without any problems.
        altPublications = new ArrayList<>();
        altPublications.add(publication);
        security.addTemporaryPayloads(null, altPublications);

        security.clearTemporaryPayloads();

        // but after cleaning the second payload cannot be attached
        altPublications = new ArrayList<>();
        altPublications.add(publication2);
        try {
            security.addTemporaryPayloads(null, altPublications);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("Publication does not connect to VeriBlock blockchain", e.getMessage());
        }
    }
    
    @Test
    public void addRemovePayloadTest() throws SignatureException, VerificationException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);

        VeriBlockTransaction tx = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication = VeriBlockTransactionsAtv.createAtvPublicationAttached(veriGenesisBlock, tx);

        ValidationService.verify(publication);

        List<AltPublication> altPublications = new ArrayList<>();
        altPublications.add(publication);

        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        
        // only genesis block should exist
        List<VBlakeHash> vbkBlocks = security.getLastKnownVBKBlocks(5);
        Assert.assertTrue(vbkBlocks.size() == 1);

        security.addPayloads(blockIndex, null, altPublications);
        
        vbkBlocks = security.getLastKnownVBKBlocks(5);
        // and now two blocks exist
        Assert.assertTrue(vbkBlocks.size() == 2);
        
        security.removePayloads(blockIndex);
        
        vbkBlocks = security.getLastKnownVBKBlocks(5);
        // blocks are rewinded with removePayloads so just genesis block stays
        Assert.assertTrue(vbkBlocks.size() == 1);
    }
}
