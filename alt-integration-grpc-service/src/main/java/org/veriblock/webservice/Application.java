// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.integrations.Context;
import org.veriblock.integrations.VeriBlockSecurity;
import org.veriblock.integrations.auditor.store.AuditorChangesStore;
import org.veriblock.integrations.blockchain.store.BitcoinStore;
import org.veriblock.integrations.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.integrations.blockchain.store.VeriBlockStore;
import org.veriblock.integrations.forkresolution.ForkresolutionComparator;
import org.veriblock.integrations.rewards.PopRewardCalculator;
import org.veriblock.integrations.sqlite.ConnectionSelector;
import org.veriblock.integrations.sqlite.FileManager;
import org.veriblock.sdk.conf.AppInjector;
import org.veriblock.sdk.conf.DefaultConfiguration;

import java.io.IOException;
import java.nio.file.Paths;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);


    private static String PACKAGE_NAME = "webservice";
    public Boolean terminated = false;
    private VeriBlockSecurity security = null;
    private Server server = null;
    private DefaultConfiguration defaultConfiguration;

    @Inject
    public Application(DefaultConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppInjector(PACKAGE_NAME));
        Application app = injector.getInstance(Application.class);

        app.run(args);
    }

    public void run(String[] args) {
        log.info(defaultConfiguration.getPackageName() + " " + AppConstants.APP_VERSION);
        terminated = false;

        String databasePath = Paths.get(FileManager.getDataDirectory(), ConnectionSelector.defaultDatabaseName).toString();
        try {
            VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
            BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
            AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
            PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(databasePath);
            Context.init(defaultConfiguration, veriBlockStore, bitcoinStore, auditStore, popTxDBStore);
            security = new VeriBlockSecurity();

            ForkresolutionComparator.setSecurity(security);
            PopRewardCalculator.setSecurity(security);
        } catch (Exception e) {
            log.debug("Could not initialize VeriBlock security", e);
            return;
        }

        if(security == null) {
            return;
        }

        server = ServerBuilder.forPort(defaultConfiguration.getApiPort())
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

        log.info("Started API server at " + defaultConfiguration.getApiHost() + ":" + defaultConfiguration.getApiPort());

        try {
            while(true) {
                if(terminated) break;
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.warn(defaultConfiguration.getPackageName() + " terminated");
            terminated = true;
        }

        shutdown();

        log.warn(defaultConfiguration.getPackageName() + " stopped");
    }

    public void shutdown() {
        if(server != null) {
            server.shutdown();

            try {
                while(true) {
                    if(server.isTerminated()) break;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log.warn(defaultConfiguration.getPackageName() + " server terminated");
            }

            server = null;
        }

        if(security != null) {
            security.shutdown();
        }

        security = null;
    }

    public VeriBlockSecurity getSecurity() {
        return security;
    }
}
