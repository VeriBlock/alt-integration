// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.util.MerklePathUtil;

import java.util.Random;

public class VeriBlockBlockDataTest {

    private void checkBlockData(VeriBlockBlockData blockData) {
        Sha256Hash merkleRoot = blockData.getMerkleRoot();
        
        for (int index = 0; index < blockData.getRegularTransactions().size(); index++) {
            VeriBlockMerklePath merklePath = blockData.getRegularMerklePath(index);

            Assert.assertEquals(merklePath.getSubject(),
                                Sha256Hash.of(blockData.getRegularTransactions().get(index)));
        
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
                                Sha256Hash.of(blockData.getPoPTransactions().get(index)));
        
            Assert.assertEquals(merkleRoot,
                                MerklePathUtil.calculateVeriMerkleRoot(merklePath));
        }

        try {
            blockData.getPoPMerklePath(blockData.getPoPTransactions().size());
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private VeriBlockBlockData createBlockData(Random random, int regularCount, int popCount, int maxSize) {
        VeriBlockBlockData blockData = new VeriBlockBlockData();

        for (int i = 0; i < regularCount; i++) {
            byte[] data = new byte[random.nextInt(maxSize)];
            random.nextBytes(data);
            blockData.getRegularTransactions().add(data);
        }

        for (int i = 0; i < popCount; i++) {
            byte[] data = new byte[random.nextInt(maxSize)];
            random.nextBytes(data);
            blockData.getPoPTransactions().add(data);
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
        Random random = new Random(0); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 1, 1, 16);

        checkBlockData(blockData);
    }

    @Test
    public void multiItemTest() {
        Random random = new Random(0); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 5, 5, 128);

        checkBlockData(blockData);
    }
}
