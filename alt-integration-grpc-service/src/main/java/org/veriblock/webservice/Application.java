// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.forkresolution.ForkresolutionComparator;
import org.veriblock.integrations.params.MainNetParameters;
import org.veriblock.integrations.rewards.PopRewardCalculator;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public final class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String packageName = "webservice";

    public static final String version = AppConstants.APP_VERSION;
    public static Boolean terminated = false;

    public static DefaultConfiguration config = new DefaultConfiguration();
    public static int apiPort = config.getApiPort();
    public static String apiHost = "localhost";

    private static VeriBlockSecurity security = null;
    private static Server server = null;

    public static void main(String[] args)
    {
        log.info(packageName + " " + version);
        terminated = false;

        String databasePath = Paths.get(FileManager.getDataDirectory(), ConnectionSelector.defaultDatabaseName).toString();
        try {
            VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
            BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
            AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
            PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(databasePath);
            Context securityFiles = new Context(new MainNetParameters(), veriBlockStore, bitcoinStore, auditStore, popTxDBStore);
            security = new VeriBlockSecurity(securityFiles);

            ForkresolutionComparator.setSecurity(security);
            PopRewardCalculator.setSecurity(security);
        } catch (Exception e) {
            log.debug("Could not initialize VeriBlock security", e);
            return;
        }

        if(security == null) {
            return;
        }

        server = ServerBuilder.forPort(apiPort)
                .addService(new IntegrationGrpcService(security))
                .addService(new RewardsGrpcService())
                .addService(new GrpcDeserializeService())
                .addService(new GrpcSerializeService())
                .addService(new GrpcValidationService())
                .addService(new ForkresolutionGrpcService())
                .build();
        try {
            server.start();
        } catch (IOException e) {
            log.debug("Could not start GRPC server", e);
        }

        log.info("Started API server at " + apiHost + ":" + apiPort);

        try {
            while(true) {
                if(terminated) break;
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.warn(packageName + " terminated");
            terminated = true;
        }

        shutdown();

        log.warn(packageName + " stopped");
    }

    public static void shutdown() {
        if(server != null) {
            server.shutdown();

            try {
                while(true) {
                    if(server.isTerminated()) break;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log.warn(packageName + " server terminated");
            }

            server = null;
        }

        if(security != null) {
            security.shutdown();
        }

        security = null;
    }

    public static VeriBlockSecurity getSecurity() {
        return security;
    }
}