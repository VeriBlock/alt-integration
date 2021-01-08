// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.auditor.store.ChangeStore;
import org.veriblock.sdk.auditor.store.StoredChange;
import org.veriblock.sdk.util.Preconditions;
import org.veriblock.sdk.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuditJournal {
    private static final Logger log = LoggerFactory.getLogger(AuditJournal.class);

    private final ChangeStore store;

    public AuditJournal(ChangeStore store) {
        Preconditions.notNull(store, "Store cannot be null");

        this.store = store;
    }

    public void record(Changeset changeset) throws SQLException {
        log.info("Recording changeset containing " + changeset.getChanges().size() + " identified by block " + Utils.bytesToHex(changeset.getBlockIdentifier().getBytes()));
        BlockIdentifier identifier = changeset.getBlockIdentifier();
        List<Change> changes = changeset.getChanges();

        log.debug("\tChanges: " + changes.size());
        for (int i = 0; i < changes.size(); i++) {
            log.debug("\t\tChange " + i + ":");
            log.debug("\t\t\tChange Identifier: " + changes.get(i).getChainIdentifier());
            log.debug("\t\t\tChange Operation: " + changes.get(i).getOperation().name());
            log.debug("\t\t\tChange Old Value: " + Utils.bytesToHex(changes.get(i).getOldValue()));
            log.debug("\t\t\tChange New Value: " + Utils.bytesToHex(changes.get(i).getNewValue()));
        }

        for (int i = 0; i < changes.size(); i++) {
            StoredChange storedChange = new StoredChange(identifier, i, changes.get(i));
            store.put(storedChange);
        }
    }

    public Changeset get(BlockIdentifier blockIdentifier) throws SQLException {
        List<StoredChange> storedChanges = store.get(blockIdentifier);

        List<Change> changes = new ArrayList<Change>(Collections.nCopies(storedChanges.size(), (Change) null));
        for (StoredChange change : storedChanges) {
            changes.set(change.getSequenceNumber(), change.getChange());
        }

        Changeset changeset = new Changeset(blockIdentifier);
        changeset.addChanges(changes);
        return changeset;
    }

    public void clear(BlockIdentifier blockIdentifier) throws SQLException {
        store.clear(blockIdentifier);
    }
}
