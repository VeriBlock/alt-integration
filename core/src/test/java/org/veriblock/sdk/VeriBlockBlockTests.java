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
import org.veriblock.sdk.models.Constants;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.Utils;

import java.nio.ByteBuffer;
import java.util.Base64;

public class VeriBlockBlockTests {
    @Test
    public void newBlockFromArguments() {
        VeriBlockBlock block = new VeriBlockBlock(
                5000,
                (short)2,
                VBlakeHash.wrap("00000000000062948EA92EB694E7DC3E3BE21A96ECCF0FBD"),
                VBlakeHash.wrap("00000000000082396E1549F40989D4F5F62A3331DC995C36"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                Sha256Hash.wrap("DB0F135312B2C27867C9A83EF1B99B981ACBE73C1380F6DD", 24),
                1553699987,
                117586646,
                1924857207
        );

        Assert.assertTrue("000000000000480D8196D5B0B41861D032377F5165BB4452".equalsIgnoreCase(block.getHash().toString()));
    }

    @Test
    public void newBlockFromBytes() {
        byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");

        VeriBlockBlock block = SerializeDeserializeService.parseVeriBlockBlock(raw);
        Assert.assertTrue("000000000000480D8196D5B0B41861D032377F5165BB4452".equalsIgnoreCase(block.getHash().toString()));
    }

    @Test
    public void equals() {
        VeriBlockBlock block = new VeriBlockBlock(
                5000,
                (short)2,
                VBlakeHash.wrap("00000000000062948EA92EB694E7DC3E3BE21A96ECCF0FBD"),
                VBlakeHash.wrap("00000000000082396E1549F40989D4F5F62A3331DC995C36"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                Sha256Hash.wrap("DB0F135312B2C27867C9A83EF1B99B981ACBE73C1380F6DD", 24),
                1553699987,
                117586646,
                1924857207
        );

        byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");
        VeriBlockBlock compareTo = SerializeDeserializeService.parseVeriBlockBlock(raw);

        Assert.assertTrue(block.equals(compareTo));
    }

    @Test
    public void verify_WhenValid() {
        VeriBlockBlock block = new VeriBlockBlock(
                5000,
                (short)2,
                VBlakeHash.wrap("00000000000062948EA92EB694E7DC3E3BE21A96ECCF0FBD"),
                VBlakeHash.wrap("00000000000082396E1549F40989D4F5F62A3331DC995C36"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                Sha256Hash.wrap("DB0F135312B2C27867C9A83EF1B99B981ACBE73C1380F6DD", 24),
                1553699987,
                117586646,
                1924857207
        );

        try {
            ValidationService.verify(block);
        } catch (VerificationException e) {
            Assert.fail();
        }
    }

    @Test
    public void checkProofOfWork_WhenProofOfWorkInvalid() {
        VeriBlockBlock block = new VeriBlockBlock(
                5000,
                (short)2,
                VBlakeHash.wrap("00000000000062948EA92EB694E7DC3E3BE21A96ECCF0FBD"),
                VBlakeHash.wrap("00000000000082396E1549F40989D4F5F62A3331DC995C36"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                Sha256Hash.wrap("DB0F135312B2C27867C9A83EF1B99B981ACBE73C1380F6DD", 24),
                1553699987,
                117586646,
                1
        );

        try {
            ValidationService.checkProofOfWork(block);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue(e.getMessage().startsWith("Block hash is higher than target"));
        }
    }

    @Test
    public void checkMaximumDrift_WhenMaximumDriftInvalid() {
        VeriBlockBlock block = new VeriBlockBlock(
                5000,
                (short)2,
                VBlakeHash.wrap("00000000000062948EA92EB694E7DC3E3BE21A96ECCF0FBD"),
                VBlakeHash.wrap("00000000000082396E1549F40989D4F5F62A3331DC995C36"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                Sha256Hash.wrap("DB0F135312B2C27867C9A83EF1B99B981ACBE73C1380F6DD", 24),
                Integer.MAX_VALUE,
                16842752,
                1
        );

        try {
            ValidationService.checkMaximumDrift(block);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue(e.getMessage().equals("Block is too far in the future"));
        }
    }

    @Test
    public void serialize() {
        byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");

        VeriBlockBlock block = SerializeDeserializeService.parseVeriBlockBlock(raw);

        byte[] serialized = SerializeDeserializeService.serializeHeaders(block);

        Assert.assertArrayEquals(raw, serialized);
    }

    @Test
    public void parse() {
        byte[] raw = Base64.getDecoder().decode("AAATiAAClOfcPjviGpbszw+99fYqMzHcmVw2sJNWN4YGed3V2w8TUxKywnhnyag+8bmbmFyblJMHAjrWcrr9dw==");

        VeriBlockBlock input = SerializeDeserializeService.parseVeriBlockBlock(raw);
        byte[] serialized = SerializeDeserializeService.serialize(input);
        VeriBlockBlock deserialized = SerializeDeserializeService.parseVeriBlockBlock(ByteBuffer.wrap(serialized));

        Assert.assertEquals(input, deserialized);
    }
    
    @Test
    public void parseWhenInvalidLarge() {
        byte[] raw = Utils.fillBytes((byte) 0xFF, Constants.HEADER_SIZE_VeriBlockBlock + 1);
        ByteBuffer buffer = ByteBuffer.wrap(raw);
        
        try {
            SerializeDeserializeService.parseVeriBlockBlock(buffer);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
}
