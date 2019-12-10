#!/usr/bin/env python
# VeriBlock Blockchain Project
# Copyright 2017-2018 VeriBlock, Inc
# Copyright 2018-2019 Xenios SEZC
# All rights reserved.
# https://www.veriblock.org
# Distributed under the MIT software license, see the accompanying
# file LICENSE or http://www.opensource.org/licenses/mit-license.php.

import os
import requests

from requests.auth import HTTPBasicAuth

import argparse

parser = argparse.ArgumentParser(
    description='Download Bitcoin headers and generate a test data '
                'file for the Bitcoin difficulty calculator')

parser.add_argument('--endpoint', required=True,
                    help='bitcoind RPC endpoint (http://host:port)')
parser.add_argument('--user', required=True,
                    help='bitcoind RPC auth user')
parser.add_argument('--password', required=True,
                    help='bitcoind RPC auth password')

parser.add_argument('--block-count', type=int, default=1,
                    help='the number of blocks headers to download')

parser.add_argument('--starting-height', type=int, metavar='BLOCK_HEIGHT',
                    help='the height of the first block to download')


parser.add_argument('--java-class', metavar='CLASS_NAME',
                    help='the name of the java class to generate')

args = parser.parse_args()

def api_call(method, params):
    return requests.post(args.endpoint,
                         auth=HTTPBasicAuth(args.user, args.password),
                         json={'jsonRpc':'1.0',
                               'method':method,
                               'params':params}
                        ).json()['result']
                         
def get_info():
    return api_call('getblockchaininfo', [])

def get_block(block_hash):
    return api_call('getblock', [block_hash])

def get_block_hash(height):
    return api_call('getblockhash', [height])

def get_block_header(block_hash):
    return api_call('getblockheader', [block_hash, False])

def get_starting_height():
    if args.starting_height is None:
        last_height = api_call('getblockcount', [])
        return max(last_height - args.block_count + 1, 1)
    else:
        return args.starting_height

def ensure_blocks_exist(starting_height, count):
    last_height = api_call('getblockcount', [])
    if starting_height + count -1 > last_height:
        raise Exception("There are less than %d blocks starting at height %d"
                        % (count, starting_height))

def get_headers(starting_height, count):
    return [get_block_header(get_block_hash(height))
            for height in range(starting_height, starting_height + args.block_count)]

    
starting_height = get_starting_height()
ensure_blocks_exist(starting_height, args.block_count)

headers = get_headers(starting_height, args.block_count)

if args.java_class is None:
    print "Block count %d" % len(headers)
    print "Starting height %s" % starting_height

    for header in headers:
        print header
else:
    print """// This file has been automatically generated using scripts/btc_header_downloader.py

// VeriBlock Blockchain Project
// Copyright 2017-2018 VeriBlock, Inc
// Copyright 2018-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package org.veriblock.sdk.blockchain.difficulty;

import java.util.Arrays;
import java.util.List;

public class %s {
    private %s() {}

    public static final int firstBlockHeight = %d;

    public static final List<String> headers = Arrays.asList(
%s
    );
}
""" % (args.java_class,
       args.java_class,
       starting_height,
       ",\n".join(['        "%s"' % header for header in headers]))
