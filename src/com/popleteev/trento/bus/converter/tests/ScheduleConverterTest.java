package com.popleteev.trento.bus.converter.tests;

import java.io.IOException;

import com.popleteev.trento.bus.converter.Schedule;
import com.popleteev.trento.bus.converter.ScheduleConverter;

public class ScheduleConverterTest {

	/**
	 * Loads the specified schedule and prints it to System.out.
	 * Useful for debug.
	 * @param fileName full name of raw schedule file
	 */
	public static void convertAndPrintSchedule(String fileName) {
    	ScheduleConverter converter = new ScheduleConverter();
		try {
			Schedule sch = converter.loadRawSchedule(fileName);
	    	sch.saveToStream(System.out);
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		convertAndPrintSchedule("c:\\Users\\papliatseyeu\\Documents\\Projects\\eclipse\\ScheduleConverter\\res\\txt\\Summer2011\\Orari di Direttrice - T11E - T-05A - Feriale.txt");
	}
}
