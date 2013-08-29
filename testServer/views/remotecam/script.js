// Generated by CoffeeScript 1.6.3
var app, fn;

app = angular.module("remoteapp", ['ngRoute']);

app.config(["$routeProvider", function($routeProvider) {}]);

app.factory("socket", function() {
  var socket;
  socket = io.connect("http://192.168.1.111:3030");
  return socket;
});

fn = function(s, r, l, socket) {
  s.snap = function() {
    return socket.emit("snap", "now");
  };
  s.flash = function() {
    return socket.emit("flash", "toggle");
  };
  socket.on("snapFinish", function(data) {
    return console.log(data);
  });
  return socket.on("newImageExists", function(data) {
    return setTimeout(function() {
      $("#imgPreview").prepend('<img src="../../uploads/abc.jpg?' + (new Date()).toString() + '" alt="" class="smallimg"/>');
      return console.log("triggered");
    }, 2000);
  });
};

app.controller("mainCtrl", ["$scope", "$rootScope", "$location", "socket", fn]);
