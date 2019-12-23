// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockMerklePath;

import java.util.ArrayList;
import java.util.List;

public class VeriBlockBlockData {

    List<byte[]> txs = new ArrayList<>();
    List<byte[]> popTxs = new ArrayList<>();

    byte[] blockContentMetapackage = new byte[0];

    public Sha256Hash getMerkleRoot() {
        return Sha256Hash.of(
                    Sha256Hash.of(blockContentMetapackage).getBytes(),
                    Sha256Hash.of(getMerkleRoot(txs).getBytes(),
                                  getMerkleRoot(popTxs).getBytes()).getBytes());
    }

    public List<byte[]> getRegularTransactions() {
        return txs;
    }

    public List<byte[]> getPoPTransactions() {
        return popTxs;
    }

    public void setBlockContentMetapackage(byte[] data) {
        blockContentMetapackage = data;
    }

    public byte[] getBlockContentMetapackage(byte[] data) {
        return blockContentMetapackage;
    }

    private VeriBlockMerklePath getMerklePath(int treeIndex, int index) {
        assert(treeIndex == 0 || treeIndex == 1);

        List<byte[]> transactions = treeIndex == 0 ? txs : popTxs;

        Sha256Hash subject = Sha256Hash.of(transactions.get(index));

        List<Sha256Hash> layers = getMerkleLayers(index, transactions);

        // the other transaction subtree
        layers.add(getMerkleRoot(treeIndex == 1 ? txs : popTxs));

        layers.add(Sha256Hash.of(blockContentMetapackage));

        return new VeriBlockMerklePath(treeIndex, index, subject, layers);
    }

    public VeriBlockMerklePath getRegularMerklePath(int index) {
        return getMerklePath(0, index);
    }

    public VeriBlockMerklePath getPoPMerklePath(int index) {
        return getMerklePath(1, index);
    }

    private Sha256Hash getMerkleRoot(List<byte[]> txs) {
        return calculateSubtreeHash(0, 0, txs);
    }

    // calculate the number of bits it takes to store size()
    private int getMaxDepth(List<byte[]> txs) {
        return (int)(Math.log(txs.size()) / Math.log(2) + 1);
    }

    // at each depth, there are 2**depth subtrees
    // leaves are at the depth equal to getMaxDepth()
    private Sha256Hash calculateSubtreeHash(int index, int depth, List<byte[]> txs) {
        if (depth >= getMaxDepth(txs)) {
            return Sha256Hash.of(index < txs.size() ? txs.get(index) : new byte[0]);
        }

        return Sha256Hash.of(calculateSubtreeHash(index * 2, depth + 1, txs).getBytes(),
                             calculateSubtreeHash(index * 2 + 1, depth + 1, txs).getBytes());
    }

    private List<Sha256Hash> getMerkleLayers(int index, List<byte[]> txs) {
        if (index >= txs.size())
            throw new IndexOutOfBoundsException("index must be less than size()");

        int maxDepth = getMaxDepth(txs);
        int layerIndex = index;

        // 2 layers will be added by get*MerklePath()
        List<Sha256Hash> layers = new ArrayList<>(maxDepth + 2);

        for (int depth = maxDepth; depth > 0; depth--) {
            // invert the last bit of layerIndex to reach the opposite subtree
            Sha256Hash layer = calculateSubtreeHash(layerIndex ^ 1, depth, txs);
            layers.add(layer);

            layerIndex /= 2;
        }

        return layers;
    }

}
