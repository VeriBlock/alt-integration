// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.sdk.models.Constants;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.MerklePathUtil;
import org.veriblock.sdk.util.StreamUtils;
import org.veriblock.sdk.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MerklePathTests {
    @Test
    public void getMerkleRoot() {
        // A Bitcoin merkle root is in Little Endian byte order
        Sha256Hash merkleRoot = Sha256Hash.wrapReversed("C03FC4B36B1E0B35BAF030C67012BBF9493301ED880A0BD22990F6E74BF1401C");

        MerklePath test = new MerklePath("64:" +
                "B9A7CC1379305E314204B55C01D55EE3017E836D9ACF27A87D95BE2F09C6E1DA:" +
                "188F9AF33769493739A89AA294CE9E2BE58680C53F3483808D3B476370A1B311:" +
                "44006E07786819DF755B9C59CFEF149C11B1D7E553F168B14F498688299C3B1C:" +
                "D0D8901810B42B4A4EBE3A985A9E8FBACC211602C1505144961C88569CAFFBA3:" +
                "9FE9677AA9A46F7DC5BEF183E0609F1BA3CE3E761527C5BE5E6B91B8E9249E95:" +
                "FE0384D1493C84669F0BAAE754E8F5769756BDAC64DC5AEEFE86ED12919BCFA7:" +
                "E2934990E334AFC30B39CCBE0AAD35551C0BB006F4BC8183B2DEE2FD055941B9:" +
                "B21635D36D58751F83BF8B72CD5B2AAB3717AEC807557E791EAACBAFE31A9DEE:" +
                "C0104D3C5F059897CBB32CE939198E87554A8FE113D4AE4E35D80035C1CAB2A5:" +
                "BC615381DCEF1E34ABDB4A3ABE1D8197655BF29651FA6A7D713CCE203A192428:" +
                "FCC2C5D8039EF8545775BC86A5910A174221A0FC8D069E3525AAE1358C0E32B7");

        Assert.assertEquals(merkleRoot, MerklePathUtil.calculateMerkleRoot(test));
    }

    @Test
    public void parse() {
        MerklePath input = new MerklePath("64:" +
                "B9A7CC1379305E314204B55C01D55EE3017E836D9ACF27A87D95BE2F09C6E1DA:" +
                "188F9AF33769493739A89AA294CE9E2BE58680C53F3483808D3B476370A1B311:" +
                "44006E07786819DF755B9C59CFEF149C11B1D7E553F168B14F498688299C3B1C:" +
                "D0D8901810B42B4A4EBE3A985A9E8FBACC211602C1505144961C88569CAFFBA3:" +
                "9FE9677AA9A46F7DC5BEF183E0609F1BA3CE3E761527C5BE5E6B91B8E9249E95:" +
                "FE0384D1493C84669F0BAAE754E8F5769756BDAC64DC5AEEFE86ED12919BCFA7:" +
                "E2934990E334AFC30B39CCBE0AAD35551C0BB006F4BC8183B2DEE2FD055941B9:" +
                "B21635D36D58751F83BF8B72CD5B2AAB3717AEC807557E791EAACBAFE31A9DEE:" +
                "C0104D3C5F059897CBB32CE939198E87554A8FE113D4AE4E35D80035C1CAB2A5:" +
                "BC615381DCEF1E34ABDB4A3ABE1D8197655BF29651FA6A7D713CCE203A192428:" +
                "FCC2C5D8039EF8545775BC86A5910A174221A0FC8D069E3525AAE1358C0E32B7");

        byte[] serialized = SerializeDeserializeService.serialize(input);
        MerklePath deserialized = SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(serialized), Sha256Hash.wrap("B9A7CC1379305E314204B55C01D55EE3017E836D9ACF27A87D95BE2F09C6E1DA"));

        Assert.assertEquals(input, deserialized);
    }
    
    @Test
    public void parseValidBytes() throws IOException {
        byte[] merkleBytes;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            stream.write(Sha256Hash.ZERO_HASH.length);
            StreamUtils.writeSingleByteLengthValueToStream(stream, Sha256Hash.ZERO_HASH.getBytes());

            merkleBytes = stream.toByteArray();
        }
        
        byte[] merklePathBytes;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeVariableLengthValueToStream(stream, merkleBytes);
            merklePathBytes = stream.toByteArray();
        }

        SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(merklePathBytes), Sha256Hash.ZERO_HASH);
    }
    
    @Test
    public void parseWhenInvalidOversize() throws IOException {
        // try to parse wery long data first
        byte[] array = Utils.fillBytes((byte) 0xFF, Constants.MAX_MERKLE_BYTES + 1);
        
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, array);
            ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
            SerializeDeserializeService.parseMerklePath(buffer, Sha256Hash.ZERO_HASH);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
    
    @Test
    public void parseWhenInvalidIndex() throws IOException {
        byte[] merkleBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.trimmedByteArrayFromLong(Integer.MAX_VALUE + 1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            stream.write(Sha256Hash.ZERO_HASH.length);
            StreamUtils.writeSingleByteLengthValueToStream(stream, Sha256Hash.ZERO_HASH.getBytes());

            merkleBytes = stream.toByteArray();
        }
        
        byte[] merklePathBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeVariableLengthValueToStream(stream, merkleBytes);
            merklePathBytes = stream.toByteArray();
        }
        
        try {
            SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(merklePathBytes), Sha256Hash.ZERO_HASH);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
    
    @Test
    public void parseWhenInvalidNumLayers() throws IOException {
        byte[] merkleBytes;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(Constants.MAX_LAYER_COUNT_MERKLE + 1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            stream.write(Sha256Hash.ZERO_HASH.length);
            StreamUtils.writeSingleByteLengthValueToStream(stream, Sha256Hash.ZERO_HASH.getBytes());

            merkleBytes = stream.toByteArray();
        }
        
        byte[] merklePathBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeVariableLengthValueToStream(stream, merkleBytes);
            merklePathBytes = stream.toByteArray();
        }
        
        try {
            SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(merklePathBytes), Sha256Hash.ZERO_HASH);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected layer count"));
        }
    }
    
    @Test
    public void parseWhenInvalidSubject() throws IOException {
        byte[] merkleBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            stream.write(Sha256Hash.ZERO_HASH.length + 1);
            StreamUtils.writeSingleByteLengthValueToStream(stream, Sha256Hash.ZERO_HASH.getBytes());

            merkleBytes = stream.toByteArray();
        }
        
        byte[] merklePathBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeVariableLengthValueToStream(stream, merkleBytes);
            merklePathBytes = stream.toByteArray();
        }
        
        try {
            SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(merklePathBytes), Sha256Hash.ZERO_HASH);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected sizeBottomData"));
        }
    }
    
    @Test
    public void parseWhenInvalidLayer() throws IOException {
        byte[] merkleBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            StreamUtils.writeSingleByteLengthValueToStream(stream, Utils.toByteArray(1));
            stream.write(Sha256Hash.ZERO_HASH.length);
            
            byte[] layer = Utils.fillBytes((byte) 0, Sha256Hash.BITCOIN_LENGTH + 1);
            StreamUtils.writeSingleByteLengthValueToStream(stream, layer);

            merkleBytes = stream.toByteArray();
        }
        
        byte[] merklePathBytes = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            StreamUtils.writeVariableLengthValueToStream(stream, merkleBytes);
            merklePathBytes = stream.toByteArray();
        }
        
        try {
            SerializeDeserializeService.parseMerklePath(ByteBuffer.wrap(merklePathBytes), Sha256Hash.ZERO_HASH);
            Assert.fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected length"));
        }
    }
}
