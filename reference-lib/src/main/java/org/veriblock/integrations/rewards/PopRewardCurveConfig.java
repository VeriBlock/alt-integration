// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import java.math.BigDecimal;

public class PopRewardCurveConfig {
    public BigDecimal startOfDecreasingLine;
    public BigDecimal widthOfDecreasingLineNormal;
    public BigDecimal widthOfDecreasingLineKeystone;
    public BigDecimal aboveIntendedPayoutMultiplierNormal;
    public BigDecimal aboveIntendedPayoutMultiplierKeystone;
    
    public PopRewardCurveConfig() {
        startOfDecreasingLine = new BigDecimal(RewardDefaults.START_OF_DECREASING_LINE_REWARD);
        widthOfDecreasingLineNormal = new BigDecimal(RewardDefaults.WIDTH_OF_DECREASING_LINE_REWARD_PERIOD_NORMAL);
        widthOfDecreasingLineKeystone = new BigDecimal(RewardDefaults.WIDTH_OF_DECREASING_LINE_REWARD_PERIOD_KEYSTONE);
        aboveIntendedPayoutMultiplierNormal = new BigDecimal(RewardDefaults.ABOVE_INTENDED_PAYOUT_MULTIPLIER_NORMAL);
        aboveIntendedPayoutMultiplierKeystone = new BigDecimal(RewardDefaults.ABOVE_INTENDED_PAYOUT_MULTIPLIER_KEYSTONE);
    }
}
