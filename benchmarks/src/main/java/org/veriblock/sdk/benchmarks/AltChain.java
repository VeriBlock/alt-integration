// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.benchmarks;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.mock.BitcoinBlockchain;
import org.veriblock.sdk.mock.BitcoinBlockData;
import org.veriblock.sdk.mock.PoPMiningCoordinator.Payloads;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.rewards.PopRewardCalculator;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;

public class AltChain extends BitcoinBlockchain {
    private static final Logger log = LoggerFactory.getLogger(AltChain.class);

    // FIXME: obtain this from a config
    public static final int POP_REWARD_PAYMENT_DELAY = 500;

    private final VeriBlockSecurity security;

    public static AltChain create(VeriBlockSecurity security, Connection connection) throws SQLException {
        return new AltChain(security, AltChainDefaults.networkParameters, new BitcoinStore(connection));
    }

    public AltChain(VeriBlockSecurity security, BitcoinNetworkParameters networkParameters, BlockStore<StoredBitcoinBlock, Sha256Hash> store) throws SQLException {
        super(networkParameters, store);
        this.security = security;
        PopRewardCalculator.setSecurity(security);
    }

    public VeriBlockSecurity getVeriBlockSecurity() {
        return security;
    }

    public boolean bootstrap() throws SQLException {
        return bootstrap(AltChainDefaults.bootstrap);
    }

    private String getHash(Payloads popTx) {
        Sha256Hash hash = Sha256Hash.ZERO_HASH;

        for (AltPublication atv : popTx.atvs) {
            hash = Sha256Hash.of(hash.getBytes(), SerializeDeserializeService.serialize(atv));
        }
        for (VeriBlockPublication vtb : popTx.vtbs) {
            hash = Sha256Hash.of(hash.getBytes(), SerializeDeserializeService.serialize(vtb));
        }

        return hash.toString();
    };

    public BitcoinBlock mine(List<Payloads> popTransactions) throws SQLException {
        log.debug("Mining");
        BitcoinBlock minedBlock = super.mine(new BitcoinBlockData()); // we don't actually store anything in altchain blocks

        // FIXME: we need the block height
        assert(minedBlock.equals(getChainHead()));

        // we don't pre-validate payloads
        BlockIndex blockIndex = new BlockIndex(getBestChainHeight(), minedBlock.getHash().toString());
        List<AltPublication> atvs = popTransactions.stream().map((tx) -> tx.atvs)
                                                            .flatMap(Collection::stream)
                                                            .collect(Collectors.toList());
        List<VeriBlockPublication> vtbs = popTransactions.stream().map((tx) -> tx.vtbs)
                                                                  .flatMap(Collection::stream)
                                                                  .collect(Collectors.toList());
        security.addPayloads(blockIndex, vtbs, atvs);

        log.debug("Storing PoP transactions");
        AltChainBlock containingBlock = new AltChainBlock(minedBlock.getHash().toString(), getBestChainHeight(), minedBlock.getTimestamp());
        for (Payloads popTx : popTransactions) {
            assert(popTx.atvs.size() == 1);
            AltPublication atv = popTx.atvs.get(0);

            PublicationData publicationData = atv.getTransaction().getPublicationData();
            BitcoinBlock altchainBlock = SerializeDeserializeService.parseBitcoinBlock(publicationData.getHeader());

            AltChainBlock endorsedBlock = new AltChainBlock(altchainBlock.getHash().toString(),
                                                            ByteBuffer.wrap(publicationData.getContextInfo()).getInt(),
                                                            altchainBlock.getTimestamp());

            PoPTransactionData popTxData = new PoPTransactionData(getHash(popTx), atv, popTx.vtbs);
            security.getContext().getPopTxStore().addPoPTransaction(popTxData, containingBlock, endorsedBlock);
        }

        return minedBlock;
    }

    public PopPayoutRound calculateRewards() throws SQLException {
        return calculateRewards(getBestChainHeight());
    }

    public AltChainBlock getAsAltChainBlock(int height) throws SQLException {
        BitcoinBlock block = get(height);
        return block == null ?  null
                             : new AltChainBlock(block.getHash().toString(), height, block.getTimestamp());
    }

    public PopPayoutRound calculateRewards(int previousHeight) throws SQLException {
        PopRewardCalculatorConfig config = PopRewardCalculator.getCalculatorConfig();

        if (previousHeight < POP_REWARD_PAYMENT_DELAY) {
            return new PopPayoutRound(0, 0, new ArrayList<>());
        }

        int checkHeight = previousHeight + 1 - POP_REWARD_PAYMENT_DELAY;

        AltChainBlock endorsedBlock = getAsAltChainBlock(checkHeight);

        List<AltChainBlock> endorsementBlocks = new ArrayList<>(config.popRewardSettlementInterval);
        for (int i = checkHeight + config.popRewardSettlementInterval; i > checkHeight; i--) {
            endorsementBlocks.add(getAsAltChainBlock(i));
        }

        List<AltChainBlock> difficultyBlocks = new ArrayList<>(config.popRewardSettlementInterval + config.popDifficultyAveragingInterval);
        for (int i = checkHeight + config.popRewardSettlementInterval - 1; i >= Math.max(checkHeight - config.popDifficultyAveragingInterval, 0); i--) {
            difficultyBlocks.add(getAsAltChainBlock(i));
        }

        BigDecimal difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(difficultyBlocks);

        return PopRewardCalculator.calculatePopPayoutRound(checkHeight,
                    endorsedBlock, endorsementBlocks,
                    difficulty);
    };

    public static class PoPData {
        byte[] header;
        byte[] contextInfo;
        VeriBlockBlock lastKnownVBKBlock;
        BitcoinBlock lastKnownBTCBlock;

        public PoPData(byte[] header, byte[] contextInfo, VeriBlockBlock lastKnownVBKBlock, BitcoinBlock lastKnownBTCBlock) {
            this.header = header;
            this.contextInfo = contextInfo;
            this.lastKnownVBKBlock = lastKnownVBKBlock;
            this.lastKnownBTCBlock = lastKnownBTCBlock;
        }

        public PublicationData createPublicationData(byte[] payoutInfo) {
            return new PublicationData(0, header, payoutInfo, contextInfo);
        }
    };

    public int getBestChainHeight() throws SQLException {
        return getStoredChainHead().getHeight();
    }

    public PoPData getPoPData() throws SQLException {
        return getPoPData(getBestChainHeight());
    }

    public PoPData getPoPData(int height) throws SQLException {
        BitcoinBlock block = get(height);
        return new PoPData(SerializeDeserializeService.getHeaderBytesBitcoinBlock(block),
                           ByteBuffer.allocate(4).putInt(height).array(), // we use this to store the block height
                           security.getVeriBlockBlockchain().getChainHead(),
                           security.getBitcoinBlockchain().getChainHead());
    }

}
