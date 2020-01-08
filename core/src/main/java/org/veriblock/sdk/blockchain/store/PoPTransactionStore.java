// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.store;

import org.veriblock.sdk.models.AltChainBlock;
import org.veriblock.sdk.models.AltPublication;
import org.veriblock.sdk.models.VeriBlockPublication;
import org.veriblock.sdk.sqlite.tables.PoPTransactionData;

import java.sql.SQLException;
import java.util.List;

public interface PoPTransactionStore {

    /**
     * Delete all change records from the store
     * @throws SQLException
     */
    void clear() throws SQLException;

    /**
     * Shut down the store and release resources
     */
    void shutdown();

    /**
     * Put the given PoP transaction into the store
     * @param popTx the PoP transaction
     * @param containingBlock the altchain block that contains the PoP transaction
     * @param endorsedBlock the altchain block the PoP transaction endorses
     * @throws SQLException
     */
    void addPoPTransaction(PoPTransactionData popTx, AltChainBlock containingBlock, AltChainBlock endorsedBlock) throws SQLException;

    /**
     * Retrieve AltPublications(ATVs) from the given containing blocks that endorse the given endorsed block
     * @param endorsedBlock the endorsed altchain block
     * @param containingBlocks the blocks to search for ATVs
     * @return the list of AltPublications
     * @throws SQLException
     */
    List<AltPublication> getAltPublicationsEndorse(AltChainBlock endorsedBlock, List<AltChainBlock> containingBlocks) throws SQLException;

    /**
     * Retrieve AltPublications(ATVs) from the given containing block
     * @param block the block to retrieve ATVs from
     * @return the list of AltPublications
     * @throws SQLException
     */
    List<AltPublication> getAltPublicationsFromBlock(AltChainBlock block) throws SQLException;

    /**
     * Retrieve VeriBlockPublications(VTBs) from the given containing block
     * @param block the block to retrieve VTBs from
     * @return the list of VeriBlockPublications
     * @throws SQLException
     */
    List<VeriBlockPublication> getVeriBlockPublicationsFromBlock(AltChainBlock block) throws SQLException;

    /**
     * Retrieve AltPublications(ATVs) from the given block height
     * @param height the block height to retrieve ATVs from
     * @return the list of AltPublications
     * @throws SQLException
     */
    List<AltPublication> getAltPublicationsFromBlockHeight(long height) throws SQLException;

    /**
     * Retrieve common keyStone, or null.
     * @param keyStones keyStone list
     * @return first common keyStone.
     * @throws SQLException
     */
    AltChainBlock findFirstCommonKeystone(List<AltChainBlock> keyStones);
}
