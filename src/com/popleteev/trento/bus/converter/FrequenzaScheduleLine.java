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

/**
 * DTO for schedule header "Frequenza" 
 * @author Andrei Popleteev
 */
public class FrequenzaScheduleLine extends ScheduleLine {
    public ArrayList<String> getValues() {
        return super.getValues();
    }
    public void setTimes(ArrayList<String> values) {
        super.setValues(values);
    }
}
