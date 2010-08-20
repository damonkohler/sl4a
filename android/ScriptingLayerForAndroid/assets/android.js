var Android = function () { 
  this._callbacks = [], 
  this._id = 0, 
  this._call = function (method, args) { 
    this._id += 1; 
    //This converts associative map args into array params 
    var params = Array.prototype.slice.call(args);
    var request = JSON.stringify({'id': this._id, 'method': method, 'params':params});
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
