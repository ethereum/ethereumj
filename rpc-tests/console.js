#!/usr/bin/env node

var Web3 = require('web3');
var readline = require('readline');
fs = require('fs')


// dont override global variable
if (typeof window !== 'undefined' && typeof window.Web3 === 'undefined') {
    window.Web3 = web3;
}

var server = "localhost:4444";
var file = "";

getInputParamsFromCommandLineIfAny();
initializeWeb3();

if (file == ""){
	startConsole();
} else {
	executeScript();
}

function executeScript(){
	fs.readFile(file, 'utf8', function (err, script) {
	if (err) {
		return console.log(err);
	}
	
	eval(script);
});
}

function startConsole(){

	var rl = null;
	
	rl = readline.createInterface(process.stdin, process.stdout);
	rl.setPrompt('RSK > ');
	rl.prompt();	
	
	rl.on('line', function(line) {
		
		if (line === "quit") rl.close();
		try
		{
			var result = eval(line);
			if (result != null)
			{
				console.log(result);
			}
		}
		catch(ex)
		{
			console.log('ERROR DURING EXECUTION OF SCRIPT\n');
			console.log(ex);
		}
		
		rl.prompt();
	}).on('close',function(){
		process.exit(0);
	});
}

function getInputParamsFromCommandLineIfAny(){
	for(var i = 2; i < process.argv.length; i = i + 2 ) {
		switch(process.argv[i]){
			case '-file': 
				if (process.argv[i + 1] == null)
				{
					console.log('Please specify script file to use. Example: node console -file c:\\myscripts\\getcoinbase.js');
					process.exit(0);
				}
				file = process.argv[i + 1];
				break;
			case '-help':
				printHelp();		
				process.exit(0);
			case '-server':
				if (process.argv[i + 1] == null)
				{
					console.log('Please specify server address to use. Example: node console -server www.remote.com:8888.');
					console.log('If port is not specified connection will try port 80.');
					process.exit(0);
				}
				server = process.argv[i + 1];
				break;
			default: 
				console.log('Invalid option. Please type "node console -help" for help.');
				process.exit(0);
		}
	};
}

function initializeWeb3(){
	web3 = new Web3();
	web3.setProvider(new web3.providers.HttpProvider('http://' + server));
	module.exports = web3;	
}
function printHelp(){
	console.log("\n");
	console.log("RSK Console usage: ");
	console.log("-----------------   \n");
	console.log("usage: node console [-file script-filename] [-server address]   \n");
	console.log("-file: script to use as input. Executes that file and exit console. ");
	console.log("-server: specified RSK RPC JSON server to connect to. Default is localhost:4444 ");
	console.log("\n");
}