// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.transactions.bitcoin;

import org.veriblock.sdk.models.Address;
import org.veriblock.sdk.models.BitcoinTransaction;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.Utils;

import java.nio.ByteBuffer;

public class VeriBlockBitcoinTransactions {

    private VeriBlockBitcoinTransactions() { }
    
    public static String bitcoinTransactionGetId(BitcoinTransaction tx) {
        return Utils.encodeHex(Utils.flip(Sha256Hash.twiceOf(tx.getRawBytes()).getBytes()));
    }
    
    public static Sha256Hash merkleTreeRootHash(BitcoinMerkleTree tree) {
        return Sha256Hash.wrap(Utils.decodeHex(tree.getMerkleRoot()));
    }
    
    public static byte[] publicationDataToBitcoinHeader(VeriBlockBlock publishedBlock, Address sender) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(80);
        buffer.put(SerializeDeserializeService.serializeHeaders(publishedBlock));
        // PoPBytes is storing the first 16 bytes of the BASE58 decoded address
        buffer.put(sender.getPoPBytes());
        buffer.flip();
        
        byte[] header = new byte[80];
        buffer.get(header);
        return header;
    }
}
