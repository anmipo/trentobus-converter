package com.popleteev.trento.bus.converter;

import java.io.DataOutputStream;
import java.io.IOException;

public class BusStop implements Comparable<BusStop> {
	final public String name;
	final public Direction direction;
	
	public BusStop(String name, Direction direction) {
		this.name = name;
		this.direction = direction;
	}
	public void saveToDataStream(DataOutputStream out) throws IOException {
		out.writeUTF(name);
		out.writeUTF(direction.getSymbol());
	}
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof BusStop) {
			BusStop bs = (BusStop) obj;
			result = this.direction.equals(bs.direction) && 
					this.name.equals(bs.name);
		}
		return result;
	}
	@Override
	public int compareTo(BusStop bs) {
		int result = name.compareTo(bs.name);
		if (result == 0) result = direction.compareTo(bs.direction);
		return result;
	}
}