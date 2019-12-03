// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
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
import org.veriblock.sdk.models.PublicationData;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockMerklePath;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;

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
