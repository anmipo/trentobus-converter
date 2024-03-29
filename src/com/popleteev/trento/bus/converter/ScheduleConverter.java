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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The main class of the converter utility.
 * Converts TXT schedules to binary format used by TrentoBus midlet.
 * TXT files are converted from Trentino Trasporti's PDF schedules using Foxit Reader.
 *  
 * @author Andrei Popleteev
 */
public class ScheduleConverter {
    Log log = LogFactory.getLog(this.getClass());
    
    private static final int MAX_FILE_INDEX = 10;
    private static final String SCHEDULE_FILE_NAME_FORMAT = "%s-%s-%d.dat";
    private static final String BUS_INDEX_FILE_NAME = "bus.idx";
    private static final String BUSSTOP_INDEX_FILE_NAME = "stops.idx";
    private static final String DATES_FILE_NAME = "validity.dat";

    private Schedule schedule = null;
    // validity dates for the whole dataset (intersection of schedules' validity)
    private Date validFrom = null;
    private Date validTo = null;
    
    public static void main(String[] args) {
        if (args.length<2) {
            System.out.println("Schedule converter for TrentoBus project. Version 1.0.");
            System.out.println("Copyright � 2008-2011 Andrei Popleteev\n");
            System.out.println("Takes two parameters:");
            System.out.println("1. input directory with .txt files");
            System.out.println("2. output directory for .dat files.");
        } else {
            String sourceDir = normalizePath(args[0]);
            String targetDir = normalizePath(args[1]);
            ScheduleConverter converter = new ScheduleConverter();
            converter.convertDir(sourceDir, targetDir);
        }
    }

    /**
     * Returns the path with a trailing slash.
     * @param path
     * @return
     */
    private static String normalizePath(String path) {
        return (path.endsWith(File.separator)) ? path : path+File.separator;
    }

    /**
     * Converts all matching files in the sourceDirPath and stores the results into targetDirPath.
     * @param sourceDirPath input folder with .txt files
     * @param targetDirPath output forlder for .dat files
     */
    public void convertDir(String sourceDirPath, String targetDirPath) {
        log.info("Source directory: " + sourceDirPath);
        log.info("Target directory: " + targetDirPath);
        if (createDirectoryIfNotExists(targetDirPath)) {
            log.info("Target directory did not exist, created.");
        }
        
        validFrom = null;
        validTo = null;
        BusStopIndex busStopIndex = new BusStopIndex();
        ScheduleConverter converter = new ScheduleConverter();
        try {
            DataOutputStream dirInfoOutputStream = new DataOutputStream(new FileOutputStream(targetDirPath+BUS_INDEX_FILE_NAME));
            try {
                File srcDir = new File(sourceDirPath);
                
                File[] files = srcDir.listFiles(new FileExtensionFilter(".txt"));
                Arrays.sort(files, 0, files.length, new Comparator<File>() {
					@Override
					public int compare(File f1, File f2) {
						return f1.getName().compareToIgnoreCase(f2.getName());
					}
                });
                
                dirInfoOutputStream.writeShort(files.length); //write number of schedules
                for (int fileIndex=0; fileIndex<files.length; fileIndex++) {
                    String fullFileName = files[fileIndex].getAbsolutePath();
                    Schedule sch = converter.loadRawSchedule(fullFileName);
                    
                    String savedFileName = converter.saveScheduleToDirectory(targetDirPath);
                    updateValidityDates(sch.getDetails());
                    saveDirInfoEntry(dirInfoOutputStream, savedFileName, sch);
                    addAllStopsToIndex(sch, busStopIndex, savedFileName);
                    log.info("Converted: " + files[fileIndex].getName()+" -> "+savedFileName);
                }
                dirInfoOutputStream.flush();
            } finally {
                dirInfoOutputStream.close();
            }
            busStopIndex.saveToFile(targetDirPath + BUSSTOP_INDEX_FILE_NAME);
            saveValidityDates(targetDirPath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void saveValidityDates(String targetDirPath) throws IOException {
    	DataOutputStream validityOutputStream = new DataOutputStream(
    			new FileOutputStream(targetDirPath+DATES_FILE_NAME));
    	try {
    		validityOutputStream.writeLong(validFrom.getTime());
    		validityOutputStream.writeLong(validTo.getTime());
    		
    		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy"); 
    		System.out.println("Validity: " 
    				+ dateFormat.format(validFrom) + " - " 
    				+ dateFormat.format(validTo));
    	} finally {
    		validityOutputStream.close();
    	}
	}

	private void updateValidityDates(ScheduleDetails details) {
		if (validFrom == null) {
			validFrom = details.getValidFrom();
		} else {
			if (validFrom.compareTo(details.getValidFrom()) < 0) {
				validFrom = details.getValidFrom();
			} 
		}
		
		if (validTo == null) {
			validTo = details.getValidTo();
		} else {
			if (validTo.compareTo(details.getValidTo()) > 0) {
				validTo = details.getValidTo();
			}
		}
	}

	/**
     * Ensures that the path exists.
     * @param path
     * @return true if successful, false if there are errors.
     */
    private boolean createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        boolean result = true;
        try {
            dir.mkdirs();
        } catch(SecurityException se) {
            result = false;
            log.error("Cannot create directory: " + path, se);
        }
        return result;
    }

    /**
     * Saves info about the schedule into the index file stream.
     * @param out
     * @param savedFileName
     * @param sch
     * @throws IOException
     */
    private static void saveDirInfoEntry(DataOutputStream out, String savedFileName, Schedule sch) throws IOException {
        out.writeUTF(savedFileName);
        out.writeUTF(sch.getDetails().getBusNumber());
        sch.getDetails().getDirection().writeToDataStream(out);
        out.writeUTF(sch.getDetails().getIsHolidayChar());
        out.writeUTF(sch.getDetails().getRoute());
    }

    /**
     * Adds all stop names from the given schedule to the index.
     * @param sch
     * @param index
     * @param scheduleFileName
     */
    private static void addAllStopsToIndex(Schedule sch, BusStopIndex index, String scheduleFileName) {
    	Direction dir = sch.getDetails().getDirection();
        for (TimeScheduleLine schLine: sch.getLines()) {
        	BusStop busStop = new BusStop(schLine.getBusStopName(), dir);
            index.put(busStop, scheduleFileName);
        }
       }

    /**
     * Saves the current schedule to the specified directory and 
     * returns the short name of the saved file.
     * @param targetDirPath
     * @return
     * @throws IOException
     */
    private String saveScheduleToDirectory(String targetDirPath) throws IOException {
        String result = null;
        if (schedule!=null) {
            File timetableDir = new File(targetDirPath);
            timetableDir.mkdirs();
            String fileName = makeScheduleFileName(schedule, timetableDir);
            File targetFile = new File(fileName);
            result = targetFile.getName();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(targetFile));
            try {
                schedule.saveToDataStream(out);
            } finally {
                out.close();
            }
        }
        return result;
    }

    /**
     * Builds a (full) file name from schedule details.
     * @param schedule - schedule details
     * @param path - path for the file
     * @return full file name, starting with "path".
     */
    public String makeScheduleFileName(Schedule schedule, File path) {
        String result = null;
        ScheduleDetails details = schedule.getDetails();
        if (path!=null && path.isDirectory() && path.exists() && details!=null) {
            String safeBusNumber = replaceInvalidFileNameCharacters(details.getBusNumber());
            String holidayChar = details.getIsHolidayChar();
            for (int index=1; index<MAX_FILE_INDEX; index++) {
                String fileName = String.format(SCHEDULE_FILE_NAME_FORMAT, safeBusNumber, holidayChar, index);
                File testFile = new File(path.getAbsolutePath() + File.separator + fileName);
                if (!testFile.exists()) {
                    result = testFile.getAbsolutePath();
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Replaces invalid characters in a fileName by underscore
     * @param fileName must be non-null
     * @return
     */
    private String replaceInvalidFileNameCharacters(String fileName) {
        return fileName.replaceAll("/", "_"); //only "6/" misbehaves at the moment
    }

    /**
     * Loads single schedule from the given .txt file.
     * @param fileName
     * @return schedule info
     * @throws IOException
     */
    public Schedule loadRawSchedule(String fileName) throws IOException {
        schedule = null;
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            String line;
            LinkedList<String> allLines = new LinkedList<String>(); 
            while ((line=br.readLine())!=null)
                allLines.add(line);
            LinkedList<RawSchedulePage> rawPages = RawSchedulePage.splitToPages(allLines);
            LinkedList<SchedulePage> pages = new LinkedList<SchedulePage>();
            for (RawSchedulePage rawPage: rawPages) {
                SchedulePage page = SchedulePage.valueOf(rawPage);
                pages.add(page);
                //Log.log(page.toString());
            }
            
            schedule = new Schedule();
            schedule.combineFromPages(pages);
            schedule.getDetails().setDirection(Direction.fromFileName(fileName));
            //log.info(schedule.toString());
            
            //Log.log("Done!");
        } catch (ScheduleConverterException e) {
            log.error(e);
        } finally {
            br.close();
        }
        return schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }
    
    private class FileExtensionFilter implements FilenameFilter {

        private String filterExt;
        public FileExtensionFilter(String filterExt) {
            this.filterExt = filterExt; 
        }
        public boolean accept(File dir, String name) {
            return (name!=null && name.endsWith(filterExt));
        }    
    }
}
