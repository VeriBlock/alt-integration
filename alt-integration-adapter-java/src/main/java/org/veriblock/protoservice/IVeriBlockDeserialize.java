// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.Address;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BitcoinTransaction;
import org.veriblock.sdk.MerklePath;
import org.veriblock.sdk.Output;
import org.veriblock.sdk.Pair;
import org.veriblock.sdk.PublicationData;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;

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
