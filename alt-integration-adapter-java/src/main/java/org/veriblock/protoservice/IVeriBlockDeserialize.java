// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.MerklePath;
import org.veriblock.sdk.models.Output;
import org.veriblock.sdk.models.Pair;
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;

public interface IVeriBlockDeserialize {

    public Pair<ValidationResult, AltPublication> parseAltPublication(byte[] data);
    
    public Pair<ValidationResult, PublicationData> parsePublicationData(byte[] data);
    
    public Pair<ValidationResult, BitcoinTransaction> parseBitcoinTransaction(byte[] data);
    
    public Pair<ValidationResult, VeriBlockBlock> parseVeriBlockBlock(byte[] data);
    
    public Pair<ValidationResult, VeriBlockTransaction> parseVeriBlockTransaction(byte[] data);
    
    public Pair<ValidationResult, VeriBlockPublication> parseVeriBlockPublication(byte[] data);
    
    public Pair<ValidationResult, VeriBlockPoPTransaction> parseVeriBlockPopTx(byte[] data);
    
    public Pair<ValidationResult, Output> parseOutput(byte[] data);
    
    public Pair<ValidationResult, Address> parseAddress(byte[] data);
    
    public Pair<ValidationResult, BitcoinBlock> parseBitcoinBlock(byte[] data);
    
    public Pair<ValidationResult, VeriBlockMerklePath> parseVeriBlockMerklePath(byte[] data);
    
    public Pair<ValidationResult, MerklePath> parseMerklePath(byte[] data, Sha256Hash subject);
}
