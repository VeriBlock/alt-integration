// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.transactions;

import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.util.Arrays;
import java.util.List;

public class FullBlock extends VeriBlockBlock {
    private List<VeriBlockTransaction> normalTransactions;
    public List<VeriBlockTransaction> getNormalTransactions() {
        return normalTransactions;
    }
    public void setNormalTransactions(List<VeriBlockTransaction> normalTransactions) {
        this.normalTransactions = normalTransactions;
    }

    private List<VeriBlockPoPTransaction> popTransactions;
    public List<VeriBlockPoPTransaction> getPoPTransactions() {
        return popTransactions;
    }
    public void setPoPTransactions(List<VeriBlockPoPTransaction> popTransactions) {
        this.popTransactions = popTransactions;
    }

    private BlockMetaPackage metaPackage;
    public BlockMetaPackage getMetaPackage() {
        return metaPackage;
    }
    public void setMetaPackage(BlockMetaPackage metaPackage) {
        this.metaPackage = metaPackage;
    }

    public FullBlock(int height, short version, VBlakeHash previousBlock, VBlakeHash previousKeystone, VBlakeHash secondPreviousKeystone, Sha256Hash merkleRoot, int timestamp, int difficulty, int nonce) {
        super(height, version, previousBlock, previousKeystone, secondPreviousKeystone, merkleRoot, timestamp, difficulty, nonce);
    }
    
    public FullBlock(VeriBlockBlock block) {
        super(block.getHeight(), block.getVersion(), block.getPreviousBlock(), block.getPreviousKeystone(),
                block.getSecondPreviousKeystone(), block.getMerkleRoot(), block.getTimestamp(), block.getDifficulty(), block.getNonce());
    }
    
    public static FullBlock parse(byte[] raw) {
        VeriBlockBlock block = SerializeDeserializeService.parseVeriBlockBlock(raw);
        return new FullBlock(block);
    }

    @Override
    public boolean equals(Object o) {
        ///TODO: avoid such expensive comparison
        return this == o || o != null &&
                (this.getClass() == o.getClass() || VeriBlockBlock.class == o.getClass()) &&
                Arrays.equals(SerializeDeserializeService.serialize(this), SerializeDeserializeService.serialize(((VeriBlockBlock)o)));
    }
}
