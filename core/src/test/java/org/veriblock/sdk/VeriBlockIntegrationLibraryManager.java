// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;

import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.VeriBlockCachedStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.sqlite.ConnectionSelector;

///TODO: this is not a test - move to helpers package
//TODO: refactoring is overdue
public class VeriBlockIntegrationLibraryManager {
    private static VeriBlockSecurity security = null;
    private final VeriBlockNetworkParameters veriblockNetworkParameters;
    private final BitcoinNetworkParameters bitcoinNetworkParameters;

    public VeriBlockIntegrationLibraryManager() {
        veriblockNetworkParameters = new MainNetParameters();
        bitcoinNetworkParameters = new BitcoinMainNetParameters();
    }

    public VeriBlockIntegrationLibraryManager(VeriBlockNetworkParameters veriblockNetworkParameters,
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

    public VeriBlockSecurity init() throws SQLException, IOException {
        Context context = initContext(null);
        context.resetSecurity();

        security = new VeriBlockSecurity(context);
        return security;
    }

    public void shutdown() throws SQLException {
        if(security != null) {
            security.shutdown();
        }
        
        security = null;
    }
    
    private Context initContext(String path) throws SQLException {
        return new Context(getVeriblockNetworkParameters(), getBitcoinNetworkParameters(),
                           new VeriBlockCachedStore(
                                    new VeriBlockStore(ConnectionSelector.setConnection(path))),
                           new BitcoinStore(ConnectionSelector.setConnection(path)),
                           new AuditorChangesStore(ConnectionSelector.setConnection(path)),
                           new PoPTransactionsDBStore(ConnectionSelector.setConnection(path)));
    }
}
