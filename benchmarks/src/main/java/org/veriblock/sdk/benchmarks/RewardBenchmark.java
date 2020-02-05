// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.benchmarks;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;

import org.openjdk.jmh.annotations.*;
import org.veriblock.sdk.rewards.PopPayoutRound;
import org.veriblock.sdk.sqlite.ConnectionSelector;

public class RewardBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        public final AltChainBenchmark benchmark;
        {
            try {
                benchmark = AltChainBenchmark.create(()->ConnectionSelector.setConnectionInMemory("security"),
                                                    ()->ConnectionSelector.setConnectionInMemory("altchain"),
                                                    ()->ConnectionSelector.setConnectionInMemory("mock-pop-miner"),
                                                    2);
                benchmark.mine(500);
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | SQLException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.Throughput)
    public PopPayoutRound calculateRewards(MyState state) throws SQLException {
        return state.benchmark.getAltChain().calculateRewards();
    }
}
