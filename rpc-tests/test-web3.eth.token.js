var Web3 = require('web3');

// dont override global variable
if (typeof window !== 'undefined' && typeof window.Web3 === 'undefined') {
    window.Web3 = web3;
}

web3 = new Web3();

module.exports = web3;

web3.setProvider(new web3.providers.HttpProvider('http://localhost:4444'));

var tokenDefinition = web3.eth.contract([{ "constant": false, "inputs": [{ "name": "receiver", "type": "address" }, { "name": "amount", "type": "uint256" }], "name": "sendCoin", "outputs": [{ "name": "sufficient", "type": "bool" }], "type": "function" }, { "constant": true, "inputs": [{ "name": "", "type": "address" }], "name": "coinBalanceOf", "outputs": [{ "name": "", "type": "uint256" }], "type": "function" }, { "inputs": [{ "name": "supply", "type": "uint256" }], "type": "constructor" }, { "anonymous": false, "inputs": [{ "indexed": false, "name": "sender", "type": "address" }, { "indexed": false, "name": "receiver", "type": "address" }, { "indexed": false, "name": "amount", "type": "uint256" }], "name": "CoinTransfer", "type": "event" }]);


var token = tokenDefinition.at('0x1ed614cd3443efd9c70f04b6d777aed947a4b0c4');

console.log('Balance of creator is ' + token.coinBalanceOf('0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826') + ' tokens');

var tx = token.sendCoin('0x3181a6daf97af717134f832beb64c12b6ffce8c3', 1000, { from: '0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826', gasPrice: 1, gasLimit: 1000000 });

var receipt = web3.eth.getTransactionReceipt(tx);
while (receipt == null) {
    receipt = web3.eth.getTransactionReceipt(tx);
}

console.log('Balance of creator is ' + token.coinBalanceOf('0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826') + ' tokens');
console.log('Balance of receiver is ' + token.coinBalanceOf('0x3181a6daf97af717134f832beb64c12b6ffce8c3') + ' tokens');



//result = greeter.setMessage('no grabar', { from: '0xcd2a3d9f938e13cd947ec05abc7fe734df8dd826', gasPrice: 1, gasLimit: 1000000 });

//var result1 = greeter.greet();

//console.log(result1);

