package org.ethereum.vm;

/**
 * A wrapper for a message call from a contract to another account.
 * This can either be a normal CALL, STATELESS call or POST call.
 */
public class MessageCall {
	
	public enum MsgType { 
		CALL, 
		STATELESS, 
		POST;
	}
	
	/** Type of internal call. Either CALL, STATELESS or POST */
	private MsgType type;

    /** gas to pay for the call, remaining gas will be refunded to the caller */
	private DataWord gas;
    /** address to call */
	private DataWord toAddress;
    /** address of the storage to use */
	private DataWord storageAddress;
    /** the value that can be transfer along with the code execution */
	private DataWord endowment;
    /** start of memory to be input data to the call */
	private DataWord inDataOffs;
    /** size of memory to be input data to the call */
	private DataWord inDataSize;
    /** start of memory to be output of the call */
	private DataWord outDataOffs;
    /** size of memory to be output data to the call */
	private DataWord outDataSize;

	public MessageCall(MsgType type, DataWord gas, DataWord toAddress,
			DataWord storageAddress, DataWord endowment,
			DataWord inDataOffs, DataWord inDataSize) {
		this.type = type;
		this.gas = gas;
		this.toAddress = toAddress;
		this.storageAddress = (type == MsgType.STATELESS) ? storageAddress: toAddress;
		this.endowment = endowment;
		this.inDataOffs = inDataOffs;
		this.inDataSize = inDataSize;
	}
	
	public MessageCall(MsgType type, DataWord gas, DataWord toAddress,
			DataWord storageAddress, DataWord endowment,
			DataWord inDataOffs, DataWord inDataSize, DataWord outDataOffs,
			DataWord outDataSize) {
		this(type, gas, toAddress, storageAddress, endowment, inDataOffs, inDataSize);
		this.outDataOffs = outDataOffs;
		this.outDataSize = outDataSize;
	}

	public MsgType getType() {
		return type;
	}

	public DataWord getGas() {
		return gas;
	}

	public DataWord getToAddress() {
		return toAddress;
	}

	public DataWord getStorageAddress() {
		return storageAddress;
	}

	public DataWord getEndowment() {
		return endowment;
	}

	public DataWord getInDataOffs() {
		return inDataOffs;
	}

	public DataWord getInDataSize() {
		return inDataSize;
	}

	public DataWord getOutDataOffs() {
		return outDataOffs;
	}

	public DataWord getOutDataSize() {
		return outDataSize;
	}
}
