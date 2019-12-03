// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSelector {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionSelector.class);

    public static final String defaultDatabaseName = "database.sqlite";
    public static final String testDatabaseName = "database-test.sqlite";

    private ConnectionSelector() { }

    public static Connection setConnection(String databasePath) throws SQLException
    {
        String url;

        if (databasePath == null) {
            logger.info("Using SqlLite in-memory store");
            url = "jdbc:sqlite:file:memdb1?mode=memory&cache=shared";
        } else {Thread.currentThread().dumpStack();
            logger.info("SqlLite path: '{}'", databasePath);
            url = String.format("jdbc:sqlite:%s", databasePath);
        }

        DriverManager.registerDriver(new JDBC());
        Connection connection = DriverManager.getConnection(url);
        return connection;
    }

    public static Connection setConnectionInMemory() throws SQLException
    {
        return setConnection(null);
    }

    public static Connection setConnectionDefault() throws SQLException
    {
        String databasePath = Paths.get(FileManager.getDataDirectory(), defaultDatabaseName).toString();
        return setConnection(databasePath);
    }

    public static Connection setConnectionTestnet() throws SQLException
    {
        String databasePath = Paths.get(FileManager.getDataDirectory(), testDatabaseName).toString();
        return setConnection(databasePath);
    }
}
