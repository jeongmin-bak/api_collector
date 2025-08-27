package com.example.externalurl.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DateUtil {
    public static Temporal parseBaseDate(String input) {
        if (input.matches("\\d{8}")) {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else if (input.matches("\\d{6}")) {
            return YearMonth.parse(input, DateTimeFormatter.ofPattern("yyyyMM"));
        } else if (input.matches("\\d{4}")) {
            return Year.parse(input, DateTimeFormatter.ofPattern("yyyy"));
        } else {
            throw new IllegalArgumentException("지원하지 않는 날짜 형식 : " + input);
        }
    }

    public static String changeBasicDateToStrDate(String basicDateFormat, String inputDate) {
        // 반기, 분기, 반월처리 함수
        int yyyy = Integer.parseInt(inputDate.substring(0,4));
        int mm = Integer.parseInt(inputDate.substring(4,6));
        int dd = 0;
        String resDate = String.valueOf(yyyy);

        // S, Q, SM 타입인지 타입을 뽑아낸다.
        Pattern pattern = Pattern.compile("\\d{4}(\\S{1,2})(\\d{1})");
        Matcher matcher = pattern.matcher(basicDateFormat);
        String type = "";
        while (matcher.find()) {
            type = matcher.group(2);
        }

        if (inputDate.matches("^\\d{8}$")) {
            dd = Integer.parseInt(inputDate.substring(6,8));
        }

        if (type.equals("S")) {
            if(mm >= 1 && mm <= 6) {
                resDate += "S1";
            } else if(mm >= 7 && mm <= 12) {
                resDate += "S2";
            } 
        } else if(type.equals("Q")) {
            if(mm >= 1 && mm <= 3) {
                resDate += "Q1";
            } else if(mm >= 4 && mm <= 6) {
                resDate += "Q2";
            } else if(mm >= 7 && mm <= 9) {
                resDate += "Q3";
            } else if(mm >= 10 && mm <= 12) {
                resDate += "Q4";
            }
        } else if(type.equals("SM")) {
            if(mm >= 1 && mm <= 12) {
                if (dd >= 1 && dd <= 15) {
                    resDate += "SM1";
                } else if (dd >= 16 && dd <= 31) {
                    resDate += "SM2";
                }
            } else {
                throw new IllegalArgumentException("잘못된 날짜 입력 방식입니다. : " + mm + "월" + dd + "일");
            }
        }
        return resDate;
    }

    public static String checkDateFormat(String inputDate) {
        if (inputDate.matches("^\\d{4}/\\d{2}/\\d{2}$")) {
            return "yyyy/MM/dd";
        } else if (inputDate.matches("^\\d{4}/\\d{2}/\\d{2}$")) {
            return "yyyy-MM-dd";
        } else if (inputDate.matches("^\\d{8}$")) {
            return "yyyyMMdd";
        } else if (inputDate.matches("^\\d{4}$")) {
            return "yyyy";
        } else if (inputDate.matches("^\\d{6}")) {
            if (isValidDate(inputDate + "01", "yyyyMMdd"))
                return "yyyyMM";
            if (isValidDate(inputDate, "yyMMdd"))
                return "yyMMdd";
            return "yyMMdd";
        }
        return "";
    }

    public static Temporal applyRelative(Temporal base, String expr) {
        if (expr == null || expr.isEmpty()) return base;

        int amount = Integer.parseInt(expr.substring(0, expr.length() - 1));
        char unit = Character.toUpperCase(expr.charAt(expr.length() - 1));

        if (base instanceof LocalDate) {
            return switch (unit) {
                case 'D' -> ((LocalDate) base).plusDays(amount);
                case 'M' -> ((LocalDate) base).plusMonths(amount);
                case 'Y' -> ((LocalDate) base).plusYears(amount);
                default -> throw new IllegalArgumentException("YearMonth는 M 또는 Y 단위만 가능");
            };
        } else if (base instanceof YearMonth) {
            return switch (unit) {
                case 'M' -> ((LocalDate) base).plusMonths(amount);
                case 'Y' -> ((LocalDate) base).plusYears(amount);
                default -> throw new IllegalArgumentException("YearMonth는 M 또는 Y 단위만 가능");
            };
        } else if (base instanceof Year) {
            if (unit == 'Y') return ((Year) base).plusYears(amount);
            throw new IllegalArgumentException("Year는 Y 단위만 가능");
        } else {
            throw new IllegalArgumentException("지원하지 않는 Temporal 타입");
        }
    }

    public static String format(Temporal date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (date instanceof LocalDate) {
            return ((LocalDate) date).format(formatter);
        } else if (date instanceof YearMonth ) {
            return ((YearMonth) date).format(formatter);
        } else if (date instanceof Year) {
            return ((Year) date).format(formatter);
        } else {
            throw new IllegalArgumentException("지원하지 않는 Temporal 타입");
        }
    }

    public static String calculate(String baseDate, String relative, String outputFormat) {
        Temporal parsed = parseBaseDate(baseDate);
        Temporal adjusted = (relative == null || relative.isBlank() || relative.equals("0")) ? parsed : applyRelative(parsed, relative);
        return format(adjusted, outputFormat);
    }

    public static boolean isValidDate(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}