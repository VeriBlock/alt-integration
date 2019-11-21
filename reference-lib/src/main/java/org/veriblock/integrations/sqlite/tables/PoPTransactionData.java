// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.VeriBlockPublication;

import java.util.List;

public class PoPTransactionData {
     public String txHash;
     public AltPublication altPublication;
     public List<VeriBlockPublication> veriBlockPublications;

     public PoPTransactionData(String hash, AltPublication altPublciation, List<VeriBlockPublication> veriBlockPublications)
     {
          this.txHash = hash;
          this.altPublication = altPublciation;
          this.veriBlockPublications = veriBlockPublications;
     }
}
