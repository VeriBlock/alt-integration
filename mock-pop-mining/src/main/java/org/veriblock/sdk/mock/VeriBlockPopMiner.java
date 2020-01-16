// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.List;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Base58;
import org.veriblock.sdk.util.Utils;

public class VeriBlockPopMiner {
    private final BitcoinBlockchain bitcoinBlockchain;
    private final VeriBlockBlockchain veriblockBlockchain;

    public VeriBlockPopMiner(VeriBlockBlockchain veriblockBlockchain, BitcoinBlockchain bitcoinBlockchain) {
        this.bitcoinBlockchain = bitcoinBlockchain;
        this.veriblockBlockchain = veriblockBlockchain;
    }

    public BitcoinBlockchain getBitcoinBlockchain() {
        return bitcoinBlockchain;
    }

    public VeriBlockBlockchain getVeriBlockBlockchain() {
        return veriblockBlockchain;
    }

    private byte[] createPublicationData(VeriBlockBlock publishedBlock, Address address) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(80);
        buffer.put(SerializeDeserializeService.serializeHeaders(publishedBlock));
        buffer.put(address.getPoPBytes());
        buffer.flip();

        byte[] payoutInfo = new byte[80];
        buffer.get(payoutInfo);

        return payoutInfo;
    }

    private BitcoinTransaction createBitcoinTx(VeriBlockBlock publishedBlock, Address address) {
        byte[] publicationData = createPublicationData(publishedBlock, address);
        return new BitcoinTransaction(publicationData);
    }

    private Address deriveAddress(PublicKey key) {
        String data = "V" + Base58.encode(Sha256Hash.of(key.getEncoded()).getBytes()).substring(0, 24);
        
        Sha256Hash hash = Sha256Hash.of(data.getBytes(StandardCharsets.UTF_8));
        String checksum = Base58.encode(hash.getBytes()).substring(0, 4 + 1);

        return new Address(data + checksum);
    
    }
    
    private VeriBlockPoPTransaction signTransaction(VeriBlockPoPTransaction tx, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {        
        byte[] signature = Utils.signMessageWithPrivateKey(SerializeDeserializeService.getId(tx).getBytes(),
                                                           privateKey);

        return new VeriBlockPoPTransaction(
                tx.getAddress(),
                tx.getPublishedBlock(),
                tx.getBitcoinTransaction(),
                tx.getMerklePath(),
                tx.getBlockOfProof(),
                tx.getBlockOfProofContext(),
                signature,
                tx.getPublicKey(),
                tx.getNetworkByte());
    }

    public VeriBlockPublication mine(VeriBlockBlock blockToEndorse, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair key) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        Address address = deriveAddress(key.getPublic());

        // publish an endorsement transaction to Bitcoin

        BitcoinTransaction bitcoinProofTx = createBitcoinTx(blockToEndorse, address);

        BitcoinBlockData btcBlockData = new BitcoinBlockData();
        btcBlockData.add(bitcoinProofTx.getRawBytes());

        BitcoinBlock blockOfProof = bitcoinBlockchain.mine(btcBlockData);
       
        // create a VeriBlock PoP transaction

        List<BitcoinBlock> blockOfProofContext = bitcoinBlockchain.getContext(lastKnownBTCBlock);

        VeriBlockPoPTransaction popTx = signTransaction(
                                                new VeriBlockPoPTransaction(
                                                    address,
                                                    blockToEndorse,
                                                    bitcoinProofTx,
                                                    btcBlockData.getMerklePath(0),
                                                    blockOfProof,
                                                    blockOfProofContext,
                                                    new byte[1],
                                                    key.getPublic().getEncoded(),
                                                    veriblockBlockchain.getNetworkParameters().getTransactionMagicByte()),
                                                key.getPrivate());

        // publish the PoP transaction to VeriBlock

        VeriBlockBlockData blockData = new VeriBlockBlockData();
        blockData.getPoPTransactions().add(popTx);

        VeriBlockBlock block = veriblockBlockchain.mine(blockData);

        // create a VTB

        List<VeriBlockBlock> context = veriblockBlockchain.getContext(lastKnownVBKBlock);

        return new VeriBlockPublication(popTx,
                                        blockData.getPoPMerklePath(0),
                                        block,
                                        context);
    };
}
