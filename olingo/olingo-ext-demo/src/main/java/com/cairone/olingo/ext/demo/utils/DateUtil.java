package com.cairone.olingo.ext.demo.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {
	
	public static Date asDate(LocalDateTime localDateTime) {
		return localDateTime == null ? null :  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static LocalDateTime asLocalDateTime(Date date) {
		return date == null ? null :  Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
