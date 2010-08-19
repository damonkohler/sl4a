var Android = function () { 
  this._callbacks = [], 
  this._id = 0, 
  this.call = function () { 
    this._id += 1;
    var method = arguments[0]; 
    var args = [];
    for (var i = 1; i < arguments.length; i++) {
      args[i - 1] = arguments[i];
    }
    var request = JSON.stringify({'id': this._id, 'method': method, 'params': args});
    var response = _rpc_wrapper.call(request); 
    return eval("(" + response + ")");
  },
  
  this.registerCallback = function (event, receiver) {
    var id = this._callbacks.push(receiver) - 1; 
    _callback_wrapper.register(event, id);
  },

  this._callback = function (id, data) {
    var receiver = this._callbacks[id];
    receiver(data);
  }
};

var droid = new Android();
