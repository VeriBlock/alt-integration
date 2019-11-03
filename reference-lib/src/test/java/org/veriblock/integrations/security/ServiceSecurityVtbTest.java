// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.IntegrationLibraryGenesis;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.transactions.VeriBlockTransactionsVtb;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockIndex;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VerificationException;
import org.veriblock.sdk.services.ValidationService;

public class ServiceSecurityVtbTest {
    
    private static VeriBlockSecurity security;
    
    @Before
    public void setUp() throws SQLException, IOException {
        security = VeriBlockIntegrationLibraryManager.init();
    }
    
    @After
    public void tearDown() throws SQLException {
        VeriBlockIntegrationLibraryManager.shutdown();
    }
    
    @Test
    public void dummyAddPayloadTest() throws SQLException, IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationNotAttached();
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        // this test should fail to add some random publication since it does not connect to any block
        boolean success = security.addPayloads(blockIndex, vtbPublications, null);
        Assert.assertFalse(success);
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
        boolean success = security.addPayloads(blockIndex, vtbPublications, null);
        Assert.assertTrue(success);
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
        
        boolean success = security.addTemporaryPayloads(vtbPublications, null);
        Assert.assertTrue(success);
        
        VeriBlockPoPTransaction tx2 = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(publication.getTransaction().getBlockOfProof().getHash());
        VeriBlockPublication publication2 = VeriBlockTransactionsVtb.createVtbPublicationAttached(publication.getContainingBlock(), tx2);
        ValidationService.verify(publication2);

        vtbPublications = new ArrayList<>();
        vtbPublications.add(publication2);
        
        success = security.addTemporaryPayloads(vtbPublications, null);
        Assert.assertTrue(success);
    }
}
