// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.util.KeyGenerator;

public class PoPMiningCoordinatorTest {

    private PoPMiningCoordinator coordinator;
    private VeriBlockSecurity security;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory factory = new SecurityFactory();
        security = factory.createInstance();

        MockFactory mockFactory = new MockFactory();
        coordinator = mockFactory.getCoordinator();
    }

    @After
    public void tearDown() {
        security.shutdown();
    }

    @Test
    public void miningTest() throws SQLException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        coordinator.getVeriBlockPopMiner().getVeriBlockBlockchain().bootstrap(VeriBlockDefaults.bootstrap);
        coordinator.getVeriBlockPopMiner().getBitcoinBlockchain().bootstrap(BitcoinDefaults.bootstrap);

        security.getVeriBlockBlockchain().bootstrap(VeriBlockDefaults.bootstrap);
        security.getBitcoinBlockchain().bootstrap(BitcoinDefaults.bootstrap);

        KeyPair key = KeyGenerator.generate();

        PublicationData publicationData = new PublicationData(0,
                                                              "headerBytes".getBytes(),
                                                              "payoutInfo".getBytes(),
                                                              "contextInfo".getBytes());

        int vtbCount = 3;
        PoPMiningCoordinator.Payloads payloads = coordinator.mine(
                publicationData,
                security.getVeriBlockBlockchain().getChainHead(),
                security.getBitcoinBlockchain().getChainHead(),
                key,
                vtbCount, 3, 3);

        Assert.assertEquals(payloads.atvs.size(), 1);
        Assert.assertEquals(payloads.vtbs.size(), vtbCount);

        for (int i = 0; i < payloads.vtbs.size() - 1; i++) {
            Assert.assertNotEquals(payloads.vtbs.get(i), payloads.vtbs.get(i+1));
        }
                
        BlockIndex blockIndex = new BlockIndex(0, "hash");

        security.addPayloads(blockIndex, payloads.vtbs, payloads.atvs);
    }
}
