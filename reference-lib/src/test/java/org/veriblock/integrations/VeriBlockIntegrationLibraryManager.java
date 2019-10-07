// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;

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
            
        securityFiles = new Context(new MainNetParameters(), veriBlockStore, bitcoinStore, auditStore);
        
        // erase database for testing determination
        if(securityFiles != null) {            
            securityFiles.getBitcoinStore().clear();
            securityFiles.getVeriblockStore().clear();
            securityFiles.getChangeStore().clear();
        }

        security = new VeriBlockSecurity(securityFiles);
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
