// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.changes;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.integrations.auditor.BlockIdentifier;
import org.veriblock.integrations.auditor.store.StoredChange;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;

public class AddBitcoinBlockChangeTest {

    @Test
    public void addBitcoinBlockChangeTest() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        StoredBitcoinBlock oldValue = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.ONE, 0);
        StoredBitcoinBlock newValue = new StoredBitcoinBlock(SerializeDeserializeService.parseBitcoinBlock(raw), BigInteger.TEN, 0);

        Random random = new Random(100L);
        byte[] scratch = new byte[BlockIdentifier.LENGTH];
        random.nextBytes(scratch);
        BlockIdentifier blockIdentifier = BlockIdentifier.wrap(scratch);
        StoredChange storedChangeActual = new StoredChange(blockIdentifier, 1, new AddBitcoinBlockChange(oldValue, newValue));

        ByteBuffer storedChangeBytes = ByteBuffer.allocateDirect((StoredChange.SIZE));
        storedChangeActual.serialize(storedChangeBytes);
        storedChangeBytes.flip();

        StoredChange storedChangeExpected = StoredChange.deserialize(storedChangeBytes);

        Assert.assertEquals(storedChangeActual.hashCode(), storedChangeExpected.hashCode());
        Assert.assertEquals(storedChangeActual, storedChangeExpected);
    }
}