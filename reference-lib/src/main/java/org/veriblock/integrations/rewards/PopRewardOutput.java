// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

public class PopRewardOutput {
    private String minerAddress;
    private long reward;
    
    public PopRewardOutput(String minerAddress, long reward) {
        this.minerAddress = minerAddress;
        this.reward = reward;
    }
    
    public String getPopMinerAddress() {
        return minerAddress;
    }
    
    public long getReward() {
        return reward;
    }
}
