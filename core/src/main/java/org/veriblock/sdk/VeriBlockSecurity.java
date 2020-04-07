// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veriblock.sdk.auditor.AuditJournal;
import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.auditor.Change;
import org.veriblock.sdk.auditor.Changeset;
import org.veriblock.sdk.blockchain.BitcoinBlockchain;
import org.veriblock.sdk.blockchain.VeriBlockBlockchain;
import org.veriblock.sdk.blockchain.VeriBlockPublicationUtilities;
import org.veriblock.sdk.models.*;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.LogFormatter;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VeriBlockSecurity {
    private static final Logger log = LoggerFactory.getLogger(VeriBlockSecurity.class);

    private final VeriBlockBlockchain veriblockBlockchain;
    private final BitcoinBlockchain bitcoinBlockchain;
    private final AuditJournal journal;
    private final Context context;
    private AltChainParametersConfig altChainParametersConfig;

    public VeriBlockSecurity(Context context) {
        this.context = context;
        veriblockBlockchain = new VeriBlockBlockchain(context.getVeriBlockNetworkParameters(), context.getVeriblockStore(), context.getBitcoinStore());
        bitcoinBlockchain = new BitcoinBlockchain(context.getBitcoinNetworkParameters(), context.getBitcoinStore());
        journal = new AuditJournal(context.getChangeStore());
        altChainParametersConfig = new AltChainParametersConfig();
    }
    
    public void shutdown() {
        context.getBitcoinStore().shutdown();
        context.getVeriblockStore().shutdown();
        context.getChangeStore().shutdown();
        context.getPopTxStore().shutdown();
    }

    public Context getContext() {
        return context;
    }

    public VeriBlockBlockchain getVeriBlockBlockchain() {
        return veriblockBlockchain;
    }
    
    public BitcoinBlockchain getBitcoinBlockchain() {
        return bitcoinBlockchain;
    }
    
    public void setAltChainParametersConfig(AltChainParametersConfig config) { this.altChainParametersConfig = config; }

    public AltChainParametersConfig getAltChainParametersConfig() { return this.altChainParametersConfig; }

    public ValidationResult checkATVInternally(AltPublication publication) {
        try {
            log.debug("CheckATVInternally called for an {}", LogFormatter.toString(publication));
            ValidationService.verify(publication);

            return ValidationResult.success();
        } catch (VerificationException e) {
            log.debug("CheckATVInternally failed: {}", e.getMessage());
            return ValidationResult.fail(e.getMessage());
        }
    }

    public ValidationResult checkVTBInternally(VeriBlockPublication publication) {
        try {
            log.debug("checkVTBInternally called for a {}", LogFormatter.toString(publication));
            ValidationService.verify(publication);

            return ValidationResult.success();
        } catch (VerificationException e) {
            log.debug("CheckVTBInternally failed: {}", e.getMessage());
            return ValidationResult.fail(e.getMessage());
        }
    }

    // TODO: Exception when blockIndex.height is less than or equal to highest known
    public void addPayloads(BlockIndex blockIndex, List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws VerificationException, BlockStoreException, SQLException {
        log.info("AddPayloads {} VTB(s) and {} ATV(s) for block {}:{}",
                 String.valueOf(veriblockPublications == null ? 0 : veriblockPublications.size()),
                 String.valueOf(altPublications == null ? 0 : altPublications.size()),
                 blockIndex.getHash(), String.valueOf(blockIndex.getHeight()));

        if (veriblockPublications != null) {
            log.info("All VTB BTC Contexts:");
            for (int i = 0; i < veriblockPublications.size(); i++) {
                log.info("\tVTB #" + i);
                VeriBlockPublication selected = veriblockPublications.get(i);

                if (selected != null) {
                    VeriBlockPoPTransaction popTx = selected.getTransaction();
                    List<BitcoinBlock> contextBlocks = popTx.getBlockOfProofContext();
                    BitcoinBlock blockOfProof = popTx.getBlockOfProof();

                    if (contextBlocks != null) {
                        for (int j = 0; j < contextBlocks.size(); j++) {
                            log.info("\t\t" + contextBlocks.get(j).getHash() + " (" + Utils.bytesToHex(contextBlocks.get(j).getRaw()));
                        }
                    }
                    if (blockOfProof != null) {
                        log.info("\t\t" + blockOfProof.getHash() + " (" + Utils.bytesToHex(blockOfProof.getRaw()));
                    }
                } else {
                    log.info("<null PoP transaction>");
                }
            }
        }

        Changeset changeset = new Changeset(BlockIdentifier.wrap(Utils.decodeHex(blockIndex.getHash())));

        try {
            if (veriblockPublications != null && veriblockPublications.size() > 0) {
                for (VeriBlockPublication publication : veriblockPublications) {
                    log.debug("Processing a {}", LogFormatter.toString(publication));
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    changeset.addChanges(bitcoinBlockchain.addAll(publication.getTransaction().getBlocks()));

                    List<VeriBlockBlock> veriBlockBlocks = publication.getBlocks();
                    if (veriBlockBlocks.contains(publication.getTransaction().getPublishedBlock())) {
                        // The published block is part of this publication's supplied context, add the blocks individually
                        log.debug("The published block is part of this publication's supplied context");
                        for (VeriBlockBlock block : veriBlockBlocks) {
                            if (block.equals(publication.getTransaction().getPublishedBlock())) {
                                changeset.addChanges(veriblockBlockchain.addWithProof(block, publication.getTransaction().getBlockOfProof().getHash()));
                            } else {
                                changeset.addChanges(veriblockBlockchain.add(block));
                            }
                        }
                    } else {
                        // The published block is pre-existing, therefore set its block of proof and add these new blocks
                        log.debug("The published block is pre-existing");
                        changeset.addChanges(veriblockBlockchain.setBlockOfProof(
                                publication.getTransaction().getPublishedBlock(),
                                publication.getTransaction().getBlockOfProof().getHash()));
                        changeset.addChanges(veriblockBlockchain.addAll(publication.getBlocks()));
                    }
                }
            }

            if (altPublications != null && altPublications.size() > 0) {
                for (AltPublication publication : altPublications) {
                    log.debug("Processing an {}", LogFormatter.toString(publication));
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    changeset.addChanges(veriblockBlockchain.addAll(publication.getBlocks()));
                }
            }

            log.info("adding a changeset of {} items to the journal", changeset.getChanges().size());
            journal.record(changeset);

        } catch (VerificationException e) {
            log.info("AddPayloads failed: {} ", e.getMessage());
            rewind(changeset);
            throw e;
        }
    }

    private void rewind(Changeset changeset) throws SQLException {
        Iterator<Change> changeIterator = changeset.reverseIterator();
        while (changeIterator.hasNext()) {
            Change change = changeIterator.next();
            bitcoinBlockchain.rewind(Collections.singletonList(change));
            veriblockBlockchain.rewind(Collections.singletonList(change));
        }
    }

    public void removePayloads(BlockIndex blockIndex) throws SQLException {
        log.info("RemovePayloads for block {}:{}",
                 blockIndex.getHash(), String.valueOf(blockIndex.getHeight()));

        BlockIdentifier blockIdentifier = BlockIdentifier.wrap(Utils.decodeHex(blockIndex.getHash()));

        Changeset changeset = journal.get(blockIdentifier);

        // Clear change/audit history for block for which payloads were removed
        log.info("Clearing for block identifier " + Utils.encodeHex(blockIdentifier.getBytes()));
        journal.clear(blockIdentifier);

        log.info("Rewinding a changeset of {} items", changeset.getChanges().size());
        rewind(changeset);
    }

    public void addTemporaryPayloads(List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws VerificationException, BlockStoreException, SQLException {
        log.info("AddTemporaryPayloads {} VTB(s) and {} ATV(s)",
                 String.valueOf(veriblockPublications == null ? 0 : veriblockPublications.size()),
                 String.valueOf(altPublications == null ? 0 : altPublications.size()));

        try {
            if (veriblockPublications != null && veriblockPublications.size() > 0) {
                for (VeriBlockPublication publication : veriblockPublications) {
                    log.debug("Processing a {}", LogFormatter.toString(publication));
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    // Temporarily add Bitcoin blocks
                    bitcoinBlockchain.addAllTemporarily(publication.getTransaction().getBlocks());

                    List<VeriBlockBlock> veriBlockBlocks = publication.getBlocks();
                    if (veriBlockBlocks.contains(publication.getTransaction().getPublishedBlock())) {
                        // The published block is part of this publication's supplied context, add the blocks individually
                        log.debug("The published block is part of this publication's supplied context");
                        for (VeriBlockBlock block : veriBlockBlocks) {
                            if (block.equals(publication.getTransaction().getPublishedBlock())) {
                                veriblockBlockchain.addTemporarily(block, publication.getTransaction().getBlockOfProof().getHash());
                            } else {
                                veriblockBlockchain.addTemporarily(block);
                            }
                        }
                    } else {
                        // The published block is pre-existing, therefore set its block of proof and add these new blocks
                        log.debug("The published block is pre-existing");
                        veriblockBlockchain.setBlockOfProofTemporarily(
                                publication.getTransaction().getPublishedBlock(),
                                publication.getTransaction().getBlockOfProof().getHash());
                        veriblockBlockchain.addAllTemporarily(publication.getBlocks());
                    }
                }
            }

            if (altPublications != null && altPublications.size() > 0) {
                for (AltPublication publication : altPublications) {
                    log.debug("Processing an {}", LogFormatter.toString(publication));
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    veriblockBlockchain.addAllTemporarily(publication.getBlocks());
                }
            }

        } catch (VerificationException e) {
            log.debug("AddTemporaryPayloads failed: {}", e.getMessage());
            clearTemporaryPayloads();
            throw e;
        }
    }

    public void clearTemporaryPayloads() {
        log.debug("Removing all temporary payloads");
        veriblockBlockchain.clearTemporaryModifications();
        bitcoinBlockchain.clearTemporaryModifications();
    }

    public List<VeriBlockPublication> simplifyVTBs(List<VeriBlockPublication> publications) throws BlockStoreException, SQLException {
        return VeriBlockPublicationUtilities.simplifyVeriBlockPublications(
                    publications, context.getBitcoinStore());
    }

    public ValidationResult checkATVAgainstView(AltPublication publication) throws BlockStoreException, SQLException {
        try {
            ValidationService.verify(publication);
            verifyPublicationContextually(publication);

            return ValidationResult.success();
        } catch (VerificationException e) {
            return ValidationResult.fail(e.getMessage());
        }
    }

    public int getMainVBKHeightOfATV(AltPublication publication) throws BlockStoreException, SQLException {
        VeriBlockBlock block = veriblockBlockchain.searchBestChain(publication.getContainingBlock().getHash());
        return block != null ? block.getHeight() : Integer.MAX_VALUE;
    }

    public List<VBlakeHash> getLastKnownVBKBlocks(int maxBlockCount) throws SQLException {
        List<VBlakeHash> result = new ArrayList<>(maxBlockCount);

        VeriBlockBlock block = veriblockBlockchain.getChainHead();
        for (int count = 0; block != null && count < maxBlockCount; ++count) {
            result.add(block.getHash());

            VBlakeHash prevBlockHash = block.getPreviousBlock();
            block = prevBlockHash == null ? null
                                          : veriblockBlockchain.get(prevBlockHash);
        }

        return result;
    }

    public List<Sha256Hash> getLastKnownBTCBlocks(int maxBlockCount) throws SQLException {
        List<Sha256Hash> result = new ArrayList<>(maxBlockCount);

        BitcoinBlock block = bitcoinBlockchain.getChainHead();
        for (int count = 0; block != null && count < maxBlockCount; ++count) {
            result.add(block.getHash());

            Sha256Hash prevBlockHash = block.getPreviousBlock();
            block = prevBlockHash == null ? null
                                          : bitcoinBlockchain.get(prevBlockHash);
        }

        return result;
    }

    private void verifyPublicationContextually(VeriBlockPublication publication) throws VerificationException, BlockStoreException, SQLException {
        checkConnectivity(publication.getFirstBlock());
        checkConnectivity(publication.getFirstBitcoinBlock());
    }

    private void verifyPublicationContextually(AltPublication publication) throws VerificationException, BlockStoreException, SQLException {
        checkConnectivity(publication.getFirstBlock());
    }

    public void checkConnectivity(VeriBlockBlock block) throws BlockStoreException, SQLException {
        if (block == null) {
            throw new VerificationException("Publication does not have any VeriBlock blocks");
        }

        VeriBlockBlock previous = veriblockBlockchain.searchBestChain(block.getPreviousBlock());
        if (previous != null) {
            return;
        }

        // corner case: the first bootstrap block has no previous block
        // but does connect to the blockchain by definition
        if (veriblockBlockchain.searchBestChain(block.getHash()) == null) {
            log.debug("VeriBlock block {} does not connect to the best chain",
                      LogFormatter.toStringExtended(block));
            log.debug("VeriBlock best chain tip: {}",
                      LogFormatter.toStringExtended(veriblockBlockchain.getChainHead()));
            throw new VerificationException("Publication does not connect to VeriBlock blockchain");
        }
    }

    public void checkConnectivity(BitcoinBlock block) throws BlockStoreException, SQLException {
        if (block == null) {
            throw new VerificationException("Publication does not have any Bitcoin blocks");
        }

        BitcoinBlock previous = bitcoinBlockchain.searchBestChain(block.getPreviousBlock());
        if (previous != null) {
            return;
        }

        // corner case: the first bootstrap block has no previous block
        // but does connect to the blockchain by definition
        if (bitcoinBlockchain.searchBestChain(block.getHash()) == null) {
            log.debug("Bitcoin block {} does not connect to the best chain",
                      LogFormatter.toStringExtended(block));
            log.debug("Bitcoin best chain tip: {}",
                      LogFormatter.toStringExtended(bitcoinBlockchain.getStoredChainHead()));
            throw new VerificationException("Publication does not connect to Bitcoin blockchain");
        }
    }

    public void updateContext(List<BitcoinBlock> bitcoinBlocks, List<VeriBlockBlock> veriBlockBlocks) throws SQLException, VerificationException {
        log.info("UpdateContext with {} Bitcoin and {} VeriBlock blocks",
                 String.valueOf(bitcoinBlocks.size()),
                 String.valueOf(veriBlockBlocks.size()));

        log.debug("Bitcoin context blocks: {}", LogFormatter.bitcoinContextToString(bitcoinBlocks));
        log.debug("VeriBlock context blocks: {}", LogFormatter.veriblockContextToString(veriBlockBlocks));

        List<Change> changes = new ArrayList<Change>();
        try {
            changes.addAll(bitcoinBlockchain.addAll(bitcoinBlocks));
            changes.addAll(veriblockBlockchain.addAll(veriBlockBlocks));
        }
        catch (VerificationException e) {
            log.info("UpdateContext failed: {} ", e.getMessage());

            Collections.reverse(changes);
            Iterator<Change> changeIterator = changes.iterator();
            while (changeIterator.hasNext()) {
                Change change = changeIterator.next();
                bitcoinBlockchain.rewind(Collections.singletonList(change));
                veriblockBlockchain.rewind(Collections.singletonList(change));
            }
            throw e;
        }
    }
}
