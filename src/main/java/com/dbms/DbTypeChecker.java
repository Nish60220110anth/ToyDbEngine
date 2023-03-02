package com.dbms;

public class DbTypeChecker {
    /**
    * Checks if a string is a float. This is useful for checking if a string is in the format " 0. 0 "
    * 
    * @param str - The string to check
    */
    public static boolean IsFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
    * Checks if a string is an integer. This is used to check if a string is a string of integer values
    * 
    * @param mystr - string to check if it is int
    */
    public static boolean IsInt(String mystr) {
        try {
            Integer.parseInt(mystr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
    * Checks if a string is a date. This is a case insensitive check so we don't have to worry about locale.
    * 
    * @param mystr - String to check for date format ( DD - MMM - YYYY )
    */
    public static boolean IsDate(String mystr) {
        // 06-Nov-2020
        return mystr.matches("\\d{1,2}-.{3}-\\d{4}");
    }
}
