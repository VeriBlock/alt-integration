// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.forkresolution;

public class ForkresolutionConfig {

    public int keystoneFinalityDelay;

    public int amnestyPeriod;

    public long[] publicationLatencyLookUpTable;

    public ForkresolutionConfig(int keystoneFinalityDelay, int amnestyPeriod)
    {
        this.keystoneFinalityDelay = keystoneFinalityDelay;
        this.amnestyPeriod = amnestyPeriod;
        publicationLatencyLookUpTable = new long[keystoneFinalityDelay];

        for(int i = 0; i < keystoneFinalityDelay; i++)
        {
            if( i <= amnestyPeriod)
                publicationLatencyLookUpTable[i] = 100000000;
            if(i > amnestyPeriod)
                publicationLatencyLookUpTable[i] = (long) (1.0 / Math.pow(i - amnestyPeriod, 1.03) * 100000000);
        }

    }

    public ForkresolutionConfig()
    {
        this.keystoneFinalityDelay = ForkresolutionDefaults.KEYSTONE_FINALITY_DELAY;
        this.amnestyPeriod = ForkresolutionDefaults.AMNESTY_PERIOD;
        this.publicationLatencyLookUpTable = ForkresolutionDefaults.PUBLICATION_LATENCY_LOOK_UP_TABLE;
    }
}
