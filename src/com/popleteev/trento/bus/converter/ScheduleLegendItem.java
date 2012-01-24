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

/**
 * Represents a single item of a schedule legend, 
 * containing a key symbol and its description.
 * 
 * @author Andrei Popleteev
 */
public class ScheduleLegendItem {
    private static final int SYMBOL_WIDTH = 3;
    
    private String symbol;
    private String description;
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Parses the line to extract a {@link ScheduleLegendItem}.
     * @param line can be null
     * @return parsed ScheduleLegendItem or null if the string is too short or empty.
     */
    public static ScheduleLegendItem valueOf(String line) {
        if (line==null || line.trim().length()<SYMBOL_WIDTH)
            return null;

        line = line.trim();
        ScheduleLegendItem result = new ScheduleLegendItem();
        result.setSymbol(line.substring(0, SYMBOL_WIDTH).trim());
        result.setDescription(line.substring(SYMBOL_WIDTH+1).trim());
        return result;
    }
    public String toString() {
        return String.format("%s\t%s", symbol, description);
    }
}
