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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Schedule details, such as bus number, direction, isHoliday flag, 
 * validity dates and schedule legend.
 * Also capable of parsing {@link RawSchedulePage} and extracting these data.
 * 
 * @author Andrei Popleteev
 */
public class ScheduleDetails {

    private static final String HOLIDAY = "FESTIVO";
    private static final String NON_HOLIDAY = "FERIALE";
    private static final String VALIDO_DAL = "Valido dal ";
    private static final int SYMBOLS_BETWEEN_VALIDITY_DATES = 4;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private String busNumber;  // bus number
    private String direction;  // route direction text
    private boolean isHoliday; // workday/holiday flag
    private Date validFrom;    // schedule validity start date
    private Date validTo;      // schedule validity end date
    private ScheduleLegend legend; // schedule legend, containing remarks and their descriptions

    private ScheduleDetails() {
        //left empty, caller must fill the fields
    }
    
    public ScheduleDetails(String busNumber, String direction,
            boolean isHoliday, Date validFrom, Date validTo,
            ScheduleLegend legend) {
        this.busNumber = busNumber;
        this.direction = direction;
        this.isHoliday  = isHoliday ;
        this.validFrom  = validFrom ;
        this.validTo = validTo;
        this.legend = legend;
    }

    public ScheduleDetails(ScheduleDetails details) {
        this.assign(details);
    }

    /**
     * Finds and returns schedule details (bus number, direction, validity)from the page.
     * @param rawPage
     * @return
     * @throws ScheduleConverterException
     */
    public static ScheduleDetails extractFrom(RawSchedulePage rawPage) throws ScheduleConverterException {

        ScheduleDetails result = new ScheduleDetails(); 
        String busNumberAndDirection = rawPage.getBusNumberAndRouteLine();
        result.busNumber = busNumberAndDirection.substring(0, 3).trim();
        result.direction = busNumberAndDirection.substring(3).trim();
        result.direction = result.direction.replaceAll("\\s{2,}", " "); //removing redundant spaces

        String schType = rawPage.getScheduleTypeLine();
        if (schType.endsWith(HOLIDAY))
            result.isHoliday = true;
        else if (schType.endsWith(NON_HOLIDAY))
            result.isHoliday = false;
        else
            throw new ScheduleConverterException("Unrecognized schedule type: "+schType);
        
        String validity = rawPage.getValidityLine();
        ParsePosition startFrom = new ParsePosition(validity.indexOf(VALIDO_DAL)+VALIDO_DAL.length());
        result.validFrom = DATE_FORMAT.parse(validity, startFrom);
        startFrom.setIndex(startFrom.getIndex()+SYMBOLS_BETWEEN_VALIDITY_DATES);
        result.validTo = DATE_FORMAT.parse(validity, startFrom);
        
        //if there are legend lines, convert them too
        LinkedList<String> rawLegend = rawPage.getLegendLines();
        result.legend = ScheduleLegend.valueOf(rawLegend);
        
        return result;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public ScheduleLegend getLegend() {
        return legend;
    }

/*    public ScheduleDetails clone() {
        return new ScheduleDetails();
    }*/

    /**
     * Combines the given schedule details with the current one,
     * filling the missing fields, if possible.
     * The present fields must be equal, otherwise an exception is thrown.
     */
    public void join(ScheduleDetails src) throws ScheduleConverterException {
        if (!this.busNumber.equals(src.busNumber) || !this.direction.equals(src.direction) || 
                this.isHoliday!=src.isHoliday || !this.validFrom.equals(src.validFrom) || 
                !this.validTo.equals(src.validTo))
            throw new ScheduleConverterException("Error joining schedule details");

        this.busNumber = src.busNumber; 
        this.direction = src.direction; 
        this.isHoliday = src.isHoliday; 
        this.validFrom = src.validFrom!=null ? new Date(src.validFrom.getTime()) : null; 
        this.validTo = src.validTo!=null ? new Date(src.validTo.getTime()) : null;
        if (this.legend!=null)
            legend.join(src.legend);
        else 
            legend = src.legend;
    }
    
    public void assign(ScheduleDetails src) {
        this.busNumber = src.busNumber; 
        this.direction = src.direction; 
        this.isHoliday = src.isHoliday; 
        this.validFrom = src.validFrom!=null ? new Date(src.validFrom.getTime()) : null; 
        this.validTo = src.validTo!=null ? new Date(src.validTo.getTime()) : null;  
        if (src.legend!=null)
            this.legend = new ScheduleLegend(src.legend);
        else
            this.legend = null;
    }

    public String toString() {
        return String.format("Schedule for Bus #%s. Direction: %s (%s)\nValid from %s to %s.\n%s", 
                busNumber,
                direction, 
                getIsHolidayString(), 
                DATE_FORMAT.format(validFrom), 
                DATE_FORMAT.format(validTo), 
                legend==null ? "" : legend.toString());
    }

    /**
     * @return "Holiday" or "Workday", depending on isHoliday value.
     */
    String getIsHolidayString() {
        return isHoliday() ? "Holiday" : "Workday";
    }
    /**
     * @return "H" or "W", depending on isHoliday value.
     */
    String getIsHolidayChar() {
        return isHoliday() ? "H" : "W";
    }

    /**
     * Writes schedule data to stream in a human-readable form.
     * @param out
     */
    public void saveToStream(OutputStream out) {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println(getInfoLine());
        pw.print(ScheduleDetails.DATE_FORMAT.format(getValidFrom()) + "\t");
        pw.print(ScheduleDetails.DATE_FORMAT.format(getValidTo()) + "\t");
        pw.println(getValidFrom().getTime() + "\t" +getValidTo().getTime()); //in millis from 1970/1/1
        
        ScheduleLegend legend = getLegend();
        if (legend!=null) {
            legend.saveToStream(out);
        }
        pw.println(); //empty line
        pw.flush();
    }

    /**
     * Writes schedule data to stream in a binary form, suitable for the midlet.
     * @param out
     * @throws IOException
     */
    public void saveToDataStream(DataOutputStream out) throws IOException {
        out.writeUTF(getBusNumber());
        out.writeUTF(getIsHolidayChar());
        out.writeUTF(getDirection());
        out.writeLong(getValidFrom().getTime()); //in millis from 1970/1/1
        out.writeLong(getValidTo().getTime()); 
        ScheduleLegend legend = getLegend();
        if (legend!=null) {
            legend.saveToDataStream(out);
        } else {
            (new ScheduleLegend()).saveToDataStream(out); //saving empty legend, if none 
        }
    }

    /**
     * @return string containing bus number, holiday flag and direction info.
     */
    public String getInfoLine() {
        return getBusNumber() + "\t" + getIsHolidayString() + "\t" + getDirection();
    }
}
