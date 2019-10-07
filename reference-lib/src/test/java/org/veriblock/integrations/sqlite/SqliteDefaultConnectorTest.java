// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

public class SqliteDefaultConnectorTest {

    @Test
    public void createConnectionTest() throws IOException, SQLException {
        Connection connection = ConnectionSelector.setConnectionDefault();
        Assert.assertTrue(connection != null);
        connection.close();
    }
}