// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.util.Preconditions;

import java.math.BigInteger;
import java.sql.SQLException;

public class Context {
    private static VeriBlockNetworkParameters veriblockNetworkParameters;
    private static BitcoinNetworkParameters bitcoinNetworkParameters;
    private static VeriBlockStore veriblockStore;
    private static BitcoinStore bitcoinStore;
    private static AuditorChangesStore changeStore;
    private static PoPTransactionsDBStore popTxDBStore;

    private Context() {
    }

    public static VeriBlockNetworkParameters getVeriBlockNetworkParameters() {
        return veriblockNetworkParameters;
    }

    public static BitcoinNetworkParameters getBitcoinNetworkParameters() {
        return bitcoinNetworkParameters;
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

    public static PoPTransactionsDBStore getPopTxDBStore() {return popTxDBStore;}

    public static void resetSecurity() throws SQLException {
        veriblockStore.clear();
        bitcoinStore.clear();
        changeStore.clear();
        popTxDBStore.clear();
    }

    public static void init(VeriBlockNetworkParameters veriblockNetworkParametersArg,
                            BitcoinNetworkParameters bitcoinNetworkParametersArg,
                            VeriBlockStore veriblockStoreArg, BitcoinStore bitcoinStoreArg,
                            AuditorChangesStore changeStoreArg, PoPTransactionsDBStore popTxDBRepoArg) {
        Preconditions.notNull(veriblockNetworkParametersArg, "VeriBlock network parameters cannot be null");
        Preconditions.notNull(bitcoinNetworkParametersArg, "Bitcoin network parameters cannot be null");
        Preconditions.notNull(veriblockStoreArg, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStoreArg, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStoreArg, "Change store cannot be null");

        veriblockNetworkParameters = veriblockNetworkParametersArg;
        bitcoinNetworkParameters = bitcoinNetworkParametersArg;
        veriblockStore = veriblockStoreArg;
        bitcoinStore = bitcoinStoreArg;
        changeStore = changeStoreArg;
        popTxDBStore = popTxDBRepoArg;
    }

    public static void init() throws BlockStoreException, SQLException {
        // check if the context is initialized as none of the parameters can be null
        if(veriblockNetworkParameters == null) {
            init(new MainNetParameters(), new BitcoinMainNetParameters(),
                 new VeriBlockStore(), new BitcoinStore(),
                 new AuditorChangesStore(), new PoPTransactionsDBStore());
        }
    }
}
