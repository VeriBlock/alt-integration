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
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.transactions.VeriBlockTransactionsAtv;
import org.veriblock.integrations.transactions.VeriBlockTransactionsVtb;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BlockIndex;
import org.veriblock.sdk.VeriBlockPublication;

public class ServiceSecurityTest {
    
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
}
