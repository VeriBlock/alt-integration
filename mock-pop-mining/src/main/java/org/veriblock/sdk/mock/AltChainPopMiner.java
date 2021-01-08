// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.Coin;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

// Also known as APM
public class AltChainPopMiner {
    private static final Logger log = LoggerFactory.getLogger(AltChainPopMiner.class);

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

    public static class EndorsementData {
        PublicationData publicationData;
        KeyPair key;

        public EndorsementData(PublicationData publicationData, KeyPair key) {
            this.publicationData = publicationData;
            this.key = key;
        }
    }

    public AltPublication mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, KeyPair key) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return mine(new EndorsementData(publicationData, key), lastKnownVBKBlock);
    }

    public AltPublication mine(EndorsementData endorsementData, VeriBlockBlock lastKnownVBKBlock) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return mine(Arrays.asList(endorsementData), lastKnownVBKBlock).get(0);
    }

    private VeriBlockTransaction createEndorsementTx(EndorsementData endorsementData)  throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Address address = Address.fromPublicKey(endorsementData.key.getPublic().getEncoded());

        return signTransaction(
                new VeriBlockTransaction(
                        (byte) 1,
                        address,
                        Coin.valueOf(1),
                        new ArrayList<>(),
                        7,
                        endorsementData.publicationData,
                        new byte[1],
                        endorsementData.key.getPublic().getEncoded(),
                        veriblockBlockchain.getNetworkParameters().getTransactionMagicByte()),
                endorsementData.key.getPrivate());
    };

    public List<AltPublication> mine(List<EndorsementData> endorsements, VeriBlockBlock lastKnownVBKBlock) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        log.debug("Mining");

        // publish endorsement transactions to VeriBlock

        VeriBlockBlockData blockData = new VeriBlockBlockData();

        for (EndorsementData endorsementData : endorsements) {
            blockData.getRegularTransactions().add(createEndorsementTx(endorsementData));
        }

        VeriBlockBlock block = veriblockBlockchain.mine(blockData);

        // create ATVs

        List<VeriBlockBlock> context = veriblockBlockchain.getContext(lastKnownVBKBlock);

        List<AltPublication> atvs = new ArrayList<>(blockData.getRegularTransactions().size());
        for (int i = 0; i < blockData.getRegularTransactions().size(); i ++) {
            atvs.add(new AltPublication(blockData.getRegularTransactions().get(i),
                                        blockData.getRegularMerklePath(i),
                                        block,
                                        context));
        }

        return atvs;
    }
}
