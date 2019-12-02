// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.sqlite.tables;

import org.veriblock.sdk.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuditorChangesRepository {
    private Connection connectionSource;

    public AuditorChangesRepository(Connection connection) throws SQLException {
        this.connectionSource = connection;
        Statement stmt = null;
        try {
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tableAuditorChanges (\n"
                    + " id INTEGER PRIMARY KEY,\n"
                    + " blockId TEXT NOT NULL,\n"
                    + " networkId TEXT,\n"
                    + " operation INTEGER,\n"
                    + " sequenceNum INTEGER,\n"
                    + " oldValue TEXT,\n"
                    + " newValue TEXT\n"
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
            stmt.execute("DELETE FROM tableAuditorChanges");
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public void save(AuditorChangeData change) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("REPLACE INTO tableAuditorChanges "
                + "('id', 'blockId', 'networkId', 'operation', 'sequenceNum', 'oldValue', 'newValue') "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)");
            int i = 0;
            stmt.setObject(++i, change.id);
            stmt.setObject(++i, change.blockId);
            stmt.setObject(++i, change.networkId);
            stmt.setObject(++i, change.operation);
            stmt.setObject(++i, change.sequenceNum);
            stmt.setObject(++i, Utils.encodeHex(change.oldValue));
            stmt.setObject(++i, Utils.encodeHex(change.newValue));
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }
    
    public AuditorChangeData get(Long id) throws SQLException {
        List<AuditorChangeData> values = new ArrayList<AuditorChangeData>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM tableAuditorChanges WHERE id = ?");
            int i = 0;
            stmt.setObject(++i, id);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next()) {
                AuditorChangeData data = new AuditorChangeData();
                data.id = resultSet.getLong("id");
                data.blockId = resultSet.getString("blockId");
                data.networkId = resultSet.getString("networkId");
                data.operation = resultSet.getShort("operation");
                data.sequenceNum = resultSet.getInt("sequenceNum");
                data.oldValue = Utils.decodeHex(resultSet.getString("oldValue"));
                data.newValue = Utils.decodeHex(resultSet.getString("newValue"));
    
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
    
    public void delete(String blockId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("DELETE FROM tableAuditorChanges WHERE blockId = ?");
            int i = 0;
            stmt.setObject(++i, blockId);
            stmt.execute();
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public List<AuditorChangeData> getWithBlockId(String blockId) throws SQLException {
        List<AuditorChangeData> values = new ArrayList<AuditorChangeData>();
        PreparedStatement stmt = null;
        try {
            stmt = connectionSource.prepareStatement("SELECT * FROM tableAuditorChanges WHERE blockId = ?");
            int i = 0;
            stmt.setObject(++i, blockId);
            ResultSet resultSet = stmt.executeQuery();
    
            while (resultSet.next()) {
                AuditorChangeData data = new AuditorChangeData();
                data.id = resultSet.getLong("id");
                data.blockId = resultSet.getString("blockId");
                data.networkId = resultSet.getString("networkId");
                data.operation = resultSet.getShort("operation");
                data.sequenceNum = resultSet.getInt("sequenceNum");
                data.oldValue = Utils.decodeHex(resultSet.getString("oldValue"));
                data.newValue = Utils.decodeHex(resultSet.getString("newValue"));
    
                values.add(data);
            }
        } finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
        return values;
    }
}