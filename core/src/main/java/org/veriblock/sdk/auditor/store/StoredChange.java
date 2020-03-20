// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor.store;

import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.auditor.Change;
import org.veriblock.sdk.util.Preconditions;

import java.nio.ByteBuffer;
import java.util.Objects;

public class StoredChange {
    public static final int SIZE = BlockIdentifier.LENGTH + 4 + Change.MAX_SIZE;

    private final BlockIdentifier id;
    private final int sequenceNumber;
    private final Change change;

    public BlockIdentifier getId() {
        return id;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Change getChange() {
        return change;
    }

    public StoredChange(BlockIdentifier id, int sequenceNumber, Change change) {
        Preconditions.notNull(id, "Id cannot be null");
        Preconditions.notNull(change, "Change cannot be null");
        Preconditions.argument(sequenceNumber >= 0, "Sequence number must be positive");

        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.change = change;
    }

    public static StoredChange deserialize(ByteBuffer buffer) {
        byte[] blockIdentifierBytes = new byte[BlockIdentifier.LENGTH];
        buffer.get(blockIdentifierBytes);

        int sequenceNumber = buffer.getInt();

        Change change = Change.deserialize(buffer);

        return new StoredChange(BlockIdentifier.wrap(blockIdentifierBytes), sequenceNumber, change);
    }

    public void serialize(ByteBuffer buffer) {
        int startCursor = buffer.position();
        buffer.put(id.getBytes());
        buffer.putInt(sequenceNumber);
        buffer.put(change.serialize());

        //Fill all left bytes "0".
        int length = buffer.position() - startCursor;
        if(length < SIZE){
            byte[] empty = new byte[SIZE - length];
            buffer.get(empty);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredChange that = (StoredChange) o;
        return sequenceNumber == that.sequenceNumber &&
                Objects.equals(id, that.id) &&
                Objects.equals(change, that.change);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sequenceNumber, change);
    }
}
