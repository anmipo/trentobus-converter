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
import java.util.LinkedList;

/**
 * Represents a schedule legend (remarks and their descriptions).
 * 
 * @author Andrei Popleteev
 */
public class ScheduleLegend {
    private LinkedList<ScheduleLegendItem> items = new LinkedList<ScheduleLegendItem>();

    public ScheduleLegend() {
        super();
    }
    public ScheduleLegend(ScheduleLegend src) {
        super();
        this.assign(src);
    }

    /**
     * Parses raw text legend, given as one item per line.
     * If there are two columns, use {@link splitTwoColumnLegendLines}
     * 
     * @param rawLegend
     * @return parsed ScheduleLegend
     */
    public static ScheduleLegend valueOf(LinkedList<String> rawLegend) {
        if (rawLegend==null || rawLegend.size()==0)
            return null;

        LinkedList<String> lines = splitTwoColumnLegendLines(rawLegend);
        ScheduleLegend result = new ScheduleLegend();
        for (String line: lines) {
            ScheduleLegendItem item = ScheduleLegendItem.valueOf(line);
            if (null!=item)
                result.items.add(item);
        }
        return result;
    }

    /**
     * Handles the (rare) cases of two-column legend, 
     * by splitting the columns and returning the legend text as one column. 
     * Correctly handles single-column input.
     *  
     * @param rawLegend
     * @return
     */
    private static LinkedList<String> splitTwoColumnLegendLines(LinkedList<String> rawLegend) {
        LinkedList<String> splitLines = new LinkedList<String>();
        for (String line : rawLegend) {
            if (line.length()>ScheduleLine.MAX_LINE_NAME_LENGTH) {
                splitLines.add(line.substring(0, ScheduleLine.MAX_LINE_NAME_LENGTH-1));
                splitLines.add(line.substring(ScheduleLine.MAX_LINE_NAME_LENGTH));
            } else
                splitLines.add(line);
        }
        return splitLines;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nLegend:\n");
        for (ScheduleLegendItem item: items) {
            sb.append(item.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void join(ScheduleLegend legend) {
        items.addAll(legend.getItems());
    }

    protected LinkedList<ScheduleLegendItem> getItems() {
        return items;
    }
/*    public ScheduleLegend clone() {
        ScheduleLegend result = new ScheduleLegend();
        result.items = (Vector<ScheduleLegendItem>) this.items.clone();
        return result;
    }*/

    @SuppressWarnings("unchecked")
    public void assign(ScheduleLegend src) {
        this.items = (LinkedList<ScheduleLegendItem>) src.getItems().clone();
    }
    /**
     * Writes the legend to a stream in text format.
     * @param out
     */
    public void saveToStream(OutputStream out) {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println("Legend");
        for (ScheduleLegendItem item: items) {
            pw.println(String.format("%s\t%s", item.getSymbol(), item.getDescription()));
        }
        pw.flush();
    }
    
    /**
     * Writes the legend to a stream in binary format.
     * @param out
     * @throws IOException
     */
    public void saveToDataStream(DataOutputStream out) throws IOException {
        out.writeByte(items.size()); //write number of items
        for (ScheduleLegendItem item: items) {
            out.writeUTF(item.getSymbol());
            out.writeUTF(item.getDescription());
        }
    }
}
