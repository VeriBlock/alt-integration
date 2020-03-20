// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.changes;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.auditor.store.StoredChange;
import org.veriblock.sdk.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;

public class AddVeriBlockBlockChangeTest {

    @Test
    public void addVeriBlockBlockChangeTest() {
        byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
        StoredVeriBlockBlock oldValue = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.ONE);
        StoredVeriBlockBlock newValue = new StoredVeriBlockBlock(SerializeDeserializeService.parseVeriBlockBlock(raw), BigInteger.TEN);

        Random random = new Random(100L);
        byte[] scratch = new byte[BlockIdentifier.LENGTH];
        random.nextBytes(scratch);
        BlockIdentifier blockIdentifier = BlockIdentifier.wrap(scratch);
        StoredChange storedChangeActual = new StoredChange(blockIdentifier, 1, new AddVeriBlockBlockChange(oldValue, newValue));

        ByteBuffer storedChangeBytes = ByteBuffer.allocateDirect(StoredChange.SIZE);
        storedChangeActual.serialize(storedChangeBytes);

        storedChangeBytes.flip();
        StoredChange storedChangeExpected = StoredChange.deserialize(storedChangeBytes);

        Assert.assertEquals(storedChangeActual.hashCode(), storedChangeExpected.hashCode());
        Assert.assertEquals(storedChangeActual, storedChangeExpected);
    }
}