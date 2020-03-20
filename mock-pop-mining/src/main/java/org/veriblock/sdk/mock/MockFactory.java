// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.mock;

import java.sql.SQLException;

import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.VeriBlockCachedStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.sqlite.ConnectionSelector;

public class MockFactory {
    private final VeriBlockBlockchain veriBlockBlockchain;
    private final BitcoinBlockchain bitcoinBlockchain;

    private final AltChainPopMiner apm;
    private final VeriBlockPopMiner vpm;

    private final PoPMiningCoordinator coordinator;

    public MockFactory(VeriBlockNetworkParameters veriblockNetworkParameters, BitcoinNetworkParameters bitcoinNetworkParameters, ConnectionSelector.Factory connectionFactory) throws SQLException {
        VeriBlockCachedStore veriBlockStore = new VeriBlockCachedStore(new VeriBlockStore(connectionFactory.createConnection()));
        BitcoinStore bitcoinStore = new BitcoinStore(connectionFactory.createConnection());
        veriBlockStore.clear();
        bitcoinStore.clear();

        veriBlockBlockchain = new VeriBlockBlockchain(veriblockNetworkParameters, veriBlockStore, bitcoinStore);
        bitcoinBlockchain = new BitcoinBlockchain(bitcoinNetworkParameters, bitcoinStore);

        apm = new AltChainPopMiner(veriBlockBlockchain);
        vpm = new VeriBlockPopMiner(veriBlockBlockchain, bitcoinBlockchain);

        coordinator = new PoPMiningCoordinator(apm, vpm);
    }

    public MockFactory(ConnectionSelector.Factory connectionFactory) throws SQLException {
        this(VeriBlockDefaults.networkParameters, BitcoinDefaults.networkParameters, connectionFactory);
    }

    public MockFactory() throws SQLException {
        this(() -> ConnectionSelector.setConnectionInMemory());
    }

    public VeriBlockBlockchain getVeriBlockBlockchain() {
        return veriBlockBlockchain;
    }

    public BitcoinBlockchain getBitcoinBlockchain() {
        return bitcoinBlockchain;
    }

    public AltChainPopMiner getAltChainPopMiner() {
        return apm;
    }

    public VeriBlockPopMiner getVeriBlockPopMiner() {
        return vpm;
    }

    public PoPMiningCoordinator getCoordinator() {
        return coordinator;
    }

}
