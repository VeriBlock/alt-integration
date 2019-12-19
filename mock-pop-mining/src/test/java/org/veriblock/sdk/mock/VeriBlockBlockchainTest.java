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
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.util.Utils;

public class VeriBlockBlockchainTest {

    // FIXME: need a proper genesis block
    private final byte[] rawGenesis = Utils.decodeHex("0001998300029690ACA425987B8B529BEC04654A16FCCE708F3F0DEED25E1D2513D05A3B17C49D8B3BCFEFC10CB2E9C4D473B2E25DB7F1BD040098960DE0E313");
    private final VeriBlockBlock genesis = SerializeDeserializeService.parseVeriBlockBlock(rawGenesis);
        
    private final List<VeriBlockBlock> bootstrap = Arrays.asList(genesis);

    private VeriBlockSecurity security;
    private org.veriblock.sdk.blockchain.VeriBlockBlockchain blockchain;
    private VeriBlockBlockchain mockchain;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory factory = new SecurityFactory();
        security = factory.createInstance();

        blockchain = security.getVeriBlockBlockchain();


        VeriBlockStore veriBlockStore = new VeriBlockStore(ConnectionSelector.setConnectionInMemory());
        BitcoinStore bitcoinStore = new BitcoinStore(ConnectionSelector.setConnectionInMemory());
        mockchain = new VeriBlockBlockchain(new MainNetParameters(), veriBlockStore, bitcoinStore);
    }

    @After
    public void tearDown() {
        security.shutdown();
    }

    @Test
    public void miningTest() throws SQLException {
        // FIXME: turn on difficulty checking after refactoring the calculator
        blockchain.setSkipValidateBlocksDifficulty(true);
        
        mockchain.bootstrap(bootstrap);
        blockchain.bootstrap(bootstrap);
        
        // FIXME: fix mining
        try {
            VeriBlockBlock block = mockchain.mine(new VeriBlockBlockData());
            blockchain.add(block);
            Assert.fail("Expected VerificationException");
        } catch(VerificationException e) {
            Assert.assertTrue(e.getMessage().startsWith("Block hash is higher than target"));
        }
    }
}
