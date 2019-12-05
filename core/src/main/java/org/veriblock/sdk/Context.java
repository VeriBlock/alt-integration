// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.auditor.store.ChangeStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.BlockStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionStore;
import org.veriblock.sdk.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.util.Preconditions;

import java.sql.SQLException;

public class Context {
    private static VeriBlockNetworkParameters veriblockNetworkParameters;
    private static BitcoinNetworkParameters bitcoinNetworkParameters;
    private static BlockStore<StoredVeriBlockBlock, VBlakeHash> veriblockStore;
    private static BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStore;
    private static ChangeStore changeStore;
    private static PoPTransactionStore popTxStore;

    private Context() {
    }

    public static VeriBlockNetworkParameters getVeriBlockNetworkParameters() {
        return veriblockNetworkParameters;
    }

    public static BitcoinNetworkParameters getBitcoinNetworkParameters() {
        return bitcoinNetworkParameters;
    }

    public static BlockStore<StoredVeriBlockBlock, VBlakeHash> getVeriblockStore() {
        return veriblockStore;
    }

    public static BlockStore<StoredBitcoinBlock, Sha256Hash> getBitcoinStore() {
        return bitcoinStore;
    }

    public static ChangeStore getChangeStore() {
        return changeStore;
    }

    public static PoPTransactionStore getPopTxStore() {
        return popTxStore;
    }

    public static void resetSecurity() throws SQLException {
        veriblockStore.clear();
        bitcoinStore.clear();
        changeStore.clear();
        popTxStore.clear();
    }

    public static void init(VeriBlockNetworkParameters veriblockNetworkParametersArg,
                            BitcoinNetworkParameters bitcoinNetworkParametersArg,
                            BlockStore<StoredVeriBlockBlock, VBlakeHash> veriblockStoreArg,
                            BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStoreArg,
                            ChangeStore changeStoreArg, PoPTransactionStore popTxStoreArg) {
        Preconditions.notNull(veriblockNetworkParametersArg, "VeriBlock network parameters cannot be null");
        Preconditions.notNull(bitcoinNetworkParametersArg, "Bitcoin network parameters cannot be null");
        Preconditions.notNull(veriblockStoreArg, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStoreArg, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStoreArg, "Change store cannot be null");
        Preconditions.notNull(popTxStoreArg, "popTxStore cannot be null");

        veriblockNetworkParameters = veriblockNetworkParametersArg;
        bitcoinNetworkParameters = bitcoinNetworkParametersArg;
        veriblockStore = veriblockStoreArg;
        bitcoinStore = bitcoinStoreArg;
        changeStore = changeStoreArg;
        popTxStore = popTxStoreArg;
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
