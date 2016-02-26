var Web3 = require('web3');

// dont override global variable
if (typeof window !== 'undefined' && typeof window.Web3 === 'undefined') {
    window.Web3 = web3;
}

web3 = new Web3();

module.exports = web3;

web3.setProvider(new web3.providers.HttpProvider('http://localhost:4444'));

//var greeterDefinition = web3.eth.contract([{ "constant": false, "inputs": [], "name": "kill", "outputs": [], "type": "function" }, { "constant": true, "inputs": [{ "name": "message", "type": "string" }], "name": "greet", "outputs": [{ "name": "", "type": "string" }], "type": "function" }, { "inputs": [{ "name": "_greeting", "type": "string" }], "type": "constructor" }]);
var greeterDefinition = web3.eth.contract([{ "constant": false, "inputs": [{ "name": "message", "type": "string" }], "name": "setMessage", "outputs": [], "type": "function" }, { "constant": false, "inputs": [], "name": "kill", "outputs": [], "type": "function" }, { "constant": true, "inputs": [], "name": "greet", "outputs": [{ "name": "", "type": "string" }], "type": "function" }, { "inputs": [], "type": "constructor" }]);



var greeter = greeterDefinition.at('0x77045e71a7a2c50903d88e564cd72fab11e82051');
/*                                      
var result = greeter.kill({ from: '0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826', gasPrice: 1, gasLimit: 1000000 });

console.log(result);
*/

var result = greeter.greet();

console.log(result);

result = greeter.setMessage('no grabar', { from: '0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826', gasPrice: 1, gasLimit: 1000000 });

var result1 = greeter.greet();

console.log(result1);

