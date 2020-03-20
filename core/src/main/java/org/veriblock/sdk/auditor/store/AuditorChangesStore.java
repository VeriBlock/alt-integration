// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.auditor.Change;
import org.veriblock.sdk.auditor.Operation;
import org.veriblock.sdk.auditor.ReadOnlyChange;
import org.veriblock.sdk.sqlite.tables.AuditorChangeData;
import org.veriblock.sdk.sqlite.tables.AuditorChangesRepository;
import org.veriblock.sdk.util.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditorChangesStore implements ChangeStore {
    //private static final int DEFAULT_NUM_HEADERS = 90000;
    private static final Logger log = LoggerFactory.getLogger(AuditorChangesStore.class);

    // underlying database
    private final Connection databaseConnection;
    private final AuditorChangesRepository changesRepository;

    public AuditorChangesStore(Connection databaseConnection) throws SQLException {
        this.databaseConnection = databaseConnection;
        changesRepository = new AuditorChangesRepository(databaseConnection);
    }

    public void shutdown() {
        try {
            if(databaseConnection != null) databaseConnection.close();
        } catch (SQLException e) {
            log.error("Error closing database connection", e);
        }
    }

    public void clear() throws SQLException {
        changesRepository.clear();
    }

    public void put(StoredChange storedChange) throws SQLException {
        AuditorChangeData data = new AuditorChangeData();
        
        data.blockId = Utils.encodeHex(storedChange.getId().getBytes());
        data.sequenceNum = storedChange.getSequenceNumber();
        
        Change change = storedChange.getChange();
        data.networkId = change.getChainIdentifier();
        data.operation = change.getOperation().getValue();
        data.oldValue = change.getOldValue();
        data.newValue = change.getNewValue();
        changesRepository.save(data);
    }

    public List<StoredChange> get(BlockIdentifier blockIdentifier) throws SQLException {
        List<AuditorChangeData> rows = changesRepository.getWithBlockId(Utils.encodeHex(blockIdentifier.getBytes()));
        List<StoredChange> changes = new ArrayList<>();
        
        for(AuditorChangeData row : rows) {
            Operation operation = Operation.valueOf(row.operation).get(); 
            Change change = new ReadOnlyChange(row.networkId, operation, row.oldValue, row.newValue);
            
            BlockIdentifier blockId = BlockIdentifier.wrap(Utils.decodeHex(row.blockId));
            int sequenceNum = row.sequenceNum;
            StoredChange storedChange = new StoredChange(blockId, sequenceNum, change);
            changes.add(storedChange);
        }
        
        return changes;
    }

    public void clear(BlockIdentifier blockIdentifier) throws SQLException {
        log.info("Clearing all changes stored for identifier " + Utils.encodeHex(blockIdentifier.getBytes()));
        List<AuditorChangeData> data = changesRepository.getWithBlockId(Utils.encodeHex(blockIdentifier.getBytes()));
        log.info("\tBefore clear: " + data.size() + " changes stored.");
        changesRepository.delete(Utils.encodeHex(blockIdentifier.getBytes()));
        data = changesRepository.getWithBlockId(Utils.encodeHex(blockIdentifier.getBytes()));
        log.info("\tAfter clear: " + data.size() + " changes stored.");
    }
}
