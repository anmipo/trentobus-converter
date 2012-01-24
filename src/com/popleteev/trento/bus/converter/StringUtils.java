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
 * Utility class for string operations.
 * 
 * @author Andrei Popleteev
 */
public class StringUtils {

    /**
     * Returns a substring of the given string, starting form beginIndex.
     * Null or out-of-range parameters result in empty (non-null) output string.
     * Never returns null.
     * 
     * @param s
     * @param beginIndex
     * @return a non-empty substring, if possible, or an empty string otherwise.
     */
    public static String safeSubstring(String s, int beginIndex) {
        if (s==null || beginIndex>s.length())
            return "";
        else
            return s.substring(beginIndex); 
    }
    
    /**
     * Returns a trimmed substring of s, between start and end positions.
     * If the input is null or the positions are out of range, returns an empty string.
     * Never returns null.
     *  
     * @param s
     * @param start
     * @param end
     * @return a non-empty trimmed substring, if possible, or an empty string otherwise.
     */
    public static String safeTrimSubstring(String s, int start, int end) {
        String result = "";
        if (s!=null) {
            end = Math.min(end, s.length());
            if (end>start)
                result = s.substring(start, end).trim();
        }
        return result;
    }

    /**
     * Returns the original string without trailing spaces.
     * 
     * @param line cannot be null.
     * @return
     */
    public static String trimRight(String line) {
        int len = line.length();
        char[] val = line.toCharArray();

        while ((len>0) && (val[len - 1] <= ' ')) {
            len--;
        }
        return (len < line.length()) ? line.substring(0, len) : line;

    }
}
