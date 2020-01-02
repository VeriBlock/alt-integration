// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.Constants;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.Output;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Base58;
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

    private byte[] randomBytes(Random random, int count) {
        byte[] data = new byte[count];
        random.nextBytes(data);
        return data;
    }

    private Sha256Hash randomSha256Hash(Random random) {
        return Sha256Hash.wrap(randomBytes(random, Sha256Hash.BITCOIN_LENGTH));
    }

    private Address randomAddress(Random random) {
        Sha256Hash hash = randomSha256Hash(random);
        String data = "V" + Base58.encode(hash.getBytes()).substring(0, 24);

        Sha256Hash dataHash = Sha256Hash.of(data.getBytes(StandardCharsets.UTF_8));
        String checksum = Base58.encode(dataHash.getBytes()).substring(0, 4 + 1);

        return new Address(data + checksum);
    }

    private BitcoinBlock randomBitcoinBlock(Random random) {
        return SerializeDeserializeService.parseBitcoinBlock(randomBytes(random, Constants.HEADER_SIZE_BitcoinBlock));
    }

    private VeriBlockBlock randomVeriBlockBlock(Random random) {
        return SerializeDeserializeService.parseVeriBlockBlock(randomBytes(random, Constants.HEADER_SIZE_VeriBlockBlock));
    }

    private BitcoinTransaction randomBitcoinTransaction(Random random) {
        return new BitcoinTransaction(randomBytes(random, 100));
    }

    private MerklePath randomMerklePath(Random random, int maxIndex) {
        int index = random.nextInt(maxIndex);

        int layerCount = (int)(Math.log(maxIndex) / Math.log(2) + 1);

        List<Sha256Hash> layers = new ArrayList<>(layerCount);
        for (int i = 0; i < layerCount; i++) {
            layers.add(randomSha256Hash(random));
        }

        return new MerklePath(index, randomSha256Hash(random), layers);
    }

    private List<BitcoinBlock> randomBitcoinContext(Random random, int maxSize) {
        int size = random.nextInt(maxSize);
        List<BitcoinBlock> context = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            context.add(randomBitcoinBlock(random));
        }

        return context;
    }

    private Coin randomCoin(Random random, int maxUnits) {
        return Coin.valueOf(random.nextInt(maxUnits));
    }

    private Output randomOutput(Random random, int maxUnits) {
        return new Output(randomAddress(random), randomCoin(random, maxUnits));
    }

    private List<Output> randomOutputs(Random random, int maxOutputs, int maxUnits) {
        int size = random.nextInt(maxOutputs);

        List<Output> outputs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            outputs.add(randomOutput(random, maxUnits));
        }

        return outputs;
    }

    private PublicationData randomPublicationData(Random random) {
        return new PublicationData(random.nextInt(1000000),
                                   randomBytes(random, 16),
                                   randomBytes(random, 32),
                                   randomBytes(random, 64));
    };

    private VeriBlockBlockData createBlockData(Random random, int regularCount, int popCount) {
        VeriBlockBlockData blockData = new VeriBlockBlockData();

        for (int i = 0; i < regularCount; i++) {
            VeriBlockTransaction tx = new VeriBlockTransaction(
                                            (byte)1,
                                            randomAddress(random),
                                            randomCoin(random, 1000000),
                                            randomOutputs(random, 10, 100000),
                                            7,
                                            randomPublicationData(random),
                                            randomBytes(random, 40),
                                            randomBytes(random, 40),
                                            (byte)0xAA);
            blockData.getRegularTransactions().add(tx);
        }

        for (int i = 0; i < popCount; i++) {
            VeriBlockPoPTransaction tx = new VeriBlockPoPTransaction(
                                            randomAddress(random),
                                            randomVeriBlockBlock(random),
                                            randomBitcoinTransaction(random),
                                            randomMerklePath(random, 256),
                                            randomBitcoinBlock(random),
                                            randomBitcoinContext(random, 16),
                                            randomBytes(random, 40),
                                            randomBytes(random, 40),
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
        Random random = new Random(0); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 1, 1);

        checkBlockData(blockData);
    }

    @Test
    public void multiItemTest() {
        Random random = new Random(0); // make the test repeatable
        VeriBlockBlockData blockData = createBlockData(random, 5, 8);

        checkBlockData(blockData);
    }
}
