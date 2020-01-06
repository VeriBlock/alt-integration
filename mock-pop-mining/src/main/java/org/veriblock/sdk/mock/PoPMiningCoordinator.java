// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
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
import java.util.List;

import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;

public class PoPMiningCoordinator {

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
        for (int i = 0; i < btcBlockCount; i++) {
            vpm.getBitcoinBlockchain().mine(new BitcoinBlockData());
        }

        for (int i = 0; i < vbkBlockCount; i++) {
            vpm.getVeriBlockBlockchain().mine(new VeriBlockBlockData());
        }
    };

    public class Payloads {
        public List<AltPublication> atvs = new ArrayList<>();
        public List<VeriBlockPublication> vtbs = new ArrayList<>();
    }

    public Payloads mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair key, int vtbCount, int vbkSpacerCount, int btcSpacerCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        Payloads payloads = new Payloads();

        payloads.atvs.add(apm.mine(publicationData, lastKnownVBKBlock, key));

        VeriBlockBlock lastVBKBlock = lastKnownVBKBlock;
        BitcoinBlock lastBTCBlock = lastKnownBTCBlock;

        for (int i = 0; i < vtbCount; i++) {
            mineSpacerBlocks(vbkSpacerCount, btcSpacerCount);

            VeriBlockPublication vtb = vpm.mine(vpm.getVeriBlockBlockchain().getChainHead(),
                                                lastVBKBlock, lastBTCBlock,
                                                key);
            payloads.vtbs.add(vtb);

            lastVBKBlock = vtb.getContainingBlock();
            lastBTCBlock = vtb.getTransaction().getBlockOfProof();
        }

        return payloads;
    }

    public Payloads mine(PublicationData publicationData, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock, KeyPair key, int vtbCount) throws SQLException, SignatureException, InvalidKeyException, NoSuchAlgorithmException  {
        return mine(publicationData, lastKnownVBKBlock, lastKnownBTCBlock, key, vtbCount);
    }

}
