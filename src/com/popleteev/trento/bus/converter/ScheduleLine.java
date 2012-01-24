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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Represents a generic schedule line with a title and values aligned by columns.
 * This is a base class for schedule header lines and timetable lines.
 * 
 * @author Andrei Popleteev
 */
public abstract class ScheduleLine {
    /**
     * Maximum possible length of line name 
     * (empirically found to be 41)
     */
    public static final Integer MAX_LINE_NAME_LENGTH = 41;
    private String lineName;
    private ArrayList<String> values = new ArrayList<String>();
    
    public void addEmptyValues(int finalLength) {
        values.ensureCapacity(finalLength);
        while (values.size()<finalLength)
            values.add("");
    }
    protected String getLineName() {
        return lineName;
    }
    protected void setLineName(String lineName) {
        this.lineName = lineName;
    }
    protected ArrayList<String> getValues() {
        return values;
    }
    @SuppressWarnings("unchecked")
    protected void setValues(ArrayList<String> time) {
        this.values = (ArrayList<String>) time.clone();
    }
    
    /**
     * Parses the raw schedule line using precalculated column positions.
     * 
     * @param rawLine
     * @param columnEnds
     */
    public void parseRawLine(String rawLine, ArrayList<Integer> columnEnds) {
        values.clear();
        if (columnEnds.size()==0)
            return;
        
        String name = StringUtils.safeTrimSubstring(rawLine, 0, columnEnds.get(0));
        name = normalizeLineName(name);
        setLineName(name);
        int start = columnEnds.get(0);
        int end;
        for (int i=1; i<columnEnds.size(); i++) {
            end = columnEnds.get(i);
            values.add(StringUtils.safeTrimSubstring(rawLine, start, end));
            start = end;
        }
    }

    /**
     * Removes redundant characters from the line name (e.g. quotes, extra spaces, etc) 
     * @param name
     * @return
     */
    protected String normalizeLineName(String name) {
        String result = name.replaceAll("\\s{2,}", " ");//remove extra spaces
        result = result.replaceAll("^\"(.+)\"$", "$1");    //remove surrounding quotes
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append(getFixedLengthLineName());
        for (String value: values) {
            //sb.append("*"); 
            sb.append("\t");
            sb.append(value);
        }
        return sb.toString();
    }
    
    /**
     * Returns lineName with added spaces, so that 
     * the result's length is MAX_LINE_NAME_LENGTH.  
     * @return
     */
    private String getFixedLengthLineName() {
        StringBuffer result = new StringBuffer(lineName);
        for (int i=result.length(); i<MAX_LINE_NAME_LENGTH; i++)
            result.append(" ");
        return result.toString();
    }
    
    /**
     * Writes the schedule line data to stream in text format.
     * @param out
     */
    public void saveToStream(OutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        pw.print(getFixedLengthLineName());
        for (String value: values) {
            pw.print("\t");
            pw.print(value.replace('.', ':')); //"." is used only in Italy, the rest of the world uses ":" time separator
        }
        pw.println();
        pw.flush();
    }
    
    /**
     * Writes the schedule line data to stream in binary format.
     * @param out
     * @throws IOException
     */
    public void saveToDataStream(DataOutputStream out) throws IOException {
        out.writeUTF(getLineName());
        for (String value: values) {
            out.writeUTF(value.replace('.', ':')); //"." is used only in Italy, the rest of the world uses ":" time separator
        }
    }
}
