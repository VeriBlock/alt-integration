// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.changes;

import org.veriblock.integrations.auditor.Change;
import org.veriblock.integrations.auditor.Operation;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.Constants;

public class SetBitcoinHeadChange extends Change {
    @Override
    public String getChainIdentifier() {
        return Constants.BITCOIN_HEADER_MAGIC;
    }

    @Override
    public Operation getOperation() {
        return Operation.SET_HEAD;
    }

    public SetBitcoinHeadChange(StoredBitcoinBlock oldValue, StoredBitcoinBlock newValue) {
        super(oldValue != null ? oldValue.serialize() : new byte[]{},
                newValue != null ? newValue.serialize() : new byte[]{});
    }
}
