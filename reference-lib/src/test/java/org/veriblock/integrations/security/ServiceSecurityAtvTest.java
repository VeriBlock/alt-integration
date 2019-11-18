// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.security;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.IntegrationLibraryGenesis;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BlockIndex;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;
import org.veriblock.sdk.VerificationException;
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
        
        // this test should fail to add some random publication since it does not connect to any block
        boolean success = security.addPayloads(blockIndex, vtbPublications, altPublications);
        Assert.assertFalse(success);
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

        boolean success = security.addPayloads(blockIndex, null, altPublications);
        Assert.assertTrue(success);
    }
    
    @Test
    public void temporalAddPayload() throws SignatureException, BlockStoreException, SQLException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockBlock veriGenesisBlock = IntegrationLibraryGenesis.addVeriBlockGenesisBlock(security);

        VeriBlockTransaction tx = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication = VeriBlockTransactionsAtv.createAtvPublicationAttached(veriGenesisBlock, tx);
        ValidationService.verify(publication);

        List<AltPublication> altPublications = new ArrayList<>();
        altPublications.add(publication);

        boolean success = security.addTemporaryPayloads(null, altPublications);
        Assert.assertTrue(success);

        VeriBlockTransaction tx2 = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication2 = VeriBlockTransactionsAtv.createAtvPublicationAttached(publication.getContainingBlock(), tx2);
        ValidationService.verify(publication2);

        altPublications = new ArrayList<>();
        altPublications.add(publication2);

        success = security.addTemporaryPayloads(null, altPublications);
        Assert.assertTrue(success);

        // we have successfully stored two payloads in the temporal storage
        // now let's try to clean the storage

        security.clearTemporaryPayloads();

        // we recreate the first transaction again. It should be added without any problems.
        altPublications = new ArrayList<>();
        altPublications.add(publication);
        success = security.addTemporaryPayloads(null, altPublications);
        Assert.assertTrue(success);

        security.clearTemporaryPayloads();

        // but after cleaning the second payload cannot be attached
        altPublications = new ArrayList<>();
        altPublications.add(publication2);
        success = security.addTemporaryPayloads(null, altPublications);
        Assert.assertFalse(success);
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

        boolean success = security.addPayloads(blockIndex, null, altPublications);
        Assert.assertTrue(success);
        
        vbkBlocks = security.getLastKnownVBKBlocks(5);
        // and now two blocks exist
        Assert.assertTrue(vbkBlocks.size() == 2);
        
        security.removePayloads(blockIndex);
        
        vbkBlocks = security.getLastKnownVBKBlocks(5);
        // blocks are rewinded with removePayloads so just genesis block stays
        Assert.assertTrue(vbkBlocks.size() == 1);
    }
}
