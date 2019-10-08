// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webclient;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.protoservice.DeserializeProtoClient;
import org.veriblock.integrations.AltChainParametersConfig;
import org.veriblock.integrations.forkresolution.ForkresolutionConfig;
import org.veriblock.protoservice.VeriBlockForkresolutionProtoClient;
import org.veriblock.protoservice.VeriBlockSecurityProtoClient;
import org.veriblock.sdk.*;
import org.veriblock.sdk.util.BitcoinUtils;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

public final class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String packageName = "webclient";

    public static final String version = AppConstants.APP_VERSION;
    public static Boolean terminated = false;

    public static DefaultConfiguration config = new DefaultConfiguration();
    public static int apiPort = config.getApiPort();
    public static String apiHost = "localhost";
    
    public static ManagedChannel channel = null;

    public static void main(String[] args) throws SignatureException
    {
        log.info(packageName + " " + version);
        terminated = false;

        channel = NettyChannelBuilder.forAddress(apiHost, apiPort).usePlaintext().build();
        VeriBlockSecurityProtoClient client = new VeriBlockSecurityProtoClient(channel);

        ValidationResult resultReset = client.resetSecurity();
        log.info("Reset command success: " + resultReset.isValid());
        
        long veriBits = BitcoinUtils.encodeCompactBits(BigInteger.ONE);
        VeriBlockBlock genesis = new VeriBlockBlock(0, (short) 2, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH, VBlakeHash.EMPTY_HASH,
                Sha256Hash.ZERO_HASH, 1, (int) veriBits, 1);

        ValidationResult replyGenesisVeriBlock = client.addGenesisVeriBlock(genesis);
        log.info("Add VeriBlock Genesis command success: " + replyGenesisVeriBlock.isValid());
        
        int bitcoinBits = BitcoinUtils.bitcoinVeryHighPowEncodeToBits();
        BitcoinBlock genesisBitcoin = new BitcoinBlock(0, Sha256Hash.ZERO_HASH, Sha256Hash.ZERO_HASH, 1, bitcoinBits, 1);
        
        ValidationResult replyBitcoinBlock = client.addGenesisBitcoin(genesisBitcoin);
        log.info("Add Bicoin Genesis command success: " + replyBitcoinBlock.isValid());

        VeriBlockTransaction tx = VeriBlockTransactionsAtv.createAtv();
        AltPublication publication = VeriBlockTransactionsAtv.createAtvPublicationAttached(genesis, tx);

        ValidationResult replyCheckATV = client.checkATVAgainstView(publication);
        log.info("CheckATVAgainstView command success: " + replyCheckATV.isValid());
        
        ///HACK: bits value is hardcoded in createVtbAttachedToBitcoinBlock()
        VeriBlockPoPTransaction txPop = VeriBlockTransactionsVtb.createVtbAttachedToBitcoinBlock(genesisBitcoin.getHash());
        VeriBlockPublication vtbPublication = VeriBlockTransactionsVtb.createVtbPublicationAttached(genesis, txPop);
        
        List<VeriBlockPublication> vtbPublications = new ArrayList<>();
        vtbPublications.add(vtbPublication);
        
        long blockHeight = 1L;
        String blockHash = "01";
        BlockIndex blockIndex = new BlockIndex(blockHeight, blockHash);
        List<AltPublication> atvs = new ArrayList<>();
        
        ValidationResult replyAddPayloads = client.addPayloads(blockIndex, atvs, vtbPublications);
        log.info("AddPayloads command success: " + replyAddPayloads.isValid());
        
        DeserializeProtoClient client2 = new DeserializeProtoClient(channel);
        client2.parseAltPublication("123".getBytes());

        AltChainParametersConfig altChainConfig = new AltChainParametersConfig();
        altChainConfig.keystoneInterval = 13;
        ValidationResult replySetAltChainConfig = client.setAltChainParametersConfig(altChainConfig);
        log.info("SetAltChainParametersConfig command success: " + replySetAltChainConfig.isValid());

        TestForkresolutionService();

        shutdown();

        log.warn(packageName + " stopped");
    }

    public static void TestForkresolutionService()
    {
        VeriBlockForkresolutionProtoClient client  = new VeriBlockForkresolutionProtoClient(channel);

        ForkresolutionConfig forkresolutionConfig = new ForkresolutionConfig(60, 20);
        ValidationResult replySetForkresolutionConfig = client.setForkresolutionConfig(forkresolutionConfig);
        log.info("SetForkresolutionConfig command success: " + replySetForkresolutionConfig.isValid());

        Pair<ValidationResult, Integer> replyCompareTwoBranches = client.compareTwoBranches(new ArrayList<AltChainBlock>(), new ArrayList<AltChainBlock>());
        log.info("CompareTwoBranches command success: " + replyCompareTwoBranches.getFirst().isValid());
        log.info("CompareTwoBranches command result: " + replyCompareTwoBranches.getSecond());
    }

    public static void shutdown() {
        if(channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }
}