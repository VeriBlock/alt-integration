// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;
import org.veriblock.sdk.conf.DefaultConfiguration;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

///TODO: this is not a test - move to helpers package
public class VeriBlockIntegrationLibraryManager {
    
    private static Context securityFiles = null;
    private static VeriBlockSecurity security = null;
    
    private VeriBlockIntegrationLibraryManager() { }

    public static VeriBlockSecurity init() throws SQLException, IOException {
        if(security != null) return security;
        
        String databasePath = Paths.get(FileManager.getTempDirectory(), ConnectionSelector.defaultDatabaseName).toString();
            
        VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
        BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
        AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
        PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(databasePath);

        Properties properties = new Properties();
        properties.setProperty("veriblockNetwork", "main");
        Context.init(new DefaultConfiguration(properties), veriBlockStore, bitcoinStore, auditStore, popTxDBStore);
        Context.resetSecurity();

        security = new VeriBlockSecurity();
        return security;
    }
    
    public static void shutdown() throws SQLException {
        if(security != null) {
            security.shutdown();
        }
        
        security = null;
    }
    
    public static Context getContext() {
        return securityFiles;
    }
}
