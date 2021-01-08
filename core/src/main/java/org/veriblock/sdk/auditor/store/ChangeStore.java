// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor.store;

import org.veriblock.sdk.auditor.BlockIdentifier;

import java.sql.SQLException;
import java.util.List;

public interface ChangeStore {

    /**
     * Shut down the store and release resources
     */
    void shutdown();

    /**
     * Delete all change records from the store
     * @throws SQLException
     */
    void clear() throws SQLException;

    /**
     * Retrieve the list of changes that correspond to the given block identifier
     * @param blockIdentifier the block identifier
     * @return the list of changes
     * @throws SQLException
     */
    List<StoredChange> get(BlockIdentifier blockIdentifier) throws SQLException;

    /**
     * Put the given change into the store
     * @param change the change to put into the store
     * @throws SQLException
     */
    void put(StoredChange change) throws SQLException;

    void clear(BlockIdentifier blockIdentifier) throws SQLException;
}
