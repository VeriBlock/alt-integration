// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.junit.Assert;
import org.junit.Test;
import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.Output;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.models.VerificationException;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.Utils;

import java.nio.ByteBuffer;
import java.util.Collections;

public class VeriBlockTransactionTests {
    @Test
    public void serialize() {
        // Must be unique TX
        PublicationData publicationData = new PublicationData(0, new byte[0], new byte[0], new byte[0]);

        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("V8dy5tWcP7y36kxiJwxKPKUrWAJbjs"),
                Coin.valueOf(3500000000L),
                Collections.singletonList(Output.of("V7GghFKRA6BKqtHD7LTdT2ao93DRNA", 3499999999L)),
                5904L,
                publicationData,
                new byte[64],
                new byte[64], null);

        byte[] serialized = SerializeDeserializeService.serialize(tx);
        byte[] expected = Utils.decodeHex("014901011667A654EE3E0C918D8652B63829D7F3BEF98524BF899604D09DC30001011667901A1E11C650509EFC46E09E81678054D8562AF02B04D09DC2FF02171001080100010001000100" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        Assert.assertArrayEquals(expected, serialized);
    }

    @Test
    public void serialize_WhenNoOutputs() {
        // Must be unique TX
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("V8dy5tWcP7y36kxiJwxKPKUrWAJbjs"),
                Coin.valueOf(3500000000L),
                null,
                5904L,
                null,
                new byte[64],
                new byte[64], null);

        byte[] serialized = SerializeDeserializeService.serialize(tx);
        byte[] expected = Utils.decodeHex("012401011667A654EE3E0C918D8652B63829D7F3BEF98524BF899604D09DC300000217100100" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        Assert.assertArrayEquals(expected, serialized);
    }

    @Test
    public void serialize_WhenDataPresent() {
        // Must be unique TX
        PublicationData publicationData = new PublicationData(0, "header bytes".getBytes(), "payoutinfo bytes".getBytes(), "contextInfo bytes".getBytes());

        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("V8dy5tWcP7y36kxiJwxKPKUrWAJbjs"),
                Coin.valueOf(3500000000L),
                Collections.singletonList(Output.of("V7GghFKRA6BKqtHD7LTdT2ao93DRNA", 3499999999L)),
                5904L,
                publicationData,
                new byte[64],
                new byte[64], null);

        byte[] serialized = SerializeDeserializeService.serialize(tx);
        byte[] expected = Utils.decodeHex("017601011667A654EE3E0C918D8652B63829D7F3BEF98524BF899604D09DC30001011667901A1E11C650509EFC46E09E81678054D8562AF02B04D09DC2FF02171001350100010C6865616465722062797465730111636F6E74657874496E666F20627974657301107061796F7574696E666F206279746573" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        Assert.assertArrayEquals(expected, serialized);
    }

    @Test
    public void verify_WhenAllValid() {
        PublicationData publicationData = new PublicationData(0,  "header bytes".getBytes(), "payout info bytes".getBytes(), "context info bytes".getBytes());

        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)0x01,
                new Address("V5Ujv72h4jEBcKnALGc4fKqs6CDAPX"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                publicationData,
                Utils.decodeHex("30440220398B74708DC8F8AEE68FCE0C47B8959E6FCE6354665DA3ED87A83F708E62AA6B02202E6C00C00487763C55E92C7B8E1DD538B7375D8DF2B2117E75ACBB9DB7DEB3C7"),
                Utils.decodeHex("3056301006072A8648CE3D020106052B8104000A03420004DE4EE8300C3CD99E913536CF53C4ADD179F048F8FE90E5ADF3ED19668DD1DBF6C2D8E692B1D36EAC7187950620A28838DA60A8C9DD60190C14C59B82CB90319E"), null);

        ValidationService.verify(tx);
        Assert.assertEquals(
                Sha256Hash.wrap("1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C"), SerializeDeserializeService.getId(tx));
    }


    @Test
    public void checkSignature_WhenSignedByDifferentAddress() {
        PublicationData publicationData = new PublicationData(0,  "header bytes".getBytes(), "payout info bytes".getBytes(), "context info bytes".getBytes());

        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)0x01,
                new Address("V5Ujv72h4jEBcKnALGc4fKqs6CDAPX"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                publicationData,
                Utils.decodeHex("30440220398B74708DC8F8AEE68FCE0C47B8959E6FCE6354665DA3ED87A83F708E62AA6B02202E6C00C00487763C55E92C7B8E1DD538B7375D8DF2B2117E75ACBB9DB7DEB3C7"),
                Utils.decodeHex("3056301006072A8648CE3D020106552B8104000A03420004DE4EE8300C3CD99E913536CF53C4ADD179F048F8FE90E5ADF3ED19668DD1DBF6C2D8E692B1D36EAC7187950620A28838DA60A8C9DD60190C14C59B82CB90319E"), null);

        try {
            ValidationService.checkSignature(tx);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("VeriBlock transaction contains an invalid public key", e.getMessage());
        }
    }

    @Test
    public void checkSignature_WhenSignatureIncorrect() {
        PublicationData publicationData = new PublicationData(0,  "header bytes".getBytes(), "payout info bytes".getBytes(), "context info bytes".getBytes());

        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)0x01,
                new Address("V5Ujv72h4jEBcKnALGc4fKqs6CDAPX"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                publicationData,
                Utils.decodeHex("30440220398B74708DC8F8AEE68FCE0C47B8959E6FCE6354665DA3ED87583F708E62AA6B02202E6C00C00487763C55E92C7B8E1DD538B7375D8DF2B2117E75ACBB9DB7DEB3C7"),
                Utils.decodeHex("3056301006072A8648CE3D020106052B8104000A03420004DE4EE8300C3CD99E913536CF53C4ADD179F048F8FE90E5ADF3ED19668DD1DBF6C2D8E692B1D36EAC7187950620A28838DA60A8C9DD60190C14C59B82CB90319E"), null);

        try {
            ValidationService.checkSignature(tx);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("VeriBlock transaction is incorrectly signed", e.getMessage());
        }
    }

    @Test
    public void parse() {
        PublicationData publicationData = new PublicationData(0,  "header bytes".getBytes(), "payout info bytes".getBytes(), "context info bytes".getBytes());

        VeriBlockTransaction input = new VeriBlockTransaction(
                (byte)0x01,
                new Address("V5Ujv72h4jEBcKnALGc4fKqs6CDAPX"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                publicationData,
                Utils.decodeHex("30440220398B74708DC8F8AEE68FCE0C47B8959E6FCE6354665DA3ED87A83F708E62AA6B02202E6C00C00487763C55E92C7B8E1DD538B7375D8DF2B2117E75ACBB9DB7DEB3C7"),
                Utils.decodeHex("3056301006072A8648CE3D020106052B8104000A03420004DE4EE8300C3CD99E913536CF53C4ADD179F048F8FE90E5ADF3ED19668DD1DBF6C2D8E692B1D36EAC7187950620A28838DA60A8C9DD60190C14C59B82CB90319E"), null);

        byte[] serialized = SerializeDeserializeService.serialize(input);
        VeriBlockTransaction deserialized = SerializeDeserializeService.parseVeriBlockTransaction(ByteBuffer.wrap(serialized));

        Assert.assertEquals(input, deserialized);
        Assert.assertEquals(
                Sha256Hash.wrap("1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C"), SerializeDeserializeService.getId(deserialized));
    }
}
