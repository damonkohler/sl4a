-----------------------------------------------------------------------------
-- JSONRPC4Lua: JSON RPC server for exposing Lua objects as JSON RPC callable
-- objects via http.
-- json.rpcserver Module. 
-- Author: Craig Mason-Jones
-- Homepage: http://json.luaforge.net/
-- Version: 0.9.10
-- This module is released under the The GNU General Public License (GPL).
-- Please see LICENCE.txt for details.
--
-- USAGE:
-- This module exposes one function:
--   server(luaClass, packReturn)
--     Manages incoming JSON RPC request forwarding the method call to the given
--     object. If packReturn is true, multiple return values are packed into an 
--     array on return.
--
-- IMPORTANT NOTES:
--   1. This version ought really not be 0.9.10, since this particular part of the 
--      JSONRPC4Lua package is very first-draft. However, the JSON4Lua package with which
--      it comes is quite solid, so there you have it :-)
--   2. This has only been tested with Xavante webserver, with which it works 
--      if you patch CGILua to accept 'text/plain' content type. See doc\cgilua_patch.html
--      for details.
----------------------------------------------------------------------------

module ('json.rpcserver')

---
-- Implements a JSON RPC Server wrapping for luaClass, exposing each of luaClass's
-- methods as JSON RPC callable methods.
-- @param luaClass The JSON RPC class to expose.
-- @param packReturn If true, the server will automatically wrap any
-- multiple-value returns into an array. Single returns remain single returns. If
-- false, when a function returns multiple values, only the first of these values will
-- be returned.
--
function serve(luaClass, packReturn)
  cgilua.contentheader('text','plain')
  require('cgilua')
  require ('json')
  local postData = ""
  
  if not cgilua.servervariable('CONTENT_LENGTH') then
    cgilua.put("Please access JSON Request using HTTP POST Request")
    return 0
  else
    postData = cgi[1]	-- SAPI.Request.getpostdata()  --[[{ "id":1, "method":"echo","params":["Hi there"]}]]  --
  end
  -- @TODO Catch an error condition on decoding the data
  local jsonRequest = json.decode(postData)
  local jsonResponse = {}
  jsonResponse.id = jsonRequest.id
  local method = luaClass[ jsonRequest.method ]

  if not method then
	jsonResponse.error = 'Method ' .. jsonRequest.method .. ' does not exist at this server.'
  else
    local callResult = { pcall( method, unpack( jsonRequest.params ) ) }
    if callResult[1] then	-- Function call successfull
      table.remove(callResult,1)
      if packReturn and table.getn(callResult)>1 then
        jsonResponse.result = callResult
      else
        jsonResponse.result = unpack(callResult)	-- NB: Does not support multiple argument returns
      end
    else
      jsonResponse.error = callResult[2]
    end
  end 
  
  -- Output the result
  -- TODO: How to be sure that the result and error tags are there even when they are nil in Lua?
  -- Can force them by hand... ?
  cgilua.contentheader('text','plain')
  cgilua.put( json.encode( jsonResponse ) )
end

