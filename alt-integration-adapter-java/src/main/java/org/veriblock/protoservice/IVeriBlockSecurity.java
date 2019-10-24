// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import java.util.List;

import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.sqlite.tables.PoPTransactionData;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockIndex;
import org.veriblock.sdk.Pair;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPublication;

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

    public ValidationResult setAltChainParametersConfig(AltChainParametersConfig config);

    public ValidationResult savePoPTransactionData(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock);
}
