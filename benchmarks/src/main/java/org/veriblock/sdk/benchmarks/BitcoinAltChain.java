// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.benchmarks;

import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.mock.BitcoinDefaults;
import org.veriblock.sdk.mock.VeriBlockDefaults;
import org.veriblock.sdk.sqlite.ConnectionSelector;

import java.sql.SQLException;

public class BitcoinAltChain {
    public static VeriBlockSecurity create(ConnectionSelector.Factory connectionFactory) throws SQLException {
        Context context = Context.init(VeriBlockDefaults.networkParameters,
                                       BitcoinDefaults.networkParameters,
                                       connectionFactory);
        context.resetSecurity();

        return new VeriBlockSecurity(context);
    }
}
