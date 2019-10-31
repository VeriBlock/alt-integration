// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

public final class RewardDefaults {
    
    // never
    private RewardDefaults() { }
    
    // this is our 100% reward budget. May vary up to 500%
    public static final long POP_DEFAULT_REWARD_PER_BLOCK = 10 * 1000000;

    // average 50 blocks before the endorsed block to calculate it's difficulty
    public static final int POP_DIFFICULTY_AVERAGING_INTERVAL = 50;
    // we only store 50 values in relativeScoreLookupTable and do not want to search more blocks
    public static final int POP_SEARCH_UP_TO_VBK_BLOCKS = 50;
    
    public static final int PAYOUT_ROUNDS_COUNT = 4;
    
    public static final int ROUND_1 = 0;
    public static final int ROUND_2 = 1;
    public static final int ROUND_3 = 2;
    public static final int ROUND_4 = 3;
    
    // we gradually increase the reward for every consecutive payout round
    public static final double ROUND_1_RATIO = 0.97;
    public static final double ROUND_2_RATIO = 1.03;
    public static final double ROUND_3_RATIO = 1.07;
    public static final double ROUND_4_RATIO = 3.00;
    
    // we limit the maximum rewards to 200% for normal POP and 300% for keystones POP
    public static final double MAX_REWARD_PERCENT_THRESHOLD_NORMAL   = 200.0;
    public static final double MAX_REWARD_PERCENT_THRESHOLD_KEYSTONE = 300.0;
    
    // the score when the rewards starts decreasing
    public static final double START_OF_DECREASING_LINE_REWARD = 100.0;
    
    // we decrease each score point to 80% of initial value when difficulty is above 1.0
    public static final double ABOVE_INTENDED_PAYOUT_MULTIPLIER_NORMAL   = 0.8000;
    // we decrease each keystone score point to 57% of initial value when difficulty is above 1.0
    public static final double ABOVE_INTENDED_PAYOUT_MULTIPLIER_KEYSTONE = 0.5735;
    // this is the length of the decreasing part of the reward curve
    public static final double WIDTH_OF_DECREASING_LINE_REWARD_PERIOD_NORMAL   = 100.0;
    ///HACK: keystone reward curve is more steep even with 2x larger width
    public static final double WIDTH_OF_DECREASING_LINE_REWARD_PERIOD_KEYSTONE = 200.0;

    // reward score table
    // we score each VeriBlock and lower the reward for late blocks
    public static final double[] popRelativeScoreLookupTable = {
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        1.00000000,
        0.48296816,
        0.31551694,
        0.23325824,
        0.18453616,
        0.15238463,
        0.12961255,
        0.11265630,
        0.09955094,
        0.08912509,
        0.08063761,
        0.07359692,
        0.06766428,
        0.06259873,
        0.05822428,
        0.05440941,
        0.05105386,
        0.04807993,
        0.04542644,
        0.04304458,
        0.04089495,
        0.03894540,
        0.03716941,
        0.03554497,
        0.03405359,
        0.03267969,
        0.03141000,
        0.03023319,
        0.02913950,
        0.02812047,
        0.02716878,
        0.02627801,
        0.02544253,
        0.02465739,
        0.02391820,
        0.02322107,
        0.02256255,
        0.02193952,
        0.02134922 };
}