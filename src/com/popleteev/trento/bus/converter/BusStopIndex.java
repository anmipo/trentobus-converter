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
import java.io.OutputStream;
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
    SortedMap<String, ArrayList<String>> data;
    public BusStopIndex() {
        data = new TreeMap<String, ArrayList<String>>();
    }
    
    /**
     * Adds a filename containing busStopName into the index.
     * @param busStopName
     * @param scheduleFileName
     */
    public void put(String busStopName, String scheduleFileName) {
        ArrayList<String> subData = data.get(busStopName);
        if (subData==null) {
            subData = new ArrayList<String>();
            data.put(busStopName, subData);
        }
        if (!subData.contains(scheduleFileName))
            subData.add(scheduleFileName);
    }
    
    /**
     * Adds filenames containing busStopName into the index.
     * @param busStopName
     * @param scheduleFileNames
     */
    public void putAll(String busStopName, Collection<String> scheduleFileNames) {
        ArrayList<String> subData = data.get(busStopName);
        if (subData==null) {
            subData = new ArrayList<String>();
            data.put(busStopName, subData);
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
     */
    public void saveToStream(OutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        try {
            Iterator<String> stops = data.keySet().iterator();
            while (stops.hasNext()) {
                String busStop = stops.next();
                StringBuffer line = new StringBuffer();
                line.append(busStop);
                Collection<String> fileNames = data.get(busStop);
                for (String fileName: fileNames) {
                    line.append("\t");
                    line.append(fileName);
                }
                pw.println(line);
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
        out.writeShort(data.size()); //write number of stops
        Iterator<String> stops = data.keySet().iterator();
        while (stops.hasNext()) {
            String busStop = stops.next();
            out.writeUTF(busStop); //write busstop name
            Collection<String> fileNames = data.get(busStop);
            out.writeShort(fileNames.size()); //write number of related files
            for (String fileName: fileNames) {
                out.writeUTF(fileName); //write related file name
            }
        }
    }
}
