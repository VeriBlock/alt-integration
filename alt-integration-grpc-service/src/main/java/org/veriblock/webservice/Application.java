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
import org.veriblock.sdk.AltChainParametersConfig;
import org.veriblock.sdk.Context;
import org.veriblock.sdk.VeriBlockSecurity;
import org.veriblock.sdk.auditor.store.AuditorChangesStore;
import org.veriblock.sdk.blockchain.BitcoinBlockchainBootstrapConfig;
import org.veriblock.sdk.blockchain.VeriBlockBlockchainBootstrapConfig;
import org.veriblock.sdk.blockchain.store.BitcoinStore;
import org.veriblock.sdk.blockchain.store.PoPTransactionsDBStore;
import org.veriblock.sdk.blockchain.store.VeriBlockStore;
import org.veriblock.sdk.forkresolution.ForkresolutionComparator;
import org.veriblock.sdk.forkresolution.ForkresolutionConfig;
import org.veriblock.sdk.rewards.PopRewardCalculator;
import org.veriblock.sdk.rewards.PopRewardCalculatorConfig;
import org.veriblock.sdk.sqlite.ConnectionSelector;
import org.veriblock.sdk.sqlite.FileManager;

import java.io.IOException;
import java.nio.file.Paths;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String packageName = "altservice";
    public Boolean terminated = false;
    private VeriBlockSecurity security = null;
    private Server server = null;
    private AppConfiguration appConfiguration;

    @Inject
    public Application(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    public static void main(String[] args) {
        try {
            AppConfiguration configuration = new AppConfiguration();
            Injector injector = Guice.createInjector(new AppInjector(configuration));
            Application app = injector.getInstance(Application.class);

            app.run(args);
        } catch (AltConfigurationException ex){
            log.error(ex.getMessage(), ex);
        }
    }

    public void run(String[] args) {
        log.info(packageName + " " + AppConstants.APP_VERSION);
        terminated = false;

        String databasePath = Paths.get(FileManager.getDataDirectory(), ConnectionSelector.defaultDatabaseName).toString();
        ConfigurationParser config = null;
        try {
            config = new ConfigurationParser(appConfiguration.getProperties());
            
            VeriBlockStore veriBlockStore = new VeriBlockStore(databasePath);
            BitcoinStore bitcoinStore = new BitcoinStore(databasePath);
            AuditorChangesStore auditStore = new AuditorChangesStore(databasePath);
            PoPTransactionsDBStore popTxDBStore = new PoPTransactionsDBStore(databasePath);
            Context.init(config.getVeriblockNetworkParameters(), config.getBitcoinNetworkParameters(),
                         veriBlockStore, bitcoinStore, auditStore, popTxDBStore);
            
            security = new VeriBlockSecurity();

            BitcoinBlockchainBootstrapConfig btcBootstrap = config.getBitcoinBlockchainBootstrapConfig();
            if (btcBootstrap != null) {
                security.getBitcoinBlockchain().bootstrap(btcBootstrap);
            }

            VeriBlockBlockchainBootstrapConfig vbkBootstrap = config.getVeriBlockBlockchainBootstrapConfig();
            if (vbkBootstrap != null) {
                security.getVeriBlockBlockchain().bootstrap(vbkBootstrap);
            }

            AltChainParametersConfig altChainParametersConfig = config.getAltChainParametersConfig();
            if (altChainParametersConfig != null) {
                security.setAltChainParametersConfig(altChainParametersConfig);
            }

            ForkresolutionConfig forkresolutionConfig = config.getForkresolutionConfig();
            if (forkresolutionConfig != null) {
                ForkresolutionComparator.setForkresolutionConfig(forkresolutionConfig);
            }

            PopRewardCalculatorConfig popRewardCalculatorConfig = config.getPopRewardCalculatorConfig();
            if (popRewardCalculatorConfig != null) {
                PopRewardCalculator.setCalculatorConfig(popRewardCalculatorConfig);
            }

            ForkresolutionComparator.setSecurity(security);
            PopRewardCalculator.setSecurity(security);
        } catch (Exception e) {
            log.debug("Could not initialize VeriBlock security", e);
            return;
        }

        if(security == null) {
            return;
        }

        server = ServerBuilder.forPort(config.getApiPort())
                .addService(new IntegrationGrpcService(security))
                .addService(new RewardsGrpcService())
                .addService(new GrpcDeserializeService())
                .addService(new GrpcSerializeService())
                .addService(new GrpcValidationService())
                .addService(new ForkresolutionGrpcService())
                .addService(new GrpcPopService(security))
                .build();
        try {
            server.start();
        } catch (IOException e) {
            log.debug("Could not start GRPC server", e);
        }

        log.info("Started API server at " + config.getApiHost() + ":" + config.getApiPort());

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

    public void shutdown() {
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

    public VeriBlockSecurity getSecurity() {
        return security;
    }
}
