// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.util.BitcoinUtils;

import java.math.BigInteger;
import java.sql.SQLException;

public class IntegrationLibraryGenesis {
    private IntegrationLibraryGenesis() { }

    public static VeriBlockBlock addVeriBlockGenesisBlock(VeriBlockSecurity security) throws VerificationException, BlockStoreException, SQLException {
        // the VeriBlock block should have small difficulty
        long veriBits = BitcoinUtils.encodeCompactBits(BigInteger.ONE);
        VeriBlockBlock genesis = new VeriBlockBlock(0, (short) 2, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                Sha256Hash.ZERO_HASH, 1, (int) veriBits, 1);
        security.getVeriBlockBlockchain().add(genesis);
        return genesis;
    }
    
    public static BitcoinBlock addBitcoinGenesisBlock(VeriBlockSecurity security) throws VerificationException, BlockStoreException, SQLException {
        // the Bitcoin block should have big difficulty
        int bitcoinBits = BitcoinUtils.bitcoinVeryHighPowEncodeToBits();
        BitcoinBlock genesis = new BitcoinBlock(0, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 1, bitcoinBits, 1);
        security.getBitcoinBlockchain().add(genesis);
        return genesis;
    }
}
