// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.util;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.math.BigInteger;
import java.util.Base64;

public class BitcoinUtilsTest {

    BigInteger WORK_EXPECTED = new BigInteger("1773379319961352732670545446265903382336667012364909500432384");

    @Test
    public void decodeCompactBits() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        BitcoinBlock block = SerializeDeserializeService.parseBitcoinBlock(raw);

        BigInteger result = BitcoinUtils.decodeCompactBits(block.getBits());
        Assert.assertTrue(WORK_EXPECTED.equals(result));
    }

    @Test
    public void encodeCompactBits() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        BitcoinBlock block = SerializeDeserializeService.parseBitcoinBlock(raw);

        long bitsActual = BitcoinUtils.encodeCompactBits(WORK_EXPECTED);
        Assert.assertEquals((long) block.getBits(), bitsActual);
    }
}