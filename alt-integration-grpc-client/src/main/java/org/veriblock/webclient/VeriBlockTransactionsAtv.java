// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webclient;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.veriblock.sdk.Address;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.Coin;
import org.veriblock.sdk.PublicationData;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.transactions.signature.VeriBlockSignatureKeys;
import org.veriblock.sdk.transactions.signature.VeriBlockTransactionSigner;

public class VeriBlockTransactionsAtv {

    // forbid constructor
    private VeriBlockTransactionsAtv() {};

    public static VeriBlockTransaction createAtv(PublicationData pubData) throws SignatureException {
        String sender = VeriBlockSignatureKeys.getDefaultAddress();
        byte[] publicKey = VeriBlockSignatureKeys.getDefaultPublicKey();

        VeriBlockTransaction unsignedTx = new VeriBlockTransaction(
                (byte) 1,
                new Address(sender),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                pubData,
                new byte[] { 0 },
                publicKey,
                null);

        VeriBlockTransaction tx = VeriBlockTransactionSigner.sign(unsignedTx, VeriBlockSignatureKeys.getDefaultKey());
        return tx;
    }

    public static VeriBlockTransaction createAtv() throws SignatureException {
        byte[] headerBytes = "header bytes".getBytes();
        byte[] payoutInfo = "payout info".getBytes();
        byte[] contextInfo = "context info".getBytes();
        PublicationData defaultPublicationData = new PublicationData(0, headerBytes, payoutInfo, contextInfo);
        VeriBlockTransaction tx = createAtv(defaultPublicationData);
        return tx;
    }

    public static AltPublication createAtvPublicationWithContainingBlockAttached(VeriBlockBlock containingBlockReference, VeriBlockTransaction tx) throws SignatureException {
        List<VeriBlockTransaction> normalTransactions = new ArrayList<>();
        normalTransactions.add(tx);
        MerkleTree tree = MerkleTree.buildTree(new BlockMetaPackage(Sha256Hash.ZERO_HASH), Collections.emptyList(), normalTransactions);

        // now that we have a merkle tree let's build a real block
        VeriBlockBlock containingBlock = new VeriBlockBlock(containingBlockReference.getHeight(), containingBlockReference.getVersion(),
                containingBlockReference.getPreviousBlock(),
                containingBlockReference.getPreviousKeystone(),
                containingBlockReference.getSecondPreviousKeystone(),
                tree.getMerkleRoot().getHash(),
                containingBlockReference.getTimestamp(),
                containingBlockReference.getDifficulty(),
                containingBlockReference.getNonce());

        AltPublication publication = new AltPublication(
                tx,
                tree.getMerklePath(SerializeDeserializeService.getId(tx), 0, true),
                containingBlock,
                Collections.emptyList());
        return publication;
    }

    public static AltPublication createAtvPublicationAttached(VeriBlockBlock attachToBlock, VeriBlockTransaction tx) throws SignatureException {
        VeriBlockBlock containingBlockReference = new VeriBlockBlock(1, (short)2,
                attachToBlock.getHash(),
                VBlakeHash.EMPTY_HASH,
                VBlakeHash.EMPTY_HASH,
                Sha256Hash.ZERO_HASH,
                2,
                attachToBlock.getDifficulty(),
                1);

        return createAtvPublicationWithContainingBlockAttached(containingBlockReference, tx);
    }

    // NotAttached publications are not attached to any block so they cannot be added in addPayloads

    public static AltPublication createAtvPublicationNotAttached(VeriBlockTransaction tx, int veriBlockHeight) {
        List<VeriBlockTransaction> normalTransactions = new ArrayList<>();
        normalTransactions.add(tx);
        MerkleTree tree = MerkleTree.buildTree(new BlockMetaPackage(Sha256Hash.ZERO_HASH), Collections.emptyList(), normalTransactions);
        Sha256Hash root = tree.getMerkleRoot().getHash().trim(Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH);

        VeriBlockBlock containingBlock = new VeriBlockBlock(veriBlockHeight, (short)2,
                VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                root,
                1553699059,
                16842752,
                1);

        VeriBlockMerklePath proof = tree.getMerklePath(SerializeDeserializeService.getId(tx), 0, true);
        AltPublication publication = new AltPublication(
                tx,
                proof,
                containingBlock,
                Collections.emptyList());

        return publication;
    }

    public static AltPublication createAtvPublicationNotAttached(VeriBlockTransaction tx) {        
        return createAtvPublicationNotAttached(tx, 5000);
    }

    public static AltPublication createAtvPublicationNotAttached() throws SignatureException {
        VeriBlockTransaction tx = createAtv();
        AltPublication publication = createAtvPublicationNotAttached(tx);
        return publication;
    }

    public static AltPublication createAtvPublicationNotAttached(PublicationData pubData) throws SignatureException {
        VeriBlockTransaction tx = createAtv(pubData);
        AltPublication publication = createAtvPublicationNotAttached(tx);
        return publication;
    }
}
