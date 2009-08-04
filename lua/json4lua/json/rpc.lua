-----------------------------------------------------------------------------
-- JSONRPC4Lua: JSON RPC client calls over http for the Lua language.
-- json.rpc Module. 
-- Author: Craig Mason-Jones
-- Homepage: http://json.luaforge.net/
-- Version: 0.9.10
-- This module is released under the The GNU General Public License (GPL).
-- Please see LICENCE.txt for details.
--
-- USAGE:
-- This module exposes two functions:
--   proxy( 'url')
--     Returns a proxy object for calling the JSON RPC Service at the given url.
--   call ( 'url', 'method', ...)
--     Calls the JSON RPC server at the given url, invokes the appropriate method, and
--     passes the remaining parameters. Returns the result and the error. If the result is nil, an error
--     should be there (or the system returned a null). If an error is there, the result should be nil.
--
-- REQUIREMENTS:
--  Lua socket 2.0 (http://www.cs.princeton.edu/~diego/professional/luasocket/)
--  json (The JSON4Lua package with which it is bundled)
--  compat-5.1 if using Lua 5.0.
-----------------------------------------------------------------------------

module('json.rpc')

-----------------------------------------------------------------------------
-- Imports and dependencies
-----------------------------------------------------------------------------
local json = require('json')
local http = require("socket.http")

-----------------------------------------------------------------------------
-- PUBLIC functions
-----------------------------------------------------------------------------

--- Creates an RPC Proxy object for the given Url of a JSON-RPC server.
-- @param url The URL for the JSON RPC Server.
-- @return Object on which JSON-RPC remote methods can be called.
-- EXAMPLE Usage:
--   local jsolait = json.rpc.proxy('http://jsolait.net/testj.py')
--   print(jsolait.echo('This is a test of the echo method!'))
--   print(jsolait.args2String('first','second','third'))
--   table.foreachi( jsolait.args2Array(5,4,3,2,1), print)
function proxy(url)
  local serverProxy = {}
  local proxyMeta = {
    __index = function(t, key) 
      return function(...)
        return json.rpc.call(url, key, unpack(arg))
      end
    end
  }
  setmetatable(serverProxy, proxyMeta)
  return serverProxy
end

--- Calls a JSON RPC method on a remote server.
-- Returns a boolean true if the call succeeded, false otherwise.
-- On success, the second returned parameter is the decoded
-- JSON object from the server.
-- On http failure, returns nil and an error message.
-- On success, returns the result and nil.
-- @param url The url of the JSON RPC server.
-- @param method The method being called.
-- @param ... Parameters to pass to the method.
-- @return result, error The JSON RPC result and error. One or the other should be nil. If both
-- are nil, this means that the result of the RPC call was nil.
-- EXAMPLE Usage:
--   print(json.rpc.call('http://jsolait.net/testj.py','echo','This string will be returned'))
function call(url, method, ...)
  assert(method,'method param is nil to call')
  local JSONRequestArray = {
    id="httpRequest",
    ["method"]=method,
    params = arg
  }
  local httpResponse, result , code
  local jsonRequest = json.encode(JSONRequestArray)
  -- We use the sophisticated http.request form (with ltn12 sources and sinks) so that
  -- we can set the content-type to text/plain. While this shouldn't strictly-speaking be true,
  -- it seems a good idea (Xavante won't work w/out a content-type header, although a patch
  -- is needed to Xavante to make it work with text/plain)
  local ltn12 = require('ltn12')
  local resultChunks = {}
  httpResponse, code = http.request(
    { ['url'] = url,
      sink = ltn12.sink.table(resultChunks),
      method = 'POST',
      headers = { ['content-type']='text/plain', ['content-length']=string.len(jsonRequest) },
      source = ltn12.source.string(jsonRequest)
    }
  )
  httpResponse = table.concat(resultChunks)
  -- Check the http response code
  if (code~=200) then
    return nil, "HTTP ERROR: " .. code
  end
  -- And decode the httpResponse and check the JSON RPC result code
  result = json.decode( httpResponse )
  if result.result then
    return result.result, nil
  else
    return nil, result.error
  end
end
