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
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a complete parsed schedule.
 * @author Andrei Popleteev
 */
public class Schedule extends SchedulePage {

    private Direction direction = Direction.UNDEFINED;

	public Schedule() {
        super();
    }

    /**
     * Joins the info from multiple pages to a single set of schedule lines.
     * Assumes that the busstop names list is the same over all the pages.
     * @param pages
     * @throws ScheduleConverterException
     */
    public void combineFromPages(LinkedList<SchedulePage> pages) 
            throws ScheduleConverterException {
        Iterator<SchedulePage> iter = pages.iterator();
        if (iter.hasNext()) {
            this.assign(iter.next()); //Init current page to be = pages[0]
        }
        while (iter.hasNext())
            joinPage(iter.next());
    }

    /**
     * Saves the schedule a human-readable text format.
     * @param out
     */
    public void saveToStream(OutputStream out) {
        getDetails().saveToStream(out);
        getFrequenza().saveToStream(out);
        getLinea().saveToStream(out);
        for (ScheduleLine line: getLines()) {
            line.saveToStream(out);
        }
    }

    public String getInfoLine() {
        return getDetails().getInfoLine();
    }

    /**
     * Saves the schedule in binary format.
     * @param out
     * @throws IOException
     */
    public void saveToDataStream(DataOutputStream out) throws IOException {
        getDetails().saveToDataStream(out);
        out.writeShort(getFrequenza().getValues().size()+1); //number of columns (+1 for row name);
        out.writeShort(getLines().size()); //number of timed lines (frequenza&linea are excluded)
        getFrequenza().saveToDataStream(out);
        getLinea().saveToDataStream(out);
        for (ScheduleLine line: getLines()) {
            line.saveToDataStream(out);
        }
    }
}
