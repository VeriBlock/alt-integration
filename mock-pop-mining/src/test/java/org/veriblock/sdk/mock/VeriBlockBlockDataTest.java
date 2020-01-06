// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.util.MerklePathUtil;

public class VeriBlockBlockDataTest {

    private void checkBlockData(VeriBlockBlockData blockData) {
        Sha256Hash merkleRoot = blockData.getMerkleRoot();
        
        for (int index = 0; index < blockData.getRegularTransactions().size(); index++) {
            VeriBlockMerklePath merklePath = blockData.getRegularMerklePath(index);

            Assert.assertEquals(merklePath.getSubject(),
                                blockData.getRegularTransactions().getSubject(index));
        
            Assert.assertEquals(merkleRoot,
                                MerklePathUtil.calculateVeriMerkleRoot(merklePath));
        }

        try {
            blockData.getRegularMerklePath(blockData.getRegularTransactions().size());
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        for (int index = 0; index < blockData.getPoPTransactions().size(); index++) {
            VeriBlockMerklePath merklePath = blockData.getPoPMerklePath(index);

            Assert.assertEquals(merklePath.getSubject(),
                                blockData.getPoPTransactions().getSubject(index));
        
            Assert.assertEquals(merkleRoot,
                                MerklePathUtil.calculateVeriMerkleRoot(merklePath));
        }

        try {
            blockData.getPoPMerklePath(blockData.getPoPTransactions().size());
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private VeriBlockBlockData createBlockData(RandomFactory random, int regularCount, int popCount) {
        VeriBlockBlockData blockData = new VeriBlockBlockData();

        for (int i = 0; i < regularCount; i++) {
            VeriBlockTransaction tx = new VeriBlockTransaction(
                                            (byte)1,
                                            random.nextAddress(),
                                            random.nextCoin(1000000),
                                            random.nextOutputs(10, 100000),
                                            7,
                                            random.nextPublicationData(),
                                            random.nextBytes(40),
                                            random.nextBytes(40),
                                            (byte)0xAA);
            blockData.getRegularTransactions().add(tx);
        }

        for (int i = 0; i < popCount; i++) {
            VeriBlockPoPTransaction tx = new VeriBlockPoPTransaction(
                                            random.nextAddress(),
                                            random.nextVeriBlockBlock(),
                                            random.nextBitcoinTransaction(),
                                            random.nextMerklePath(256),
                                            random.nextBitcoinBlock(),
                                            random.nextBitcoinContext(16),
                                            random.nextBytes(40),
                                            random.nextBytes(40),
                                            (byte)0xAA);
            blockData.getPoPTransactions().add(tx);
        }

        return blockData;
    }

    @Test
    public void emptyTest() {
        VeriBlockBlockData blockData = new VeriBlockBlockData();

        checkBlockData(blockData);
    }

    @Test
    public void singleItemTest() {
        RandomFactory random = new RandomFactory(new Random(0)); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 1, 1);

        checkBlockData(blockData);
    }

    @Test
    public void multiItemTest() {
        RandomFactory random = new RandomFactory(new Random(0)); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 5, 8);

        checkBlockData(blockData);
    }
}
