
var express = require("express")
var app = express()
var socket = require("nodejs-websocket")
var fs = require("fs")
var yml=fs.readFileSync('test.yml')
var stream ;


var server = socket.createServer(function (conn){
	var input="";
	
	// console.log("new connection")
	conn.on('connect',function(){
		console.log("connnection accepted")
	
	})
	conn.on("text",function (str){
		// console.log("recieved\n"+str)
		// conn.sendText(str.toUpperCase()+"#")
		if(str.startsWith("...###...###>>>")){
			stream = fs.createWriteStream(str.substring(15));
		}else{
			input=input+str;
		}
		

	})
	conn.on("error",function(error){
		console.log("error" +error)
	})
	//conn.sendBinary(yml)
	conn.on("close",function(code,reason){
			stream.write(input)
			console.log("connection closed\n"+reason)
			//console.log(input)
			stream.end()

	})
}).listen(8001)

app.get("/getLoc",function(req,res){
	res.send('patna.bihta.kanpur.up')
})
app.listen(8009)