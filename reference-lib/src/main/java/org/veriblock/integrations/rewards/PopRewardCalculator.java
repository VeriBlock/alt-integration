// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopRewardCalculator {    
    // payout rounds methods
    
    private static PopRewardCalculatorConfig config = new PopRewardCalculatorConfig();

    private static VeriBlockSecurity security;

    private static PoPTransactionsDBStore popTxDBStore;

    public static void setSecurity(VeriBlockSecurity security)
    {
        PopRewardCalculator.security = security;
        PopRewardCalculator.popTxDBStore = security.getSecurityFiles().getPopTxDBStore();
    }

    public static PopRewardCalculatorConfig getCalculatorConfig() {
        return config;
    }
    
    public static void setCalculatorConfig(PopRewardCalculatorConfig config) {
        PopRewardCalculator.config = config;
    }

    public static AltChainParametersConfig getAltChainConfig() { return security.getAltChainParametersConfig(); }
    
    public static boolean isKeystoneRound(int payoutRound) {
        return payoutRound == config.keystoneRound;
    }
    
    // rounds for blocks are [4, 1, 2, 3, 1, 2, 3, 1, 2, 3, 4, ...]
    public static int getRoundForBlockNumber(int number) {
        if (number % security.getAltChainParametersConfig().keystoneInterval == 0) {
            return config.keystoneRound;
        }
        
        if(config.payoutRounds <= 1) {
            return 0;
        }

        int round = ((number - 1) % security.getAltChainParametersConfig().keystoneInterval) % (config.payoutRounds - 1);
        return round;
    }
    
    public static boolean isKeystoneHeight(int blockHeight) {
        return isKeystoneRound(getRoundForBlockNumber(blockHeight));
    }

    public static boolean isImmediatelyAfterKeystoneRound(int blockHeight) {
        return isKeystoneRound(blockHeight - 1);
    }
    
    // with this function we can detect how many payout rounds of the given type occured up to this block height
    // starting from the previous keystone
    // eg for blockNumber = 8 and round = ROUND_3 we have seen 3rd round 2 times so the result will be 2.
    ///HACK: this function is only being used to detect the first ROUND_3 after the keystone.
    public static int getIndexOfRound(int blockNumber, int round) {
        int roundForBlockNumber = getRoundForBlockNumber(blockNumber);
        if (round != roundForBlockNumber) return -1;

        int keystonePeriodIndex = blockNumber % security.getAltChainParametersConfig().keystoneInterval;
        
        if(keystonePeriodIndex == 0) return 1;
        if(config.payoutRounds == 0) return -1;
        if(config.payoutRounds == 1) return keystonePeriodIndex;
        
        
        return keystonePeriodIndex / (config.payoutRounds - 1);
    }
    
    // payout curve methods
    
    public static BigDecimal getRoundRatio(int payoutRound) {
        return config.roundRatios.get(payoutRound);
    }
    
    public static BigDecimal getRoundSlope(int payoutRound) {
        BigDecimal roundRatio = getRoundRatio(payoutRound);
        if(payoutRound == config.keystoneRound) {
            return config.curveConfig.aboveIntendedPayoutMultiplierKeystone
                    .multiply(roundRatio)
                    .subtract(roundRatio)
                    .divide(config.curveConfig.widthOfDecreasingLineKeystone, RoundingMode.FLOOR);
        }
        
        return config.curveConfig.aboveIntendedPayoutMultiplierNormal
                .multiply(roundRatio)
                .subtract(roundRatio)
                .divide(config.curveConfig.widthOfDecreasingLineNormal, RoundingMode.FLOOR);
    }
    
    // payout maximum limits
    
    private static BigDecimal getMaxRewardThreshold(int payoutRound) {
        if (isKeystoneRound(payoutRound)) {
            return config.maxRewardThresholdKeystone;
        } else {
            return config.maxRewardThresholdNormal;
        }
    }
    
    public static BigDecimal getScoreMultiplierFromRelativeBlock(int relativeBlock) {
        if (relativeBlock < 0 || relativeBlock >= config.relativeScoreLookupTable.size()) {
            return BigDecimal.ZERO;
        }

        return config.relativeScoreLookupTable.get(relativeBlock);
    }

    public static int getBestPublicationHeight(List<AltPublication> publications) throws SQLException {
        int bestPublication = -1;
        for(AltPublication publication : publications) {
            ValidationResult fsuccess = security.checkATVAgainstView(publication);
            int vbkHeight = publication.getContainingBlock().getHeight();

            if(fsuccess.isValid() && (vbkHeight < bestPublication || bestPublication < 0))
                bestPublication = vbkHeight;
        }
        return bestPublication;
    }

    public static BigDecimal calculatePopDifficultyForBlock(List<AltChainBlock> blocksInterval) throws SQLException
    {
        BigDecimal difficulty = BigDecimal.ZERO;

        Collections.sort(blocksInterval);  // make the ascending order for the blocks in the collection, it needs for the correct calculation of the pop score

        for(int i = 0; i < blocksInterval.size(); ++i)
        {
            BigDecimal score = calculatePopScoreFromEndorsements(blocksInterval.get(i), blocksInterval.subList(i , blocksInterval.size()));
            if(score.compareTo(BigDecimal.ZERO) == 0)
            {
                score = BigDecimal.ONE;
            }

            difficulty = difficulty.add(score);
        }

        return difficulty.divide(new BigDecimal(config.popDifficultyAveragingInterval));
    }

    public static BigDecimal calculatePopScoreFromEndorsements(AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks) throws SQLException {
        BigDecimal totalScore = BigDecimal.ZERO;

        List<AltPublication> endorsements = popTxDBStore.getAltPublciationsEndorse(endorsedBlock, endorsementBlocks);

        int bestPublication = getBestPublicationHeight(endorsements);

        for(AltPublication publication : endorsements) {
            ValidationResult fsuccess = security.checkATVAgainstView(publication);
            int relativeHeight = publication.getContainingBlock().getHeight() - bestPublication;

            if(fsuccess.isValid())
                totalScore = totalScore.add(getScoreMultiplierFromRelativeBlock(relativeHeight));
        }
        return totalScore;
    }

    public static BigDecimal calculatePopScoreFromEndorsements(List<AltPublication> endorsements , int bestPublication) throws SQLException {
        BigDecimal totalScore = BigDecimal.ZERO;

        for(AltPublication publication : endorsements) {
            ValidationResult fsuccess = security.checkATVAgainstView(publication);
            int relativeHeight = publication.getContainingBlock().getHeight() - bestPublication;

            if(fsuccess.isValid())
                totalScore = totalScore.add(getScoreMultiplierFromRelativeBlock(relativeHeight));
        }
        return totalScore;
    }

    // apply the reward curve to the score and subtract it from the current round multiplier
    ///HACK: we get the negative result when applying the slope so we add it to the roundRatio instead of subtracting
    private static BigDecimal calculateProtoRewardPerPoint(BigDecimal choppedNormalizedScore, int payoutRound) {
        BigDecimal slope = getRoundSlope(payoutRound);
        BigDecimal roundRatio = getRoundRatio(payoutRound);
        // slope * (choppedNormalizedScore - START_OF_DECREASING_LINE_REWARD) + roundRatio
        return slope
                .multiply(choppedNormalizedScore.subtract(config.curveConfig.startOfDecreasingLine))
                .add(roundRatio);
    }

    public static BigDecimal calculateTotalPopBlockReward(int blockNumber, BigDecimal difficulty, BigDecimal score) {
        if (difficulty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("calculateTotalReward cannot be called with a negative difficulty" +
                    " (called with " + difficulty + ")!");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("calculateTotalReward cannot be called with a negative score" +
                    " (called with " + score + ")!");
        }

        if (score.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (difficulty.compareTo(BigDecimal.ONE) < 0) {
            difficulty = BigDecimal.ONE; // Minimum difficulty
        }

        int payoutRound = getRoundForBlockNumber(blockNumber);

        BigDecimal normalizationMultiplier = new BigDecimal(100).divide(difficulty, RoundingMode.FLOOR);
        // normalizedScore has such a value that (normalizedScore / 1 == original score * 100 / difficulty)
        // so having normalizedScore we can assume the difficulty == 1
        BigDecimal normalizedScore = score.multiply(normalizationMultiplier);
        BigDecimal scoreToDifficultyRatio = score.divide(difficulty, RoundingMode.FLOOR);
        BigDecimal rewardInitialBudget = new BigDecimal(config.basicReward).divide(new BigDecimal(100), RoundingMode.FLOOR);

        // No use of penalty multiplier, this payout occurs on the flat part of the payout curve
        if (scoreToDifficultyRatio.multiply(new BigDecimal(100)).compareTo(config.curveConfig.startOfDecreasingLine) <= 0) {
            BigDecimal roundRatio = getRoundRatio(payoutRound);

            // now we apply the score to our budget and apply the current round multiplier
            return rewardInitialBudget
                    .multiply(scoreToDifficultyRatio)
                    .multiply(new BigDecimal(100))
                    .multiply(roundRatio);
        } else {
            BigDecimal maxRewardThreshold = getMaxRewardThreshold(payoutRound);
            BigDecimal choppedNormalizedScore = normalizedScore;
            if(normalizedScore.compareTo(maxRewardThreshold) > 0) {
                choppedNormalizedScore = maxRewardThreshold;
            }

            // Note that this reward per point is not the true rewardPerPoint if the score to difficulty ratio
            // is greater than the max reward threshold. Past the max reward threshold, the block reward
            // ceases to grow, but is split amongst a larger number of participants.
            BigDecimal protoRewardPerPoint = calculateProtoRewardPerPoint(choppedNormalizedScore, payoutRound);

            // now we apply the score to our budget and reduce the payout according to our reward curve
            return rewardInitialBudget.multiply(choppedNormalizedScore).multiply(protoRewardPerPoint);
        }
    }
    
    // we calculate the reward for a given block
    public static BigDecimal calculatePopRewardForBlock(int blockNumber, BigDecimal scoreForThisBlock, BigDecimal difficulty) {
        if (scoreForThisBlock.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Special case for the first ROUND 3 after keystone - do not adjust for score to difficulty ratio
        if(config.flatScoreRoundUse) {
            if (getIndexOfRound(blockNumber, config.flatScoreRound) == 1) {
                return calculateTotalPopBlockReward(blockNumber, BigDecimal.ONE, BigDecimal.ONE);
            }
        }

        return calculateTotalPopBlockReward(blockNumber, difficulty, scoreForThisBlock);
    }

    public static PopPayoutRound calculatePopPayoutRound(int blockNumber, AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks, BigDecimal popDifficulty) throws SQLException {

        List<AltPublication> endorsements = popTxDBStore.getAltPublciationsEndorse(endorsedBlock, endorsementBlocks);

        int veriBlockLowestHeight = getBestPublicationHeight(endorsements);
        BigDecimal scoreForThisBlock = calculatePopScoreFromEndorsements(endorsements, veriBlockLowestHeight);
        // round down the reward to integer value
        long popBlockReward = calculatePopRewardForBlock(blockNumber, scoreForThisBlock, popDifficulty).longValue();

        // we have the total reward per block in popBlockReward. Let's distribute it now.

        List<PopRewardOutput> outputsToPopMiners = new ArrayList<>();

        long totalRewardPaidOut = 0L;

        ///HACK: I hoped to simplify the reward calculation rules and express the rewardPerBlock value but could
        ///      not find a proper expression. So we are having quite complex function to obtain the rewardPerEndorsement and it looks
        ///      like:
        ///      rewardPerEndorsement = popBlockReward * endorsementLevelWeight(i) / (sum(endorsementLevelWeight(k) * endorsmentsLength(k))
        ///      where k iterates over blocks containing endorsements

        for(AltPublication publication : endorsements) {
            int veriBlockHeight = publication.getContainingBlock().getHeight();
            int relativeHeight = veriBlockHeight - veriBlockLowestHeight;
            BigDecimal endorsementLevelWeight = getScoreMultiplierFromRelativeBlock(relativeHeight);

            long rewardPerEndorsement = new BigDecimal(popBlockReward)
                    .multiply(endorsementLevelWeight)
                    .divide(scoreForThisBlock, RoundingMode.FLOOR)
                    .longValue();

            if (rewardPerEndorsement <= 0) {
                continue;
            }

            byte[] popMinerAddress = publication.getTransaction().getPublicationData().getPayoutInfo();
            outputsToPopMiners.add(new PopRewardOutput(popMinerAddress, rewardPerEndorsement));
            totalRewardPaidOut += rewardPerEndorsement;
        }

        if (totalRewardPaidOut > popBlockReward) {
            throw new IllegalStateException("When calculating a PoP payout round for block " +
                    blockNumber + ", the total reward (" +
                    totalRewardPaidOut + ") is higher than the allowed reward (" + popBlockReward + ")!");
        }

        return new PopPayoutRound(totalRewardPaidOut, popBlockReward, outputsToPopMiners);
    }
}