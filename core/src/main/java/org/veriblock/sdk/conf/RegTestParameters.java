// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.conf;

import java.math.BigInteger;

public class RegTestParameters implements VeriBlockNetworkParameters {
    private static final BigInteger MINIMUM_POW_DIFFICULTY = new BigInteger("1");

    @Override
    public BigInteger getMinimumDifficulty() {
        return MINIMUM_POW_DIFFICULTY;
    }

    @Override
    public Byte getTransactionMagicByte() {
        return (byte)0xBB;
    }
}
