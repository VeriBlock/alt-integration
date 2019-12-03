// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.Pair;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;

import java.math.BigDecimal;
import java.util.List;

public interface IVeriBlockRewards {    
    
    public ValidationResult resetRewards();
    
    public Pair<ValidationResult, PopRewardCalculatorConfig> getCalculator();

    public Pair<ValidationResult, BigDecimal> rewardsCalculateScore(AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks);
    
    public Pair<ValidationResult, PopPayoutRound> rewardsCalculateOutputs(int blockNumber, AltChainBlock endorsedBlock, List<AltChainBlock> endorsementBlocks, BigDecimal popDifficulty);

    public Pair<ValidationResult, BigDecimal> rewardsCalculatePopDifficulty(List<AltChainBlock> blocks);
}
