Schedule Converter for TrentoBus, version 1.0
Copyright © 2009 Andrei Popleteev
-------------------------------------------------------------------------------
This is free software: you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.
-------------------------------------------------------------------------------

This utility parses the schedule files published on Trentino Trasporti website 
(http://ttesercizio.it), extracts the schedule data and saves the schedules
in a more manageable binary format.

The procedure:
1. Go to http://ttesercizio.it and download all the PDF schedules 
   for each bus number (multipage files, not per-bus-stop posters).
2. Download a free version of Foxit Reader from http://foxitsoftware.com.
3. Use Foxit Reader to save PDF schedules as TXT files.
4. Run ScheduleConverter: 
   "java -jar converter.jar input/path/to/txt output/path/to/dat"
   
Contact: trentobus@popleteev.com 