package ua.onpu.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import ua.onpu.service.DateParser;

import java.text.ParseException;
import java.util.Date;

@Service
public class DateParseImpl implements DateParser {
    @Override
    public Date parse(String date) throws ParseException {
        return DateUtils.parseDate(date,
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
    }
}
