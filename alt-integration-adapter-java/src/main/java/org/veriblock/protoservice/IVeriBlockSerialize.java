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
import org.veriblock.sdk.PublicationData;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockMerklePath;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;

public interface IVeriBlockSerialize {

    public byte[] serializeAltPublication(AltPublication request);
    
    public byte[] serializePublicationData(PublicationData request);
    
    public byte[] serializeBitcoinTransaction(BitcoinTransaction request);
    
    public byte[] serializeVeriBlockBlock(VeriBlockBlock request);
    
    public byte[] serializeVeriBlockTransaction(VeriBlockTransaction request);
    
    public byte[] serializeVeriBlockPublication(VeriBlockPublication request);
    
    public byte[] serializeVeriBlockPopTx(VeriBlockPoPTransaction request);
    
    public byte[] serializeOutput(Output request);
    
    public byte[] serializeAddress(Address request);
    
    public byte[] serializeBitcoinBlock(BitcoinBlock request);
    
    public byte[] serializeVeriBlockMerklePath(VeriBlockMerklePath request);
    
    public byte[] serializeMerklePath(MerklePath request);
}
