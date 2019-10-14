// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.forkresolution;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;
import org.veriblock.integrations.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.*;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class ForkresolutionComparatorTests {

    private static AltPublication generateATV(int containingBlockHeight, int containingBlockTimestamp) {
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte) 0x01,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                Base64.getDecoder().decode("BP////8BEAARIjNEVWZ3iJmqu8zd7v8BAQABAf8="),
                Utils.decodeHex("304402201124F58AC7AF281A5B7889E02F726483DA1DC2387C5B58456F969B9B0AEF02FC022074F8522D51D4E01329E435657EF7F4D424D7DBD64F7E3FECBC9554C75988AA97"),
                Utils.decodeHex("3056301006072A8648CE3D020106052B8104000A03420004B558286EE19E59D8B4D0F72505B62491D239AD3AAF6657D52AB5C2A09C93EC361E11359B2527A924A79135AAF9C61EB9150D34F40E89299ED54DD5372EBB2C88"), null);

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:13:E20ED2CFFAC2DDB4E85C8A852BD63320324B6014259DA1E0FE4491F084704997:5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:0000000000000000000000000000000000000000000000000000000000000000:36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A"),
                new VeriBlockBlock(containingBlockHeight, (short) 2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("0356EB39B851682679F9A0131A4E4A5F", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        containingBlockTimestamp,
                        16842752,
                        1),
                Collections.emptyList());

        return publication;
    }

    private class PoPTransactionsDBStoreMock extends PoPTransactionsDBStore {
        private Map<String, List<AltPublication>> containingAltPublication;
        private Map<String, List<VeriBlockPublication>> containingVeriBlockPublication;
        private Map<String, List<AltPublication>> endoresedAltPublication;

        public PoPTransactionsDBStoreMock() throws SQLException {
        }

        {
            this.containingAltPublication = new TreeMap<String, List<AltPublication>>();
            this.containingVeriBlockPublication = new TreeMap<String, List<VeriBlockPublication>>();
            this.endoresedAltPublication = new TreeMap<String, List<AltPublication>>();
        }


        @Override
        public List<AltPublication> getAltPublciationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containBlocks) throws SQLException {
            Set<AltPublication> altPublications1 = new HashSet<AltPublication>();
            for (AltChainBlock block : containBlocks) {
                altPublications1.addAll(containingAltPublication.get(block.getHash()));
            }

            Set<AltPublication> altPublications2 = new HashSet<AltPublication>();
            altPublications2.addAll((endoresedAltPublication.get(endorsedBlock.getHash())));
            altPublications2.retainAll(altPublications1);
            return new ArrayList<AltPublication>(altPublications2);
        }

        @Override
        public void addPoPTransaction(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock) throws SQLException {
            List<AltPublication> altPublications = containingAltPublication.get(containingBlock.getHash());
            if (altPublications != null) {
                altPublications.add(popTx.altPublication);
            } else {
                altPublications = new ArrayList<AltPublication>();
                altPublications.add(popTx.altPublication);
                containingAltPublication.put(containingBlock.getHash(), altPublications);
            }

            List<VeriBlockPublication> veriBlockPublications = containingVeriBlockPublication.get(containingBlock.getHash());
            if (veriBlockPublications != null) {
                veriBlockPublications.addAll(popTx.veriBlockPublications);
            } else {
                containingVeriBlockPublication.put(containingBlock.getHash(), new ArrayList<VeriBlockPublication>(popTx.veriBlockPublications));
            }

            altPublications = endoresedAltPublication.get(endorsedBlock.getHash());
            if (altPublications != null) {
                altPublications.add(popTx.altPublication);
            } else {
                altPublications = new ArrayList<AltPublication>();
                altPublications.add(popTx.altPublication);
                endoresedAltPublication.put(endorsedBlock.getHash(), altPublications);
            }
        }

        @Override
        public List<AltPublication> getAltPublicationsFromBlock(AltChainBlock block) throws SQLException {
            return containingAltPublication.get(block.getHash());
        }

        @Override
        public List<VeriBlockPublication> getVeriBlockPublicationsFromBlock(AltChainBlock block) throws SQLException {
            return containingVeriBlockPublication.get(block.getHash());
        }

        @Override
        public void clear() throws SQLException {
            this.containingAltPublication = new TreeMap<String, List<AltPublication>>();
            this.containingVeriBlockPublication = new TreeMap<String, List<VeriBlockPublication>>();
            this.endoresedAltPublication = new TreeMap<String, List<AltPublication>>();
        }

    }

    private class VeriBlockSecurityMock extends VeriBlockSecurity {

        public VeriBlockSecurityMock(Context context) throws SQLException {
            super(context);
        }

        public VeriBlockSecurityMock(VeriBlockSecurity security) throws SQLException {
            super(security.getSecurityFiles());
        }

        @Override
        public void clearTemporaryPayloads() {
        }

        @Override
        public ValidationResult checkATVAgainstView(AltPublication publication) throws BlockStoreException, SQLException {
            return ValidationResult.success();
        }

        @Override
        public boolean addTemporaryPayloads(List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws BlockStoreException, SQLException {
            return true;
        }
    }

    private class ForkresolutionComparatorTest extends ForkresolutionComparator {
        public List<Integer> getReducedPublicationViewTest(List<AltChainBlock> blocks) throws SQLException {
            return ForkresolutionComparator.getReducedPublicationView(blocks);
        }

        public int getBestPublicationHeightTest(List<AltChainBlock> blockSequence) throws SQLException {
            return ForkresolutionComparator.getBestPublicationHeight(blockSequence);
        }

        public long getPublicationScoreTest(int lowestKeystonePublication, int keystonePublication) {
            return ForkresolutionComparator.getPublicationScore(lowestKeystonePublication, keystonePublication);
        }
    }

    private static VeriBlockSecurityMock securityMock;

    @Before
    public void setUp() throws IOException, SQLException {
        String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();

        VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
        BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
        AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
        PoPTransactionsDBStoreMock popTxDBStore = new PoPTransactionsDBStoreMock();

        Context securityFiles = new Context(new MainNetParameters(), veriBlockStore, bitcoinStore, auditStore, popTxDBStore);

        securityMock = new VeriBlockSecurityMock(securityFiles);
        ForkresolutionComparator.setSecurity(securityMock);
    }

    @After
    public void tearDown() throws SQLException {
        VeriBlockIntegrationLibraryManager.shutdown();
    }

    @Test
    public void GetPublicationScoreTest() {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();

        int leftPublicationHeight = -1;
        int rightPublicationHeight = -1;
        int lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(0, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = -1;
        rightPublicationHeight = 155;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 314;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 314;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(0, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 80;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(19057279, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 80;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(19057279, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 85;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(9332543, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 85;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(9332543, comparatorTest.getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));
    }

    @Test
    public void GetBestPublicationHeightSimpleTest() throws SQLException {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(120, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);

        Assert.assertEquals(120, comparatorTest.getBestPublicationHeightTest(blockList));
    }

    @Test
    public void GetBestPublicationHeightWithOneBlockInFutureTest() throws SQLException {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(120, timestamp - 10);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);

        Assert.assertEquals(130, comparatorTest.getBestPublicationHeightTest(blockList));
    }

    @Test
    public void GetBestPublicationHeightWithFirstBlockIsNotKeystoneTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 49, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(120, timestamp - 10);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);

        Assert.assertEquals(-1, comparatorTest.getBestPublicationHeightTest(blockList));
    }

    @Test
    public void GetBestPublicationHeightWithAllBlockInTheFutureTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp - 10);
        AltPublication altPublication2 = generateATV(120, timestamp - 10);
        AltPublication altPublication3 = generateATV(140, timestamp - 10);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);

        Assert.assertEquals(-1, comparatorTest.getBestPublicationHeightTest(blockList));
    }


    @Test
    public void GetReducedPublciationViewSimpleTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);
        AltChainBlock block4 = new AltChainBlock("blockHash4", 53, timestamp);
        AltChainBlock block5 = new AltChainBlock("blockHash5", 54, timestamp);
        AltChainBlock block6 = new AltChainBlock("blockHash6", 55, timestamp);
        AltChainBlock block7 = new AltChainBlock("blockHash7", 56, timestamp);
        AltChainBlock block8 = new AltChainBlock("blockHash8", 57, timestamp);
        AltChainBlock block9 = new AltChainBlock("blockHash9", 58, timestamp);
        AltChainBlock block10 = new AltChainBlock("blockHash10", 59, timestamp);
        AltChainBlock block11 = new AltChainBlock("blockHash11", 60, timestamp);
        AltChainBlock block12 = new AltChainBlock("blockHash12", 61, timestamp);
        AltChainBlock block13 = new AltChainBlock("blockHash13", 62, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(120, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);
        AltPublication altPublication4 = generateATV(155, timestamp * 2);
        AltPublication altPublication5 = generateATV(165, timestamp * 2);
        AltPublication altPublication6 = generateATV(147, timestamp * 2);
        AltPublication altPublication7 = generateATV(114, timestamp * 2);
        AltPublication altPublication8 = generateATV(115, timestamp * 2);
        AltPublication altPublication9 = generateATV(178, timestamp * 2);
        AltPublication altPublication10 = generateATV(165, timestamp * 2);
        AltPublication altPublication11 = generateATV(145, timestamp * 2);
        AltPublication altPublication12 = generateATV(125, timestamp * 2);
        AltPublication altPublication13 = generateATV(176, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", altPublication4, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", altPublication5, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", altPublication6, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", altPublication7, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", altPublication8, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx9 = new PoPTransactionData("popTxHash9", altPublication9, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx10 = new PoPTransactionData("popTxHash10", altPublication10, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx11 = new PoPTransactionData("popTxHash11", altPublication11, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx12 = new PoPTransactionData("popTxHash12", altPublication12, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx13 = new PoPTransactionData("popTxHash13", altPublication13, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);
        popTxStore.addPoPTransaction(popTx4, block4, block4);
        popTxStore.addPoPTransaction(popTx5, block5, block5);
        popTxStore.addPoPTransaction(popTx6, block6, block6);
        popTxStore.addPoPTransaction(popTx7, block7, block7);
        popTxStore.addPoPTransaction(popTx8, block8, block8);
        popTxStore.addPoPTransaction(popTx9, block9, block9);
        popTxStore.addPoPTransaction(popTx10, block10, block10);
        popTxStore.addPoPTransaction(popTx11, block11, block11);
        popTxStore.addPoPTransaction(popTx12, block12, block12);
        popTxStore.addPoPTransaction(popTx13, block13, block13);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);
        blockList.add(block4);
        blockList.add(block5);
        blockList.add(block6);
        blockList.add(block7);
        blockList.add(block8);
        blockList.add(block9);
        blockList.add(block10);
        blockList.add(block11);
        blockList.add(block12);
        blockList.add(block13);

        List<Integer> reducedPulciationView = comparatorTest.getReducedPublicationViewTest(blockList);

        Assert.assertEquals(2 , reducedPulciationView.size());
        Assert.assertEquals(new Integer(114), reducedPulciationView.get(0));
        Assert.assertEquals(new Integer(125), reducedPulciationView.get(1));
    }

    @Test
    public void GetReducedPublciationViewWithFailFinalityDelayTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);
        AltChainBlock block4 = new AltChainBlock("blockHash4", 53, timestamp);
        AltChainBlock block5 = new AltChainBlock("blockHash5", 54, timestamp);
        AltChainBlock block6 = new AltChainBlock("blockHash6", 55, timestamp);
        AltChainBlock block7 = new AltChainBlock("blockHash7", 56, timestamp);
        AltChainBlock block8 = new AltChainBlock("blockHash8", 57, timestamp);
        AltChainBlock block9 = new AltChainBlock("blockHash9", 58, timestamp);
        AltChainBlock block10 = new AltChainBlock("blockHash10", 59, timestamp);
        AltChainBlock block11 = new AltChainBlock("blockHash11", 60, timestamp);
        AltChainBlock block12 = new AltChainBlock("blockHash12", 61, timestamp);
        AltChainBlock block13 = new AltChainBlock("blockHash13", 62, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(120, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);
        AltPublication altPublication4 = generateATV(155, timestamp * 2);
        AltPublication altPublication5 = generateATV(165, timestamp * 2);
        AltPublication altPublication6 = generateATV(147, timestamp * 2);
        AltPublication altPublication7 = generateATV(114, timestamp * 2);
        AltPublication altPublication8 = generateATV(115, timestamp * 2);
        AltPublication altPublication9 = generateATV(178, timestamp * 2);
        AltPublication altPublication10 = generateATV(165, timestamp * 2);
        AltPublication altPublication11 = generateATV(175, timestamp * 2);
        AltPublication altPublication12 = generateATV(176, timestamp * 2);
        AltPublication altPublication13 = generateATV(177, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", altPublication4, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", altPublication5, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", altPublication6, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", altPublication7, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", altPublication8, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx9 = new PoPTransactionData("popTxHash9", altPublication9, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx10 = new PoPTransactionData("popTxHash10", altPublication10, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx11 = new PoPTransactionData("popTxHash11", altPublication11, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx12 = new PoPTransactionData("popTxHash12", altPublication12, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx13 = new PoPTransactionData("popTxHash13", altPublication13, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);
        popTxStore.addPoPTransaction(popTx4, block4, block4);
        popTxStore.addPoPTransaction(popTx5, block5, block5);
        popTxStore.addPoPTransaction(popTx6, block6, block6);
        popTxStore.addPoPTransaction(popTx7, block7, block7);
        popTxStore.addPoPTransaction(popTx8, block8, block8);
        popTxStore.addPoPTransaction(popTx9, block9, block9);
        popTxStore.addPoPTransaction(popTx10, block10, block10);
        popTxStore.addPoPTransaction(popTx11, block11, block11);
        popTxStore.addPoPTransaction(popTx12, block12, block12);
        popTxStore.addPoPTransaction(popTx13, block13, block13);

        List<AltChainBlock> blockList = new ArrayList<AltChainBlock>();
        blockList.add(block1);
        blockList.add(block2);
        blockList.add(block3);
        blockList.add(block4);
        blockList.add(block5);
        blockList.add(block6);
        blockList.add(block7);
        blockList.add(block8);
        blockList.add(block9);
        blockList.add(block10);
        blockList.add(block11);
        blockList.add(block12);
        blockList.add(block13);

        List<Integer> reducedPulciationView = comparatorTest.getReducedPublicationViewTest(blockList);

        Assert.assertEquals(1, reducedPulciationView.size());
        Assert.assertEquals(new Integer(114), reducedPulciationView.get(0));
    }

    @Test
    public void SimpleCompareTwoBranchesLeftForkPriorityTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        // left branch
        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        // right branch
        AltChainBlock block4 = new AltChainBlock("blockHash4", 50, timestamp);
        AltChainBlock block5 = new AltChainBlock("blockHash5", 51, timestamp);
        AltChainBlock block6 = new AltChainBlock("blockHash6", 52, timestamp);
        AltChainBlock block7 = new AltChainBlock("blockHash7", 53, timestamp);
        AltChainBlock block8 = new AltChainBlock("blockHash8", 54, timestamp);

        AltPublication altPublication1 = generateATV(130, timestamp * 2);
        AltPublication altPublication2 = generateATV(114, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        AltPublication altPublication4 = generateATV(155, timestamp * 2);
        AltPublication altPublication5 = generateATV(165, timestamp * 2);
        AltPublication altPublication6 = generateATV(147, timestamp * 2);
        AltPublication altPublication7 = generateATV(136, timestamp * 2);
        AltPublication altPublication8 = generateATV(155, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", altPublication4, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", altPublication5, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", altPublication6, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", altPublication7, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", altPublication8, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);
        popTxStore.addPoPTransaction(popTx4, block4, block4);
        popTxStore.addPoPTransaction(popTx5, block5, block5);
        popTxStore.addPoPTransaction(popTx6, block6, block6);
        popTxStore.addPoPTransaction(popTx7, block7, block7);
        popTxStore.addPoPTransaction(popTx8, block8, block8);

        List<AltChainBlock> leftFork = new ArrayList<AltChainBlock>();
        leftFork.add(block1);
        leftFork.add(block2);
        leftFork.add(block3);

        List<AltChainBlock> rigthFork = new ArrayList<AltChainBlock>();
        rigthFork.add(block4);
        rigthFork.add(block5);
        rigthFork.add(block6);
        rigthFork.add(block7);
        rigthFork.add(block8);

        Assert.assertEquals(1, comparatorTest.compareTwoBranches(leftFork, rigthFork));
    }

    @Test
    public void SimpleCompareTwoBranchesRightForkPriorityTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        // left branch
        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        // right branch
        AltChainBlock block4 = new AltChainBlock("blockHash4", 50, timestamp);
        AltChainBlock block5 = new AltChainBlock("blockHash5", 51, timestamp);
        AltChainBlock block6 = new AltChainBlock("blockHash6", 52, timestamp);
        AltChainBlock block7 = new AltChainBlock("blockHash7", 53, timestamp);
        AltChainBlock block8 = new AltChainBlock("blockHash8", 54, timestamp);

        AltPublication altPublication1 = generateATV(160, timestamp * 2);
        AltPublication altPublication2 = generateATV(150, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        AltPublication altPublication4 = generateATV(155, timestamp * 2);
        AltPublication altPublication5 = generateATV(165, timestamp * 2);
        AltPublication altPublication6 = generateATV(147, timestamp * 2);
        AltPublication altPublication7 = generateATV(110, timestamp * 2);
        AltPublication altPublication8 = generateATV(155, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", altPublication4, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", altPublication5, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", altPublication6, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", altPublication7, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", altPublication8, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);
        popTxStore.addPoPTransaction(popTx4, block4, block4);
        popTxStore.addPoPTransaction(popTx5, block5, block5);
        popTxStore.addPoPTransaction(popTx6, block6, block6);
        popTxStore.addPoPTransaction(popTx7, block7, block7);
        popTxStore.addPoPTransaction(popTx8, block8, block8);

        List<AltChainBlock> leftFork = new ArrayList<AltChainBlock>();
        leftFork.add(block1);
        leftFork.add(block2);
        leftFork.add(block3);

        List<AltChainBlock> rigthFork = new ArrayList<AltChainBlock>();
        rigthFork.add(block4);
        rigthFork.add(block5);
        rigthFork.add(block6);
        rigthFork.add(block7);
        rigthFork.add(block8);

        Assert.assertEquals(-1, comparatorTest.compareTwoBranches(leftFork, rigthFork));
    }

    @Test
    public void SimpleCompareTwoBranchesForksEqualTest() throws SQLException
    {
        ForkresolutionComparatorTest comparatorTest = new ForkresolutionComparatorTest();
        PoPTransactionsDBStore popTxStore = securityMock.getSecurityFiles().getPopTxDBStore();
        int timestamp = 100;

        // left branch
        AltChainBlock block1 = new AltChainBlock("blockHash1", 50, timestamp);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 51, timestamp);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 52, timestamp);

        // right branch
        AltChainBlock block4 = new AltChainBlock("blockHash4", 50, timestamp);
        AltChainBlock block5 = new AltChainBlock("blockHash5", 51, timestamp);
        AltChainBlock block6 = new AltChainBlock("blockHash6", 52, timestamp);
        AltChainBlock block7 = new AltChainBlock("blockHash7", 53, timestamp);
        AltChainBlock block8 = new AltChainBlock("blockHash8", 54, timestamp);

        AltPublication altPublication1 = generateATV(160, timestamp * 2);
        AltPublication altPublication2 = generateATV(110, timestamp * 2);
        AltPublication altPublication3 = generateATV(140, timestamp * 2);

        AltPublication altPublication4 = generateATV(155, timestamp * 2);
        AltPublication altPublication5 = generateATV(165, timestamp * 2);
        AltPublication altPublication6 = generateATV(147, timestamp * 2);
        AltPublication altPublication7 = generateATV(110, timestamp * 2);
        AltPublication altPublication8 = generateATV(155, timestamp * 2);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", altPublication3, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", altPublication4, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", altPublication5, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", altPublication6, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", altPublication7, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", altPublication8, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, block1, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block2);
        popTxStore.addPoPTransaction(popTx3, block3, block3);
        popTxStore.addPoPTransaction(popTx4, block4, block4);
        popTxStore.addPoPTransaction(popTx5, block5, block5);
        popTxStore.addPoPTransaction(popTx6, block6, block6);
        popTxStore.addPoPTransaction(popTx7, block7, block7);
        popTxStore.addPoPTransaction(popTx8, block8, block8);

        List<AltChainBlock> leftFork = new ArrayList<AltChainBlock>();
        leftFork.add(block1);
        leftFork.add(block2);
        leftFork.add(block3);

        List<AltChainBlock> rigthFork = new ArrayList<AltChainBlock>();
        rigthFork.add(block4);
        rigthFork.add(block5);
        rigthFork.add(block6);
        rigthFork.add(block7);
        rigthFork.add(block8);

        Assert.assertEquals(0, comparatorTest.compareTwoBranches(leftFork, rigthFork));
    }

}
