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
import org.veriblock.sdk.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.conf.BitcoinRegTestParameters;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.util.Utils;

public class BitcoinBlockchainTest {

    // FIXME: need a proper genesis block
    private final byte[] rawGenesis = Utils.decodeHex("00000020e1205431708b69b0a0c48ef51d3bdd1049643cfa7e1d841a18d43dcf48d6be5ef3bb206027319c0939db20cf223be3fc7385d1aa871461ce89a441577978f40afd9adf5dffff7f2001000000");
    private final BitcoinBlock genesis = SerializeDeserializeService.parseBitcoinBlock(rawGenesis);
    private final int genesisHeight = 92;
    private final BitcoinBlockchainBootstrapConfig bootstrap = new BitcoinBlockchainBootstrapConfig(
                                                                    Arrays.asList(genesis), genesisHeight);

    private VeriBlockSecurity security;
    private org.veriblock.sdk.blockchain.BitcoinBlockchain blockchain;
    private BitcoinBlockchain mockchain;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory factory = new SecurityFactory();
        security = factory.createInstance();

        blockchain = security.getBitcoinBlockchain();

        BitcoinStore bitcoinStore = new BitcoinStore(ConnectionSelector.setConnectionInMemory());
        mockchain = new BitcoinBlockchain(new BitcoinRegTestParameters(), bitcoinStore);
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

        for (int i = 0; i < 10; i++) {
            BitcoinBlock block = mockchain.mine(new BitcoinBlockData());
            blockchain.add(block);
        }
    }
}
