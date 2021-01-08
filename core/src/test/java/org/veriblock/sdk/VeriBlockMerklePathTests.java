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
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.MerklePathUtil;

import java.nio.ByteBuffer;

public class VeriBlockMerklePathTests {
    @Test
    public void getMerkleRoot_WhenNormalTransaction() {
        Sha256Hash merkleRoot = Sha256Hash.wrap("44450212738058A9B3119472D3ADC849C2B719FF8AD1E8C0EB5003554756F1E4");

        VeriBlockMerklePath test = new VeriBlockMerklePath("1:13:" +
                "2A014E88ED7AB65CDFAA85DAEAB07EEA6CBA5E147F736EDD8D02C2F9DDF0DEC6:" +
                "5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:" +
                "20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:" +
                "C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:" +
                "049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:" +
                "0000000000000000000000000000000000000000000000000000000000000000:" +
                "36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A");

        Assert.assertEquals(merkleRoot, MerklePathUtil.calculateVeriMerkleRoot(test));
    }

    @Test
    public void getMerkleRoot_WhenPoPTransaction() {
        Sha256Hash merkleRoot = Sha256Hash.wrap("B53C1F4E259E6A0DF23721A0B3B4B7ABD730436A85A02A72B9DD6291507A7D61");

        VeriBlockMerklePath test = new VeriBlockMerklePath("0:13:" +
                "2A014E88ED7AB65CDFAA85DAEAB07EEA6CBA5E147F736EDD8D02C2F9DDF0DEC6:" +
                "5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:" +
                "20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:" +
                "C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:" +
                "049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:" +
                "0000000000000000000000000000000000000000000000000000000000000000:" +
                "36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A");

        Assert.assertEquals(merkleRoot, MerklePathUtil.calculateVeriMerkleRoot(test));
    }

    @Test
    public void getMerkleRoot_WhenSingleNormalTransaction() {
        Sha256Hash merkleRoot = Sha256Hash.wrap("6AA47826569AAF9BAD831A069E491048B24F079A689137059D54BC01EF0F0219");

        VeriBlockMerklePath test = new VeriBlockMerklePath("1:0:" +
                "7898EFAA2BF5E2783C9DAF2B62883827D3C2D1BABEC101DD622C42F47D63154A:" +
                "0000000000000000000000000000000000000000000000000000000000000000:" +
                "F104886044635416387482387D10D074A7AD6E3C474688ADAE85CF8C4D588C38");

        Assert.assertEquals(merkleRoot, MerklePathUtil.calculateVeriMerkleRoot(test));
    }

    @Test
    public void getMerkleRoot_WhenSinglePoPTransaction() {
        Sha256Hash merkleRoot = Sha256Hash.wrap("867E397100E699D2CB5CCB32A7281C5075A700502A49DE30A3F2CE909D373330");

        VeriBlockMerklePath test = new VeriBlockMerklePath("0:0:" +
                "7898EFAA2BF5E2783C9DAF2B62883827D3C2D1BABEC101DD622C42F47D63154A:" +
                "0000000000000000000000000000000000000000000000000000000000000000:" +
                "F104886044635416387482387D10D074A7AD6E3C474688ADAE85CF8C4D588C38");

        Assert.assertEquals(merkleRoot, MerklePathUtil.calculateVeriMerkleRoot(test));
    }

    @Test
    public void parse() {
        VeriBlockMerklePath input = new VeriBlockMerklePath("1:13:" +
                "2A014E88ED7AB65CDFAA85DAEAB07EEA6CBA5E147F736EDD8D02C2F9DDF0DEC6:" +
                "5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:" +
                "20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:" +
                "C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:" +
                "049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:" +
                "0000000000000000000000000000000000000000000000000000000000000000:" +
                "36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A");

        byte[] serialized = SerializeDeserializeService.serialize(input);
        VeriBlockMerklePath deserialized = SerializeDeserializeService.parseVeriBlockMerklePath(ByteBuffer.wrap(serialized));

        Assert.assertEquals(input, deserialized);
    }
}
