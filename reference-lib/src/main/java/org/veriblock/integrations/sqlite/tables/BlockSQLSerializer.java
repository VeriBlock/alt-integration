// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface BlockSQLSerializer<Block> {
    void toStmt(Block block, PreparedStatement stmt) throws SQLException;
    Block fromResult(ResultSet result)  throws SQLException;
    String getSchema();
    List<String> getColumns();
}
