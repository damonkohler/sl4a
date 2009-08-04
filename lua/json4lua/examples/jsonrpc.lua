--
-- jsonrpc.lua
-- Installed in a CGILua webserver environment (with necessary CGI Lua 5.0 patch)
--
require ('json.rpcserver')

-- The Lua class that is to serve JSON RPC requests
local myServer = {
  echo = function (msg) return msg end,
  average = function(...)
    local total=0
    local count=0
    for i=1, table.getn(arg) do
      total = total + arg[i]
      count = count + 1
    end
    return { average= total/count, sum = total, n=count }
  end
}

json.rpcserver.serve(myServer)