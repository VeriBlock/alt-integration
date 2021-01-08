// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

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
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Base58;

public class RandomFactory {

    private final Random random;

    public RandomFactory() {
        random = new Random();
    }

    public RandomFactory(Random random) {
        this.random = random;
    }

    public Random getRandom() {
        return random;
    }

    public byte[] nextBytes(int count) {
        byte[] data = new byte[count];
        random.nextBytes(data);
        return data;
    }

    public Sha256Hash nextSha256Hash() {
        return Sha256Hash.wrap(nextBytes(Sha256Hash.BITCOIN_LENGTH));
    }

    public Address nextAddress() {
        Sha256Hash hash = nextSha256Hash();
        String data = "V" + Base58.encode(hash.getBytes()).substring(0, 24);

        Sha256Hash dataHash = Sha256Hash.of(data.getBytes(StandardCharsets.UTF_8));
        String checksum = Base58.encode(dataHash.getBytes()).substring(0, 4 + 1);

        return new Address(data + checksum);
    }

    public BitcoinBlock nextBitcoinBlock() {
        return SerializeDeserializeService.parseBitcoinBlock(nextBytes(Constants.HEADER_SIZE_BitcoinBlock));
    }

    public VeriBlockBlock nextVeriBlockBlock() {
        return SerializeDeserializeService.parseVeriBlockBlock(nextBytes(Constants.HEADER_SIZE_VeriBlockBlock));
    }

    public BitcoinTransaction nextBitcoinTransaction() {
        return new BitcoinTransaction(nextBytes(100));
    }

    public MerklePath nextMerklePath(int maxIndex) {
        int index = random.nextInt(maxIndex);

        int layerCount = (int)(Math.log(maxIndex) / Math.log(2) + 1);

        List<Sha256Hash> layers = new ArrayList<>(layerCount);
        for (int i = 0; i < layerCount; i++) {
            layers.add(nextSha256Hash());
        }

        return new MerklePath(index, nextSha256Hash(), layers);
    }

    public List<BitcoinBlock> nextBitcoinContext(int maxSize) {
        int size = random.nextInt(maxSize);
        List<BitcoinBlock> context = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            context.add(nextBitcoinBlock());
        }

        return context;
    }

    public Coin nextCoin(int maxUnits) {
        return Coin.valueOf(random.nextInt(maxUnits));
    }

    public Output nextOutput(int maxUnits) {
        return new Output(nextAddress(), nextCoin(maxUnits));
    }

    public List<Output> nextOutputs(int maxOutputs, int maxUnits) {
        int size = random.nextInt(maxOutputs);

        List<Output> outputs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            outputs.add(nextOutput(maxUnits));
        }

        return outputs;
    }

    public PublicationData nextPublicationData() {
        return new PublicationData(random.nextInt(1000000),
                                   nextBytes(16),
                                   nextBytes(32),
                                   nextBytes(64));
    };

}
