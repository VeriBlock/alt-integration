// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor;

import org.veriblock.sdk.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Changeset {
    private final BlockIdentifier blockIdentifier;
    private final List<Change> changes;

    public BlockIdentifier getBlockIdentifier() {
        return blockIdentifier;
    }

    public Changeset(BlockIdentifier blockIdentifier) {
        Preconditions.notNull(blockIdentifier, "Block identifier cannot be null");

        this.blockIdentifier = blockIdentifier;
        this.changes = new LinkedList<>();
    }

    public void addChanges(List<Change> changes) {
        if (changes == null || changes.size() == 0) return;

        this.changes.addAll(changes);
    }

    public List<Change> getChanges() {
        return new ArrayList<>(changes);
    }

    public Iterator<Change> reverseIterator() {
        ArrayList<Change> local = new ArrayList<>(changes);
        Collections.reverse(local);

        return local.iterator();
    }
}
