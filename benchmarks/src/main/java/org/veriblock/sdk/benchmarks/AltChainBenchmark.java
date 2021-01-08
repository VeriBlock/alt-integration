// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.benchmarks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.mock.AltChainPopMiner.EndorsementData;
import org.veriblock.sdk.mock.BitcoinDefaults;
import org.veriblock.sdk.mock.MockFactory;
import org.veriblock.sdk.mock.PoPMiningCoordinator;
import org.veriblock.sdk.mock.VeriBlockDefaults;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.util.KeyGenerator;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AltChainBenchmark {
    private static final Logger log = LoggerFactory.getLogger(AltChainBenchmark.class);

    private final AltChain altChain;
    private final PoPMiningCoordinator popMiningCoordinator;
    private final List<KeyPair> popMinerKeys;

    private final KeyPair vpmKey;
    private final int vtbCount = 3;
    private final int vbkSpacerCount = 0;
    private final int btcSpacerCount = 0;

    public AltChainBenchmark(AltChain altChain, PoPMiningCoordinator popMiningCoordinator, int popMinerCount) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        this.altChain = altChain;
        this.popMiningCoordinator = popMiningCoordinator;

        popMinerKeys = new ArrayList<>(popMinerCount);
        for (int i = 0; i < popMinerCount; i++) {
            popMinerKeys.add(KeyGenerator.generate());
        }

        vpmKey = KeyGenerator.generate();
    }

    public static AltChainBenchmark create(ConnectionSelector.Factory securityConnectionFactory, ConnectionSelector.Factory altChainConnectionFactory, ConnectionSelector.Factory mockPopMinerConnectionFactory, int popMinerCount) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, SQLException {
        MockFactory mockFactory = new MockFactory(mockPopMinerConnectionFactory);
        VeriBlockSecurity security = AltService.create(securityConnectionFactory);
        AltChain altChain = AltChain.create(security, altChainConnectionFactory.createConnection());
        altChain.bootstrap();
        mockFactory.getVeriBlockBlockchain().bootstrap(VeriBlockDefaults.bootstrap);
        mockFactory.getBitcoinBlockchain().bootstrap(BitcoinDefaults.bootstrap);
        return new AltChainBenchmark(altChain, mockFactory.getCoordinator(), popMinerCount);
    };

    public AltChain getAltChain() {
        return altChain;
    }

    public PoPMiningCoordinator getPoPMiningCoordinator() {
        return popMiningCoordinator;
    }

    public int getPoPMinerCount() {
        return popMinerKeys.size();
    }

    public BitcoinBlock mine() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, SQLException {
        List<EndorsementData> endorsementData = new ArrayList<>(popMinerKeys.size());
        AltChain.PoPData popData = altChain.getPoPData();
        for (KeyPair key : popMinerKeys) {
            endorsementData.add(new EndorsementData(popData.createPublicationData(key.getPublic().getEncoded()), key));
        }

        log.info("PoP Mining altchain block #{}", altChain.getBestChainHeight());
        VeriBlockBlock lastKnownVBKBlock = altChain.getVeriBlockSecurity().getVeriBlockBlockchain().getChainHead();
        BitcoinBlock lastKnownBTCBlock = altChain.getVeriBlockSecurity().getBitcoinBlockchain().getChainHead();
        List<PoPMiningCoordinator.Payloads> payloads = popMiningCoordinator.mine(
                endorsementData, lastKnownVBKBlock, lastKnownBTCBlock,
                vpmKey, vtbCount,vbkSpacerCount, btcSpacerCount);

        log.info("Mining altchain block #{}", altChain.getBestChainHeight());
        return altChain.mine(payloads);
    }

    public List<BitcoinBlock> mine(int blockCount) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, SQLException {
        List<BitcoinBlock> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(mine());
        }
        return blocks;
    }

}
