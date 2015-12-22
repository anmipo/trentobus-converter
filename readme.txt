Schedule Converter for TrentoBus, version 1.0
Copyright © 2009-2015 Andrei Popleteev

-------------------------------------------------------------------------------
This is free software: you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.
-------------------------------------------------------------------------------

This utility parses the schedule files published on Trentino Trasporti website 
(http://ttesercizio.it), extracts the schedule data and saves the schedules
in a more manageable binary format.

The procedure:
1. In "res/pdf/schedule-pdfs-trento.txt" replace all entries of "T15I" with appropriate 
   year and season (Inverno/Estivo). E.g. "T17E" for 2017 summer schedules.
   The file contains brute-force paths to schedule PDFs for buses 1-17, A-D, NP.
   Add new entries if required (Funivia/cable car is a special case, see below).
2. Download schedule files:
   "wget --random-wait -i schedule-pdfs-trento.txt"  (or whatever is the new file)
3. Download the free Foxit PDF Reader (http://foxitsoftware.com)
4. Use Foxit Reader to save PDF schedules as TXT files (one pdf -> one txt).
   Coverting 60+ files is rather boring and time-consuming, so open many PDFs
   simultaneously and let Ctrl-Shift-S be your friend.
   After conversion, move *.txt to "res/txt" folder.
   Note: In 2009, Foxit Reader was the only PDF utility which could preserve
         PDF table columns after conversion to TXT. This schedule converter
         will probably not work with other PDF to TXT converters.
5. Trento cable car (funivia) timetable has a different PDF format, but luckily
   these data rarely change. So just manually update the dates in two files:
   "res/txt/OraridiDirettrice-*-T-zFVA.txt"
   Update the year in the file names and put them with the other TXT schedules.
5. Run ScheduleConverter: 
   "java -Dfile.encoding=UTF8 -jar converter.jar input/path/to/txt/ output/path/to/dat/"
   IMPORTANT: The converter DOES NOT replace files in the output folder, it just
   adds new ones. So make sure the output folder is empty before conversion.
6. If there are any error messages, try to fix the offending .txt manually.
   Starting from 2014, Foxit Reader sometimes splits one table row into two.
   The converter will alert you about the error, so just fix it quickly in 
   any text editor.