// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.


package org.veriblock.integrations.forkresolution;

import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.AltChainBlock;
import org.veriblock.sdk.AltPublication;
import org.veriblock.sdk.ValidationResult;
import org.veriblock.sdk.VeriBlockPublication;

import java.sql.SQLException;
import java.util.*;

public class ForkresolutionComparator {

    private static ForkresolutionConfig forkresolutionConfig = new ForkresolutionConfig();

    private static VeriBlockSecurity security;

    private static PoPTransactionsDBStore popTxDBStore;

    public static ForkresolutionConfig getForkresolutionConfig() { return forkresolutionConfig; }

    public static void setForkresolutionConfig(ForkresolutionConfig config) { ForkresolutionComparator.forkresolutionConfig = config; }

    public static VeriBlockSecurity getSecurity() { return security; }

    public static void setSecurity(VeriBlockSecurity security)
    {
        ForkresolutionComparator.security = security;
        ForkresolutionComparator.popTxDBStore = security.getSecurityFiles().getPopTxDBStore();
    }

    // return 1 if leftBranchScore > rightBranchScore
    // return -1 if leftBranchScore < rightBranchScore
    // return 0 if leftBranchScore == rightBranchScore
    public static int compareTwoBranches(List<AltChainBlock> leftBranch, List<AltChainBlock> rightBranch) throws SQLException
    {
        Collections.sort(leftBranch);  // make the ascending order for the blocks in the collection, it needs for the fork resolution
        Collections.sort(rightBranch);

        addBranchTemporarily(leftBranch);
        List<Integer> leftReducedPublicationView = getReducedPublicationView(leftBranch);
        security.clearTemporaryPayloads();

        addBranchTemporarily(rightBranch);
        List<Integer> rightReducedPublicationView = getReducedPublicationView(rightBranch);
        security.clearTemporaryPayloads();

        int lastKs = Math.max(leftReducedPublicationView.size(), rightReducedPublicationView.size());
        long leftScore = 0, rigthScore = 0;

        for(int keystoneForConsideration = 0; keystoneForConsideration < lastKs; keystoneForConsideration++)
        {
            int leftForkKeystonePublciation = -1;
            int rigthForkKeystonePublciation = -1;

            if(leftReducedPublicationView.size() > keystoneForConsideration)
                leftForkKeystonePublciation = leftReducedPublicationView.get(keystoneForConsideration);
            if(rightReducedPublicationView.size() > keystoneForConsideration)
                rigthForkKeystonePublciation = rightReducedPublicationView.get(keystoneForConsideration);

            int lowestKeystonePublication = Math.min(leftForkKeystonePublciation, rigthForkKeystonePublciation);

            leftScore += getPublicationScore(lowestKeystonePublication, leftForkKeystonePublciation);
            rigthScore += getPublicationScore(lowestKeystonePublication, rigthForkKeystonePublciation);
        }

        if(leftScore > rigthScore)
            return 1;
        if(leftScore < rigthScore)
            return -1;

        return 0;
    }

    // execute for the PoP data from PoP txs the VeriBlockSecurity.addTemporaryPayloads method
    protected static void addBranchTemporarily(List<AltChainBlock> blocks) throws SQLException
    {
        for(AltChainBlock block : blocks)
        {
            List<AltPublication> altPublications = popTxDBStore.getAltPublicationsFromBlock(block);
            List<VeriBlockPublication> veriBlockPublications = popTxDBStore.getVeriBlockPublicationsFromBlock(block);
            security.addTemporaryPayloads(veriBlockPublications, altPublications);
        }
    }

    protected static List<Integer> getReducedPublicationView(List<AltChainBlock> blocks) throws SQLException
    {
        List<Integer> reducedPublciationView = new ArrayList<Integer>();
        for(int i =0; i < blocks.size(); i++)
        {
            if(blocks.get(i).isKeystone(security.getAltChainParametersConfig().keystoneInterval))
            {
                int bestPublication = getBestPublicationHeight(blocks.subList(i, blocks.size()));

                if(reducedPublciationView.size() != 0)
                {
                    int publicationDelay = bestPublication - reducedPublciationView.get(0);
                    if(bestPublication == -1 || publicationDelay > forkresolutionConfig.keystoneFinalityDelay )
                        return reducedPublciationView;
                }

                reducedPublciationView.add(bestPublication);
            }
        }
        return reducedPublciationView;
    }

    // return -1 when it is something wrong
    protected static int getBestPublicationHeight(List<AltChainBlock> blockSequence) throws SQLException
    {
        int bestPublication = -1;

        if(blockSequence.size() == 0)
            return bestPublication;

        AltChainBlock keystoneBlock = blockSequence.get(0); // the first block should be a keystone block

        if(!keystoneBlock.isKeystone(security.getAltChainParametersConfig().keystoneInterval))
            return bestPublication;

        AltChainBlock tip = blockSequence.get(blockSequence.size() - 1);

        for(int i = blockSequence.size() - 1; i >= 0; i--) {
            AltChainBlock workingBlock = blockSequence.get(i);

            if(workingBlock.getHeight() < keystoneBlock.getHeight() + security.getAltChainParametersConfig().keystoneInterval) {
                List<AltPublication> publications = popTxDBStore.getAltPublciationsEndorse(workingBlock, blockSequence.subList(i, blockSequence.size()));

                for(AltPublication publication: publications) {
                    ValidationResult fsuccess = security.checkATVAgainstView(publication);
                    int vbkHeight = publication.getContainingBlock().getHeight();
                    int publicationTime = publication.getContainingBlock().getTimestamp();

                    if(fsuccess.isValid() && (vbkHeight < bestPublication || bestPublication < 0) && keystoneBlock.getTimestamp() <= publicationTime)
                        bestPublication = vbkHeight;

                }

            }

        }
        return bestPublication;
    }

    protected static long getPublicationScore(int lowestKeystonePublication, int keystonePublication)
    {
        // the case when one of the publication is -1
        if(lowestKeystonePublication == -1)
        {
            if(keystonePublication == -1)
                return 0;

            int publicationDelay = 0;
            return forkresolutionConfig.publicationLatencyLookUpTable[publicationDelay];

        }
        else
        {
            int publicationDelay = keystonePublication - lowestKeystonePublication;
            if(publicationDelay < forkresolutionConfig.keystoneFinalityDelay)
                return forkresolutionConfig.publicationLatencyLookUpTable[publicationDelay];

            return 0;
        }
    }
}
