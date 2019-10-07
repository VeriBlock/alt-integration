// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.integrations.sqlite.tables;

import java.sql.Connection;
import java.sql.SQLException;

public class VeriBlockBlocksRepository {
    private GenericBlocksRepository blocksRepository;
    
    public VeriBlockBlocksRepository(Connection connection) throws SQLException {
        blocksRepository = new GenericBlocksRepository(connection, "tableVeriBlockBlocks");
    }
    
    public GenericBlocksRepository getBlocksRepository() {
        return blocksRepository;
    }
}