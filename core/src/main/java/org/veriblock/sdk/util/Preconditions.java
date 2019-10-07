// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.util;

public class Preconditions {
    public static <T> void notNull(T object, String errorMessage) {
        if (object == null) throw new NullPointerException();
    }

    public static void state(boolean state, String errorMessage) {
        if (!state) throw new IllegalStateException(errorMessage);
    }

    public static <T> void argument(boolean state, String errorMessage) {
        if (!state) throw new IllegalArgumentException(errorMessage);
    }
}
