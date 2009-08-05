# Copyright (C) 2009 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

AP_PORT = ENV['AP_PORT']

require 'json/pure'
require 'socket'


class Android

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

end
