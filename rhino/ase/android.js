/*
 * Copyright 2009 Brice Lambson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var AP_PORT = java.lang.System.getenv("AP_PORT");
var AP_HOST = java.lang.System.getenv("AP_HOST");
var AP_HANDSHAKE = java.lang.System.getenv("AP_HANDSHAKE");

load('/sdcard/sl4a/extras/rhino/json2.js');

function Android() {

  this.connection = new java.net.Socket(String(AP_HOST), AP_PORT),
  this.input = new java.io.BufferedReader(
      new java.io.InputStreamReader(this.connection.getInputStream(), "8859_1"),
                                    1 << 13),
  this.output = new java.io.PrintWriter(new java.io.OutputStreamWriter(
      new java.io.BufferedOutputStream(this.connection.getOutputStream(),
                                       1 << 13),
      "8859_1"), true),
  this.id = 0,

  this.rpc = function(method, args) {
    this.id += 1;
    var request = JSON.stringify({'id': this.id, 'method': method,
                                  'params': args});
    this.output.write(request + '\n');
    this.output.flush();
    var response = this.input.readLine();
    return eval("(" + response + ")");
  },

  this.__noSuchMethod__ = function(id, args) {
    var response = this.rpc(id, args);
    if (response.error != null) {
      throw response.error;
    }
    return response.result;
  }

  this._authenticate(String(AP_HANDSHAKE));
}
