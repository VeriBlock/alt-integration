// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

public class PopRewardOutput {
    private byte[] minerPayoutInfo;
    private long reward;
    
    public PopRewardOutput(byte[] minerPayoutInfo, long reward) {
        this.minerPayoutInfo = minerPayoutInfo;
        this.reward = reward;
    }
    
    public byte[] getPopMinerPayoutInfo() {
        return minerPayoutInfo;
    }
    
    public long getReward() {
        return reward;
    }
}
