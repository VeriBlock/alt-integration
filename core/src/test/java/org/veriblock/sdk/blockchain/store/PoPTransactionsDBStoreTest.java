// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.store;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PoPTransactionsDBStoreTest {

    private static PoPTransactionsDBStore popTxDBStore;

    private static AltPublication generateATV()
    {
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

        return publication;
    }

    private static VeriBlockPublication generateVTB()
    {
        VeriBlockPoPTransaction tx = new VeriBlockPoPTransaction(
                new Address("VE6MJFzmGdYdrxC8o6UCovVv7BdhdX"),
                SerializeDeserializeService.parseVeriBlockBlock(Base64.getDecoder().decode("AAATNQACp5PIctb2Rg6QvtYjQruWgZX4xRXT7tcnegnvrEvpn5XwoVYosGujtEwBkLXASVybis0HAcUjXru+nA==")),
                new BitcoinTransaction(Base64.getDecoder().decode("AQAAAAEM508ftpSgAe67HX0IzmIIAz9b9yY+utLeB7v1GGcnMgAAAABqRzBEAiAM9JmKuhaCq+t3fnYoB6ndJjWgt3dz9mSRuD7jyHCZugIgM7fKJNxSCRW4sCAMvc+VumroZjVFha+cU+6G8nNi6+wBIQPluvBwnDlagu8L1jvIhHVkrCAdaajmv0SNh6pTocQxqv////8CtycNAAAAAAAZdqkUi56oVFBZ86kiRXr9FN3zhV2LEJmIrAAAAAAAAAAAU2pMUAAAEzUAAqeTyHLW9kYOkL7WI0K7loGV+MUV0+7XJ3oJ76xL6Z+V8KFWKLBro7RMAZC1wElcm4rNBwHFI167vpzU6UPv4YZN8EIWYVz5IIP0AAAAAA==")),
                new MerklePath("1659:94E097B110BA3ADBB7B6C4C599D31D675DE7BE6E722407410C08EF352BE585F1:4D66077FDF24246FFD6B6979DFEDEF5D46588654ADDEB35EDB11E993C131F612:023D1ABE8758C6F917EC0C65674BBD43D66EE14DC667B3117DFC44690C6F5AF1:096DDBA03CA952AF133FB06307C24171E53BF50AB76F1EDEABDE5E99F78D4EAD:2F32CF1BEE50349D56FC1943AF84F2D2ABDA520F64DC4DB37B2F3DB20B0ECB57:93E70120F1B539D0C1495B368061129F30D35F9E436F32D69967AE86031A2756:F554378A116E2142F9F6315A38B19BD8A1B2E6DC31201F2D37A058F03C39C06C:0824705685CECA003C95140434EE9D8BBBF4474B83FD4ECC2766137DB9A44D74:B7B9E52F3EE8CE4FBB8BE7D6CF66D33A20293F806C69385136662A74453FB162:1732C9A35E80D4796BABEA76AACE50B49F6079EA3E349F026B4491CFE720AD17:2D9B57E92AB51FE28A587050FD82ABB30ABD699A5CE8B54E7CD49B2A827BCB99:DCBA229ACDC6B7F028BA756FD5ABBFEBD31B4227CD4137D728EC5EA56C457618:2CF1439A6DBCC1A35E96574BDDBF2C5DB9174AF5AD0D278FE92E06E4AC349A42"),
                SerializeDeserializeService.parseBitcoinBlock(Base64.getDecoder().decode("AADAIBNPCdQ2WetTmC2a+0RLlvpLtYwDfSkUAAAAAAAAAAAAzgsamnfdDbEntd9Lw2jNasKZqXR9mR7C2svAtpmi5KWzkZtcbB8sF3NwO8A=")),
                Arrays.asList(
                        SerializeDeserializeService.parseBitcoinBlock(Base64.getDecoder().decode("AACAIPxhzJ1OrEstFHYaTQavip7wc9zX+14NAAAAAAAAAAAAoxUI1LEB0K0R5D75QZwj/Cd/Z+2ug8WY7nCGbbzvXiUmi5tcbB8sF+EYdK8=")),
                        SerializeDeserializeService.parseBitcoinBlock(Base64.getDecoder().decode("AABAID+OOYAwRDnYU8MC9uSWKF4RDiUSUVMTAAAAAAAAAAAAOacsIiaDgb2Nnc/gAvRyY0okzwRU3otQ+J4QiR5f+x3gjZtcbB8sF0QpCpI=")),
                        SerializeDeserializeService.parseBitcoinBlock(Base64.getDecoder().decode("AAAAILqkLkA0Wn+CajHTfbGl1ktntycyR3QiAAAAAAAAAAAAozrWvgY0ZHsmYzq4X6jeJYSAu7JeWcaOSLsLYIsSNisQkZtcbB8sF0nE0fA="))),
                Base64.getDecoder().decode("MEUCIQD03ORe3Ma/xKH0TvBOR+kKNI79Rx90Lxi4gqx3qNDongIgYXz3xKIiEZkWh7FxJsG7AHo7KiXFUPddZrhXqP2ddec="),
                Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEs8EEcMjo5Cbxk3dY2ftel6GJEXbLN9TBLUr0EHsao+iop1TAaiJ2DkTGBkL7qIOWfBl0DVIxM2Mm95YnUMjfmQ=="), null);

        VeriBlockPublication publication = new VeriBlockPublication(
                tx,
                new VeriBlockMerklePath("0:13:2A014E88ED7AB65CDFAA85DAEAB07EEA6CBA5E147F736EDD8D02C2F9DDF0DEC6:5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:0000000000000000000000000000000000000000000000000000000000000000:36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A"),
                SerializeDeserializeService.parseVeriBlockBlock(Base64.getDecoder().decode("AAATcAACRJxgYZKUVGrYJa8DsJNWN4YGed3VXuT9IQguGGhutTwfTiWeag3yNyGgs7S3q1ybkhEHAhHK8Bw/AQ==")),
                Collections.emptyList());

        return publication;
    }


    @Before
    public void setUp() throws SQLException
    {
        popTxDBStore = new PoPTransactionsDBStore(ConnectionSelector.setConnectionInMemory());
    }

    @After
    public void tearDown() throws SQLException
    {
       //popTxDBStore.clear();
    }

    @Test
    public void getAltPublicationsEndorsedTest() throws SQLException
    {
        int timestamp = 1200000000;
        AltChainBlock endorsedBlock = new AltChainBlock("endorsedBlockHash", 45, timestamp);

        int N = 10;
        List<AltChainBlock> containBlocks = new ArrayList<AltChainBlock>(N);
        for(int i = 0;i < N; i++)
        {
            String hash = "containBlockHash" + i;
            containBlocks.add(new AltChainBlock(hash, 100  + i, timestamp));
        }

        AltPublication altPublication = generateATV();

        for(int i = 0; i < N; i++)
        {

            PoPTransactionData popTxData = new PoPTransactionData("hash" + i, altPublication, new ArrayList<VeriBlockPublication>());
            popTxDBStore.addPoPTransaction(popTxData, containBlocks.get(i), endorsedBlock);
        }

        List<AltPublication> altPublications = popTxDBStore.getAltPublicationsEndorse(endorsedBlock, containBlocks);

        Assert.assertEquals(altPublications.size(), 1);

        Assert.assertEquals(altPublication, altPublications.get(0));
    }

    @Test
    public void getAltPublicationsFromBlockTest() throws SQLException
    {
        int timestamp = 1200000000;
        AltChainBlock containBlock = new AltChainBlock("containBlockHash", 50, timestamp);
        AltChainBlock endorsedBlock = new AltChainBlock("endorsedBlockHash", 45, timestamp);

        AltPublication altPublication = generateATV();

        PoPTransactionData popTxData = new PoPTransactionData("txHash", altPublication, new ArrayList<VeriBlockPublication>());

        popTxDBStore.addPoPTransaction(popTxData, containBlock, endorsedBlock);

        List<AltPublication> altPublicationList = popTxDBStore.getAltPublicationsFromBlock(containBlock);

        Assert.assertEquals(altPublicationList.size() , 1);

        Assert.assertEquals(altPublicationList.get(0), altPublication);
    }

    @Test
    public void getVeriBlockPublicationsFromBlockTest() throws SQLException
    {
        int timestamp = 1200000000;
        AltChainBlock containBlock = new AltChainBlock("containBlockHash", 50, timestamp);
        AltChainBlock endorsedBlock = new AltChainBlock("endorsedBlockHash", 45, timestamp);

        VeriBlockPublication veriBlockPublication = generateVTB();
        AltPublication altPublication = generateATV();

        PoPTransactionData popTxData = new PoPTransactionData("txHash", altPublication, new ArrayList<VeriBlockPublication>());
        popTxData.veriBlockPublications.add(veriBlockPublication);

        popTxDBStore.addPoPTransaction(popTxData, containBlock, endorsedBlock);

        List<VeriBlockPublication> altPublicationList = popTxDBStore.getVeriBlockPublicationsFromBlock(containBlock);

        Assert.assertEquals(altPublicationList.size() , 1);

        Assert.assertEquals(altPublicationList.get(0), veriBlockPublication);
    }
}
