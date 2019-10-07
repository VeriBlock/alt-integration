// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.auditor;

import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.auditor.store.StoredChange;
import org.veriblock.sdk.util.Preconditions;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AuditJournal {

    private final AuditorChangesStore store;

    public AuditJournal(AuditorChangesStore store) {
        Preconditions.notNull(store, "Store cannot be null");

        this.store = store;
    }

    public void record(Changeset changeset) throws SQLException {
        BlockIdentifier identifier = changeset.getBlockIdentifier();
        List<Change> changes = changeset.getChanges();

        for (int i = 0; i < changes.size(); i++) {
            StoredChange storedChange = new StoredChange(identifier, i, changes.get(i));
            store.put(storedChange);
        }
    }

    public List<Change> get(BlockIdentifier blockIdentifier) throws SQLException {
        List<StoredChange> storedChanges = store.get(blockIdentifier);

        return storedChanges.stream().map(StoredChange::getChange).collect(Collectors.toList());
    }
}
