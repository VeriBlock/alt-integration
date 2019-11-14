package org.veriblock.sdk.conf;// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.


import java.math.BigInteger;

public class MainNetParameters implements NetworkParameters {
    private static final BigInteger MINIMUM_POW_DIFFICULTY = BigInteger.valueOf(900_000_000_000L);

    @Override
    public BigInteger getMinimumDifficulty() {
        return MINIMUM_POW_DIFFICULTY;
    }

    @Override
    public Byte getTransactionMagicByte() {
        return null;
    }
}