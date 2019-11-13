// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain;

import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.VeriBlockBlock;

public class VeriBlockBlockchainBootstrapConfig {

    List<VeriBlockBlock> blocks;

    public VeriBlockBlockchainBootstrapConfig() {
        this.blocks = new ArrayList<VeriBlockBlock>();
    }

    public VeriBlockBlockchainBootstrapConfig(List<VeriBlockBlock> blocks) {
        this.blocks = blocks;
    }

}
