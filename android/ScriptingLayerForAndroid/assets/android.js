function Android(){ 
  this.callbacks = [], 
  this.id = 0, 
  this.call = function(){ 
    this.id += 1;
    var method = arguments[0]; 
    var args = [];
    for (var i=1; i<arguments.length; i++){
      args[i-1]=arguments[i];
    }
    var request = JSON.stringify({'id': this.id, 'method': method,'params': args});
    var response = droid_rpc.call(request); 
    return eval("(" + response + ")");
  },
  this.registerCallback = function(event, receiver){
    var id = this.callbacks.push(receiver)-1; 
    droid_callback.register(event, id);
  },
  this._callback = function(id, data){
    var receiver = this.callbacks[id];
    receiver(data);
  }
};
var droid = new Android();