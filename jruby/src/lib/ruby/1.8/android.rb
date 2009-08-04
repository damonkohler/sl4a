puts "Android.rb"

AP_PORT = ENV['AP_PORT']

require 'json/pure'
require 'socket'

# function rpc(client, method, ...)
#   assert(method, 'method param is nil')
#   local rpc = {
#     ['id'] = id,
#     ['method'] = method,
#     params = arg
#   }
#   local request = json.encode(rpc)
#   client:send(request .. '\n')
#   id = id + 1
#   local response = client:receive('*l')
#   local result = json.decode(response)
#   if result.error ~= nil then
#     print(result.error)
#   end
#   return result
# end

class Droid
  
  def initialize()
    @client = TCPSocket.new('localhost', AP_PORT)
    @id = 0
  end
  
  def rpc(method, *args)
    @id += 1
    request = {'id' => @id, 'method' => method, 'params' => args}.to_json()
    @client.puts request
    response = @client.gets()
    return JSON.parse(response)
  end
  
  def method_missing(method, *args)
    rpc(method, *args)
  end
  
  def dial(uri)
    self.startActivity('android.intent.action.DIAL', uri)
  end

  def dialNumber(number)
    sefl.dial('tel:' + number)
  end

  def call(uri)
    self.startActivity('android.intent.action.CALL', uri)
  end

  def callNumber(number)
    self.call('tel:' + number)
  end
end
