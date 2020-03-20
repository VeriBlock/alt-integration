// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.util.KeyGenerator;

public class VeriBlockPopMinerTest {

    private VeriBlockSecurity security;
    private VeriBlockPopMiner vpm;

    @Before
    public void setUp() throws IOException, SQLException {
        SecurityFactory factory = new SecurityFactory();
        security = factory.createInstance();

        MockFactory mockFactory = new MockFactory();
        vpm = mockFactory.getVeriBlockPopMiner();
    }

    @After
    public void tearDown() {
        security.shutdown();
    }

    @Test
    public void miningTest() throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        vpm.getBitcoinBlockchain().bootstrap(BitcoinDefaults.bootstrap);
        vpm.getVeriBlockBlockchain().bootstrap(VeriBlockDefaults.bootstrap);

        security.getVeriBlockBlockchain().bootstrap(VeriBlockDefaults.bootstrap);
        security.getBitcoinBlockchain().bootstrap(BitcoinDefaults.bootstrap);

        KeyPair key = KeyGenerator.generate();
        
        VeriBlockPublication vtb = vpm.mine(security.getVeriBlockBlockchain().getChainHead(),
                                            security.getVeriBlockBlockchain().getChainHead(),
                                            security.getBitcoinBlockchain().getChainHead(),
                                            key);

        BlockIndex blockIndex = new BlockIndex(0, "hash");
        
        security.addPayloads(blockIndex, Arrays.asList(vtb), new ArrayList<>());
    }
}
