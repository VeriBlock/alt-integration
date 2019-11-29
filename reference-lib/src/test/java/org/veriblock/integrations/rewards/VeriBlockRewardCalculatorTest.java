// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;
import org.veriblock.integrations.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.Address;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Coin;
import org.veriblock.sdk.PublicationData;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VBlakeHash;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;
import org.veriblock.sdk.util.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.veriblock.integrations.rewards.PopRewardCalculator.calculatePopScoreFromEndorsements;

public class VeriBlockRewardCalculatorTest {

    private VeriBlockRewardCalculatorTest.VeriBlockSecurityMock securityMock;
    private VeriBlockSecurity veriBlockSecurity;

    @Before
    public void setUp() throws SQLException, IOException {
        VeriBlockIntegrationLibraryManager veriBlockIntegrationLibraryManager = new VeriBlockIntegrationLibraryManager();
        veriBlockSecurity = veriBlockIntegrationLibraryManager.init();

        String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();

        VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
        BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
        AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
        VeriBlockRewardCalculatorTest.PoPTransactionsDBStoreMock popTxDBStore = new VeriBlockRewardCalculatorTest.PoPTransactionsDBStoreMock();

        Context.init(veriBlockIntegrationLibraryManager.getVeriblockNetworkParameters(),
                     veriBlockIntegrationLibraryManager.getBitcoinNetworkParameters(),
                     veriBlockStore, bitcoinStore, auditStore, popTxDBStore);

        securityMock = new VeriBlockRewardCalculatorTest.VeriBlockSecurityMock();
        PopRewardCalculator.setSecurity(securityMock);
    }

    @After
    public void tearDown() {
        veriBlockSecurity.shutdown();
    }
    
    @Test
    public void roundsBasic() {        
        Assert.assertFalse(PopRewardCalculator.isKeystoneRound(RewardDefaults.ROUND_1));
        Assert.assertTrue(PopRewardCalculator.isKeystoneRound(RewardDefaults.ROUND_4));
        Assert.assertTrue(PopRewardCalculator.getRoundForBlockNumber(20) == RewardDefaults.ROUND_4);
        Assert.assertTrue(PopRewardCalculator.getRoundForBlockNumber(21) == RewardDefaults.ROUND_1);
        Assert.assertTrue(PopRewardCalculator.getIndexOfRound(13, RewardDefaults.ROUND_3) == 1);
    }
    
    @Test
    public void ratiosBasic() {
        Assert.assertTrue(PopRewardCalculator.getRoundRatio(RewardDefaults.ROUND_1).compareTo(PopRewardCalculator.getRoundRatio(RewardDefaults.ROUND_2)) < 0);
        Assert.assertTrue(PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_1).compareTo(BigDecimal.ZERO) < 0);
        Assert.assertTrue(PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_2)
                .compareTo(PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_1)) < 0);
        Assert.assertTrue(PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_4)
                .compareTo(PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_3)) < 0);
    }
    
    @Test
    public void ratiosForEndorsement() throws SQLException {
        PoPTransactionsDBStore popTxStore = Context.getPopTxDBStore();

        // we store a single endorsed block in the first (relative to our calculator) VeriBlock block
        String payoutInfo = "payout1";
        AltPublication altPublication1 = generateATV(0, payoutInfo);

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());

        AltChainBlock endorsedBlock = new AltChainBlock("blockHash1", 50, 100);
        AltChainBlock containingBlock = new AltChainBlock("blockHash2", 51, 101);

        popTxStore.addPoPTransaction(popTx1, containingBlock, endorsedBlock);

        // first block has the most score so the total score is 1.0

        List<AltChainBlock> endorsementBlocks = new ArrayList<AltChainBlock>();
        endorsementBlocks.add(containingBlock);

        BigDecimal totalScore = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        Assert.assertTrue(totalScore.compareTo(BigDecimal.ONE) == 0);

        popTxStore.clear();

        // score table has a grace period so two blocks still have the maximum score (1.0 for each block)

        altPublication1 = generateATV(0, payoutInfo);
        AltPublication altPublication2 = generateATV(1, payoutInfo);

        popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(popTx2, containingBlock, endorsedBlock);

        BigDecimal totalScore2 = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        Assert.assertTrue(totalScore2.compareTo(new BigDecimal(2)) == 0);

        popTxStore.clear();
        // score begins to decrease after the grace period so with 15 blocks we no longer have the maximum score
        for(int i = 0; i < 15; i++) {
            PoPTransactionData poptx = new PoPTransactionData("hash" + i, generateATV(i, payoutInfo), new ArrayList<VeriBlockPublication>());
            popTxStore.addPoPTransaction(poptx, containingBlock, endorsedBlock);
        }
        BigDecimal totalScore3 = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        Assert.assertTrue(totalScore3.compareTo(new BigDecimal(15)) < 0);

        popTxStore.clear();

        // score stops increasing after 50 blocks. Let's try.

        int lotsBlocksEndorsementsSize = 50;
        for(int i = 0; i < lotsBlocksEndorsementsSize; i++) {
            PoPTransactionData poptx = new PoPTransactionData("hash" + i, generateATV(i, payoutInfo), new ArrayList<VeriBlockPublication>());
            popTxStore.addPoPTransaction(poptx, containingBlock, endorsedBlock);
        }
        BigDecimal totalScore4 = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        // make sure the last position is not filled yet

        {
            PoPTransactionData poptx = new PoPTransactionData("hash" + lotsBlocksEndorsementsSize, generateATV(lotsBlocksEndorsementsSize, payoutInfo), new ArrayList<VeriBlockPublication>());
            popTxStore.addPoPTransaction(poptx, containingBlock, endorsedBlock);
        }
        
        BigDecimal totalScore5 = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        // assert the score is no longer growing
        Assert.assertTrue(totalScore4.compareTo(totalScore5) == 0);

        popTxStore.clear();
        // now let's try with two endorsements in the same block

        altPublication1 = generateATV(0, payoutInfo);
        altPublication2 = generateATV(0, payoutInfo);

        popTx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());
        popTx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(popTx1, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(popTx2, containingBlock, endorsedBlock);

        BigDecimal totalScore6 = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        // so many endorsements simply increase score proportionally to endorsements count
        Assert.assertTrue(totalScore6.compareTo(new BigDecimal(2)) == 0);
    }

    @Test
    public void popDifficultyCalculateTest() throws SQLException, IllegalArgumentException {
        PoPTransactionsDBStore popTxStore = Context.getPopTxDBStore();

        // simple case where we don't have any publication for this sequence of blocks
        AltChainBlock block1 = new AltChainBlock("blockHash1", 10, 100);
        AltChainBlock block2 = new AltChainBlock("blockHash2", 11, 100);
        AltChainBlock block3 = new AltChainBlock("blockHash3", 12, 100);
        AltChainBlock block4 = new AltChainBlock("blockHash4", 13, 100);

        List<AltChainBlock> blocks = new ArrayList<>(4);
        blocks.add(block1);
        blocks.add(block2);
        blocks.add(block3);
        blocks.add(block4);

        // set Difficult Averaging Interval to the blocks size
        PopRewardCalculator.getCalculatorConfig().popDifficultyAveragingInterval = blocks.size() - 2;
        PopRewardCalculator.getCalculatorConfig().popRewardSettlementInterval = blocks.size() - 2;

        BigDecimal difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(blocks);

        Assert.assertTrue(difficulty.compareTo(BigDecimal.ONE) == 0);

        // test case with the publications

        int bestPublication1 = 50;
        List<AltPublication> publications1 = new ArrayList<>();
        publications1.add(generateATV(bestPublication1, ""));
        publications1.add(generateATV(bestPublication1 + 5, ""));

        int bestPublication2 = 60;
        List<AltPublication> publications2 = new ArrayList<>();
        publications2.add(generateATV(bestPublication2, ""));
        publications2.add(generateATV(bestPublication2 + 5, ""));

        PoPTransactionData popTx1 = new PoPTransactionData("popTxHash1", publications1.get(0), new ArrayList<>());
        PoPTransactionData popTx2 = new PoPTransactionData("popTxHash2", publications1.get(1), new ArrayList<>());
        PoPTransactionData popTx3 = new PoPTransactionData("popTxHash3", publications2.get(0), new ArrayList<>());
        PoPTransactionData popTx4 = new PoPTransactionData("popTxHash4", publications2.get(1), new ArrayList<>());

        popTxStore.addPoPTransaction(popTx1, block2, block1);
        popTxStore.addPoPTransaction(popTx2, block2, block1);
        popTxStore.addPoPTransaction(popTx3, block3, block2);
        popTxStore.addPoPTransaction(popTx4, block3, block2);

        BigDecimal block1_score = PopRewardCalculator.calculatePopScoreFromEndorsements(publications1, bestPublication1);
        BigDecimal block2_score = PopRewardCalculator.calculatePopScoreFromEndorsements(publications2, bestPublication2);

        BigDecimal expected_difficult;
        expected_difficult = block1_score.add(block2_score).divide(new BigDecimal(PopRewardCalculator.getCalculatorConfig().popDifficultyAveragingInterval));

        difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(blocks);

        Assert.assertTrue(expected_difficult.compareTo(difficulty) == 0);

        // test case with more altPublications
        publications1.add(generateATV(bestPublication1 + PopRewardCalculator.getCalculatorConfig().relativeScoreLookupTable.size() - 10, ""));
        publications1.add(generateATV(bestPublication1 + PopRewardCalculator.getCalculatorConfig().relativeScoreLookupTable.size() - 5, ""));

        publications2.add(generateATV(bestPublication2 + PopRewardCalculator.getCalculatorConfig().relativeScoreLookupTable.size() - 12, ""));
        publications2.add(generateATV(bestPublication2 + PopRewardCalculator.getCalculatorConfig().relativeScoreLookupTable.size() - 6, ""));

        PoPTransactionData popTx5 = new PoPTransactionData("popTxHash5", publications1.get(2), new ArrayList<>());
        PoPTransactionData popTx6 = new PoPTransactionData("popTxHash6", publications1.get(3), new ArrayList<>());

        PoPTransactionData popTx7 = new PoPTransactionData("popTxHash7", publications2.get(2), new ArrayList<>());
        PoPTransactionData popTx8 = new PoPTransactionData("popTxHash8", publications2.get(3), new ArrayList<>());

        popTxStore.addPoPTransaction(popTx5, block2, block1);
        popTxStore.addPoPTransaction(popTx6, block2, block1);
        popTxStore.addPoPTransaction(popTx7, block3, block2);
        popTxStore.addPoPTransaction(popTx8, block3, block2);

        block1_score = PopRewardCalculator.calculatePopScoreFromEndorsements(publications1, bestPublication1);
        block2_score = PopRewardCalculator.calculatePopScoreFromEndorsements(publications2, bestPublication2);

        expected_difficult = block1_score.add(block2_score).divide(new BigDecimal(PopRewardCalculator.getCalculatorConfig().popDifficultyAveragingInterval));

        difficulty = PopRewardCalculator.calculatePopDifficultyForBlock(blocks);
        Assert.assertTrue(expected_difficult.compareTo(difficulty) == 0);
    }
    
    ///HACK: there is no difference if we change difficulty or score so we fix the difficulty and test score only
    @Test
    public void popRewardBasic() throws IllegalArgumentException {
        // blockNumber is used to detect current round only. Let's start with ROUND_1.
        int blockNumber = 1;
        // round 1 ratio is 0.97
        BigDecimal round1Ratio = PopRewardCalculator.getRoundRatio(RewardDefaults.ROUND_1);
        // round 1 slope is -0.00194‬
        BigDecimal round1Slope = PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_1);
        // this is a normal reward for round 1 blocks with score = 1 and difficulty = 1
        BigDecimal defaultRewardRound1 = new BigDecimal(PopRewardCalculator.getCalculatorConfig().basicReward).multiply(round1Ratio);
        
        // let's start with hardcoded difficulty
        BigDecimal defaultDifficulty = BigDecimal.ONE;
        BigDecimal defaultScore = BigDecimal.ONE;
        BigDecimal totalReward1 = PopRewardCalculator.calculateTotalPopBlockReward(blockNumber, defaultDifficulty, defaultScore);
        // with difficulty and score = 1 we should get the total reward per block = POP_DEFAULT_REWARD_PER_BLOCK
        Assert.assertTrue(totalReward1.compareTo(defaultRewardRound1) == 0);
        
        BigDecimal halfScore = new BigDecimal(0.5);
        BigDecimal totalReward2 = PopRewardCalculator.calculateTotalPopBlockReward(blockNumber, defaultDifficulty, halfScore);
        // when score is less than difficulty we get the reward proportional to score
        Assert.assertTrue(totalReward2.compareTo(defaultRewardRound1.multiply(halfScore)) == 0);
        
        // when score is higher than difficulty we begin to gradually decrease the reward
        BigDecimal doubleScore = new BigDecimal(2.0);
        BigDecimal totalReward3 = PopRewardCalculator.calculateTotalPopBlockReward(blockNumber, defaultDifficulty, doubleScore);
        //choppedNormalizedScore == 200
        //protoRewardPerPoint == -0.00194 * 100 + 0.97 == 0.776
        //totalReward == 0.776 * 200 * 10 Coins / 100 == 15.52 Coins
        BigDecimal expectedReward = round1Slope
                .multiply(new BigDecimal(100))
                .add(round1Ratio)
                .multiply(doubleScore)
                .multiply(new BigDecimal(PopRewardCalculator.getCalculatorConfig().basicReward));
        Assert.assertTrue(totalReward3.compareTo(expectedReward) == 0);
        // assert that the block reward is less than normal block reward
        Assert.assertTrue(totalReward3.compareTo(defaultRewardRound1.multiply(doubleScore)) < 0);
        
        // we limit the reward to 200% threshold (for normal blocks). Let's check this.
        BigDecimal doublePlusScore = new BigDecimal(2.1);
        BigDecimal totalReward4 = PopRewardCalculator.calculateTotalPopBlockReward(blockNumber, defaultDifficulty, doublePlusScore);
        Assert.assertTrue(totalReward3.compareTo(totalReward4) == 0);
        
        // test the keystone highest reward
        BigDecimal totalReward5 = PopRewardCalculator.calculateTotalPopBlockReward(20, BigDecimal.ONE, new BigDecimal(3.0));
        Assert.assertTrue(totalReward5.compareTo(totalReward4) > 0);
        // multiple endorsements may increase the score to 3.0, keystone block ratio is 1.7 (due to reward curve) 
        // so the payout is 3.0 * 1.7 = 5.1 * REWARD_PER_BLOCK
        Assert.assertTrue(totalReward5.longValue() < PopRewardCalculator.getCalculatorConfig().basicReward.longValue() * 6);
        Assert.assertTrue(totalReward5.longValue() > PopRewardCalculator.getCalculatorConfig().basicReward.longValue() * 5);
        
        // test over the highest reward
        BigDecimal totalReward6 = PopRewardCalculator.calculateTotalPopBlockReward(20, BigDecimal.ONE, new BigDecimal(4.0));
        // we see that the reward is no longer growing
        Assert.assertTrue(totalReward6.longValue() == totalReward5.longValue());
    }
    
    @Test
    public void popRewardSpecialCase() throws IllegalArgumentException{
        // blockNumber is used to detect current round and ROUND3 special case so we can mostly use any number here
        int blockNumber = 1;
        
        // let's start with hardcoded difficulty
        BigDecimal defaultDifficulty = BigDecimal.ONE;
        BigDecimal defaultScore = BigDecimal.ONE;
        BigDecimal halfScore = new BigDecimal(0.5);
        
        BigDecimal totalReward1 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber, defaultScore, defaultDifficulty);
        BigDecimal totalReward2 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber, halfScore, defaultDifficulty);
        Assert.assertTrue(totalReward1.compareTo(totalReward2) > 0);
        
        BigDecimal totalReward3 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber + 1, defaultScore, defaultDifficulty);
        BigDecimal totalReward4 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber + 1, halfScore, defaultDifficulty);
        Assert.assertTrue(totalReward3.compareTo(totalReward4) > 0);
        
        BigDecimal totalReward5 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber + 2, defaultScore, defaultDifficulty);
        BigDecimal totalReward6 = PopRewardCalculator.calculatePopRewardForBlock(blockNumber + 2, halfScore, defaultDifficulty);
        // round 3 special case - any score has the same reward
        Assert.assertTrue(totalReward5.compareTo(totalReward6) == 0);
        
        // now let's see how the keystone block is being rewarded
        BigDecimal totalRewardKeystone1 = PopRewardCalculator.calculatePopRewardForBlock(
                PopRewardCalculator.getAltChainConfig().keystoneInterval, defaultScore, defaultDifficulty);
        BigDecimal totalRewardKeystone2 = PopRewardCalculator.calculatePopRewardForBlock(
                PopRewardCalculator.getAltChainConfig().keystoneInterval, halfScore, defaultDifficulty);
        Assert.assertTrue(totalRewardKeystone1.compareTo(totalRewardKeystone2) > 0);
        
        // we see that even when cut in half the keystone reward is higher than any normal reward from rounds 1-3
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward1) > 0);
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward2) > 0);
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward3) > 0);
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward4) > 0);
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward5) > 0);
        Assert.assertTrue(totalRewardKeystone2.compareTo(totalReward6) > 0);
    }

    @Test
    public void popRewardBlocks() throws SQLException {
        PoPTransactionsDBStore popTxStore = Context.getPopTxDBStore();

        int blockNumber = 1;
        // let's start with hardcoded difficulty
        BigDecimal defaultDifficulty = BigDecimal.ONE;
        // we store a single endorsed block in the first (relative to our calculator) VeriBlock block
        // round 1 ratio is 0.97
        BigDecimal round1Ratio = PopRewardCalculator.getRoundRatio(RewardDefaults.ROUND_1);
        // round 1 slope is -0.00194‬
        BigDecimal round1Slope = PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_1);
        // this is a normal reward for round 1 blocks with score = 1 and difficulty = 1
        BigDecimal defaultRewardRound1 = new BigDecimal(PopRewardCalculator.getCalculatorConfig().basicReward).multiply(round1Ratio);
        
        long reward1 = 0;
        long reward2 = 0;

        AltPublication altPublication1;
        AltPublication altPublication2;
        AltPublication altPublication3;
        AltPublication altPublication4;
        AltPublication altPublication5;

        // we start with calculating rewards for the single endorsement
        altPublication1 = generateATV(0, "");
        PoPTransactionData poptx1 = new PoPTransactionData("popTxHash1", altPublication1, new ArrayList<VeriBlockPublication>());

        AltChainBlock endorsedBlock = new AltChainBlock("blockHash1", 50, 100);
        AltChainBlock containingBlock = new AltChainBlock("blockHash2", 51, 101);

        List<AltChainBlock> endorsementBlocks = new ArrayList<AltChainBlock>();
        endorsementBlocks.add(containingBlock);

        popTxStore.addPoPTransaction(poptx1, containingBlock, endorsedBlock);

        PopRewardCalculator.getCalculatorConfig().popRewardSettlementInterval = endorsementBlocks.size();
        PopPayoutRound payout1 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, endorsedBlock, endorsementBlocks, defaultDifficulty);

        reward1 = payout1.getOutputsToPopMiners().get(0).getReward();
        // single endorsement should get the full reward
        Assert.assertTrue(payout1.getTotalRewardPaidOut() == defaultRewardRound1.longValue());
        Assert.assertTrue(reward1 == defaultRewardRound1.longValue());

        popTxStore.clear();

        // now let's try multiple endorsements for the same block

        altPublication2 = generateATV(0, "");

        PoPTransactionData poptx2 = new PoPTransactionData("popTxHash2", altPublication2, new ArrayList<VeriBlockPublication>());

        popTxStore.addPoPTransaction(poptx1, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx2, containingBlock, endorsedBlock);

         PopPayoutRound payout2 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, endorsedBlock, endorsementBlocks, defaultDifficulty);
        reward1 = payout2.getOutputsToPopMiners().get(0).getReward();
        reward2 = payout2.getOutputsToPopMiners().get(1).getReward();
        // two endorsements share the full reward
        // let's prepare what score we expect. It should be double score for we are having two endorsements.
        BigDecimal doubleScore = new BigDecimal(2.0);
        BigDecimal expectedReward = round1Slope
                .multiply(new BigDecimal(100))
                .add(round1Ratio)
                .multiply(doubleScore)
                .multiply(new BigDecimal(PopRewardCalculator.getCalculatorConfig().basicReward));
        Assert.assertTrue(payout2.getTotalRewardPaidOut() == expectedReward.longValue());
        Assert.assertTrue(reward1 == expectedReward.longValue() / 2);
        // we do not get the double normal reward because we are on the reward curve slope
        Assert.assertTrue(reward1 < defaultRewardRound1.longValue());
        Assert.assertTrue(reward1 == reward2);
        // we still having more than the normal reward paid - everyone get less but the total reward is higher
        Assert.assertTrue(payout2.getTotalRewardPaidOut() > defaultRewardRound1.longValue());
        // we still having less than the double normal reward paid
        Assert.assertTrue(payout2.getTotalRewardPaidOut() < defaultRewardRound1.longValue() * 2);

        popTxStore.clear();

        // now let's try to process some distant endorsements

        int startingBlockNumber = 30;
        altPublication1 = generateATV(0, "");
        altPublication2 = generateATV(startingBlockNumber, "");

        poptx1 = new PoPTransactionData("txHash1", altPublication1, new ArrayList<>());
        poptx2 = new PoPTransactionData("txHash2", altPublication2, new ArrayList<>());

        popTxStore.addPoPTransaction(poptx1, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx2, containingBlock, endorsedBlock);
        
        PopPayoutRound payout3 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, endorsedBlock, endorsementBlocks, defaultDifficulty);
        reward1 = payout3.getOutputsToPopMiners().get(0).getReward();
        reward2 = payout3.getOutputsToPopMiners().get(1).getReward();

        // distant endorsement gets lower than normal reward
        Assert.assertTrue(Math.min(reward1, reward2) < defaultRewardRound1.longValue());
        Assert.assertTrue(reward1 + reward2 == payout3.getTotalRewardPaidOut());

        popTxStore.clear();

        // let's try more complex example. Two endorsements for block 30 and two endorsements for block 31

        altPublication1 = generateATV(0, "");
        altPublication2 = generateATV(startingBlockNumber, "");
        altPublication3 = generateATV(startingBlockNumber, "");
        altPublication4 = generateATV(startingBlockNumber + 1, "");
        altPublication5 = generateATV(startingBlockNumber + 1, "");

        poptx1 = new PoPTransactionData("tx1", altPublication1, new ArrayList<>());
        poptx2 = new PoPTransactionData("tx2", altPublication2, new ArrayList<>());
        PoPTransactionData poptx3 = new PoPTransactionData("tx3", altPublication3, new ArrayList<>());
        PoPTransactionData poptx4 = new PoPTransactionData("tx4", altPublication4, new ArrayList<>());
        PoPTransactionData poptx5 = new PoPTransactionData("tx5", altPublication5, new ArrayList<>());

        popTxStore.addPoPTransaction(poptx1, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx2, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx3, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx4, containingBlock, endorsedBlock);
        popTxStore.addPoPTransaction(poptx5, containingBlock, endorsedBlock);

        PopPayoutRound payout4 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, endorsedBlock, endorsementBlocks, defaultDifficulty);
        List<Long> rewards = new ArrayList<>();
        for(PopRewardOutput output : payout4.getOutputsToPopMiners())
        {
            rewards.add(output.getReward());
        }

        rewards.sort(Long::compareTo);

        Assert.assertTrue(payout4.getTotalRewardPaidOut() - rewards.get(4) < defaultRewardRound1.longValue());
        // assert that two endorsements from the block 30 get the same reward
        Assert.assertEquals(rewards.get(1), rewards.get(0));
        // assert that two endorsements from the block 31 get the same reward
        Assert.assertEquals(rewards.get(3), rewards.get(2));
        // assert that endorsement from the block 30 get higher reward than from the block 31
        Assert.assertEquals(rewards.get(0).compareTo(rewards.get(3)), -1);
        
        BigDecimal scoreTotal = calculatePopScoreFromEndorsements(endorsedBlock, endorsementBlocks);
        long rewardTotal = PopRewardCalculator.calculatePopRewardForBlock(blockNumber, scoreTotal, defaultDifficulty).longValue();
        // getTotalRewardPaidOut should be equal to rewardTotal but some rounding errors occur so we compare with some margin
        Assert.assertTrue(payout4.getTotalRewardPaidOut() <= rewardTotal);
        Assert.assertTrue(payout4.getTotalRewardPaidOut() + 100 > rewardTotal);
                
        BigDecimal block30WeightToBlock31Weight =
                PopRewardCalculator.getScoreMultiplierFromRelativeBlock(startingBlockNumber)
                .divide(PopRewardCalculator.getScoreMultiplierFromRelativeBlock(startingBlockNumber + 1), RoundingMode.FLOOR);
        double rewardMiner30ToRewardMiner31 = (double) rewards.get(3) / (double) rewards.get(0);
        // we see that the reward paid to the each miner is proportional to the block weight (when we have
        // the same score for endorsements and same difficulty) 
        Assert.assertTrue(
                Math.abs(
                        block30WeightToBlock31Weight.doubleValue() * PopRewardCalculator.getCalculatorConfig().basicReward.longValue() -
                        rewardMiner30ToRewardMiner31 * PopRewardCalculator.getCalculatorConfig().basicReward.longValue())
                < 100);
        
        BigDecimal block30Weight = PopRewardCalculator.getScoreMultiplierFromRelativeBlock(startingBlockNumber);
        long rewardPerEndorsement = (long) ((double) rewardTotal * block30Weight.doubleValue() / scoreTotal.doubleValue());
        // let's check if we correctly reflect the scoring algorithm
        Assert.assertTrue(Math.abs(rewardPerEndorsement - rewards.get(3)) < 100);
    }

    private AltPublication generateATV(int containingBlockHeight, String payoutInfo) {

        PublicationData publicationData = new PublicationData(0, new byte[]{}, payoutInfo.getBytes(), new byte[]{});
        VeriBlockTransaction tx = new VeriBlockTransaction(
                (byte) 0x01,
                new Address("VB2zTVQH6JmjJJZTYwCcrDB9kAJp7G"),
                Coin.valueOf(1000L),
                Collections.emptyList(),
                7L,
                publicationData,
                Utils.decodeHex("304402201124F58AC7AF281A5B7889E02F726483DA1DC2387C5B58456F969B9B0AEF02FC022074F8522D51D4E01329E435657EF7F4D424D7DBD64F7E3FECBC9554C75988AA97"),
                Utils.decodeHex("3056301006072A8648CE3D020106052B8104000A03420004B558286EE19E59D8B4D0F72505B62491D239AD3AAF6657D52AB5C2A09C93EC361E11359B2527A924A79135AAF9C61EB9150D34F40E89299ED54DD5372EBB2C88"), null);

        AltPublication publication = new AltPublication(
                tx,
                new VeriBlockMerklePath("1:13:E20ED2CFFAC2DDB4E85C8A852BD63320324B6014259DA1E0FE4491F084704997:5B977EA09A554AD56957F662284044E7D37450DDADF7DB3647712F5969399787:20D0A3D873EEEEE6A222A75316DCE60B53CA43EAEA09D27F0ECE897303A53AE9:C06FE913DCA5DC2736563B80834D69E6DFDF1B1E92383EA62791E410421B6C11:049F68D350EEB8B3DF630C8308B5C8C2BA4CD6210868395B084AF84D19FF0E90:0000000000000000000000000000000000000000000000000000000000000000:36252DFC621DE420FB083AD9D8767CBA627EDDEEC64E421E9576CEE21297DD0A"),
                new VeriBlockBlock(containingBlockHeight, (short) 2,
                        VBlakeHash.wrap("000000000000069B7E7B7245449C60619294546AD825AF03"),
                        VBlakeHash.wrap("00000000000023A90C8B0DFE7C55C1B0935637860679DDD5"),
                        VBlakeHash.wrap("00000000000065630808D69AB26B825EE4FD21082E18686E"),
                        Sha256Hash.wrap("0356EB39B851682679F9A0131A4E4A5F", Sha256Hash.VERIBLOCK_MERKLE_ROOT_LENGTH),
                        1520158,
                        16842752,
                        1),
                Collections.emptyList());

        return publication;
    }

    public static class PoPTransactionsDBStoreMock extends PoPTransactionsDBStore {
        private Map<String, List<AltPublication>> containingAltPublication;
        private Map<String, List<VeriBlockPublication>> containingVeriBlockPublication;
        private Map<String, List<AltPublication>> endoresedAltPublication;

        public PoPTransactionsDBStoreMock() throws SQLException {
        }

        {
            this.containingAltPublication = new TreeMap<String, List<AltPublication>>();
            this.containingVeriBlockPublication = new TreeMap<String, List<VeriBlockPublication>>();
            this.endoresedAltPublication = new TreeMap<String, List<AltPublication>>();
        }


        @Override
        public List<AltPublication> getAltPublciationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containBlocks) throws SQLException {
            Set<AltPublication> altPublications1 = new HashSet<AltPublication>();
            for (AltChainBlock block : containBlocks) {
                if(containingAltPublication.get(block.getHash()) != null) {
                    altPublications1.addAll(containingAltPublication.get(block.getHash()));
                }
            }

            Set<AltPublication> altPublications2 = new HashSet<AltPublication>();
            if(endoresedAltPublication.get(endorsedBlock.getHash()) != null) {
                altPublications2.addAll(endoresedAltPublication.get(endorsedBlock.getHash()));
            }
            altPublications2.retainAll(altPublications1);
            return new ArrayList<AltPublication>(altPublications2);
        }

        @Override
        public void addPoPTransaction(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock) throws SQLException {
            List<AltPublication> altPublications = containingAltPublication.get(containingBlock.getHash());
            if (altPublications != null) {
                altPublications.add(popTx.altPublication);
            } else {
                altPublications = new ArrayList<AltPublication>();
                altPublications.add(popTx.altPublication);
                containingAltPublication.put(containingBlock.getHash(), altPublications);
            }

            List<VeriBlockPublication> veriBlockPublications = containingVeriBlockPublication.get(containingBlock.getHash());
            if (veriBlockPublications != null) {
                veriBlockPublications.addAll(popTx.veriBlockPublications);
            } else {
                containingVeriBlockPublication.put(containingBlock.getHash(), new ArrayList<VeriBlockPublication>(popTx.veriBlockPublications));
            }

            altPublications = endoresedAltPublication.get(endorsedBlock.getHash());
            if (altPublications != null) {
                altPublications.add(popTx.altPublication);
            } else {
                altPublications = new ArrayList<AltPublication>();
                altPublications.add(popTx.altPublication);
                endoresedAltPublication.put(endorsedBlock.getHash(), altPublications);
            }
        }

        @Override
        public List<AltPublication> getAltPublicationsFromBlock(AltChainBlock block) throws SQLException {
            return containingAltPublication.get(block.getHash());
        }

        @Override
        public List<VeriBlockPublication> getVeriBlockPublicationsFromBlock(AltChainBlock block) throws SQLException {
            return containingVeriBlockPublication.get(block.getHash());
        }

        @Override
        public void clear() throws SQLException {
            this.containingAltPublication = new TreeMap<String, List<AltPublication>>();
            this.containingVeriBlockPublication = new TreeMap<String, List<VeriBlockPublication>>();
            this.endoresedAltPublication = new TreeMap<String, List<AltPublication>>();
        }

        @Test
        public void name() {}
    }



    public static class VeriBlockSecurityMock extends VeriBlockSecurity {

        @Override
        public void clearTemporaryPayloads() {
        }

        @Override
        public ValidationResult checkATVAgainstView(AltPublication publication) throws BlockStoreException, SQLException {
            return ValidationResult.success();
        }

        @Override
        public void addTemporaryPayloads(List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws BlockStoreException, SQLException {
        }

        @Test
        public void name() {}
    }
}
