// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.auditor.BlockIdentifier;
import org.veriblock.sdk.models.BlockStoreException;
import org.veriblock.sdk.util.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

public class ReadOnlyChangeStore {
    private static final Logger log = LoggerFactory.getLogger(ReadOnlyChangeStore.class);

    private final int fileSize;
    private final int prologueSize;
    private final int recordSize;

    private boolean exists;
    private RandomAccessFile randomAccessFile;
    private MappedByteBuffer buffer;

    public ReadOnlyChangeStore(File file, int fileSize, int prologueSize, int recordSize) throws BlockStoreException {
        Preconditions.notNull(file, "File cannot be null");

        this.fileSize = fileSize;
        this.prologueSize = prologueSize;
        this.recordSize = recordSize;
        try {
            this.exists = file.exists();
            if (!exists) {
                log.info("Store {} does not exist", file.getName());
                return;
            }

            // Set up the backing file.
            this.randomAccessFile = new RandomAccessFile(file, "r");
            FileChannel channel = randomAccessFile.getChannel();

            // Map it into memory read/write. The kernel will take care of flushing writes to disk at the most
            // efficient times, which may mean that until the map is deallocated the data on disk is randomly
            // inconsistent. However the only process accessing it is us, via this mapping, so our own view will
            // always be correct. Once we establish the mmap the underlying file and storeFileChannel can go away. Note that
            // the details of mmapping vary between platforms.
            this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (Exception e) {
            try {
                if (randomAccessFile != null) randomAccessFile.close();
            } catch (IOException e2) {
                throw new BlockStoreException(e2);
            }
            throw new BlockStoreException(e);
        }
    }

    public boolean get(BlockIdentifier blockIdentifier, List<StoredChange> changes) throws BlockStoreException {
        if (!exists) return false;

        final MappedByteBuffer buffer = this.buffer;
        if (buffer == null) throw new BlockStoreException("Store closed");

        int cursor = fileSize;
        final byte[] targetHashBytes = blockIdentifier.getBytes();
        byte[] scratch = new byte[32];
        do {
            cursor -= recordSize;

            // Cursor is now at the start of the next record to check, so read the blockIdentifier and compare it.
            buffer.position(cursor);
            buffer.get(scratch);
            if (Arrays.equals(scratch, targetHashBytes)) {
                // Found the target.
                buffer.position(cursor);
                StoredChange change = StoredChange.deserialize(buffer);
                changes.add(change);

                // Found the requested number.
                if (change.getSequenceNumber() == 0) {
                    return true;
                }
            }
        } while (cursor >= prologueSize);

        return false;
    }

    public void close() throws BlockStoreException {
        try {
            buffer = null;  // Allow it to be GCd and the underlying file mapping to go away.
            randomAccessFile.close();
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }
}
