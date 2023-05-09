package ua.onpu.service;

import java.text.ParseException;
import java.util.Date;

public interface DateParser {

    Date parse(String date) throws ParseException;

}
