// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PopRewardEndorsements {
    // sort the endorsements by VeriBlock block height
    private SortedMap<Integer, List<PopEndorsement>> blocksWithEndorsements;
    
    public PopRewardEndorsements() {
        blocksWithEndorsements = new TreeMap<>();
    }
    
    public SortedMap<Integer, List<PopEndorsement>> getBlocksWithEndorsements() {
        return blocksWithEndorsements;
    }

    public void addEndorsement(int veriBlockHeight, PopEndorsement endorsement) {
        List<PopEndorsement> atBlockEndorsements = blocksWithEndorsements.get(veriBlockHeight);
        if(atBlockEndorsements == null) {
            atBlockEndorsements = new ArrayList<PopEndorsement>();
        }
        atBlockEndorsements.add(endorsement);
        blocksWithEndorsements.put(veriBlockHeight, atBlockEndorsements);
    }
    
    ///HACK: not sure if we ever need this API but it helps us testing things
    public void addEmptyEndorsement(int veriBlockHeight) {
        List<PopEndorsement> atBlockEndorsements = blocksWithEndorsements.get(veriBlockHeight);
        if(atBlockEndorsements != null) return;
        
        atBlockEndorsements = new ArrayList<PopEndorsement>();
        blocksWithEndorsements.put(veriBlockHeight, atBlockEndorsements);
    }
    
    public int getLowestVeriBlockHeight() {
        if(blocksWithEndorsements.keySet().size() == 0) return 0;
        return blocksWithEndorsements.firstKey();
    }
}
