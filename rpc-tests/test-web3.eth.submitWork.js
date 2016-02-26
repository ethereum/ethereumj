#!/usr/bin/env node

var Web3 = require('web3');

// dont override global variable
if (typeof window !== 'undefined' && typeof window.Web3 === 'undefined') {
    window.Web3 = web3;
}

web3 = new Web3();

module.exports = web3;

web3.setProvider(new web3.providers.HttpProvider('http://localhost:4444'));

var result = web3.eth.submitWork(
	"0x0000000000000001", 
	"0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
	"0xD1FE5700000000000000000000000000D1FE5700000000000000000000000000"
);

console.log(result);
