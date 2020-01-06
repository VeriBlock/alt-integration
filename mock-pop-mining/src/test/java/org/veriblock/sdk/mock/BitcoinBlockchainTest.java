// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.models.BitcoinBlock;

public class BitcoinBlockchainTest {

    private VeriBlockSecurity security;
    private org.veriblock.sdk.blockchain.BitcoinBlockchain blockchain;
    private BitcoinBlockchain mockchain;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory factory = new SecurityFactory();
        security = factory.createInstance();

        blockchain = security.getBitcoinBlockchain();

        MockFactory mockFactory = new MockFactory();
        mockchain = mockFactory.getBitcoinBlockchain();
    }

    @After
    public void tearDown() {
        security.shutdown();
    }

    @Test
    public void miningTest() throws SQLException {
        mockchain.bootstrap(BitcoinDefaults.bootstrap);
        blockchain.bootstrap(BitcoinDefaults.bootstrap);

        for (int i = 0; i < 100; i++) {
            BitcoinBlock block = mockchain.mine(new BitcoinBlockData());
            blockchain.add(block);
        }
    }
}
