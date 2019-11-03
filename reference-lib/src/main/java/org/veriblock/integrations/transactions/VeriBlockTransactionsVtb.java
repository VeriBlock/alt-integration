// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.transactions;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.veriblock.integrations.transactions.bitcoin.BitcoinMerklePath;
import org.veriblock.integrations.transactions.bitcoin.BitcoinMerkleTree;
import org.veriblock.integrations.transactions.bitcoin.VeriBlockBitcoinTransactions;
import org.veriblock.integrations.transactions.signature.VeriBlockSignatureKeys;
import org.veriblock.integrations.transactions.signature.VeriBlockTransactionSigner;
import org.veriblock.sdk.Address;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BitcoinTransaction;
import org.veriblock.sdk.MerklePath;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.BitcoinUtils;

public class VeriBlockTransactionsVtb {
    
    // forbid constructor
    private VeriBlockTransactionsVtb() {};
    
    public static VeriBlockPoPTransaction createVtbAttachedToBitcoinBlock(Sha256Hash previousBlockHash) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        String sender = VeriBlockSignatureKeys.getDefaultAddress();
        byte[] publicKey = VeriBlockSignatureKeys.getDefaultPublicKey();
        
        VeriBlockBlock publishedBlock = SerializeDeserializeService.parseVeriBlockBlock(
                Base64.getDecoder().decode("AAATNQACp5PIctb2Rg6QvtYjQruWgZX4xRXT7tcnegnvrEvpn5XwoVYosGujtEwBkLXASVybis0HAcUjXru+nA=="));
        
        ///HACK: bitcoin header should contain publication data so we simply fill the header from the publication
        byte[] publicationData = VeriBlockBitcoinTransactions.publicationDataToBitcoinHeader(publishedBlock, new Address(sender));
        
        // now we should construct the Bitcoin Merkle Tree
        BitcoinTransaction btcTx = new BitcoinTransaction(publicationData);
        String btcTxId = VeriBlockBitcoinTransactions.bitcoinTransactionGetId(btcTx);
        
        List<String> btcTransactions = new ArrayList<>();
        btcTransactions.add(btcTxId);
        BitcoinMerkleTree tree = new BitcoinMerkleTree(true, btcTransactions);
        BitcoinMerklePath txPath = tree.getPathFromTxID(btcTxId);
        
        // and construct the Bitcoin block proof
        int bits = BitcoinUtils.bitcoinVeryHighPowEncodeToBits();
        Sha256Hash btcBlockRoot = VeriBlockBitcoinTransactions.merkleTreeRootHash(tree);
        BitcoinBlock blockOfProof = new BitcoinBlock(1, previousBlockHash, btcBlockRoot, 1, (int) bits, 1);
        
        txPath.getCompactFormat();
        
        VeriBlockPoPTransaction unsignedTx = new VeriBlockPoPTransaction(
                new Address(sender),
                publishedBlock,
                btcTx,
                new MerklePath(txPath.getCompactFormat()),
                blockOfProof,
                Collections.emptyList(),
                new byte[] { 0 },
                publicKey,
                null);
        
        VeriBlockPoPTransaction tx = VeriBlockTransactionSigner.sign(unsignedTx, VeriBlockSignatureKeys.getDefaultKey());
        return tx;
    }
    
    public static VeriBlockPoPTransaction createVtb() throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return createVtbAttachedToBitcoinBlock(Sha256Hash.ZERO_HASH);
    }
    
    public static VeriBlockPublication createVtbPublicationAttached(VeriBlockBlock attachToBlock, VeriBlockPoPTransaction tx) throws SignatureException {
        List<VeriBlockPoPTransaction> popTransactions = new ArrayList<>();
        popTransactions.add(tx);
        MerkleTree tree = MerkleTree.buildTree(new BlockMetaPackage(Sha256Hash.ZERO_HASH), popTransactions, Collections.emptyList());
        Sha256Hash root = tree.getMerkleRoot().getHash().trim(Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH);
        
        // Containing block should attach to existing VeriBlock block
        VeriBlockBlock containingBlock = new VeriBlockBlock(5000, (short)2,
                attachToBlock.getHash(),
                VBlakeHash.EMPTY_HASH,
                VBlakeHash.EMPTY_HASH,
                root,
                2,
                attachToBlock.getDifficulty(),
                1);
        
        VeriBlockMerklePath proof = tree.getMerklePath(tx.getId(), 0, false);
        VeriBlockPublication vtbPublication = new VeriBlockPublication(
                tx,
                proof,
                containingBlock,
                Collections.emptyList());

        return vtbPublication;
    }
    
    // NotAttached publications are not attached to any block so they cannot be added in addPayloads
    
    public static VeriBlockPublication createVtbPublicationNotAttached(VeriBlockPoPTransaction tx, int veriBlockHeight) {
        List<VeriBlockPoPTransaction> popTransactions = new ArrayList<>();
        popTransactions.add(tx);
        MerkleTree tree = MerkleTree.buildTree(new BlockMetaPackage(Sha256Hash.ZERO_HASH), popTransactions, Collections.emptyList());
        Sha256Hash root = tree.getMerkleRoot().getHash().trim(Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH);
        
        VeriBlockBlock containingBlock = new VeriBlockBlock(veriBlockHeight, (short)2,
                VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                root,
                1553699059,
                16842752,
                1);
        
        VeriBlockMerklePath proof = tree.getMerklePath(tx.getId(), 0, false);
        VeriBlockPublication publication = new VeriBlockPublication(
                tx,
                proof,
                containingBlock,
                Collections.emptyList());
        
        return publication;
    }
    
    public static VeriBlockPublication createVtbPublicationNotAttached(VeriBlockPoPTransaction tx) {        
        return createVtbPublicationNotAttached(tx, 5000);
    }
    
    public static VeriBlockPublication createVtbPublicationNotAttached() throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        VeriBlockPoPTransaction tx = createVtb();
        VeriBlockPublication publication = createVtbPublicationNotAttached(tx);
        return publication;
    }
}
