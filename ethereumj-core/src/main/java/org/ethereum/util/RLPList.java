package org.ethereum.util;

import java.util.ArrayList;
import java.util.List;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 21/04/14 16:26
 */
public class RLPList implements RLPElement {

    byte[] rlpData;
    List<RLPElement> list;

    public RLPList() {
        this.list = new ArrayList<RLPElement>();
    }

    public void addItem(RLPElement element){
        list.add(element);
    }

    public RLPElement getElement(int index){
        return list.get(index);
    }

    public int size(){
        return list.size();
    }

    public List<RLPElement> getList(){
        return list;
    }

    public void setRLPData(byte[] rlpData){
        this.rlpData = rlpData;
    }

    public byte[] getRLPData(){
        return rlpData;
    }

	public static void recursivePrint(RLPElement element) {

		if (element == null)
			throw new RuntimeException("RLPElement object can't be null");
		if (element instanceof RLPList) {

			RLPList rlpList = (RLPList) element;
			System.out.print("[");			
			for (RLPElement singleElement : rlpList.getList()) {
				recursivePrint(singleElement);
			}
			System.out.print("]");
		} else {
			String hex = Utils.toHexString(((RLPItem) element).getData());
			System.out.print(hex + ", ");
		}
	}
}
