// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import org.junit.Test;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;
import org.veriblock.sdk.conf.AppInjector;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

///TODO: this is not a test - move to helpers package
public class VeriBlockIntegrationLibraryManager {
    private static VeriBlockSecurity security = null;
    private static String PACKAGE_NAME = "test";
    private AppInjector appInjector;

    //Should have public constructor.
    public VeriBlockIntegrationLibraryManager() {
        appInjector = new AppInjector(PACKAGE_NAME);

    }

    public VeriBlockSecurity init() throws SQLException, IOException {
        String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();
        initContext(databasePath);
        Context.resetSecurity();

        security = new VeriBlockSecurity();
        return security;
    }
    
    public void shutdown() throws SQLException {
        if(security != null) {
            security.shutdown();
        }
        
        security = null;
    }
    
    private void initContext(String path) throws SQLException {
        VeriBlockStore veriBlockStore = new VeriBlockStore(path);
        BitcoinStore bitcoinStore = new BitcoinStore(path);
        AuditorChangesStore auditStore = new AuditorChangesStore(path);
        PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(path);

        Context.init(appInjector.provideWallet(), veriBlockStore, bitcoinStore, auditStore, popTxDBStore);
    }

    @Test
    public void test() {
    }
}
