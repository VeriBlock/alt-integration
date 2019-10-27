// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPoPTransaction;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VeriBlockTransaction;

public interface IVeriBlockValidation {

    public ValidationResult verifyVeriBlockPoPTx(VeriBlockPoPTransaction request);

    public ValidationResult verifyVeriBlockPublication(VeriBlockPublication request);

    public ValidationResult verifyVeriBlockTransaction(VeriBlockTransaction request);

    public ValidationResult verifyVeriBlockBlock(VeriBlockBlock request);

    public ValidationResult verifyBitcoinBlock(BitcoinBlock request);

    public ValidationResult verifyAltPublication(AltPublication request);
}
