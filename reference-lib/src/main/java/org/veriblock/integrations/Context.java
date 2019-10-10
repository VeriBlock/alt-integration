// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import java.math.BigInteger;
import java.sql.SQLException;

import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.integrations.blockchain.store.StoredVeriBlockBlock;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.params.NetworkParameters;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.services.SerializeDeserializeService;
import org.veriblock.sdk.util.BitcoinUtils;
import org.veriblock.sdk.util.Preconditions;
import org.veriblock.sdk.util.Utils;

public class Context {
    private NetworkParameters networkParameters;
    private VeriBlockStore veriblockStore;
    private BitcoinStore bitcoinStore;
    private AuditorChangesStore changeStore;
    private PoPTransactionsDBStore popTxDBStore;
    private final VeriBlockBlock bootstrapVeriBlockBlock;
    private final BitcoinBlock bootstrapBitcoinBlock;

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

    public PoPTransactionsDBStore getPopTxDBStore() {return popTxDBStore;}

    public void resetSecurity() throws SQLException {
        veriblockStore.clear();
        bitcoinStore.clear();
        changeStore.clear();
        popTxDBStore.clear();
    }

    public Context(NetworkParameters networkParameters, VeriBlockStore veriblockStore,
            BitcoinStore bitcoinStore, AuditorChangesStore changeStore, PoPTransactionsDBStore popTxDBRepo)
            throws SQLException {
        Preconditions.notNull(networkParameters, "Network parameters cannot be null");
        Preconditions.notNull(veriblockStore, "VeriBlock store cannot be null");
        Preconditions.notNull(bitcoinStore, "Bitcoin store cannot be null");
        Preconditions.notNull(changeStore, "Change store cannot be null");

        this.networkParameters = networkParameters;
        this.veriblockStore = veriblockStore;
        this.bitcoinStore = bitcoinStore;
        this.changeStore = changeStore;
        this.popTxDBStore = popTxDBRepo;

        String vbkEnv = System.getenv("BOOTSTRAP_VBK_BLOCK");
        if (vbkEnv != null) {
            byte[] blockHeader = Utils.decodeHex(vbkEnv);
            assert blockHeader.length == 64;

            bootstrapVeriBlockBlock = SerializeDeserializeService.parseVeriBlockBlock(blockHeader);

            if (this.veriblockStore.getChainHead() == null) {
                BigInteger work = BitcoinUtils.decodeCompactBits(bootstrapVeriBlockBlock.getDifficulty());
                StoredVeriBlockBlock storedBlock = new StoredVeriBlockBlock(
                    this.bootstrapVeriBlockBlock, work, Sha256Hash.ZERO_HASH);

                this.veriblockStore.put(storedBlock);
                this.veriblockStore.setChainHead(storedBlock);
            }
        } else {
            bootstrapVeriBlockBlock = null;
        }

        String btcEnv = System.getenv("BOOTSTRAP_BTC_BLOCK");
        if (btcEnv != null) {
            String[] btcEnvComponents = btcEnv.split(":", 2);
            assert btcEnvComponents.length == 2;

            byte[] blockHeader = Utils.decodeHex(btcEnvComponents[0]);
            assert blockHeader.length == 80;

            bootstrapBitcoinBlock = SerializeDeserializeService.parseBitcoinBlock(blockHeader);

            if (this.bitcoinStore.getChainHead() == null) {

                int blockHeight = Integer.parseInt(btcEnvComponents[1]);
                BigInteger work = BitcoinUtils.decodeCompactBits(bootstrapBitcoinBlock.getBits());
                StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(
                                                    bootstrapBitcoinBlock, work, blockHeight);

                this.bitcoinStore.put(storedBlock);
                this.bitcoinStore.setChainHead(storedBlock);
            }
        } else {
            bootstrapBitcoinBlock = null;
        }
    }
    
    public Context() throws BlockStoreException, SQLException {
        this(new MainNetParameters(),
                new VeriBlockStore(),
                new BitcoinStore(),
                new AuditorChangesStore(), new PoPTransactionsDBStore());
    }

    public boolean isBootstrapBlock(VeriBlockBlock block) {
        return block.equals(bootstrapVeriBlockBlock);
    }

    public boolean isBootstrapBlock(BitcoinBlock block) {
        return block.equals(bootstrapBitcoinBlock);
    }

}
