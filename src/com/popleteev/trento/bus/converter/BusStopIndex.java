/*
    Copyright © 2009 Andrei Popleteev

    This file is part of Schedule Converter for TrentoBus.

    This is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TrentoBus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
*/
package com.popleteev.trento.bus.converter;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manages the mapping of bus stop names to 
 * the schedule files containing those busstops.
 * 
 * @author Andrei Popleteev
 */
public class BusStopIndex {
	SortedMap<BusStop, ArrayList<String>> data;
    public BusStopIndex() {
        data = new TreeMap<BusStop, ArrayList<String>>();
    }
    
    /**
     * Adds a filename containing busStopName into the index.
     * @param busStop
     * @param scheduleFileName
     */
    public void put(BusStop busStop, String scheduleFileName) {
        ArrayList<String> subData = data.get(busStop);
        if (subData==null) {
            subData = new ArrayList<String>();
            data.put(busStop, subData);
        }
        if (!subData.contains(scheduleFileName))
            subData.add(scheduleFileName);
    }
    
    /**
     * Adds filenames containing busStopName into the index.
     * @param busStop
     * @param scheduleFileNames
     */
    public void putAll(BusStop busStop, Collection<String> scheduleFileNames) {
        ArrayList<String> subData = data.get(busStop);
        if (subData==null) {
            subData = new ArrayList<String>();
            data.put(busStop, subData);
        }
        subData.addAll(scheduleFileNames);
    }
    
    /**
     * Returns the names of schedule files containing the given bus stop name.
     * @param busStopName
     * @return
     */
    public ArrayList<String> get(String busStopName) {
        return data.get(busStopName);
    }
    
    /**
     * Writes the data into a binary file.
     * @param fileName
     * @throws IOException
     */
    public void saveToFile(String fileName) throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName, false));
        try {
            saveToDataStream(out);
        } finally {
            out.close();
        }
    }
    /**
     * Writes data to the stream in human-readable text form.
     * @param out
     * @throws IOException 
     */
    public void saveToStream(DataOutputStream out) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        try {
            Iterator<BusStop> stops = data.keySet().iterator();
            while (stops.hasNext()) {
                BusStop busStop = stops.next();
                busStop.saveToDataStream(out);
                
                Collection<String> fileNames = data.get(busStop);
                out.writeInt(fileNames.size());
                for (String fileName: fileNames) {
                    out.writeUTF(fileName);
                }
            }
        } finally {
            pw.close();
        }
    }
    
    /**
     * Writes data to the stream in binary form.
     * @param out
     * @throws IOException
     */
    public void saveToDataStream(DataOutputStream out) throws IOException {
        out.writeInt(data.size()); //write number of stops
        Iterator<BusStop> stops = data.keySet().iterator();
        while (stops.hasNext()) {
            BusStop busStop = stops.next();
            busStop.saveToDataStream(out);
            Collection<String> fileNames = data.get(busStop);
            out.writeInt(fileNames.size()); //write number of related files
            for (String fileName: fileNames) {
                out.writeUTF(fileName); //write related file name
            }
        }
    }
}
