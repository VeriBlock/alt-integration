// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veriblock.integrations.VeriBlockIntegrationLibraryManager;
import org.veriblock.integrations.VeriBlockSecurity;

public class VeriBlockRewardCalculatorTest {

    @Before
    public void setUp() throws SQLException, IOException
    {
        VeriBlockSecurity security = VeriBlockIntegrationLibraryManager.init();
        PopRewardCalculator.setSecurity(security);
    }

    @After
    public void tearDown() throws SQLException {
        VeriBlockIntegrationLibraryManager.shutdown();
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
    public void ratiosForEndorsement() {
        PopEndorsement singleEndorseTransaction = new PopEndorsement("", "");
     
        // we store a single endorsed block in the first (relative to our calculator) VeriBlock block
        PopRewardEndorsements singleEndorsement = new PopRewardEndorsements();
        singleEndorsement.addEndorsement(0, singleEndorseTransaction);
        
        // first block has the most score so the total score is 1.0
        BigDecimal totalScore = PopRewardCalculator.calculatePopScoreFromEndorsements(singleEndorsement);
        Assert.assertTrue(totalScore.compareTo(BigDecimal.ONE) == 0);
        
        // score table has a grace period so two blocks still have the maximum score (1.0 for each block)
        PopRewardEndorsements twoBlockEndorsements = new PopRewardEndorsements();
        twoBlockEndorsements.addEndorsement(0, singleEndorseTransaction);
        twoBlockEndorsements.addEndorsement(1, singleEndorseTransaction);
        BigDecimal totalScore2 = PopRewardCalculator.calculatePopScoreFromEndorsements(twoBlockEndorsements);
        Assert.assertTrue(totalScore2.compareTo(new BigDecimal(2)) == 0);
        
        // score begins to decrease after the grace period so with 15 blocks we no longer have the maximum score
        PopRewardEndorsements multipleBlocksEndorsements = new PopRewardEndorsements();
        for(int i = 0; i < 15; i++) {
            multipleBlocksEndorsements.addEndorsement(i, singleEndorseTransaction);
        }
        BigDecimal totalScore3 = PopRewardCalculator.calculatePopScoreFromEndorsements(multipleBlocksEndorsements);
        Assert.assertTrue(totalScore3.compareTo(new BigDecimal(15)) < 0);
        
        // score stops increasing after 50 blocks. Let's try.
        PopRewardEndorsements lotsBlocksEndorsements = new PopRewardEndorsements();
        int lotsBlocksEndorsementsSize = 50;
        for(int i = 0; i < lotsBlocksEndorsementsSize; i++) {
            lotsBlocksEndorsements.addEndorsement(i, singleEndorseTransaction);
        }
        BigDecimal totalScore4 = PopRewardCalculator.calculatePopScoreFromEndorsements(lotsBlocksEndorsements);
        // make sure the last position is not filled yet
        Assert.assertTrue(lotsBlocksEndorsements.getBlocksWithEndorsements().get(lotsBlocksEndorsementsSize) == null);
        lotsBlocksEndorsements.addEndorsement(lotsBlocksEndorsementsSize, singleEndorseTransaction);
        
        BigDecimal totalScore5 = PopRewardCalculator.calculatePopScoreFromEndorsements(lotsBlocksEndorsements);
        // assert the score is no longer growing
        Assert.assertTrue(totalScore4.compareTo(totalScore5) == 0);
        
        // now let's try with two endorsements in the same block
        PopRewardEndorsements singleBlockTwoEndorsements = new PopRewardEndorsements();
        singleBlockTwoEndorsements.addEndorsement(0, singleEndorseTransaction);
        singleBlockTwoEndorsements.addEndorsement(0, singleEndorseTransaction);
        BigDecimal totalScore6 = PopRewardCalculator.calculatePopScoreFromEndorsements(singleBlockTwoEndorsements);
        // so many endorsements simply increase score proportionally to endorsements count
        Assert.assertTrue(totalScore6.compareTo(new BigDecimal(2)) == 0);
    }
    
    ///HACK: there is no difference if we change difficulty or score so we fix the difficulty and test score only
    @Test
    public void popRewardBasic() {
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
    public void popRewardSpecialCase() {
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
    public void popRewardBlocks() {
        int blockNumber = 1;
        // let's start with hardcoded difficulty
        BigDecimal defaultDifficulty = BigDecimal.ONE;
        // we store a single endorsed block in the first (relative to our calculator) VeriBlock block
        PopEndorsement singleEndorseTransaction = new PopEndorsement("", "");
        // round 1 ratio is 0.97
        BigDecimal round1Ratio = PopRewardCalculator.getRoundRatio(RewardDefaults.ROUND_1);
        // round 1 slope is -0.00194‬
        BigDecimal round1Slope = PopRewardCalculator.getRoundSlope(RewardDefaults.ROUND_1);
        // this is a normal reward for round 1 blocks with score = 1 and difficulty = 1
        BigDecimal defaultRewardRound1 = new BigDecimal(PopRewardCalculator.getCalculatorConfig().basicReward).multiply(round1Ratio);
        
        long reward1 = 0;
        long reward2 = 0;
        long reward3 = 0;
        long reward4 = 0;

        // we start with calculating rewards for the single endorsement
        PopRewardEndorsements singleEndorsement = new PopRewardEndorsements();
        singleEndorsement.addEndorsement(0, singleEndorseTransaction);
        
        PopPayoutRound payout1 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, singleEndorsement, defaultDifficulty);
        reward1 = payout1.getOutputsToPopMiners().get(0).getReward();
        // single endorsement should get the full reward
        Assert.assertTrue(payout1.getTotalRewardPaidOut() == defaultRewardRound1.longValue());
        Assert.assertTrue(reward1 == defaultRewardRound1.longValue());
        
        // now let's try multiple endorsements for the same block
        PopRewardEndorsements oneBlockTwoEndorsements = new PopRewardEndorsements();
        oneBlockTwoEndorsements.addEndorsement(0, singleEndorseTransaction);
        oneBlockTwoEndorsements.addEndorsement(0, singleEndorseTransaction);
        
        PopPayoutRound payout2 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, oneBlockTwoEndorsements, defaultDifficulty);
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
        
        // now let's try to process some distant endorsements
        
        int startingBlockNumber = 30;
        PopRewardEndorsements lotsBlocksEndorsements = new PopRewardEndorsements();
        lotsBlocksEndorsements.addEmptyEndorsement(0);
        lotsBlocksEndorsements.addEndorsement(startingBlockNumber, singleEndorseTransaction);
        
        PopPayoutRound payout3 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, lotsBlocksEndorsements, defaultDifficulty);
        reward1 = payout3.getOutputsToPopMiners().get(0).getReward();
        // distant endorsement gets lower than normal reward
        Assert.assertTrue(payout3.getTotalRewardPaidOut() < defaultRewardRound1.longValue());
        Assert.assertTrue(reward1 == payout3.getTotalRewardPaidOut());
        
        // let's try more complex example. Two endorsements for block 30 and two endorsements for block 31
        lotsBlocksEndorsements = new PopRewardEndorsements();
        lotsBlocksEndorsements.addEmptyEndorsement(0);
        lotsBlocksEndorsements.addEndorsement(startingBlockNumber, singleEndorseTransaction);
        lotsBlocksEndorsements.addEndorsement(startingBlockNumber, singleEndorseTransaction);
        lotsBlocksEndorsements.addEndorsement(startingBlockNumber + 1, singleEndorseTransaction);
        lotsBlocksEndorsements.addEndorsement(startingBlockNumber + 1, singleEndorseTransaction);
        
        PopPayoutRound payout4 = PopRewardCalculator.calculatePopPayoutRound(blockNumber, lotsBlocksEndorsements, defaultDifficulty);
        reward1 = payout4.getOutputsToPopMiners().get(0).getReward();
        reward2 = payout4.getOutputsToPopMiners().get(1).getReward();
        reward3 = payout4.getOutputsToPopMiners().get(2).getReward();
        reward4 = payout4.getOutputsToPopMiners().get(3).getReward();
        // distant endorsement gets lower than normal reward
        Assert.assertTrue(payout4.getTotalRewardPaidOut() < defaultRewardRound1.longValue());
        // assert that two endorsements from the block 30 get the same reward
        Assert.assertTrue(reward1 == reward2);
        // assert that two endorsements from the block 31 get the same reward
        Assert.assertTrue(reward3 == reward4);
        // assert that endorsement from the block 30 get higher reward than from the block 31
        Assert.assertTrue(reward1 > reward3);
        
        BigDecimal scoreTotal = PopRewardCalculator.calculatePopScoreFromEndorsements(lotsBlocksEndorsements);
        long rewardTotal = PopRewardCalculator.calculatePopRewardForBlock(blockNumber, scoreTotal, defaultDifficulty).longValue();
        // getTotalRewardPaidOut should be equal to rewardTotal but some rounding errors occur so we compare with some margin
        Assert.assertTrue(payout4.getTotalRewardPaidOut() <= rewardTotal);
        Assert.assertTrue(payout4.getTotalRewardPaidOut() + 100 > rewardTotal);
                
        BigDecimal block30WeightToBlock31Weight =
                PopRewardCalculator.getScoreMultiplierFromRelativeBlock(startingBlockNumber)
                .divide(PopRewardCalculator.getScoreMultiplierFromRelativeBlock(startingBlockNumber + 1), RoundingMode.FLOOR);
        double rewardMiner30ToRewardMiner31 = (double) reward1 / (double) reward3;
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
        Assert.assertTrue(Math.abs(rewardPerEndorsement - reward1) < 100);
    }
}
