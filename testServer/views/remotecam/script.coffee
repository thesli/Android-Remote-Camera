app = angular.module("remoteapp",['ngRoute'])

app.config([
  "$routeProvider"
  ($routeProvider)->
])

$("button").css("height",$("button").css("width"))

app.factory("socket",
	()->
		socket = io.connect("http://192.168.1.111:3030")
		return socket
	)

console.log("working")
fn =
  (s,r,l,socket)->
    s.snap =
      ->
        socket.emit("snap","now")
    s.flash =
      ->
        socket.emit("flash","toggle")
    socket.on("snapFinish",
      (data)->
        console.log(data)
    )
    socket.on("newImageExists",
      (data)->
        $("")
    )
    s.id = l.path()

app.controller(
	"mainCtrl",
	[
    "$scope","$rootScope","$location","socket",fn
	]
	)

$(document).ready(
  ->
    setTimeout(
      ->
        $("#imgPreview").append('<img src="1.gif" alt=""/>')
      10000
    )
)