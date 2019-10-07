// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import java.sql.SQLException;

import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.params.NetworkParameters;
import org.veriblock.sdk.util.Preconditions;
import org.veriblock.sdk.BlockStoreException;

public class Context {
    private NetworkParameters networkParameters;
    private VeriBlockStore veriblockStore;
    private BitcoinStore bitcoinStore;
    private AuditorChangesStore changeStore;

    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    public VeriBlockStore getVeriblockStore() {
        return veriblockStore;
    }

    public BitcoinStore getBitcoinStore() {
        return bitcoinStore;
    }

    public AuditorChangesStore getChangeStore() {
        return changeStore;
    }

    public Context(NetworkParameters networkParameters, VeriBlockStore veriblockStore,
            BitcoinStore bitcoinStore, AuditorChangesStore changeStore) {
        Preconditions.notNull(networkParameters, "Network parameters cannot be null");
        Preconditions.notNull(veriblockStore, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStore, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStore, "Change store cannot be null");

        this.networkParameters = networkParameters;
        this.veriblockStore = veriblockStore;
        this.bitcoinStore = bitcoinStore;
        this.changeStore = changeStore;
    }
    
    public Context() throws BlockStoreException, SQLException {
        this(new MainNetParameters(),
                new VeriBlockStore(),
                new BitcoinStore(),
                new AuditorChangesStore());
    }
}
