// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.io.IOException;
import java.sql.SQLException;

import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.sqlite.ConnectionSelector;

public class SecurityFactory {
    private final VeriBlockNetworkParameters veriblockNetworkParameters;
    private final BitcoinNetworkParameters bitcoinNetworkParameters;

    public SecurityFactory() {
        veriblockNetworkParameters = new MainNetParameters();
        bitcoinNetworkParameters = new BitcoinMainNetParameters();
    }

    public SecurityFactory(VeriBlockNetworkParameters veriblockNetworkParameters,
                           BitcoinNetworkParameters bitcoinNetworkParameters) {
        this.veriblockNetworkParameters = veriblockNetworkParameters;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
    }

    public VeriBlockNetworkParameters getVeriblockNetworkParameters() {
        return veriblockNetworkParameters;
    }

    public BitcoinNetworkParameters getBitcoinNetworkParameters() {
        return bitcoinNetworkParameters;
    }

    public VeriBlockSecurity createInstance() throws SQLException, IOException {
        Context context = createContext();
        context.resetSecurity();

        return new VeriBlockSecurity(context);
    }

    private Context createContext() throws SQLException {
        VeriBlockStore veriBlockStore = new VeriBlockStore(ConnectionSelector.setConnectionInMemory());
        BitcoinStore bitcoinStore = new BitcoinStore(ConnectionSelector.setConnectionInMemory());
        AuditorChangesStore changeStore = new AuditorChangesStore(ConnectionSelector.setConnectionInMemory());
        PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(ConnectionSelector.setConnectionInMemory());

        return new Context(getVeriblockNetworkParameters(), getBitcoinNetworkParameters(),
                           veriBlockStore, bitcoinStore, changeStore, popTxDBStore);
    }
}
