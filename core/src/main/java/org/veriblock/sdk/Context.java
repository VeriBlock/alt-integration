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
import org.veriblock.sdk.blockchain.store.VeriBlockCachedStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinMainNetParameters;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.MainNetParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.Sha256Hash;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.util.Preconditions;

import java.sql.SQLException;

public class Context {
    private final VeriBlockNetworkParameters veriblockNetworkParameters;
    private final BitcoinNetworkParameters bitcoinNetworkParameters;
    private final BlockStore<StoredVeriBlockBlock, VBlakeHash> veriblockStore;
    private final BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStore;
    private final ChangeStore changeStore;
    private final PoPTransactionStore popTxStore;

    public Context(VeriBlockNetworkParameters veriblockNetworkParameters,
                   BitcoinNetworkParameters bitcoinNetworkParameters,
                   BlockStore<StoredVeriBlockBlock, VBlakeHash> veriblockStore,
                   BlockStore<StoredBitcoinBlock, Sha256Hash> bitcoinStore,
                   ChangeStore changeStore, PoPTransactionStore popTxStore) {
        Preconditions.notNull(veriblockNetworkParameters, "VeriBlock network parameters cannot be null");
        Preconditions.notNull(bitcoinNetworkParameters, "Bitcoin network parameters cannot be null");
        Preconditions.notNull(veriblockStore, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStore, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStore, "Change store cannot be null");
        Preconditions.notNull(popTxStore, "popTxStore cannot be null");

        this.veriblockNetworkParameters = veriblockNetworkParameters;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.veriblockStore = veriblockStore;
        this.bitcoinStore = bitcoinStore;
        this.changeStore = changeStore;
        this.popTxStore = popTxStore;
    }

    public VeriBlockNetworkParameters getVeriBlockNetworkParameters() {
        return veriblockNetworkParameters;
    }

    public BitcoinNetworkParameters getBitcoinNetworkParameters() {
        return bitcoinNetworkParameters;
    }

    public BlockStore<StoredVeriBlockBlock, VBlakeHash> getVeriblockStore() {
        return veriblockStore;
    }

    public BlockStore<StoredBitcoinBlock, Sha256Hash> getBitcoinStore() {
        return bitcoinStore;
    }

    public ChangeStore getChangeStore() {
        return changeStore;
    }

    public PoPTransactionStore getPopTxStore() {
        return popTxStore;
    }

    public void resetSecurity() throws SQLException {
        veriblockStore.clear();
        bitcoinStore.clear();
        changeStore.clear();
        popTxStore.clear();
    }

    public static Context init() throws BlockStoreException, SQLException {
       return new Context(new MainNetParameters(), new BitcoinMainNetParameters(),
                          new VeriBlockCachedStore(
                                new VeriBlockStore(ConnectionSelector.setConnectionDefault())),
                          new BitcoinStore(ConnectionSelector.setConnectionDefault()),
                          new AuditorChangesStore(ConnectionSelector.setConnectionDefault()),
                          new PoPTransactionsDBStore(ConnectionSelector.setConnectionDefault()));
    }
}
