// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.transactions.signature;

import org.veriblock.sdk.util.KeyGenerator;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class VeriBlockSignatureKeys {
    
    // forbid constructor
    private VeriBlockSignatureKeys() {};
    
    public static KeyPair getDefaultKey() throws SignatureException {
        try {
            return KeyGenerator.generate();
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new SignatureException(e);
        }
    }
    
    public static String getDefaultAddress() throws SignatureException {
        return KeyGenerator.addressFromPublicKey(getDefaultKey().getPublic().getEncoded());
    }
    
    public static byte[] getDefaultPublicKey() throws SignatureException {
        return getDefaultKey().getPublic().getEncoded();
    }
}
