// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.util;

import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;

import java.util.List;

public class LogFormatter {

    static public String toString(byte[] array) {
        return Utils.bytesToHex(array);
    }

    static public String headerToString(VeriBlockBlock block) {
        return toString(block.getRaw());
    }

    static public String headerToString(BitcoinBlock block) {
        return toString(block.getRaw());
    }

    static public String toString(VeriBlockBlock block) {
        return block == null
             ? "null"
             : String.format("%s:%d",
                             block.getHash().toString(),
                             block.getHeight());
    }

    static public String toStringExtended(VeriBlockBlock block) {
        return block == null
             ? "null"
             : String.format("%s (header: %s)",
                             toString(block),
                             headerToString(block));
    }

    static public String toString(BitcoinBlock block) {
        return block == null
             ? "null"
             : String.format("%s",
                             block.getHash().toString());
    }

    static public String toStringExtended(BitcoinBlock block) {
        return block == null
             ? "null"
             : String.format("%s (header: %s)",
                             toString(block),
                             headerToString(block));
    }

    static public String toString(StoredBitcoinBlock block) {
        return block == null
             ? "null"
             : String.format("%s:%d",
                             toString(block.getBlock()),
                             block.getHeight());
    }

    static public String toStringExtended(StoredBitcoinBlock block) {
        return block == null
             ? "null"
             : String.format("%s (header: %s)",
                             toString(block),
                             headerToString(block.getBlock()));
    }

    static public String toString(AltPublication publication) {
        return String.format("ATV with VeriBlock tx id %s, endorsing to %s VeriBlock block, providing VeriBlock context (%s)",
                             publication.getTransaction().getId().toString(),
                             toString(publication.getContainingBlock()),
                             veriblockContextToString(publication.getContext()));
    }

    static public String toString(VeriBlockPublication publication) {
        return String.format("VTB with VeriBlock tx id %s in %s block, endorsing %s VeriBlock block to %s Bitcoin block, providing VeriBlock context (%s) and Bitcoin context (%s)",
                             publication.getTransaction().getId().toString(),
                             toString(publication.getContainingBlock()),
                             toString(publication.getTransaction().getPublishedBlock()),
                             toString(publication.getTransaction().getBlockOfProof()),
                             veriblockContextToString(publication.getContext()),
                             bitcoinContextToString(publication.getTransaction().getBlockOfProofContext()));
    }

    static public String veriblockContextToString(List<VeriBlockBlock> blocks) {
        return blocks.isEmpty()
             ? "empty"
             : String.format("%s to %s",
                             toString(blocks.get(0)),
                             toString(blocks.get(blocks.size() - 1)));
    }

    static public String bitcoinContextToString(List<BitcoinBlock> blocks) {
        return blocks.isEmpty()
             ? "empty"
             : String.format("%s to %s",
                             toString(blocks.get(0)),
                             toString(blocks.get(blocks.size() - 1)));
    }
}
