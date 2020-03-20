// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;

public class PoPMiningCoordinator {
    private static final Logger log = LoggerFactory.getLogger(PoPMiningCoordinator.class);

    private final AltChainPopMiner apm;
    private final VeriBlockPopMiner vpm;

    public PoPMiningCoordinator(AltChainPopMiner apm, VeriBlockPopMiner vpm) {
        this.apm = apm;
        this.vpm = vpm;
    }

    public AltChainPopMiner getAltChainPopMiner() {
        return apm;
    }

    public VeriBlockPopMiner getVeriBlockPopMiner() {
        return vpm;
    }

    public void mineSpacerBlocks(int vbkBlockCount, int btcBlockCount) throws SQLException {
        if (vbkBlockCount != 0 || btcBlockCount != 0) {
            log.debug("mining {} VeriBlock and {} Bitcoin spacer blocks", vbkBlockCount, btcBlockCount);
        }

        for (int i = 0; i < btcBlockCount; i++) {
            vpm.getBitcoinBlockchain().mine(new BitcoinBlockData());
        }

        for (int i = 0; i < vbkBlockCount; i++) {
            vpm.getVeriBlockBlockchain().mine(new VeriBlockBlockData());
        }
    };

    public static class Payloads {
        public List<AltPublication> atvs = new ArrayList<>();
        public List<VeriBlockPublication> vtbs = new ArrayList<>();
    }

    public Payloads mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair key, int vtbCount, int vbkSpacerCount, int btcSpacerCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return mine(new AltChainPopMiner.EndorsementData(publicationData, key),
                    lastKnownVBKBlock, lastKnownBTCBlock,
                    vtbCount, vbkSpacerCount, btcSpacerCount);
    }

    public Payloads mine(AltChainPopMiner.EndorsementData endorsementData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, int vtbCount, int vbkSpacerCount, int btcSpacerCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return mine(Arrays.asList(endorsementData),
                    lastKnownVBKBlock, lastKnownBTCBlock,
                    endorsementData.key,
                    vtbCount, vbkSpacerCount, btcSpacerCount).get(0);
    }

    public List<Payloads> mine(List<AltChainPopMiner.EndorsementData> endorsementData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair vpmKey, int vtbCount, int vbkSpacerCount, int btcSpacerCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        List<AltPublication> atvs = apm.mine(endorsementData, lastKnownVBKBlock);

        VeriBlockBlock lastVBKBlock = lastKnownVBKBlock;
        BitcoinBlock lastBTCBlock = lastKnownBTCBlock;

        List<VeriBlockPublication> vtbs = new ArrayList<>(vtbCount);
        for (int i = 0; i < vtbCount; i++) {
            mineSpacerBlocks(vbkSpacerCount, btcSpacerCount);

            VeriBlockPublication vtb = vpm.mine(vpm.getVeriBlockBlockchain().getChainHead(),
                                                lastVBKBlock, lastBTCBlock,
                                                vpmKey);
            vtbs.add(vtb);

            lastVBKBlock = vtb.getContainingBlock();
            lastBTCBlock = vtb.getTransaction().getBlockOfProof();
        }

        List<Payloads> result = new ArrayList<>(atvs.size());
        for (AltPublication atv : atvs) {
            Payloads payloads = new Payloads();
            payloads.atvs.add(atv);
            payloads.vtbs = vtbs;
            result.add(payloads);
        }

        return result;
    }

    public Payloads mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair key, int vtbCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException  {
        return mine(publicationData, lastKnownVBKBlock, lastKnownBTCBlock, key, vtbCount);
    }

}
