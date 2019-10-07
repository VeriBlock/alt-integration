// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.veriblock.sdk.util.Utils;

public class GenericBlocksRepository {
    protected Connection connectionSource;
    protected String tableBlocks;

    public GenericBlocksRepository(Connection connection, String tableName) throws SQLException {
        this.connectionSource = connection;
        this.tableBlocks = tableName;
        
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS "
                    + tableBlocks
                    + " (\n"
                    + " id TEXT PRIMARY KEY,\n"
                    + " previousId TEXT,\n"
                    + " height INTEGER,\n"
                    + " work TEXT,\n"
                    + " data TEXT\n"
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
            stmt.execute("DELETE FROM " + tableBlocks);
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public void save(BlockData block) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement(
                "REPLACE INTO "
                        + tableBlocks
                        + " ('id', 'previousId', 'height', 'work', 'data') "
                        + "VALUES(?, ?, ?, ?, ?)");
            int i = 0;
            stmt.setObject(++i, block.id);
            stmt.setObject(++i, block.previousId);
            stmt.setObject(++i, block.height);
            stmt.setObject(++i, block.work.toString());
            stmt.setObject(++i, Utils.encodeHex(block.data));
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }
    
    public BlockData get(String id) throws SQLException {
        List<BlockData> values = new ArrayList<BlockData>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM " + tableBlocks + " WHERE id = ?");
            int i = 0;
            stmt.setObject(++i, id);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next()) {
                BlockData data = new BlockData();
                data.id = resultSet.getString("id");
                data.previousId = resultSet.getString("previousId");
                data.height = resultSet.getInt("height");
                data.work = new BigInteger(resultSet.getString("work"));
                data.data = Utils.decodeHex(resultSet.getString("data"));
    
                values.add(data);
            }
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        if(values.size() > 1) throw new SQLException("Not an unique id: " + id);
        if(values.size() == 0) return null;
        return values.get(0);
    }
    
    public List<BlockData> getEndsWithId(String id) throws SQLException {
        List<BlockData> values = new ArrayList<BlockData>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM " + tableBlocks + " WHERE id LIKE ?");
            int i = 0;
            stmt.setObject(++i, "%" + id);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next()) {
                BlockData data = new BlockData();
                data.id = resultSet.getString("id");
                data.previousId = resultSet.getString("previousId");
                data.height = resultSet.getInt("height");
                data.work = new BigInteger(resultSet.getString("work"));
                data.data = Utils.decodeHex(resultSet.getString("data"));
    
                values.add(data);
            }
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
        return values;
    }

    public List<BlockData> getAll() throws SQLException {
        List<BlockData> values = new ArrayList<BlockData>();
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + tableBlocks);
    
            while (resultSet.next()) {
                BlockData data = new BlockData();
                data.id = resultSet.getString("id");
                data.previousId = resultSet.getString("previousId");
                data.height = resultSet.getInt("height");
                data.work = new BigInteger(resultSet.getString("work"));
                data.data = Utils.decodeHex(resultSet.getString("data"));
    
                values.add(data);
            }
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
        return values;
    }
    
    public void delete(String id) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("DELETE FROM " + tableBlocks + " WHERE id = ?");
            int i = 0;
            stmt.setObject(++i, id);
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }
}