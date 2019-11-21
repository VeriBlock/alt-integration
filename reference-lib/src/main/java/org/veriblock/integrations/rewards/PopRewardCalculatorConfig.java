// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PopRewardCalculatorConfig {
    public BigInteger basicReward;
    public int payoutRounds;
    public int keystoneRound;
    public List<BigDecimal> roundRatios;
    public BigDecimal maxRewardThresholdNormal;
    public BigDecimal maxRewardThresholdKeystone;
    public int flatScoreRound;
    public boolean flatScoreRoundUse;
    public PopRewardCurveConfig curveConfig;
    public List<BigDecimal> relativeScoreLookupTable;
    public int popDifficultyAveragingInterval;
    public int popRewardSettlementInterval;
    
    public PopRewardCalculatorConfig() {
        basicReward = new BigInteger(Long.toString(RewardDefaults.POP_DEFAULT_REWARD_PER_BLOCK));
        payoutRounds = RewardDefaults.PAYOUT_ROUNDS_COUNT;
        keystoneRound = RewardDefaults.ROUND_4;
        roundRatios = new ArrayList<>();
        roundRatios.add(new BigDecimal(RewardDefaults.ROUND_1_RATIO));
        roundRatios.add(new BigDecimal(RewardDefaults.ROUND_2_RATIO));
        roundRatios.add(new BigDecimal(RewardDefaults.ROUND_3_RATIO));
        roundRatios.add(new BigDecimal(RewardDefaults.ROUND_4_RATIO));
        maxRewardThresholdNormal = new BigDecimal(RewardDefaults.MAX_REWARD_PERCENT_THRESHOLD_NORMAL);
        maxRewardThresholdKeystone = new BigDecimal(RewardDefaults.MAX_REWARD_PERCENT_THRESHOLD_KEYSTONE);
        // use flat score on ROUND_3
        flatScoreRound = RewardDefaults.ROUND_3;
        flatScoreRoundUse = true;
        curveConfig = new PopRewardCurveConfig();
        relativeScoreLookupTable = new ArrayList<>();
        popDifficultyAveragingInterval = RewardDefaults.POP_DIFFICULTY_AVERAGING_INTERVAL;
        popRewardSettlementInterval = RewardDefaults.POP_REWARD_SETTLEMENT_INTERVAL;
        for(double value : RewardDefaults.popRelativeScoreLookupTable) {
            relativeScoreLookupTable.add(new BigDecimal(value));
        }
    }
}
