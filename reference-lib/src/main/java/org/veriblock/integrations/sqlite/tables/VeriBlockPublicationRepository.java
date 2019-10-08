// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import org.veriblock.sdk.VeriBlockPublication;
import org.veriblock.sdk.services.SerializeDeserializeService;

import java.sql.*;

public class VeriBlockPublicationRepository {


    private Connection connectionSource;

    public final static String tableName = "VeriBlockPublication";
    public final static String idColumnName = "id";
    public final static String veriBlockPublicationColumnName = "veriBlockPublication";

    public VeriBlockPublicationRepository(Connection connection) throws SQLException
    {
        this.connectionSource = connection;
        Statement stmt = null;
        try{
            stmt = connectionSource.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName
                    + "(\n "
                    + idColumnName + " INTEGER PRIMARY KEY,\n "
                    + veriBlockPublicationColumnName + " BLOB NOT NULL\n "
                    + ");");
        }
        finally{
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

    public void clear() throws SQLException
    {
        Statement stmt = null;
        try{
            stmt = connectionSource.createStatement();
            stmt.execute( "DELETE FROM " + tableName);
        }
        finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
    }

    public int save(VeriBlockPublication publication) throws SQLException
    {
        int id = -1;
        PreparedStatement stmt = null;
        try{
            stmt = connectionSource.prepareStatement(" REPLACE INTO " + tableName + " ('" + veriBlockPublicationColumnName + "') " +
                    "VALUES(?) ", Statement.RETURN_GENERATED_KEYS);
            stmt.setBytes(1, SerializeDeserializeService.serialize(publication));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next())
                id = rs.getInt(1);
        }
        finally {
            if(stmt != null) stmt.close();
            stmt = null;
        }
        return id;
    }
}
