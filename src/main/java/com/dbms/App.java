package com.dbms;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class App {
    private App() {

    }

    public static int attrCount = 0; // Attribute count for tablr
    public static String tableName; // Table name currently looking on
    public static HashMap<String, String> tableVar = new HashMap<>(); // mapping for table name and it's shortName
    public static int tableCount = 0; // table unique index
    public static boolean isInitial = false;// track initial attribute insertion to add load and save table in output
                                            // code

    private static DbEngine dbEngine = DbEngine.CreateInstance();

    /**
     * Removes whitespace from each string in the list. This is useful for cleaning
     * up data that is stored in the database(file)
     * 
     * @param myListStr - List of strings to clean
     */
    public static ArrayList<String> CleanInput(List<String> myListStr) {
        ArrayList<String> myRes = new ArrayList<>();

        for (String mystr : myListStr) {
            myRes.add(mystr.trim());
        }

        return myRes;
    }

    /**
     * Builds command to create table. This is an internationalized version of
     * create_table without quotes. Note that CREATE TABLE does not create columns.
     * 
     * 
     * @return String containing command to create table in internationalized
     *         version of create_table ( tableName ) with quotes
     */
    public static String CreateTableIC() {
        return String.format("create_table(%s)", tableName);
    }

    /**
     * Builds a SQL add_attribute statement for a table. This is used to add an
     * attribute to a table
     * 
     * @param typeString - the type of the attribute
     * @param varName    - the name of the variable to be added
     * 
     * @return a String containing the SQL to add an attribute to a table in the
     *         form add_attribute tableName type name
     */
    public static String AddAttribute(String typeString, String varName) {
        return String.format("add_attribute %s %s %s", tableName, typeString, varName);
    }

    /**
     * Evaluate the strings
     * 
     * @param mystr - String to be evaluated
     */
    public static ArrayList<String> Evaluate(String mystr) throws Exception {
        ArrayList<String> myres = new ArrayList<>();
        // Creates table and inserts into tableVar.
        if (mystr.startsWith("create table")) {
            String rem = mystr.substring(13).trim();
            int spaceIndex = rem.indexOf(" ");
            tableName = rem.substring(0, spaceIndex);
            attrCount = Integer.parseInt(rem.substring(spaceIndex + 1));
            if (spaceIndex == -1) {
                throw new Exception("Tablename not provided");
            }

            myres.add(CreateTableIC());
            tableCount += 1;
            isInitial = true;
            tableVar.put(tableName, String.format("t%d", tableCount));
        
        } else if (mystr.startsWith("int") | mystr.startsWith("float") | mystr.startsWith("date")
                | mystr.startsWith("string")) {

            if (attrCount == 0) {
                throw new Exception("Table column overflow");
            } else {
                attrCount -= 1;
                String typeString;
                String varName = mystr.substring(mystr.indexOf(" ") + 1);

                if (mystr.startsWith("int")) {
                    typeString = "int";

                } else if (mystr.startsWith("float")) {
                    typeString = "float";
                } else if (mystr.startsWith("string")) {
                    typeString = "string";
                } else {
                    typeString = "date";
                }

                if (isInitial) {
                    myres.add(String.format("%s = load_table(%s)", tableVar.get(tableName), tableName));
                    isInitial = false;
                }

                myres.add(AddAttribute(typeString, varName));

                if (attrCount == 0) {
                    myres.add(String.format("save_table(%s)", tableVar.get(tableName)));
                }
            }

        } else if (mystr.startsWith("insert into")) {
            String rem = mystr.substring(12).trim();
            String preTableName = rem.substring(0, rem.indexOf(" "));
            String valueStr = rem.substring(rem.indexOf(" ") + 1);

            if (tableVar.get(preTableName) == null) {
                tableCount += 1;
                tableVar.put(preTableName, String.format("t%d", tableCount));
            }

            myres.add(String.format("%s = load_table(%s)", tableVar.get(preTableName), preTableName));
            myres.add(String.format("insert_into(%s,\"%s\")", tableVar.get(preTableName), valueStr));
            myres.add(String.format("save_table(%s)", tableVar.get(preTableName)));
       
        } else if (mystr.startsWith("select")) {
            String pretableName = mystr.substring(14);
            myres.add(String.format("fetch %s %s", pretableName, mystr.substring(7, 8)));
        } else {
            myres.add(String.format("Error On [%s]", mystr));
        }

        return myres;
    }

    /**
     * Prints a list of strings to console. This is useful for debugging
     * purposes. The list is printed in a format that can be read by {@link #toString()}
     * 
     * @param myList - the list of strings to print
     */
    public static void PrettyPrint(ArrayList<String> myList) {
        for (String myStr : myList) {
            System.out.println(myStr);
        }
    }

    /**
     * Loads content into a file. This method is used to load the content of a text
     * file. The file is written to the file name passed as parameter
     * 
     * @param fileName    - Name of the file to load on
     * @param queryResult - Array list of strings to load
     */
    public static void LoadContent(String fileName, ArrayList<String> queryResult) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            for (String str : queryResult) {
                fileWriter.write(String.format("%s%s", str, GetOSLine()));
            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the line that should be used for the operating system. This is based
     * on the value of System. getProperty ("os.name").
     * 
     * 
     * @return a String that is the line that should be used for the operating
     *         system. If the system is not Windows or Unix the return value is \n
     */
    public static String GetOSLine() {
        String myProp = System.getProperty("os.name");

        // Returns a string that is the string that is the Windows Windows or Unix.
        if (myProp.startsWith("Windows")) {
            return "\n";
            // Returns a newline if the property starts with Unix
        } else if (myProp.startsWith("Unix")) {
            return "\r\n";
        } else {
            return "\n";
        }
    }

    /**
     * Main method. Loads query from file and evaluates it. After
     * evaluation results are written to file using {@link #LoadContent(String, ArrayList)}
     * 
     * @param args - command line arguments not
     */
    public static void main(String[] args) {
        String fileName = "Test.query";
        ArrayList<String> queryResult = new ArrayList<>();

        try {
            List<String> lc = Files.readAllLines(Paths.get(fileName),
                    StandardCharsets.UTF_8);
            lc = CleanInput(lc);
            for (String myStr : lc) {
                queryResult.addAll(Evaluate(myStr));
            }
            LoadContent(String.format("%s.out", fileName), queryResult);
            dbEngine.Evaluate(String.format("%s.out", fileName));
        } catch (Exception e) {
            System.out.printf("Error occured %s", e.toString());
        }
    }
}
