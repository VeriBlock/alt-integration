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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

public class VeriBlockTransactionTests {
    @Test
    public void serialize() {
        // Must be unique TX
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("V8dy5tWcP7y36kxiJwxKPKUrWAJbjs"),
                Coin.valueOf(3500000000L),
                Collections.singletonList(Output.of("V7GghFKRA6BKqtHD7LTdT2ao93DRNA", 3499999999L)),
                5904L,
                new byte[0],
                new byte[64],
                new byte[64], null);

        byte[] serialized = SerializeDeserializeService.serialize(tx);
        byte[] expected = Utils.decodeHex("014101011667A654EE3E0C918D8652B63829D7F3BEF98524BF899604D09DC30001011667901A1E11C650509EFC46E09E81678054D8562AF02B04D09DC2FF0217100100" +
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
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("V8dy5tWcP7y36kxiJwxKPKUrWAJbjs"),
                Coin.valueOf(3500000000L),
                Collections.singletonList(Output.of("V7GghFKRA6BKqtHD7LTdT2ao93DRNA", 3499999999L)),
                5904L,
                "VeriBlock".getBytes(StandardCharsets.US_ASCII),
                new byte[64],
                new byte[64], null);

        byte[] serialized = SerializeDeserializeService.serialize(tx);
        byte[] expected = Utils.decodeHex("014A01011667A654EE3E0C918D8652B63829D7F3BEF98524BF899604D09DC30001011667901A1E11C650509EFC46E09E81678054D8562AF02B04D09DC2FF021710010956657269426C6F636B" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                "4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        Assert.assertArrayEquals(expected, serialized);
    }

    @Test
    public void verify_WhenAllValid() {
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(218000000L),
                Collections.singletonList(Output.of("VFFDWUMLJwLRuNzH4NX8Rm32E59n6d", 217998200L)),
                61L,
                "VeriBlock".getBytes(StandardCharsets.US_ASCII),
                Utils.decodeHex("30450220034DC73796E9870E6679F47E48F3AC794327FD19D9023C228CD134D8ED87B796022100AD0CE8A520AAE704447920CA365D57A881A82A7455293A9C10E622E0BDD732AF"),
                Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEtVgobuGeWdi00PclBbYkkdI5rTqvZlfVKrXCoJyT7DYeETWbJSepJKeRNar5xh65FQ009A6JKZ7VTdU3LrssiA=="), null);

        ValidationService.verify(tx);
        Assert.assertEquals(
                Sha256Hash.wrap("9F220A932B166348EAAEC440B769F43C21C779B96ABA52FEB439C65E6B929E42"), SerializeDeserializeService.getId(tx));
    }


    @Test
    public void checkSignature_WhenSignedByDifferentAddress() {
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(218000000L),
                Collections.singletonList(Output.of("VFFDWUMLJwLRuNzH4NX8Rm32E59n6d", 217998200L)),
                61L,
                "VeriBlock".getBytes(StandardCharsets.US_ASCII),
                Utils.decodeHex("3044022079CDED0243F4AC12CAE5328221FC9E1691D8F7D830BB0B5BDB698F8BB23EA94E022041722E483C03C76E0B03FC0F12D271A42C48DF9578101F163B0A4158CD2F5A7E"),
                Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEy0J+QaARSHQICkseKreSDiLNLRiMhxQN76RH7l/ES7hI4cDbXvIG3i5wAvbIaVK+SCOkwI5l5M2+uQSouVdjqg=="), null);

        try {
            ValidationService.checkSignature(tx);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("VeriBlock transaction contains an invalid public key", e.getMessage());
        }
    }

    @Test
    public void checkSignature_WhenSignatureIncorrect() {
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte)1,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(218000000L),
                Collections.singletonList(Output.of("VFFDWUMLJwLRuNzH4NX8Rm32E59n6d", 217998200L)),
                61L,
                null,
                Utils.decodeHex("30450220034DC73796E9870E6679F47E48F3AC794327FD19D9023C228CD134D8ED87B796022100AD0CE8A520AAE704447920CA365D57A881A82A7455293A9C10E622E0BDD732AF"),
                Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEtVgobuGeWdi00PclBbYkkdI5rTqvZlfVKrXCoJyT7DYeETWbJSepJKeRNar5xh65FQ009A6JKZ7VTdU3LrssiA=="), null);

        try {
            ValidationService.checkSignature(tx);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertEquals("VeriBlock transaction is incorrectly signed", e.getMessage());
        }
    }

    @Test
    public void parse() {
        VeriBlockTransaction input = new VeriBlockTransaction(
                (byte)1,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(218000000L),
                Collections.singletonList(Output.of("VFFDWUMLJwLRuNzH4NX8Rm32E59n6d", 217998200L)),
                61L,
                "VeriBlock".getBytes(StandardCharsets.US_ASCII),
                Utils.decodeHex("30450220034DC73796E9870E6679F47E48F3AC794327FD19D9023C228CD134D8ED87B796022100AD0CE8A520AAE704447920CA365D57A881A82A7455293A9C10E622E0BDD732AF"),
                Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEtVgobuGeWdi00PclBbYkkdI5rTqvZlfVKrXCoJyT7DYeETWbJSepJKeRNar5xh65FQ009A6JKZ7VTdU3LrssiA=="), null);

        byte[] serialized = SerializeDeserializeService.serialize(input);
        VeriBlockTransaction deserialized = SerializeDeserializeService.parseVeriBlockTransaction(ByteBuffer.wrap(serialized));

        Assert.assertEquals(input, deserialized);
        Assert.assertEquals(
                Sha256Hash.wrap("9F220A932B166348EAAEC440B769F43C21C779B96ABA52FEB439C65E6B929E42"), SerializeDeserializeService.getId(deserialized));
    }
}
