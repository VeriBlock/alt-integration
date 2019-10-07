// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webclient;

import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MerkleTree {
    private MerkleNode merkleRoot;
    public MerkleNode getMerkleRoot() {
        return merkleRoot;
    }

    private MerkleTree() {

    }

    public VeriBlockMerklePath getMerklePath(Sha256Hash hash, Integer index, boolean isRegularTx) {
        List<Sha256Hash> path = new ArrayList<>();
        search(hash, merkleRoot, path);

        StringBuilder compactFormat = new StringBuilder();
        compactFormat.append(isRegularTx ? 1 : 0)
                .append(":")
                .append(index)
                .append(":")
                .append(hash)
                .append(":");

        compactFormat.append(String.join(":", path.stream()
                .map(Sha256Hash::toString)
                .collect(Collectors.toList())));

        return new VeriBlockMerklePath(compactFormat.toString());
    }

    private boolean search(Sha256Hash hash, MerkleNode node, List<Sha256Hash> path) {
        if (node.hash.equals(hash)) return true;

        if (node.left != null && search(hash, node.left, path)) {
            path.add(node.right.hash);
            return true;
        }
        if (node.right != null && search(hash, node.right, path)) {
            path.add(node.left.hash);
            return true;
        }

        return false;
    }


    public static MerkleTree buildTree(BlockMetaPackage meta, List<VeriBlockPoPTransaction> popTransactions, List<VeriBlockTransaction> normalTransactions) {
        MerkleTree tree = new MerkleTree();
        tree.merkleRoot = new MerkleNode(Sha256Hash.ZERO_HASH);
        tree.merkleRoot.left = new MerkleNode(meta.getHash());

        List<MerkleNode> pop = popTransactions.stream().map(tx -> new MerkleNode(SerializeDeserializeService.getId(tx))).collect(Collectors.toList());
        MerkleNode popTree = build(pop);

        List<MerkleNode> regular = normalTransactions.stream().map(tx -> new MerkleNode(SerializeDeserializeService.getId(tx))).collect(Collectors.toList());
        MerkleNode regularTree = build(regular);

        tree.merkleRoot.right = build(Arrays.asList(popTree, regularTree));
        tree.merkleRoot = build(Arrays.asList(tree.merkleRoot.left, tree.merkleRoot.right));
        return tree;
    }

    public static MerkleNode build(List<MerkleNode> nodes) {
        if (nodes.size() == 0) return new MerkleNode(Sha256Hash.ZERO_HASH);
        if (nodes.size() == 1) return nodes.get(0);

        List<MerkleNode> layer = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i += 2) {
            MerkleNode left = nodes.get(i);
            MerkleNode right = (i + 1 < nodes.size()) ? nodes.get(i + 1) : left;

            MerkleNode node = new MerkleNode(Sha256Hash.of(left.hash.getBytes(), right.hash.getBytes()));
            node.left = left;
            node.right = right;
            layer.add(node);
        }

        return build(layer);
    }

    public static class MerkleNode {
        private Sha256Hash hash;
        public Sha256Hash getHash() {
            return hash;
        }

        private MerkleNode left;
        private MerkleNode right;

        MerkleNode(Sha256Hash hash) {
            this.hash = hash;
        }
    }

}
