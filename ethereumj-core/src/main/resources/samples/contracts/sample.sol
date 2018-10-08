pragma solidity ^0.4.11;

contract Sample {
    int i;
    event Inc(
        address _from,
        int _inc,
        int _total
    );

    function inc(int n) {
        i = i + n;
        Inc(msg.sender, n, i);
    }

    function get() returns (int) {
        return i;
    }
}