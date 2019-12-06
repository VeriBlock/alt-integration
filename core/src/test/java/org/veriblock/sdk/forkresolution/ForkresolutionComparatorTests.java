// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.forkresolution;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import org.veriblock.sdk.AltChainParametersConfig;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.veriblock.sdk.forkresolution.ForkresolutionComparator.compareTwoBranches;

///TODO: unignore this
@Ignore
public class ForkresolutionComparatorTests {

    private VeriBlockSecurity veriBlockSecuritySpy;

    @Before
    public void setUp() throws IOException, SQLException {
        VeriBlockStore veriBlockStore = new VeriBlockStore(ConnectionSelector.setConnectionInMemory());
        BitcoinStore bitcoinStore = new BitcoinStore(ConnectionSelector.setConnectionInMemory());
        AuditorChangesStore auditStore = new AuditorChangesStore(ConnectionSelector.setConnectionInMemory());
        PoPTransactionsDBStoreMock popTxDBStore = new PoPTransactionsDBStoreMock();

        Context context = new Context(new MainNetParameters(), new BitcoinMainNetParameters(),
                                      veriBlockStore, bitcoinStore, auditStore, popTxDBStore);

        VeriBlockSecurity veriBlockSecuritySpy = Mockito.spy(new VeriBlockSecurity(context));

        Mockito.doReturn(ValidationResult.success()).when(veriBlockSecuritySpy).checkATVAgainstView(any());
        AltChainParametersConfig altChainParametersConfig = new AltChainParametersConfig();
        altChainParametersConfig.keystoneInterval = 10;
        Mockito.doReturn(altChainParametersConfig).when(veriBlockSecuritySpy).getAltChainParametersConfig();

        doNothing().when(veriBlockSecuritySpy).clearTemporaryPayloads();
        doNothing().when(veriBlockSecuritySpy).addTemporaryPayloads(any(), any());

        ForkresolutionComparator.setSecurity(veriBlockSecuritySpy);
    }

    @After
    public void tearDown() {
        veriBlockSecuritySpy.shutdown();
    }

    @Test
    public void GetPublicationScoreTest() {
        int leftPublicationHeight = -1;
        int rightPublicationHeight = -1;
        int lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(0, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = -1;
        rightPublicationHeight = 155;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 314;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(0, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 314;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(0, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 80;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(19057279, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 80;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(19057279, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 85;
        rightPublicationHeight = 55;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(9332543, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));

        leftPublicationHeight = 55;
        rightPublicationHeight = 85;
        lowestPublicationHeight = Math.min(leftPublicationHeight, rightPublicationHeight);

        Assert.assertEquals(100000000, getPublicationScoreTest(lowestPublicationHeight, leftPublicationHeight));
        Assert.assertEquals(9332543, getPublicationScoreTest(lowestPublicationHeight, rightPublicationHeight));
    }

    @Test
    public void getBestPublicationHeightSimpleTest() throws SQLException {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(120, getBestPublicationHeightTest(blockList));
    }

    @Test
    public void getBestPublicationHeightWithOneBlockInFutureTest() throws SQLException {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(130, getBestPublicationHeightTest(blockList));
    }

    @Test
    public void getBestPublicationHeightWithFirstBlockIsNotKeystoneTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(-1, getBestPublicationHeightTest(blockList));
    }

    @Test
    public void getBestPublicationHeightWithAllBlockInTheFutureTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(-1, getBestPublicationHeightTest(blockList));
    }


    @Test
    public void getReducedPublicationViewSimpleTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        List<Integer> reducedPulciationView = getReducedPublicationViewTest(blockList);

        Assert.assertEquals(2 , reducedPulciationView.size());
        Assert.assertEquals(new Integer(114), reducedPulciationView.get(0));
        Assert.assertEquals(new Integer(125), reducedPulciationView.get(1));
    }

    @Test
    public void getReducedPublicationViewWithFailFinalityDelayTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        List<Integer> reducedPulciationView = getReducedPublicationViewTest(blockList);

        Assert.assertEquals(1, reducedPulciationView.size());
        Assert.assertEquals(new Integer(114), reducedPulciationView.get(0));
    }

    @Test
    public void simpleCompareTwoBranchesLeftForkPriorityTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        ///TODO: should use static method calls eg ForkresolutionComparator.compareTwoBranches
        Assert.assertEquals(1, compareTwoBranches(leftFork, rigthFork));
    }

    @Test
    public void simpleCompareTwoBranchesRightForkPriorityTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(-1, compareTwoBranches(leftFork, rigthFork));
    }

    @Test
    public void simpleCompareTwoBranchesForksEqualTest() throws SQLException
    {
        PoPTransactionStore popTxStore = veriBlockSecuritySpy.getContext().getPopTxStore();
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

        Assert.assertEquals(0, compareTwoBranches(leftFork, rigthFork));
    }


    private AltPublication generateATV(int containingBlockHeight, int containingBlockTimestamp) {
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
                new VeriBlockBlock(containingBlockHeight, (short)2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("26BBFDA7D5E4462EF24AE02D67E47D78", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        containingBlockTimestamp,
                        16842752,
                        1),
                Collections.emptyList());

        return publication;
    }

    public static class PoPTransactionsDBStoreMock extends PoPTransactionsDBStore {
        private Map<String, List<AltPublication>> containingAltPublication;
        private Map<String, List<VeriBlockPublication>> containingVeriBlockPublication;
        private Map<String, List<AltPublication>> endoresedAltPublication;

        public PoPTransactionsDBStoreMock() throws SQLException {
            super(ConnectionSelector.setConnectionInMemory());
            this.containingAltPublication = new TreeMap<>();
            this.containingVeriBlockPublication = new TreeMap<>();
            this.endoresedAltPublication = new TreeMap<>();
        }

        @Override
        public List<AltPublication> getAltPublicationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containBlocks) throws SQLException {
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

        @Test
        public void test(){}
    }

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
