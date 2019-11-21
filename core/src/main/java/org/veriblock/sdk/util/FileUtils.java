// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.sdk.conf.AppConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);


    public static Properties loadProperty(String propertiesPath) {
        Properties properties = new Properties();
        try (InputStream stream = AppConfiguration.class
                .getClassLoader()
                .getResourceAsStream(propertiesPath)) {
            properties.load(stream);
        } catch (IOException e) {
            logger.error("Unable to load default properties", e);
            return null;
        }
        return properties;
    }
}
