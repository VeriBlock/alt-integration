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

public class GenericBlockRepository<Block> {
    protected Connection connectionSource;
    protected String tableBlocks;
    protected BlockSQLSerializer<Block> serializer;
    
    public GenericBlockRepository(Connection connection, String tableName, BlockSQLSerializer<Block> serializer) throws SQLException {
        this.connectionSource = connection;
        this.tableBlocks = tableName;
        this.serializer = serializer;
        
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS "
                    + tableBlocks
                    + " (\n"
                    + serializer.getSchema()
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

    private String getColumnsString() {
        List<String> columns = serializer.getColumns();
        List<String> quotedColumns = new ArrayList<String>();
        for (String col : columns)
            quotedColumns.add("'" + col + "'");
    
        return String.join(", ", quotedColumns);
    }

    private String getValuesString() {
      int columnCount = serializer.getColumns().size();

      String values = columnCount == 0 ? "" : "?";
      for(int i = 1; i < columnCount; i++)
          values += ", ?";

      return values;
    }

    public void save(Block block) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement(
                "REPLACE INTO "
                        + tableBlocks
                        + " (" + getColumnsString() + ") "
                        + "VALUES(" + getValuesString() + ")");
            serializer.toStmt(block, stmt);
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public Block get(String id) throws SQLException {
        List<Block> values = new ArrayList<Block>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM " + tableBlocks + " WHERE id = ?");
            int i = 0;
            stmt.setObject(++i, id);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next())
                values.add(serializer.fromResult(resultSet));

        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }

        if(values.size() > 1) throw new SQLException("Not an unique id: " + id);
        if(values.size() == 0) return null;
        return values.get(0);
    }
    
    public List<Block> getEndsWithId(String id) throws SQLException {
        List<Block> values = new ArrayList<Block>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM " + tableBlocks + " WHERE id LIKE ?");
            int i = 0;
            stmt.setObject(++i, "%" + id);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next())
                values.add(serializer.fromResult(resultSet));

        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
        return values;
    }

    public List<Block> getAll() throws SQLException {
        List<Block> values = new ArrayList<Block>();
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + tableBlocks);
    
            while (resultSet.next())
                values.add(serializer.fromResult(resultSet));

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
