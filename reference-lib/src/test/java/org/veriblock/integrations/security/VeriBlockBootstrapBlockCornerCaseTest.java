// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.blockchain.VeriBlockBlockchain;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;
import org.veriblock.sdk.VeriBlockBlock;

public class VeriBlockBootstrapBlockCornerCaseTest {
    
    private static VeriBlockSecurity security;
    private static VeriBlockBlockchain blockchain;
    private static final byte[] raw1 = Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
    private static final VeriBlockBlock block1 = SerializeDeserializeService.parseVeriBlockBlock(raw1);

    private static final byte[] raw2 = Utils.decodeHex("000199840002A69BF9FE9B06E641B61699A9654A16FCCE708F3F0DEED25E1D2513D05A3B7D7F80EB5E94D01C6B3796DDE5647F135DB7F1DD040098960EA12045");
    private static final VeriBlockBlock block2 = SerializeDeserializeService.parseVeriBlockBlock(raw2);

    private static final byte[] raw3 = Utils.decodeHex("000199850002461DB458CD6258D3571D4A2A654A16FCCE708F3F0DEED25E1D2513D05A3BB0B8A658CBFFCFBE9185AFDE789841EC5DB7F2360400989610B1662B");
    private static final VeriBlockBlock block3 = SerializeDeserializeService.parseVeriBlockBlock(raw3);

    @Before
    public void setUp() throws SQLException, IOException {
        security = VeriBlockIntegrationLibraryManager.init();
        blockchain = security.getVeriBlockBlockchain();
    }
    
    @After
    public void tearDown() throws SQLException {
        VeriBlockIntegrationLibraryManager.shutdown();
    }

    @Test
    public void checkVeriBlockContextuallyTest() throws SQLException {
        blockchain.bootstrap(Arrays.asList(block1, block2, block3));

        security.checkVeriBlockContextually(block1);
        security.checkVeriBlockContextually(block2);
        security.checkVeriBlockContextually(block3);
    }

    @Test
    public void AddBootstrapBlockTest() throws SQLException {
        blockchain.bootstrap(Arrays.asList(block1, block2));

        blockchain.add(block1);
        blockchain.add(block2);
        blockchain.add(block3);
    }

}
