// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.Utils;

import java.nio.ByteBuffer;
import java.util.Base64;

public class BitcoinBlockTests {
    @Test
    public void newBitcoinBlockFromArguments() {
        BitcoinBlock block = new BitcoinBlock(
                536870912,
                Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7"),
                Sha256Hash.wrap("5e16e6cef738a2eba1fe7409318e3f558bec325392427aa3d8eaf46b028654f8"),
                Integer.parseUnsignedInt("1555501858"),
                Integer.parseUnsignedInt("436279940"),
                Integer.parseUnsignedInt("2599551022"));

        Assert.assertTrue("000000000000000246200f09b513e517a3bd8c591a3b692d9852ddf1ee0f8b3a".equalsIgnoreCase(block.getHash().toString()));
    }

    @Test
    public void newBitcoinBlockFromBytes() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");

        BitcoinBlock block = SerializeDeserializeService.parseBitcoinBlock(raw);
        Assert.assertTrue("000000000000000246200f09b513e517a3bd8c591a3b692d9852ddf1ee0f8b3a".equalsIgnoreCase(block.getHash().toString()));
    }

    @Test
    public void equals() {
        BitcoinBlock block = new BitcoinBlock(
                536870912,
                Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7"),
                Sha256Hash.wrap("5e16e6cef738a2eba1fe7409318e3f558bec325392427aa3d8eaf46b028654f8"),
                Integer.parseUnsignedInt("1555501858"),
                Integer.parseUnsignedInt("436279940"),
                Integer.parseUnsignedInt("2599551022"));

        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        BitcoinBlock compareTo = SerializeDeserializeService.parseBitcoinBlock(raw);

        Assert.assertTrue(block.equals(compareTo));
    }

    @Test
    public void verify_WhenValid() {
        BitcoinBlock block = new BitcoinBlock(
                536870912,
                Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7"),
                Sha256Hash.wrap("5e16e6cef738a2eba1fe7409318e3f558bec325392427aa3d8eaf46b028654f8"),
                Integer.parseUnsignedInt("1555501858"),
                Integer.parseUnsignedInt("436279940"),
                Integer.parseUnsignedInt("2599551022"));

        try {
            ValidationService.verify(block);
        } catch (VerificationException e) {
            Assert.fail();
        }
    }

    @Test
    public void checkProofOfWork_WhenProofOfWorkInvalid() {
        BitcoinBlock block = new BitcoinBlock(
                536870912,
                Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7"),
                Sha256Hash.wrap("5e16e6cef738a2eba1fe7409318e3f558bec325392427aa3d8eaf46b028654f8"),
                Integer.parseUnsignedInt("1555501858"),
                Integer.parseUnsignedInt("436279940"),
                Integer.parseUnsignedInt("1"));

        try {
            ValidationService.checkProofOfWork(block);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue(e.getMessage().startsWith("Block hash is higher than target"));
        }
    }

    @Test
    public void checkMaximumDrift_WhenMaximumDriftInvalid() {
        BitcoinBlock block = new BitcoinBlock(
                536870912,
                Sha256Hash.wrap("00000000000000b345b7bbf29bda1507a679b97967f99a10ab0088899529def7"),
                Sha256Hash.wrap("5e16e6cef738a2eba1fe7409318e3f558bec325392427aa3d8eaf46b028654f8"),
                Integer.MAX_VALUE,
                Integer.parseUnsignedInt("553713663"),
                Integer.parseUnsignedInt("2599551022"));

        try {
            ValidationService.checkMaximumDrift(block);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue(e.getMessage().equals("Block is too far in the future"));
        }
    }

    @Test
    public void serialize() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");

        BitcoinBlock block = SerializeDeserializeService.parseBitcoinBlock(raw);

        byte[] serialized = SerializeDeserializeService.getHeaderBytesBitcoinBlock(block);

        Assert.assertArrayEquals(raw, serialized);
    }

    @Test
    public void parse() {
        byte[] raw = Base64.getDecoder().decode("AAAAIPfeKZWJiACrEJr5Z3m5eaYHFdqb8ru3RbMAAAAAAAAA+FSGAmv06tijekKSUzLsi1U/jjEJdP6h66I4987mFl4iE7dchBoBGi4A8po=");
        BitcoinBlock input = SerializeDeserializeService.parseBitcoinBlock(raw);

        byte[] serialized = SerializeDeserializeService.serialize(input);
        BitcoinBlock deserialized = SerializeDeserializeService.parseBitcoinBlockWithLength(ByteBuffer.wrap(serialized));

        Assert.assertEquals(input, deserialized);
    }
    
    @Test
    public void parseWhenInvalidSmall() {
        byte[] raw = { 1, 2, 3 };
        ByteBuffer buffer = ByteBuffer.wrap(raw);
        
        try {
            SerializeDeserializeService.parseBitcoinBlockWithLength(buffer);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
    
    @Test
    public void parseWhenInvalidLarge() {
        byte[] raw = Utils.fillBytes((byte) 0xFF, Constants.HEADER_SIZE_BitcoinBlock + 1);
        ByteBuffer buffer = ByteBuffer.wrap(raw);

        try {
            SerializeDeserializeService.parseBitcoinBlockWithLength(buffer);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
}
