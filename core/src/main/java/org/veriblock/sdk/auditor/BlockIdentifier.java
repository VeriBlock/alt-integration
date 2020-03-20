// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.auditor;

import org.veriblock.sdk.util.Preconditions;
import org.veriblock.sdk.util.Utils;

import java.util.Arrays;

public class BlockIdentifier {
    public static final int LENGTH = 64;

    private final byte[] value;

    private BlockIdentifier(byte[] value) {
        Preconditions.notNull(value, "Block identifier cannot be null");
        Preconditions.argument(value.length == LENGTH, () -> "Invalid block identifier: " + Utils.encodeHex(value));

        this.value = value;
    }

    public static BlockIdentifier wrap(byte[] value) {
        Preconditions.notNull(value, "Block identifier cannot be null");
        Preconditions.argument(value.length <= LENGTH, () -> "Invalid block identifier: " + Utils.encodeHex(value));

        if (value.length == LENGTH) return new BlockIdentifier(value);

        byte[] padded = new byte[LENGTH];
        int position = LENGTH - value.length;
        System.arraycopy(value, 0, padded, position, value.length);

        return new BlockIdentifier(padded);
    }

    public byte[] getBytes() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return Arrays.equals(value, ((BlockIdentifier)obj).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
