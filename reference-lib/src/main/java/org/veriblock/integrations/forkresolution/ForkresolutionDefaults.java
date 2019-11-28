// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.forkresolution;

public class ForkresolutionDefaults {

    private ForkresolutionDefaults() {}

    // the maximum number of VeriBlock blocks that can occur without
    // a publication of an additional AltChain keystone before 'continuity' is lost
    static final int KEYSTONE_FINALITY_DELAY = 60;

    static final int AMNESTY_PERIOD = 20;

    // calculated with KEYSTONE_FINALITY_DELAY = 60 and AMNESTY_PERIOD = 20
    static final long[] PUBLICATION_LATENCY_LOOK_UP_TABLE = {
            100000000, // 0
            100000000, // 1
            100000000, // 2
            100000000, // 3
            100000000, // 4
            100000000, // 5
            100000000, // 6
            100000000, // 7
            100000000, // 8
            100000000, // 9
            100000000, // 10
            100000000, // 11
            100000000, // 12
            100000000, // 13
            100000000, // 14
            100000000, // 15
            100000000, // 16
            100000000, // 17
            100000000, // 18
            100000000, // 19
            100000000, // 20
            100000000, // 21
            48971015, // 22
            32252628, // 23
            23981603, // 24
            19057279, // 25
            15794439, // 26
            13475628, // 27
            11744034, // 28
            10402320, // 29
            9332543, // 30
            8459905, // 31
            7734697, // 32
            7122596, // 33
            6599151, // 34
            6146473, // 35
            5751172, // 36
            5403032, // 37
            5094121, // 38
            4818188, // 39
            4570241, // 40
            4346244, // 41
            4142901, // 42
            3957494, // 43
            3787759, // 44
            3631798, // 45
            3488007, // 46
            3355021, // 47
            3231671, // 48
            3116951, // 49
            3009990, // 50
            2910029, // 51
            2816407, // 52
            2728542, // 53
            2645920, // 54
            2568088, // 55
            2494643, // 56
            2425226, // 57
            2359515, // 58
            2297224 // 59
        };
}
