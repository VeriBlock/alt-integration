// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.sqlite.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KeyValueRepository {

    private Connection connectionSource;

    public KeyValueRepository(Connection connection) throws SQLException {
        this.connectionSource = connection;

        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS genericCache (\n"
                    + " key TEXT PRIMARY KEY,\n"
                    + " value TEXT\n"
                    + ");");
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        try {
            stmt = connectionSource.createStatement();
            stmt.execute("PRAGMA journal_mode=WAL;");
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public void clear() throws SQLException {
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            stmt.execute("DELETE FROM genericCache");
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public void save(String key, String value) throws SQLException {        
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("REPLACE INTO genericCache ('key', 'value') " +
                "VALUES(?, ?)");
            int i = 0;
            stmt.setObject(++i, key);
            stmt.setObject(++i, value);
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public String getValue(String key) throws SQLException {
        List<KeyValueData> values = new ArrayList<KeyValueData>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT key, value FROM genericCache WHERE key = ?");
            int i = 0;
            stmt.setObject(++i, key);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next()) {
                KeyValueData data = new KeyValueData();
                data.key = resultSet.getString("key");
                data.value = resultSet.getString("value");
    
                values.add(data);
            }
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        if(values.size() > 1) throw new SQLException("Not an unique id: " + key);
        if(values.size() == 0) return null;
        return values.get(0).value;
    }
}