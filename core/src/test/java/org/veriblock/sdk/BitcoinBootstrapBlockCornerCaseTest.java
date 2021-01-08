// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.blockchain.BitcoinBlockchain;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class BitcoinBootstrapBlockCornerCaseTest {
    
    private static VeriBlockSecurity security;
    private static BitcoinBlockchain blockchain;

    private final static int block1Height = 601491;
    private final static BitcoinBlock block1  = new BitcoinBlock(
            766099456,
            Sha256Hash.wrap("00000000000000000004dc9c42c22f489ade54a9349e3a47aee5b55069062afd"),
            Sha256Hash.wrap("87839c0e4c6771557ef02a5076c8b46a7157e5532eff7153293791ca852d2e58"),
            1572336145, 0x17148edf, 790109764);

    private final static BitcoinBlock block2 = new BitcoinBlock(
            1073733632,
            Sha256Hash.wrap("0000000000000000000faad7ae177b313ee4e3f1da519dbbf5b3ab58ccff6338"),
            Sha256Hash.wrap("902e5a70c8fa99fb9ba6d0f855f5e84b8ffc3fe56b694889d07031d8adb6a0f8"),
            1572336708, 0x17148edf, 344118374);

    private final static BitcoinBlock block3 = new BitcoinBlock(
            536870912,
            Sha256Hash.wrap("00000000000000000001163c9e1130c26984d831cb16c16f994945a197550897"),
            Sha256Hash.wrap("2dfad61070eeea30ee035cc58ac20a325292802f9445851d14f23b4e71ddee61"),
            1572337243, 0x17148edf, 2111493782);

    @Before
    public void setUp() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager manager = new VeriBlockIntegrationLibraryManager();
        security = manager.init();
        blockchain = security.getBitcoinBlockchain();
    }
    
    @After
    public void tearDown() throws SQLException {
        security.shutdown();
    }

    @Test
    public void checkBitcoinContextuallyTest() throws SQLException {
        blockchain.bootstrap(Arrays.asList(block1, block2, block3), block1Height);

        security.checkConnectivity(block1);
        security.checkConnectivity(block2);
        security.checkConnectivity(block3);
    }

    @Test
    public void addBootstrapBlockTest() throws SQLException {
        blockchain.bootstrap(Arrays.asList(block1, block2), block1Height);

        blockchain.add(block1);
        blockchain.add(block2);
        blockchain.add(block3);
    }

}
