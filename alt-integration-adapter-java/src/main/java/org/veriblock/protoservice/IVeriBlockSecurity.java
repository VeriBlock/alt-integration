// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.AltChainParametersConfig;
import org.veriblock.sdk.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.sdk.blockchain.VeriBlockBlockchainBootstrapConfig;
import org.veriblock.sdk.forkresolution.ForkresolutionConfig;
import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BlockIndex;
import org.veriblock.sdk.models.Pair;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;

import java.util.List;

public interface IVeriBlockSecurity {    
    
    public ValidationResult resetSecurity();
    
    public ValidationResult addGenesisVeriBlock(VeriBlockBlock block);
    
    public ValidationResult addGenesisBitcoin(BitcoinBlock block);
    
    public ValidationResult addPayloads(BlockIndex blockIndex, List<AltPublication> altPublications, List<VeriBlockPublication> vtbPublications);
    
    public ValidationResult removePayloads(BlockIndex blockIndex);
    
    public ValidationResult addTemporaryPayloads(List<AltPublication> altPublications, List<VeriBlockPublication> vtbPublications);
    
    public ValidationResult clearTemporaryPayloads();
    
    public Pair<ValidationResult, List<VeriBlockPublication>> simplifyVTBs(List<VeriBlockPublication> vtbPublications);
    
    public ValidationResult checkATVAgainstView(AltPublication publication);
    
    public ValidationResult checkVTBInternally(VeriBlockPublication publication);
    
    public ValidationResult checkATVInternally(AltPublication publication);
    
    public Pair<ValidationResult, Integer> getMainVBKHeightOfATV(AltPublication publication);

    public ValidationResult savePoPTransactionData(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock);

    public Pair<ValidationResult, List<VBlakeHash>> getLastKnownVBKBlocks(int maxBlockCount);

    public Pair<ValidationResult, List<Sha256Hash>> getLastKnownBTCBlocks(int maxBlockCount);

    public ValidationResult setConfig(AltChainParametersConfig altChainConfig,
                                        ForkresolutionConfig forkresolutionConfig,
                                        PopRewardCalculatorConfig calculatorConfig,
                                        BitcoinBlockchainBootstrapConfig bitcoinBootstrapConfig,
                                        VeriBlockBlockchainBootstrapConfig veriblockBootstrapConfig);
}
