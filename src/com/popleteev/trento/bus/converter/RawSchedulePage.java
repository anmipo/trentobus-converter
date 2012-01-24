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

import java.util.LinkedList;

/**
 * Represents schedule file page (as seen in PDFs).
 * Performs some basic preprocessing by identifying and extracting 
 * special header and footer lines (Frequenza, Linea, Note di corsa)
 * @author Andrei Popleteev
 */
public class RawSchedulePage extends LinkedList<String> {
    private static final String HEADER_FREQUENZA = "Frequenza";
    private static final String HEADER_LINEA = "Linea";
    private static final String FOOTER = "www.eureka.ra.it";
    private static final String FOOTER_FREQUENZE = "Frequenze";
    private static final String FOOTER_NOTE = "Note di corsa";
    private String frequenzaLine = null;
    private String lineaLine = null;
    private String scheduleTypeLine = null;
    private String validityLine = null;
    private String busNumberAndRouteLine = null;
    private LinkedList<String> legendLines = null;

    public static LinkedList<RawSchedulePage> splitToPages(LinkedList<String> rawScheduleText) {
        LinkedList<RawSchedulePage> result = new LinkedList<RawSchedulePage>();
        RawSchedulePage page = new RawSchedulePage();
        boolean inPage = false;
        boolean inLegend = false;
        int headerLineNumber = 0;
        for (String line: rawScheduleText) {
            line = StringUtils.trimRight(line);
            if (line.length()==0)
                continue;
            
            String trimLine = line.trim();
            if (headerLineNumber==0) {
                //skipping "--- Page NN ---" separator
                headerLineNumber = 1;
            } else if (headerLineNumber==1 && trimLine.startsWith("ORARIO")) {
                page.scheduleTypeLine = trimLine;
                headerLineNumber++;
            } else if (headerLineNumber==2 && trimLine.startsWith("ORARIO Trento")) {
                page.validityLine = trimLine;
                headerLineNumber++;
            } else if (headerLineNumber==3) {
                page.busNumberAndRouteLine = trimLine;
                headerLineNumber++;
            } else if (line.startsWith(HEADER_FREQUENZA)) {
                inPage = true;
                inLegend = false;
                page.setFrequenzaLine(line);
            } else if (line.startsWith(HEADER_LINEA)) {
                page.setLineaLine(line);
            } else if (line.startsWith(FOOTER)) {
                inPage = false;
                result.add(page);
                page = new RawSchedulePage();
                headerLineNumber = 0;
            } else if (line.startsWith(FOOTER_FREQUENZE) || line.startsWith(FOOTER_NOTE)) {
                //Document end, legend found - skipping it
                inLegend = true;
                page.legendLines = new LinkedList<String>();
            } else if (inPage && line.length()>0) {
                if (inLegend)
                    page.legendLines.add(line);
                else
                    page.add(line);
            }
        }
        return result;
    }
    
    private void setFrequenzaLine(String line) {
        frequenzaLine = line;
    }

    private void setLineaLine(String line) {
        lineaLine = line;
    }

    public String getFrequenzaLine() {
        return frequenzaLine;
    }
    public String getLineaLine() {
        return lineaLine;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getFrequenzaLine());
        sb.append("\n");
        sb.append(getLineaLine());
        sb.append("\n");
        for (String rawLine: this) {
            sb.append(rawLine);
            sb.append("\n");
        }
        return sb.toString();
    }

    public LinkedList<String> getLegendLines() {
        return legendLines;
    }
    
    public String getScheduleTypeLine() {
        return scheduleTypeLine;
    }

    public String getValidityLine() {
        return validityLine;
    }

    public String getBusNumberAndRouteLine() {
        return busNumberAndRouteLine;
    }
}
