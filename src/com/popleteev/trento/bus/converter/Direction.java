package com.popleteev.trento.bus.converter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Direction associated with a schedule (forward, return, unspecified).
 * Allows distinguishing between two bus stops with the same name, but at 
 * alternate sides of the road.
 * @author "Andrei Popleteev"
 */
public enum Direction {
	UNDEFINED(" "), FORWARD("a"), RETURN("r");
	
	private String symbol;
	private Direction(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public static Direction fromFileName(String fullFileName) {
		String fileName = (new File(fullFileName)).getName();
		//fileName example: OraridiDirettrice-T11I-T- Dr-Feriale.txt
		
		String[] tokens = fileName.split("[-|\\.]");
		String busAndDirection = tokens[3].toLowerCase();
		if (busAndDirection.endsWith(FORWARD.symbol)) {
			return FORWARD;
		} else if (busAndDirection.endsWith(RETURN.symbol)) {
			return RETURN;
		} else {
			return UNDEFINED;
		}
	}

	public void writeToDataStream(DataOutputStream out) throws IOException {
		out.writeUTF(symbol);
	}
}
