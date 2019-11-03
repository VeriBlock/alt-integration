// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.transactions.signature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

public class VeriBlockSigner {

    public static byte[] signMessageWithPrivateKey(byte[] message, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return Utility.signMessageWithPrivateKey(message, privateKey);
    }
}
