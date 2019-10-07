// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations;

import org.veriblock.integrations.auditor.AuditJournal;
import org.veriblock.integrations.auditor.BlockIdentifier;
import org.veriblock.integrations.auditor.Change;
import org.veriblock.integrations.auditor.Changeset;
import org.veriblock.integrations.blockchain.BitcoinBlockchain;
import org.veriblock.integrations.blockchain.VeriBlockBlockchain;
import org.veriblock.integrations.blockchain.VeriBlockPublicationUtilities;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.BitcoinBlock;
import org.veriblock.sdk.BlockIndex;
import org.veriblock.sdk.BlockStoreException;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockBlock;
import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.VerificationException;
import org.veriblock.sdk.services.ValidationService;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class VeriBlockSecurity {

    private final Context context;
    private final VeriBlockBlockchain veriblockBlockchain;
    private final BitcoinBlockchain bitcoinBlockchain;
    private final AuditJournal journal;
    private final BitcoinStore bitcoinStore;

    public VeriBlockSecurity(Context context) {
        veriblockBlockchain = new VeriBlockBlockchain(context.getNetworkParameters(), context.getVeriblockStore(), context.getBitcoinStore());
        bitcoinBlockchain = new BitcoinBlockchain(context.getBitcoinStore());
        journal = new AuditJournal(context.getChangeStore());
        bitcoinStore = context.getBitcoinStore();
        this.context = context;
    }
    
    public VeriBlockSecurity() throws BlockStoreException, SQLException {
        this(new Context());
    }
    
    public void shutdown() {
        context.getBitcoinStore().shutdown();
        context.getVeriblockStore().shutdown();
        context.getChangeStore().shutdown();
    }
    
    public VeriBlockBlockchain getVeriBlockBlockchain() {
        return veriblockBlockchain;
    }
    
    public BitcoinBlockchain getBitcoinBlockchain() {
        return bitcoinBlockchain;
    }
    
    public Context getSecurityFiles() {
        return context;
    }

    public ValidationResult checkATVInternally(AltPublication publication) {
        try {
            ValidationService.verify(publication);

            return ValidationResult.success();
        } catch (VerificationException e) {
            return ValidationResult.fail(e.getMessage());
        }
    }

    public ValidationResult checkVTBInternally(VeriBlockPublication publication) {
        try {
            ValidationService.verify(publication);

            return ValidationResult.success();
        } catch (VerificationException e) {
            return ValidationResult.fail(e.getMessage());
        }
    }

    // TODO: Exception when blockIndex.height is less than or equal to highest known
    // TODO: Exception when publications are not valid
    public boolean addPayloads(BlockIndex blockIndex, List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws BlockStoreException, SQLException {
        Changeset changeset = new Changeset(BlockIdentifier.wrap(Utils.decodeHex(blockIndex.getHash())));

        try {
            if (veriblockPublications != null && veriblockPublications.size() > 0) {
                for (VeriBlockPublication publication : veriblockPublications) {
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    changeset.addChanges(bitcoinBlockchain.addAll(publication.getTransaction().getBlocks()));

                    List<VeriBlockBlock> veriBlockBlocks = publication.getBlocks();
                    if (veriBlockBlocks.contains(publication.getTransaction().getPublishedBlock())) {
                        // The published block is part of this publication's supplied context, add the blocks individually
                        for (VeriBlockBlock block : veriBlockBlocks) {
                            if (block.equals(publication.getTransaction().getPublishedBlock())) {
                                changeset.addChanges(veriblockBlockchain.addWithProof(block, publication.getTransaction().getBlockOfProof().getHash()));
                            } else {
                                changeset.addChanges(veriblockBlockchain.add(block));
                            }
                        }
                    } else {
                        // The published block is pre-existing, therefore set its block of proof and add these new blocks
                        changeset.addChanges(veriblockBlockchain.setBlockOfProof(
                                publication.getTransaction().getPublishedBlock(),
                                publication.getTransaction().getBlockOfProof().getHash()));
                        changeset.addChanges(veriblockBlockchain.addAll(publication.getBlocks()));
                    }
                }
            }

            if (altPublications != null && altPublications.size() > 0) {
                for (AltPublication publication : altPublications) {
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    changeset.addChanges(veriblockBlockchain.addAll(publication.getBlocks()));
                }
            }

            journal.record(changeset);

            return true;
        } catch (VerificationException e) {
            Iterator<Change> changeIterator = changeset.reverseIterator();
            while (changeIterator.hasNext()) {
                Change change = changeIterator.next();
                bitcoinBlockchain.rewind(Collections.singletonList(change));
                veriblockBlockchain.rewind(Collections.singletonList(change));
            }
            return false;
        }
    }

    public void removePayloads(BlockIndex blockIndex) throws SQLException {
        BlockIdentifier blockIdentifier = BlockIdentifier.wrap(Utils.decodeHex(blockIndex.getHash()));

        List<Change> changes = journal.get(blockIdentifier);
        veriblockBlockchain.rewind(changes);
        bitcoinBlockchain.rewind(changes);
    }

    public boolean addTemporaryPayloads(List<VeriBlockPublication> veriblockPublications, List<AltPublication> altPublications) throws BlockStoreException, SQLException {
        try {
            if (veriblockPublications != null && veriblockPublications.size() > 0) {
                for (VeriBlockPublication publication : veriblockPublications) {
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    // Temporarily add Bitcoin blocks
                    bitcoinBlockchain.addAllTemporarily(publication.getTransaction().getBlocks());

                    List<VeriBlockBlock> veriBlockBlocks = publication.getBlocks();
                    if (veriBlockBlocks.contains(publication.getTransaction().getPublishedBlock())) {
                        // The published block is part of this publication's supplied context, add the blocks individually
                        for (VeriBlockBlock block : veriBlockBlocks) {
                            if (block.equals(publication.getTransaction().getPublishedBlock())) {
                                veriblockBlockchain.addTemporarily(block, publication.getTransaction().getBlockOfProof().getHash());
                            } else {
                                veriblockBlockchain.addTemporarily(block);
                            }
                        }
                    } else {
                        // The published block is pre-existing, therefore set its block of proof and add these new blocks
                        veriblockBlockchain.setBlockOfProofTemporarily(
                                publication.getTransaction().getPublishedBlock(),
                                publication.getTransaction().getBlockOfProof().getHash());
                        veriblockBlockchain.addAllTemporarily(publication.getBlocks());
                    }
                }
            }

            if (altPublications != null && altPublications.size() > 0) {
                for (AltPublication publication : altPublications) {
                    ValidationService.verify(publication);
                    verifyPublicationContextually(publication);

                    veriblockBlockchain.addAllTemporarily(publication.getBlocks());
                }
            }

            return true;
        } catch (VerificationException e) {
            clearTemporaryPayloads();
            return false;
        }
    }

    public void clearTemporaryPayloads() {
        veriblockBlockchain.clearTemporaryModifications();
        bitcoinBlockchain.clearTemporaryModifications();
    }

    public List<VeriBlockPublication> simplifyVTBs(List<VeriBlockPublication> publications) throws BlockStoreException, SQLException {
        return VeriBlockPublicationUtilities.simplifyVeriBlockPublications(publications, bitcoinStore);
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

    private void verifyPublicationContextually(VeriBlockPublication publication) throws VerificationException, BlockStoreException, SQLException {
        checkVeriBlockContextually(publication.getFirstBlock());
        checkBitcoinContextually(publication.getFirstBitcoinBlock());
    }

    private void verifyPublicationContextually(AltPublication publication) throws VerificationException, BlockStoreException, SQLException {
        checkVeriBlockContextually(publication.getFirstBlock());
    }

    private void checkVeriBlockContextually(VeriBlockBlock firstBlock) throws BlockStoreException, SQLException {
        if (firstBlock == null) {
            throw new VerificationException("Publication does not have any VeriBlock blocks");
        }

        VeriBlockBlock previous = veriblockBlockchain.searchBestChain(firstBlock.getPreviousBlock());
        if (previous == null) {
            throw new VerificationException("Publication does not connect to VeriBlock blockchain");
        }
    }

    private void checkBitcoinContextually(BitcoinBlock firstBlock) throws BlockStoreException, SQLException {
        if (firstBlock == null) {
            throw new VerificationException("Publication does not have any Bitcoin blocks");
        }

        BitcoinBlock previous = bitcoinBlockchain.searchBestChain(firstBlock.getPreviousBlock());
        if (previous == null) {
            throw new VerificationException("Publication does not connect to Bitcoin blockchain");
        }
    }
}
