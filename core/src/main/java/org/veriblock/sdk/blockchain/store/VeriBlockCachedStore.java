// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.models.VBlakeHash;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A BlockStore wrapper that caches the best chain in RAM.
  *
  * The implementation is optimized for the case where
  * the best chain is extremely long and the chain head
  * changes due to blockchain growth or shallow reorgs.
  *
  * Thus, the expected difference between the old and
  * new best chain is a small number of recent blocks.
  *
  * The wrapper works by tracking chain head changes.
  * The new and old best chains are traced back to the
  * fork point. The slice between the fork point and
  * the new chain head is added to the cache. The slice
  * between the fork point and the old chain head is
  * dropped from the cache.
  *
  * Once more real-world usage scenarios emerge and
  * stabilize to the point where they can be profiled,
  * it might be possible to improve the implementation,
  * for example, by caching only the last X blocks of
  * the best chain.
  */
public class VeriBlockCachedStore implements BlockStore<StoredVeriBlockBlock, VBlakeHash> {
    private static final Logger log = LoggerFactory.getLogger(VeriBlockCachedStore.class);

    private final BlockStore<StoredVeriBlockBlock, VBlakeHash> store;
    private final Cache bestChain = new Cache();

    // TODO: correctness: how likely are we to get truncated VBlake hash collisions?
    // TODO: performance: how to detect them efficiently?
    private class Cache {

        // a hack to be able to look up blocks using truncated VBlake hashes
        private final Map<VBlakeHash, StoredVeriBlockBlock> full = new HashMap<>();
        private final Map<VBlakeHash, StoredVeriBlockBlock> previous = new HashMap<>();
        private final Map<VBlakeHash, StoredVeriBlockBlock> keystone = new HashMap<>();

        public void clear() {
            full.clear();
            previous.clear();
            keystone.clear();
        }

        public void add(StoredVeriBlockBlock block) {
            VBlakeHash hash = block.getHash();

            full.put(hash, block);
            previous.put(hash.trimToPreviousBlockSize(), block);
            keystone.put(hash.trimToPreviousKeystoneSize(), block);
        }

        // TODO: performance: investigate using the hash length to determine the map to query
        public StoredVeriBlockBlock get(VBlakeHash hash) {
            return full.getOrDefault(hash,
                                     previous.getOrDefault(hash,
                                                           keystone.get(hash)));
        }

        public StoredVeriBlockBlock remove(VBlakeHash hash) {
            StoredVeriBlockBlock removed = get(hash);

            if (removed != null) {
                // need to obtain the full hash in the case if 'hash' is truncated
                VBlakeHash fullHash = removed.getHash();

                full.remove(fullHash);
                previous.remove(fullHash.trimToPreviousBlockSize());
                keystone.remove(fullHash.trimToPreviousKeystoneSize());
            };

            return removed;
        }
    }
    
    public VeriBlockCachedStore(BlockStore<StoredVeriBlockBlock, VBlakeHash> store) throws SQLException {
        this.store = store;
        
        updateBestChain(null, store.getChainHead());
    }

    public void shutdown() {
        store.shutdown();
    }

    public void clear() throws SQLException {
        store.clear();
        bestChain.clear();
    }

    // Nulls as either of the arguments are treated as an empty chain.
    // The old and new chain do not need to have a common ancestor.
    private void updateBestChain(StoredVeriBlockBlock oldHead, StoredVeriBlockBlock newHead) throws SQLException {
        StoredVeriBlockBlock forkPoint = newHead;
        
        // Add the new chain to the cache until we hit a block
        // that's already cached. That block is the fork point.
        while (forkPoint != null && bestChain.get(forkPoint.getHash()) == null) {
            bestChain.add(forkPoint);
            forkPoint = get(forkPoint.getBlock().getPreviousBlock());
        }
        
        // Remove the old chain up to the fork point from the cache
        while (oldHead != null && !oldHead.equals(forkPoint)) {
            bestChain.remove(oldHead.getHash());
            oldHead = bestChain.get(oldHead.getBlock().getPreviousBlock());
        }
    }

    public StoredVeriBlockBlock getChainHead() throws BlockStoreException, SQLException {
        return store.getChainHead();
    }

    public StoredVeriBlockBlock setChainHead(StoredVeriBlockBlock chainHead) throws BlockStoreException, SQLException {
        StoredVeriBlockBlock previousChainHead = store.setChainHead(chainHead);

        updateBestChain(previousChainHead, chainHead);

        return previousChainHead;
    }

    public void put(StoredVeriBlockBlock block) throws BlockStoreException, SQLException {
        // put does not affect the best chain as it cannot replace blocks
        store.put(block);
    }

    public StoredVeriBlockBlock get(VBlakeHash hash) throws BlockStoreException, SQLException {
        return store.get(hash);
    }

    public StoredVeriBlockBlock erase(VBlakeHash hash) throws BlockStoreException, SQLException {
        // erase is not allowed to delete a block pointed to by another block
        // or a chain head, so it cannot affect the best chain
        return store.erase(hash);
     }

    public StoredVeriBlockBlock replace(VBlakeHash hash, StoredVeriBlockBlock block) throws BlockStoreException, SQLException {
        StoredVeriBlockBlock replaced = store.replace(hash, block);

        // if the block is in the best chain, update the best chain
        StoredVeriBlockBlock cached = bestChain.remove(hash);
        if (cached != null) {
            assert(cached.equals(replaced));
            bestChain.add(block);
        }

        return replaced;
    }

    public List<StoredVeriBlockBlock> get(VBlakeHash hash, int count) throws BlockStoreException, SQLException {
        return store.get(hash, count);
    }

    public StoredVeriBlockBlock getFromChain(VBlakeHash hash, int blocksAgo) throws BlockStoreException, SQLException {
        return store.getFromChain(hash, blocksAgo);
    }

    public StoredVeriBlockBlock scanBestChain(VBlakeHash hash) throws BlockStoreException, SQLException {
        return bestChain.get(hash);
    }
}
