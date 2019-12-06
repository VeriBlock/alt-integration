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

import org.junit.Test;
import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.conf.BitcoinNetworkParameters;
import org.veriblock.sdk.conf.VeriBlockNetworkParameters;
import org.veriblock.sdk.sqlite.ConnectionSelector;

///TODO: this is not a test - move to helpers package
public class VeriBlockIntegrationLibraryManager {
    private static VeriBlockSecurity security = null;

    //Should have public constructor.
    public VeriBlockIntegrationLibraryManager() {
    }

    public VeriBlockNetworkParameters getVeriblockNetworkParameters() {
        // mainnet
        String minDifficulty = "900000000000";
        Byte magicByte = null;

        return new VeriBlockNetworkParameters() {
                    public BigInteger getMinimumDifficulty() {
                        return new BigInteger(minDifficulty);
                    }
                    public Byte getTransactionMagicByte() {
                        return magicByte;
                    }
                };
    }

    public BitcoinNetworkParameters getBitcoinNetworkParameters() {
        // mainnet
        return new BitcoinNetworkParameters() {
            public BigInteger getPowLimit() {
                return new BigInteger("00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
            }
            public int getPowTargetTimespan() {
                return 1209600;
            }
            public int getPowTargetSpacing() {
                return 600;
            }
            public boolean getAllowMinDifficultyBlocks() {
                return false;
            }
            public boolean getPowNoRetargeting() {
                return false;
            }
        };
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
        VeriBlockStore veriBlockStore = new VeriBlockStore(ConnectionSelector.setConnection(path));
        BitcoinStore bitcoinStore = new BitcoinStore(ConnectionSelector.setConnection(path));
        AuditorChangesStore changeStore = new AuditorChangesStore(ConnectionSelector.setConnection(path));
        PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(ConnectionSelector.setConnection(path));

        return new Context(getVeriblockNetworkParameters(), getBitcoinNetworkParameters(),
                           veriBlockStore, bitcoinStore, changeStore, popTxDBStore);
    }

    @Test
    public void test() {
    }
}
