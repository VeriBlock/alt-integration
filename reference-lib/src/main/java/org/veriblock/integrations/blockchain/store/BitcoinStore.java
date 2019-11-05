// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain.store;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.tables.BitcoinBlockRepository;
import org.veriblock.integrations.sqlite.tables.KeyValueData;
import org.veriblock.integrations.sqlite.tables.KeyValueRepository;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.util.Utils;

public class BitcoinStore {
    //private static final int DEFAULT_NUM_HEADERS = 5000;
    private static final Logger log = LoggerFactory.getLogger(BitcoinStore.class);
    
    // underlying database
    private final Connection databaseConnection;
    private final BitcoinBlockRepository bitcoinRepository;
    private final KeyValueRepository keyValueRepository;
    
    private final String chainHeadRepositoryName = "chainHead";
    
    public BitcoinStore() throws SQLException {
        databaseConnection = ConnectionSelector.setConnectionDefault();
        bitcoinRepository = new BitcoinBlockRepository(databaseConnection);
        keyValueRepository = new KeyValueRepository(databaseConnection);
    }
    
    public BitcoinStore(String databasePath) throws SQLException {
        databaseConnection = ConnectionSelector.setConnection(databasePath);
        bitcoinRepository = new BitcoinBlockRepository(databaseConnection);
        keyValueRepository = new KeyValueRepository(databaseConnection);
    }
    
    public void shutdown() {
        try {
            if(databaseConnection != null) databaseConnection.close();
        } catch (SQLException e) {
            log.debug("Error closing database connection", e);
        }
    }
    
    public void clear() throws SQLException {
        bitcoinRepository.clear();
        keyValueRepository.clear();
    }
    
    public StoredBitcoinBlock getChainHead() throws BlockStoreException, SQLException {
        String headEncoded = keyValueRepository.getValue(chainHeadRepositoryName);
        if(headEncoded == null) return null;
        
        StoredBitcoinBlock block = get(Sha256Hash.wrap(Utils.decodeHex(headEncoded)));
        return block;
    }

    public StoredBitcoinBlock setChainHead(StoredBitcoinBlock chainHead) throws BlockStoreException, SQLException {
        StoredBitcoinBlock existingBlock = get(chainHead.getHash());
        if(existingBlock == null) {
            throw new BlockStoreException("Chain head should reference existing block");
        }
        
        StoredBitcoinBlock previousBlock = getChainHead();
        
        String headEncoded = Utils.encodeHex(chainHead.getBlock().getHash().getBytes());
        KeyValueData data = new KeyValueData();
        data.key = chainHeadRepositoryName;
        data.value = headEncoded;
        keyValueRepository.save(data.key, data.value);
        
        return previousBlock;
    }

    public void put(StoredBitcoinBlock storedBlock) throws BlockStoreException, SQLException {
        bitcoinRepository.save(storedBlock);
    }
    
    public StoredBitcoinBlock get(Sha256Hash hash) throws BlockStoreException, SQLException {
        return bitcoinRepository.get(hash);
    }

    public StoredBitcoinBlock erase(Sha256Hash hash) throws BlockStoreException, SQLException {
        StoredBitcoinBlock erased = get(hash);

        if(erased != null && bitcoinRepository.isInUse(hash)) {
            throw new BlockStoreException("Cannot erase a block referenced by another block");
        }

        bitcoinRepository.delete(hash);
        return erased;
    }

    public StoredBitcoinBlock replace(Sha256Hash hash, StoredBitcoinBlock storedBlock) throws BlockStoreException, SQLException {
        StoredBitcoinBlock replaced = erase(hash);
        put(storedBlock);
        return replaced;
    }

    public List<StoredBitcoinBlock> get(Sha256Hash hash, int count) throws BlockStoreException, SQLException {
        List<StoredBitcoinBlock> blocks = new ArrayList<>();
        Sha256Hash currentHash = hash;
        
        while(true) {
            // check if we got the needed blocks
            if(blocks.size() >= count) break;
            StoredBitcoinBlock current = get(currentHash);
            // check if the block exists
            if(current == null) break;
            blocks.add(current);
            
            // check if we found the Genesis block
            if(currentHash.toBigInteger().compareTo(BigInteger.ZERO) == 0) break;
            currentHash = current.getBlock().getPreviousBlock();
        }
        
        return blocks;
    }

    // search for a block 'blocksAgo' blocks before the block with 'hash'
    public StoredBitcoinBlock getFromChain(Sha256Hash hash, int blocksAgo) throws BlockStoreException, SQLException {
        List<StoredBitcoinBlock> blocks = get(hash, blocksAgo + 1);
        // check if the branch is long enough
        if(blocks.size() < (blocksAgo + 1)) {
            return null;
        }
        return blocks.get(blocksAgo);
    }

    // start from the chainHead and search for a block with hash
    public StoredBitcoinBlock scanBestChain(Sha256Hash hash) throws BlockStoreException, SQLException {
        StoredBitcoinBlock current = getChainHead();
        if(current == null) return null;
        
        Sha256Hash currentHash = current.getHash();
        
        while(true) {
            if(currentHash.compareTo(hash) == 0) return current;
            // check if the block exists
            if(current == null) return null;
            // check if we found the Genesis block
            if(currentHash.toBigInteger().compareTo(BigInteger.ZERO) == 0) return null;
            
            currentHash = current.getBlock().getPreviousBlock();
            current = get(currentHash);
        }
    }
}
