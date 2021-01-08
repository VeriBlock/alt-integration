// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.protoservice;

import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.BitcoinBlock;
import org.veriblock.sdk.models.ValidationResult;
import org.veriblock.sdk.models.VeriBlockBlock;
import org.veriblock.sdk.models.VeriBlockPoPTransaction;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.models.VeriBlockTransaction;

public interface IVeriBlockValidation {

    public ValidationResult verifyVeriBlockPoPTx(VeriBlockPoPTransaction request);

    public ValidationResult verifyVeriBlockPublication(VeriBlockPublication request);

    public ValidationResult verifyVeriBlockTransaction(VeriBlockTransaction request);

    public ValidationResult verifyVeriBlockBlock(VeriBlockBlock request);

    public ValidationResult verifyBitcoinBlock(BitcoinBlock request);

    public ValidationResult verifyAltPublication(AltPublication request);
}
