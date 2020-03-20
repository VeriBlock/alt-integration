// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.util.MerklePathUtil;

import java.util.Random;

public class BitcoinBlockDataTest {

    private void checkBlockData(BitcoinBlockData blockData) {
        Sha256Hash merkleRoot = blockData.getMerkleRoot();
        
        for (int index = 0; index < blockData.size(); index++) {
            MerklePath merklePath = blockData.getMerklePath(index);

            Assert.assertEquals(merklePath.getSubject(),
                                Sha256Hash.twiceOf(blockData.get(index)));
        
            Assert.assertEquals(merkleRoot,
                                MerklePathUtil.calculateMerkleRoot(merklePath));
        }

        try {
            blockData.getMerklePath(blockData.size());
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private BitcoinBlockData createBlockData(Random random, int count, int maxSize) {
        BitcoinBlockData blockData = new BitcoinBlockData();

        for (int i = 0; i < count; i++) {
            byte[] data = new byte[random.nextInt(maxSize)];
            random.nextBytes(data);
            blockData.add(data);
        }

        return blockData;
    }

    @Test
    public void emptyTest() {
        BitcoinBlockData blockData = new BitcoinBlockData();

        checkBlockData(blockData);
    }

    @Test
    public void singleItemTest() {
        Random random = new Random(0); // make the test repeatable
        BitcoinBlockData blockData = createBlockData(random, 1, 16);

        checkBlockData(blockData);
    }

    @Test
    public void multiItemTest() {
        Random random = new Random(0); // make the test repeatable
        BitcoinBlockData blockData = createBlockData(random, 5, 128);

        checkBlockData(blockData);
    }
}
