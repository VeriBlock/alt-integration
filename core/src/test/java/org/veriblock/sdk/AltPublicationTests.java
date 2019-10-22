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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class AltPublicationTests {

    @Test
    public void verify_WhenValid() {
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

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:0:1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C:0000000000000000000000000000000000000000000000000000000000000000:0000000000000000000000000000000000000000000000000000000000000000"),
                new VeriBlockBlock(5000, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1553699059,
                        16842752,
                        1),
                Collections.emptyList());

        ValidationService.verify(publication);
    }


    @Test
    public void checkMerklePath_WhenMerklePathProvesADifferentTransaction() {
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

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:13:5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:E20ED2CFFAC2DDB4E85C8A852BD63320324B6014259DA1E0FE4491F084704997:20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:0000000000000000000000000000000000000000000000000000000000000000:36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A"),
                new VeriBlockBlock(5000, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1553699059,
                        16842752,
                        1),
                Collections.emptyList());

        try {
            ValidationService.checkMerklePath(publication);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue("VeriBlock transaction cannot be proven by merkle path".equals(e.getMessage()));
        }
    }

    @Test
    public void checkMerklePath_WhenMerkleRootsDoNotMatch() {
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

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:0:1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C:0000000000000000000000000000000000000000000000000000000000000000:0000000000000000000000000000000000000000000000000000000000000000"),
                new VeriBlockBlock(5000, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("0356EB39B851682679F9A0131A4E4A5F", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1553699059,
                        16842752,
                        1),
                Collections.emptyList());

        try {
            ValidationService.checkMerklePath(publication);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue("VeriBlock transaction does not belong to containing block".equals(e.getMessage()));
        }
    }


    @Test
    public void checkBlocks_WhenNotContiguous() {
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

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:0:1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C:0000000000000000000000000000000000000000000000000000000000000000:0000000000000000000000000000000000000000000000000000000000000000"),
                new VeriBlockBlock(5000, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1553699059,
                        16842752,
                        1),
                Arrays.asList(SerializeDeserializeService.parseVeriBlockBlock(Base64.getDecoder().decode("AAATbQAC+QNCG0kCwUNJxXulsJNWN4YGed3VXuT9IQguGGhuQZwPGl6HY18fMkR2ONB77VybkYoHAhMwn88yVg==")),
                        SerializeDeserializeService.parseVeriBlockBlock(Base64.getDecoder().decode("AAATbgACR5aVyd6a4kNFGHl2sJNWN4YGed3VXuT9IQguGGhuqei8W2tBNI+T1dj6apHx1VybkZ8HAhzCNiNzkg=="))));


        try {
            ValidationService.checkBlocks(publication);
            Assert.fail();
        } catch (VerificationException e) {
            Assert.assertTrue("Blocks are not contiguous".equals(e.getMessage()));
        }
    }


    @Test
    public void serializeRoundtrip() {
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

        AltPublication input = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:0:1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C:0000000000000000000000000000000000000000000000000000000000000000:0000000000000000000000000000000000000000000000000000000000000000"),
                new VeriBlockBlock(5000, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1553699059,
                        16842752,
                        1),
                Collections.emptyList());

        byte[] serialized = SerializeDeserializeService.serialize(input);
        AltPublication deserialized = SerializeDeserializeService.parseAltPublication(serialized);

        Assert.assertEquals(input, deserialized);
    }


    @Test
    public void blobParseVerifyTest() {
        byte[] data = Utils.decodeHex("01580101166772F51AB208D32771AB1506970EEB664462730B838E0203E800010701370100010C6865616465722062797465730112636F6E7465787420696E666F20627974657301117061796F757420696E666F2062797465734630440220398B74708DC8F8AEE68FCE0C47B8959E6FCE6354665DA3ED87A83F708E62AA6B02202E6C00C00487763C55E92C7B8E1DD538B7375D8DF2B2117E75ACBB9DB7DEB3C7583056301006072A8648CE3D020106052B8104000A03420004DE4EE8300C3CD99E913536CF53C4ADD179F048F8FE90E5ADF3ED19668DD1DBF6C2D8E692B1D36EAC7187950620A28838DA60A8C9DD60190C14C59B82CB90319E04000000010400000000201FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C040000000220000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000040000013880002449C60619294546AD825AF03B0935637860679DDD55EE4FD21082E18686E26BBFDA7D5E4462EF24AE02D67E47D785C9B90F301010000000000010100");
        AltPublication deserialized = SerializeDeserializeService.parseAltPublication(data);
        ValidationService.verify(deserialized);
    }


    @Test
    public void parseWhenInvalidContextCount() {
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

        VeriBlockBlock block = new VeriBlockBlock(5000, (short) 2,
                VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                1553699059,
                16842752,
                1);

        List<VeriBlockBlock> context = new ArrayList<>();
        for (int i = 0; i < AltPublication.MAX_CONTEXT_COUNT + 1; i++) {
            context.add(block);
        }

        AltPublication input = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:0:1FEC8AA4983D69395010E4D18CD8B943749D5B4F575E88A375DEBDC5ED22531C:0000000000000000000000000000000000000000000000000000000000000000:0000000000000000000000000000000000000000000000000000000000000000"),
                block,
                context);

        byte[] serialized = SerializeDeserializeService.serialize(input);

        try {
            SerializeDeserializeService.parseAltPublication(serialized);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unexpected context count"));
        }
    }
}
