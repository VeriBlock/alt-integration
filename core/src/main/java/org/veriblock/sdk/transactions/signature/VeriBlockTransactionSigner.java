// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.transactions.signature;

import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockTransaction;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.security.KeyPair;
import java.security.SignatureException;

public class VeriBlockTransactionSigner {

    private VeriBlockTransactionSigner() { }
    
    public static VeriBlockTransaction sign(VeriBlockTransaction unsignedTx, KeyPair key) throws SignatureException {        
        Sha256Hash unsignedId = SerializeDeserializeService.getId(unsignedTx);
        byte[] signature = VeriBlockSigner.signMessageWithPrivateKey(unsignedId.getBytes(), key.getPrivate());
        
        VeriBlockTransaction tx = new VeriBlockTransaction(
                unsignedTx.getType(),
                unsignedTx.getSourceAddress(),
                unsignedTx.getSourceAmount(),
                unsignedTx.getOutputs(),
                unsignedTx.getSignatureIndex(),
                unsignedTx.getPublicationData(),
                signature,
                unsignedTx.getPublicKey(),
                unsignedTx.getNetworkByte());
        return tx;
    }
    
    public static VeriBlockPoPTransaction sign(VeriBlockPoPTransaction unsignedTx, KeyPair key) throws SignatureException {        
        Sha256Hash unsignedId = SerializeDeserializeService.getId(unsignedTx);
        byte[] signature = VeriBlockSigner.signMessageWithPrivateKey(unsignedId.getBytes(), key.getPrivate());

        VeriBlockPoPTransaction tx = new VeriBlockPoPTransaction(
                unsignedTx.getAddress(),
                unsignedTx.getPublishedBlock(),
                unsignedTx.getBitcoinTransaction(),
                unsignedTx.getMerklePath(),
                unsignedTx.getBlockOfProof(),
                unsignedTx.getBlockOfProofContext(),
                signature,
                unsignedTx.getPublicKey(),
                unsignedTx.getNetworkByte());
        return tx;
    }
}
