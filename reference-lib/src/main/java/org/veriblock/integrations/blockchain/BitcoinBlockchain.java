// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.auditor.Change;
import org.veriblock.integrations.blockchain.changes.AddBitcoinBlockChange;
import org.veriblock.integrations.blockchain.changes.SetBitcoinHeadChange;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.StoredBitcoinBlock;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.Constants;
import org.veriblock.sdk.Sha256Hash;
import org.veriblock.sdk.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.BitcoinUtils;
import org.veriblock.sdk.util.Preconditions;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BitcoinBlockchain {
    private static final Logger log = LoggerFactory.getLogger(BitcoinBlockchain.class);

    private static final int MINIMUM_TIMESTAMP_BLOCK_COUNT = 11;
    private static final int DIFFICULTY_ADJUST_BLOCK_COUNT = 2016;

    private final BitcoinStore store;
    private final Map<Sha256Hash, StoredBitcoinBlock> temporalStore;
    private StoredBitcoinBlock temporaryChainHead = null;

    private boolean hasTemporaryModifications() {
        return temporaryChainHead != null || temporalStore.size() > 0;
    }

    public BitcoinBlockchain(BitcoinStore store) {
        Preconditions.notNull(store, "Store cannot be null");

        this.store = store;
        this.temporalStore = new HashMap<>();
    }

    public BitcoinBlock get(Sha256Hash hash) throws BlockStoreException, SQLException {
        StoredBitcoinBlock storedBlock = getInternal(hash);
        if (storedBlock != null) {
            return storedBlock.getBlock();
        }

        return null;
    }

    public BitcoinBlock searchBestChain(Sha256Hash hash) throws BlockStoreException, SQLException {
        // Look at the temporal store first
        StoredBitcoinBlock storedBlock;
        if (temporaryChainHead != null) {
            storedBlock = temporalStore.get(hash);
        } else {
            storedBlock = store.scanBestChain(hash);
        }

        if (storedBlock != null) {
            return storedBlock.getBlock();
        }

        return null;
    }

    public List<Change> add(BitcoinBlock block) throws VerificationException, BlockStoreException, SQLException {
        Preconditions.state(!hasTemporaryModifications(), "Cannot add a block while having temporary modifications");

        // Lightweight verification of the header
        ValidationService.verify(block);

        BigInteger work = BigInteger.ZERO;
        // TODO: Need to be able to set this accurately on the first block
        int currentHeight = 0;
        if (getChainHeadInternal() != null) {
            // Further verification requiring context
            StoredBitcoinBlock previous = checkConnectivity(block);
            if (!verifyBlock(block, previous)) {
                return Collections.emptyList();
            }
            work = work.add(previous.getWork());
            currentHeight = previous.getHeight() + 1;
        }

        StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(
                block,
                work.add(BitcoinUtils.decodeCompactBits(block.getBits())),
                currentHeight);

        List<Change> changes = new ArrayList<>();
        store.put(storedBlock);
        changes.add(new AddBitcoinBlockChange(null, storedBlock));

        StoredBitcoinBlock chainHead = store.getChainHead();
        if (chainHead == null || storedBlock.getWork().compareTo(chainHead.getWork()) > 0) {
            StoredBitcoinBlock priorHead = store.setChainHead(storedBlock);
            ///HACK: this is a dummy block that represents a change from null to genesis block
            if(priorHead == null) {
                BitcoinBlock emptyBlock = new BitcoinBlock(0, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 0, 1, 0);
                priorHead = new StoredBitcoinBlock(emptyBlock, BigInteger.ONE, 0);
            }
            changes.add(new SetBitcoinHeadChange(priorHead, storedBlock));
        }

        return changes;
    }

    public List<Change> addAll(List<BitcoinBlock> blocks) throws VerificationException, BlockStoreException, SQLException {
        Preconditions.state(!hasTemporaryModifications(), "Cannot add blocks whle having temporary modifications");

        List<Change> changes = new ArrayList<>();
        for (BitcoinBlock block : blocks) {
            changes.addAll(add(block));
        }

        return changes;
    }

    public void addTemporarily(BitcoinBlock block) throws VerificationException, BlockStoreException, SQLException {
        // Lightweight verification of the header
        ValidationService.verify(block);

        // Further verification requiring context
        StoredBitcoinBlock previous = checkConnectivity(block);
        if (!verifyBlock(block, previous)) {
            return;
        }

        StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(
                block,
                previous.getWork().add(BitcoinUtils.decodeCompactBits(block.getBits())),
                previous.getHeight() + 1);

        temporalStore.put(block.getHash(), storedBlock);

        StoredBitcoinBlock chainHead = getChainHeadInternal();
        if (storedBlock.getWork().compareTo(chainHead.getWork()) > 0) {
            temporaryChainHead = storedBlock;
        }
    }

    public void addAllTemporarily(List<BitcoinBlock> blocks) {
        blocks.forEach(t -> {
            try {
                addTemporarily(t);
            } catch (VerificationException | BlockStoreException | SQLException e) {
                throw new BlockStoreException(e);
            }
        });
    }

    public void clearTemporaryModifications() {
        temporaryChainHead = null;
        temporalStore.clear();
    }

    public void rewind(List<Change> changes) throws BlockStoreException, SQLException {
        for (Change change : changes) {
            if (change.getChainIdentifier().equals(Constants.BITCOIN_HEADER_MAGIC)) {
                switch (change.getOperation()) {
                    case ADD_BLOCK:
                        StoredBitcoinBlock newValue = StoredBitcoinBlock.deserialize(change.getNewValue());
                        if (change.getOldValue() != null && change.getOldValue().length > 0) {
                            StoredBitcoinBlock oldValue = StoredBitcoinBlock.deserialize(change.getOldValue());
                            store.replace(newValue.getHash(), oldValue);
                        } else {
                            store.erase(newValue.getHash());
                        }
                        break;
                    case SET_HEAD:
                        StoredBitcoinBlock priorHead = StoredBitcoinBlock.deserialize(change.getOldValue());
                        store.setChainHead(priorHead);
                        break;
                default:
                    break;
                }
            }
        }
    }

    public BitcoinBlock getChainHead() throws SQLException {
        StoredBitcoinBlock chainHead = store.getChainHead();

        return chainHead == null ? null : chainHead.getBlock();
    }

    private StoredBitcoinBlock getInternal(Sha256Hash hash) throws BlockStoreException, SQLException {
        if (temporalStore.containsKey(hash)) {
            return temporalStore.get(hash);
        }

        return store.get(hash);
    }

    private StoredBitcoinBlock getChainHeadInternal() throws BlockStoreException, SQLException {
        if (temporaryChainHead != null) return temporaryChainHead;

        return store.getChainHead();
    }

    private List<StoredBitcoinBlock> getTemporaryBlocks(Sha256Hash hash, int count) {
        List<StoredBitcoinBlock> blocks = new ArrayList<>();

        Sha256Hash cursor = Sha256Hash.wrap(hash.getBytes());
        while (temporalStore.containsKey(cursor)) {
            StoredBitcoinBlock tempBlock = temporalStore.get(cursor);
            blocks.add(tempBlock);

            if (blocks.size() >= count) break;

            cursor = tempBlock.getBlock().getPreviousBlock();
        }

        return blocks;
    }


    private boolean verifyBlock(BitcoinBlock block, StoredBitcoinBlock previous) throws VerificationException, BlockStoreException, SQLException {
        if (!checkDuplicate(block)) return false;

        checkTimestamp(block);
        checkDifficulty(block, previous);

        return true;
    }

    private boolean checkDuplicate(BitcoinBlock block) throws BlockStoreException, SQLException {
        // Duplicate?
        StoredBitcoinBlock duplicate = getInternal(block.getHash());
        if (duplicate != null) {
            log.info("Block '{}' has already been added", block.getHash().toString());
            return false;
        }

        return true;
    }

    private StoredBitcoinBlock checkConnectivity(BitcoinBlock block) throws BlockStoreException, SQLException {
        // Connects to a known "seen" block (except for origin block)
        StoredBitcoinBlock previous = getInternal(block.getPreviousBlock());
        if (previous == null) {
            throw new VerificationException("Block does not fit");
        }

        return previous;
    }

    private void checkTimestamp(BitcoinBlock block) throws VerificationException, BlockStoreException, SQLException {
        // Checks the temporary blocks first
        List<StoredBitcoinBlock> context = getTemporaryBlocks(block.getPreviousBlock(), MINIMUM_TIMESTAMP_BLOCK_COUNT);
        if (context.size() > 0) {
            StoredBitcoinBlock last = context.get(context.size() - 1);
            context.addAll(store.get(last.getBlock().getPreviousBlock(), MINIMUM_TIMESTAMP_BLOCK_COUNT - context.size()));
        } else {
            context.addAll(store.get(block.getPreviousBlock(), MINIMUM_TIMESTAMP_BLOCK_COUNT));
        }

        if (context.size() < MINIMUM_TIMESTAMP_BLOCK_COUNT) {
            log.warn("Not enough context blocks to check timestamp");
            return;
        }

        Optional<Integer> median = context.stream().sorted(Comparator.comparingInt(StoredBitcoinBlock::getHeight).reversed())
                .limit(MINIMUM_TIMESTAMP_BLOCK_COUNT)
                .map(b -> b.getBlock().getTimestamp())
                .sorted()
                .skip(MINIMUM_TIMESTAMP_BLOCK_COUNT / 2)
                .findFirst();

        if (!median.isPresent() || block.getTimestamp() <= median.get()) {
            throw new VerificationException("Block is too far in the past");
        }
    }

    private void checkDifficulty(BitcoinBlock block, StoredBitcoinBlock previous) throws VerificationException, BlockStoreException, SQLException {
        if(!Context.getConfiguration().isValidateBTCBlockDifficulty()){
            return;
        }

        // Previous + 1 = height of block
        if ((previous.getHeight() + 1) % 2016 > 0) {
            // Difficulty should be same as previous
            if (block.getBits() != previous.getBlock().getBits()) {
                throw new VerificationException("Block does not match difficulty of previous block");
            }
        } else {
            // Difficulty needs to adjust

            List<StoredBitcoinBlock> tempBlocks = getTemporaryBlocks(previous.getHash(), DIFFICULTY_ADJUST_BLOCK_COUNT);

            StoredBitcoinBlock cycleStart;
            if (tempBlocks.size() == DIFFICULTY_ADJUST_BLOCK_COUNT) {
                cycleStart = tempBlocks.get(tempBlocks.size() - 1);
            } else if (tempBlocks.size() > 0) {
                StoredBitcoinBlock last = tempBlocks.get(tempBlocks.size() - 1);
                cycleStart = store.getFromChain(last.getBlock().getPreviousBlock(), DIFFICULTY_ADJUST_BLOCK_COUNT - tempBlocks.size());
            } else {
                cycleStart = store.getFromChain(previous.getHash(), DIFFICULTY_ADJUST_BLOCK_COUNT);
            }

            if (cycleStart == null) {
                // Because there will just be some Bitcoin block from whence accounting begins, it's likely
                // that the first adjustment period will not have sufficient blocks to compute correctly
                return;
            }

            BigInteger newTarget = BitcoinUtils.calculateNewTarget(
                    BitcoinUtils.decodeCompactBits(previous.getBlock().getBits()),
                    cycleStart.getBlock().getTimestamp(),
                    previous.getBlock().getTimestamp());

            int accuracyBytes = (block.getBits() >>> 24) - 3;
            long receivedTargetCompact = block.getBits();

            // The calculated difficulty is to a higher precision than received, so reduce here.
            BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
            newTarget = newTarget.and(mask);
            long newTargetCompact = BitcoinUtils.encodeCompactBits(newTarget);

            if (newTargetCompact != receivedTargetCompact) {
                throw new VerificationException("Block does not match computed difficulty adjustment");
            }
        }
    }

    // Returns true if the store was empty and the bootstrap
    // blocks were added to it successfully.
    // Otherwise, returns false.
    public boolean bootstrap(List<BitcoinBlock> blocks, int firstBlockHeight) throws SQLException, VerificationException {
        assert(blocks.size() > 0);
        assert(firstBlockHeight >= 0);

        boolean bootstrapped = store.getChainHead() != null;

        if (!bootstrapped) {
            Sha256Hash prevHash = null;
            for (BitcoinBlock block : blocks) {
                if (prevHash != null && !block.getPreviousBlock().equals(prevHash) )
                    throw new VerificationException("Bitcoin bootstrap blocks must be contiguous");

                prevHash = block.getHash();
            }

            int blockHeight = firstBlockHeight;
            for (BitcoinBlock block : blocks) {
                BigInteger work = BitcoinUtils.decodeCompactBits(block.getBits());
                StoredBitcoinBlock storedBlock = new StoredBitcoinBlock(
                                                            block, work, blockHeight);
                blockHeight++;
                store.put(storedBlock);
                store.setChainHead(storedBlock);
            }
        }

        return !bootstrapped;
    }

    public boolean bootstrap(BitcoinBlockchainBootstrapConfig config) throws SQLException, VerificationException {
        return bootstrap(config.blocks, config.firstBlockHeight);
    }

}
