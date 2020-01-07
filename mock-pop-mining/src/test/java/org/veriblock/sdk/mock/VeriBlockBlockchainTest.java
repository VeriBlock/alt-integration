// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.blockchain.VeriBlockDifficultyCalculator;
import org.veriblock.sdk.models.VeriBlockBlock;

public class VeriBlockBlockchainTest {

    private VeriBlockSecurity security;
    private org.veriblock.sdk.blockchain.VeriBlockBlockchain blockchain;
    private VeriBlockBlockchain mockchain;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory securityFactory = new SecurityFactory();
        security = securityFactory.createInstance();

        blockchain = security.getVeriBlockBlockchain();

        MockFactory mockFactory = new MockFactory();
        mockchain = mockFactory.getVeriBlockBlockchain();
    }

    @After
    public void tearDown() {
        security.shutdown();
    }

    @Test
    public void miningTest() throws SQLException {
        mockchain.bootstrap(VeriBlockDefaults.bootstrap);
        blockchain.bootstrap(VeriBlockDefaults.bootstrap);

        for (int i = 0; i < VeriBlockDifficultyCalculator.RETARGET_PERIOD + 10; i++) {
            VeriBlockBlock block = mockchain.mine(new VeriBlockBlockData());
            blockchain.add(block);
        }
    }
}
