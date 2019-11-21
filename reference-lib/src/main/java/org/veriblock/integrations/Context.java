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
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.conf.AppConfiguration;
import org.veriblock.sdk.conf.NetworkParameters;
import org.veriblock.sdk.util.Preconditions;

import java.sql.SQLException;
import java.util.Properties;

public class Context {
    private static NetworkParameters networkParameters;
    private static VeriBlockStore veriblockStore;
    private static BitcoinStore bitcoinStore;
    private static AuditorChangesStore changeStore;
    private static PoPTransactionsDBStore popTxDBStore;
    private static AppConfiguration configuration;

    private Context() {
    }

    public static NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    public static VeriBlockStore getVeriblockStore() {
        return veriblockStore;
    }

    public static BitcoinStore getBitcoinStore() {
        return bitcoinStore;
    }

    public static AuditorChangesStore getChangeStore() {
        return changeStore;
    }

    public static AppConfiguration getConfiguration(){
        return configuration;
    }

    public static PoPTransactionsDBStore getPopTxDBStore() {return popTxDBStore;}

    public static void resetSecurity() throws SQLException {
        veriblockStore.clear();
        bitcoinStore.clear();
        changeStore.clear();
        popTxDBStore.clear();
    }

    public static void init(AppConfiguration configurationArg, VeriBlockStore veriblockStoreArg,
                            BitcoinStore bitcoinStoreArg, AuditorChangesStore changeStoreArg, PoPTransactionsDBStore popTxDBRepoArg) {
        Preconditions.notNull(configurationArg, "Network parameters cannot be null");
        Preconditions.notNull(veriblockStoreArg, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStoreArg, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStoreArg, "Change store cannot be null");

        networkParameters = configurationArg.getVeriblockNetworkParameters();
        veriblockStore = veriblockStoreArg;
        bitcoinStore = bitcoinStoreArg;
        changeStore = changeStoreArg;
        popTxDBStore = popTxDBRepoArg;
        configuration = configurationArg;
    }

    public static void init() throws BlockStoreException, SQLException {
        if(networkParameters == null) {
            Properties properties = new Properties();
            properties.setProperty("veriblock.blockchain.minimumDifficulty", "900000000000");
            init(new AppConfiguration(properties), new VeriBlockStore(), new BitcoinStore(),
                    new AuditorChangesStore(), new PoPTransactionsDBStore());
        }
    }
}
