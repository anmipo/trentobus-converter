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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Represents a parsed schedule page (or complete schedule), 
 * including route details and timetable.
 * 
 * @author Andrei Popleteev
 */
public class SchedulePage {
    
    private ScheduleDetails details = null; 
    private FrequenzaScheduleLine frequenza;
    private LineaScheduleLine linea;
    private LinkedList<TimeScheduleLine> lines = new LinkedList<TimeScheduleLine>();
//    private ScheduleLegend legend = null;
    
    /**
     * Parses the rawPage, extracting info from header/footer lines,
     * and timetable data. 
     * @return SchedulePage with extracted data.
     */
    public static SchedulePage valueOf(RawSchedulePage rawPage) throws ScheduleConverterException {
        if (rawPage==null || rawPage.size()==0)
            return null;
        
        SchedulePage result = new SchedulePage();
        ArrayList<Integer> colEnds = getColumnEnds(rawPage);
        for (String rawLine: rawPage) {
            TimeScheduleLine timedLine = new TimeScheduleLine();
            timedLine.parseRawLine(rawLine, colEnds);
            result.lines.add(timedLine);
        }
        //Frequenza & Linea are parsed last, when all columns' positions are known
        result.frequenza = new FrequenzaScheduleLine();
        result.linea = new LineaScheduleLine();
        result.frequenza.parseRawLine(rawPage.getFrequenzaLine(), colEnds);
        result.linea.parseRawLine(rawPage.getLineaLine(), colEnds);
        //make all lines have the same number of items
        result.alignValueCounts();
        
        result.details = ScheduleDetails.extractFrom(rawPage);
        return result;
    }
    
    /**
     * Returns the text positions of the right sides of timetable columns.
     * For example:
     *   BusStopName   |  00.00 | 01.15 |  02.30|
     *                         ^       ^       ^
     * This method is flexible for small (+/- 1 symbol) column misalignments between lines.
     * 
     * @param rawPage
     * @return
     */
    private static ArrayList<Integer> getColumnEnds(RawSchedulePage rawPage) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        result.add(ScheduleLine.MAX_LINE_NAME_LENGTH); //add lineName column
        int offset = ScheduleLine.MAX_LINE_NAME_LENGTH;
        for (String line: rawPage) {
            line = StringUtils.safeSubstring(line, offset);
            String tabifiedLine = line.replaceAll("\\s{2,}", "%");
            StringTokenizer tokenizer = new StringTokenizer(tabifiedLine, "%", false);
            result.ensureCapacity(tokenizer.countTokens());
            
            int fromIndex = 0;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int pos = line.indexOf(token, fromIndex);
                fromIndex = pos+token.length();
                int columnEnd = fromIndex+offset;
                if (!result.contains(columnEnd) && !"|".equals(token))
                    fuzzyAdd(result, columnEnd);
            }
        }
        Collections.sort(result);
        return result;
    }

    //if the vector already contains a value similar (+/- 1) to the one being added 
    //- leave the bigger one 
    private static void fuzzyAdd(ArrayList<Integer> vector, int value) {
        int i = vector.indexOf(value-1);
        if (i>=0) 
            vector.set(i, value); //replace the old value with a bigger one
        else
            if (!vector.contains(value+1))
                vector.add(value);
        
    }

    /**
     * Appends empty values to short lines, so that all lines have the same number of values.
     */
    private void alignValueCounts() {
        int max = getMaxValueCount();
        frequenza.addEmptyValues(max);
        linea.addEmptyValues(max);
        for (TimeScheduleLine line: lines)
            line.addEmptyValues(max);
        //Search for and remove all-empty columns
        int col = 0;
        while (col < lines.getFirst().getValues().size()) {
            if (isEmptyValueColumn(col))
                removeValueColumn(col);
            else
                col++;
        }
    }
    
    /**
     * Removes the given timetable column.
     * @param columnIndex
     */
    private void removeValueColumn(int columnIndex) {
        for (TimeScheduleLine line: lines) {
            line.getValues().remove(columnIndex);
        }
        frequenza.getValues().remove(columnIndex);
        linea.getValues().remove(columnIndex);
    }

    /**
     * Checks whether the given column of the timetable is empty. 
     * @param columnIndex
     * @return
     */
    private boolean isEmptyValueColumn(int columnIndex) {
        for (TimeScheduleLine line: lines) {
            String value = line.getValues().get(columnIndex); 
            if (value!=null && value.length()>0)
                return false;
        }
        return true;
    }

    /**
     * Returns the maximum number of columns across all timetable rows.
     * @return
     */
    private int getMaxValueCount() {
        int result = 0;
        for (TimeScheduleLine line: lines) {
            int valSize = line.getValues().size();
            if (valSize>result)
                result = valSize;
        }
        return result;
    }
    
    public void assign(SchedulePage page2) {
        setLines(page2.getLines());
        setFrequenza(page2.getFrequenza());
        setLinea(page2.getLinea());
        details = new ScheduleDetails(page2.getDetails());
    }

    /**
     * Joins the provided page data to the current page.
     * Both pages must have the same line names, otherwise throws an exception.
     */
    protected void joinPage(SchedulePage page2) throws ScheduleConverterException {
        for (int i=0; i<lines.size(); i++) {
            ScheduleLine line1 = lines.get(i);
            ScheduleLine line2 = page2.getLines().get(i);
            if (!line1.getLineName().equals(line2.getLineName()))
                throw new ScheduleConverterException("Line name mismatch while joining schedule pages");
            line1.getValues().addAll(line2.getValues());
        }
        frequenza.getValues().addAll(page2.getFrequenza().getValues());
        linea.getValues().addAll(page2.getLinea().getValues());
        if (page2.details!=null) {
            if (details==null)
                details = new ScheduleDetails(page2.details);
            else
                details.join(page2.details);
        }
/*        //Join legends
        if (page2.getLegend()!=null) {
            if (this.getLegend()!=null)
                this.getLegend().join(page2.getLegend());
            else
                setLegend(page2.getLegend());
        }
*/    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(details.toString());
        sb.append("\n");
        sb.append(frequenza.toString());
        sb.append("\n");
        sb.append(linea.toString());
        sb.append("\n");
        for (TimeScheduleLine schLine: lines) {
            sb.append(schLine.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public FrequenzaScheduleLine getFrequenza() {
        return frequenza;
    }

    public LineaScheduleLine getLinea() {
        return linea;
    }

    public LinkedList<TimeScheduleLine> getLines() {
        return lines;
    }

    protected void setFrequenza(FrequenzaScheduleLine frequenza) {
        this.frequenza = frequenza;
    }

    protected void setLinea(LineaScheduleLine linea) {
        this.linea = linea;
    }

    @SuppressWarnings("unchecked")
    protected void setLines(LinkedList<TimeScheduleLine> lines) {
        this.lines = (LinkedList<TimeScheduleLine>) lines.clone();
    }

    public ScheduleDetails getDetails() {
        return details;
    }

    public void setDetails(ScheduleDetails details) {
        this.details = details;
    }
}
