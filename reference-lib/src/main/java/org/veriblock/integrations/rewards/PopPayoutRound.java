// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import java.util.ArrayList;
import java.util.List;

public class PopPayoutRound {
    private long totalRewardPaidOut;
    private long popBlockReward;
    private List<PopRewardOutput> outputsToPopMiners;

    public PopPayoutRound(long totalRewardPaidOut, long popBlockReward, List<PopRewardOutput> outputsToPopMiners) {
        this.totalRewardPaidOut = totalRewardPaidOut;
        this.popBlockReward = popBlockReward;
        this.outputsToPopMiners = new ArrayList<>();
        this.outputsToPopMiners.addAll(outputsToPopMiners);
    }
    
    public long getTotalRewardPaidOut() {
        return totalRewardPaidOut;
    }
    
    public long getPopBlockReward() {
        return popBlockReward;
    }
    
    public List<PopRewardOutput> getOutputsToPopMiners() {
        return outputsToPopMiners;
    }
}
