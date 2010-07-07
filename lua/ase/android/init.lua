-- Copyright (C) 2009 Google Inc.
--
-- Licensed under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License. You may obtain a copy of
-- the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
-- License for the specific language governing permissions and limitations under
-- the License.

local io = require 'io'
local json = require 'json'
local socket = require 'socket'
local P = {}

if _REQUIREDNAME == nil then
  android = P
else
  _G[_REQUIREDNAME] = P
end

local id = 0

function rpc(client, method, ...)
  assert(method, 'method param is nil')
  local rpc = {
    ['id'] = id,
    ['method'] = method,
    params = arg
  }
  local request = json.encode(rpc)
  client:send(request .. '\n')
  id = id + 1
  local response = client:receive('*l')
  local result = json.decode(response)
  if result.error ~= nil then
    print(result.error)
  end
  return result
end

local port = tonumber(os.getenv('AP_PORT'))
local host = os.getenv('AP_HOST')
local client = socket.connect(host, port)
local meta = {
  __index = function(t, key)
    return function(...)
      return rpc(client, key, unpack(arg))
    end
  end
}

setmetatable(P, meta)

local handshake = os.getenv('AP_HANDSHAKE')
P._authenticate(handshake)

-- Workaround for no sleep function in Lua.
function P.sleep(seconds)
  return os.execute('sleep ' .. seconds)
end

function P.printDict(d)
  for k, v in pairs(d) do print(k, v) end
end

function P.whoami()
  local f = assert(io.popen('id', 'r'))
  local s = assert(f:read('*a'))
  return string.match(s, 'uid=%d+%((.-)%)')
end

function P.ps()
  local f = assert(io.popen('ps', 'r'))
  local user = P.whoami()
  local procs = {}
  for line in f:lines() do
    if string.match(line, '^(.-)%s', 1) == user then
      local pid = string.match(line, '^.-%s+(%d+)', 1)
      local cmd = string.match(line, '%s+([^%s]+)$', 1)
      procs[pid] = cmd
    end
  end
  return procs
end

function P.kill(pid)
  os.execute('kill ' .. pid)
end

function P.killallmine()
  local procs = P.ps()
  local killcmd = 'kill '
  for pid, cmd in pairs(procs) do killcmd = killcmd .. pid end
  os.execute(killcmd)
end

return P
