// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2020 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.webservice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppInjector extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(AppInjector.class);
    private AppConfiguration appConfiguration;

    public AppInjector(AppConfiguration packageName) {
        this.appConfiguration = packageName;
    }

    @Override
    protected void configure() {
        logger.debug("App configuration started.");

        Names.bindProperties(binder(), appConfiguration.getProperties());

        logger.debug("App configuration ended.");
    }

    @Provides
    @Singleton
    public AppConfiguration provideWallet() {
        return appConfiguration;
    }

}
