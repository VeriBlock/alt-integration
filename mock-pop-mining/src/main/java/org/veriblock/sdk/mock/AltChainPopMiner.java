// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Base58;
import org.veriblock.sdk.util.Utils;

// Also known as APM
public class AltChainPopMiner {
    private final VeriBlockBlockchain veriblockBlockchain;

    public AltChainPopMiner(VeriBlockBlockchain veriblockBlockchain) {
        this.veriblockBlockchain = veriblockBlockchain;
    }

    public VeriBlockBlockchain getVeriBlockBlockchain() {
        return veriblockBlockchain;
    }

    private VeriBlockTransaction signTransaction(VeriBlockTransaction tx, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        byte[] signature = Utils.signMessageWithPrivateKey(SerializeDeserializeService.getId(tx).getBytes(),
                                                           privateKey);
        return new VeriBlockTransaction(
                tx.getType(),
                tx.getSourceAddress(),
                tx.getSourceAmount(),
                tx.getOutputs(),
                tx.getSignatureIndex(),
                tx.getPublicationData(),
                signature,
                tx.getPublicKey(),
                tx.getNetworkByte());
    }

    private Address deriveAddress(PublicKey key) {
        byte [] keyHash = Sha256Hash.of(key.getEncoded()).getBytes();
        String data = "V" + Base58.encode(keyHash).substring(0, 24);

        Sha256Hash hash = Sha256Hash.of(data.getBytes(StandardCharsets.UTF_8));
        String checksum = Base58.encode(hash.getBytes()).substring(0, 4 + 1);

        return new Address(data + checksum);
    }

    public AltPublication mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, KeyPair key) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        Address address = deriveAddress(key.getPublic());

        VeriBlockTransaction endorsementTx = signTransaction(
                new VeriBlockTransaction(
                        (byte) 1,
                        address,
                        Coin.valueOf(1),
                        new ArrayList<>(),
                        7,
                        publicationData,
                        new byte[1],
                        key.getPublic().getEncoded(),
                        veriblockBlockchain.getNetworkParameters().getTransactionMagicByte()),
                key.getPrivate());

        // publish the endorsement transaction to VeriBlock

        VeriBlockBlockData blockData = new VeriBlockBlockData();
        blockData.getRegularTransactions().add(endorsementTx);

        VeriBlockBlock block = veriblockBlockchain.mine(blockData);

        // create an ATV

        List<VeriBlockBlock> context = veriblockBlockchain.getContext(lastKnownVBKBlock);

        return new AltPublication(endorsementTx,
                                  blockData.getRegularMerklePath(0),
                                  block,
                                  context);
    }
}
