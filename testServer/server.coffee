io = require("socket.io").listen(3030)
express = require("express")
fs = require("fs")
app = new express()
app.use(express.bodyParser(
  keepExtensions: true,
  uploadDir: "uploads"
))

app.use("/uploads",express.static(__dirname+"/uploads"))

app.post("/upload",(req,res)->
  console.log(req.files)
  console.log(req.files.file.path)
  fs.rename(req.files.file.path,"uploads/abc.jpg",
    (err)->
      throw err if(err)
      console.log("change to tmp")
      io.sockets.emit("newImageExists",{"reload":"now"})
  )
#  console.log(req.files.file.name)
#  console.log(req.files.file.path)
#  res.end("<h1>something is done</h1>")
)

app.use("/",express.static(__dirname+"/views"))

app.listen(80)

lol = {}
io.sockets.on("connection",
	(s)->
    s.on("snap",
      ()->
        s.broadcast.emit("takephoto","now")
        console.log("snap")
    )
	)
