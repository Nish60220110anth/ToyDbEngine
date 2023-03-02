package com.dbms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DbEngine {

    private ArrayList<String> tableList;// to check whether that table actually exists
    private HashMap<String, String> tableVar; // shortForm <tableName>
    private HashMap<String, TableHandler> shortNameMapTableFileHandler;
    private HashMap<String, String> tableNameToShortName;

    private DbEngine() {
        tableList = new ArrayList<>();
        tableVar = new HashMap<>();
        shortNameMapTableFileHandler = new HashMap<>();
        tableNameToShortName = new HashMap<>();
        LoadAllExistingTables();
    }

    /**
     * Loads all existing tables into the tableList. This is used to make sure that
     * the user doesn't accidentally delete a table
     */
    private void LoadAllExistingTables() {
        try {
            Iterator<Path> iter = Files.walk(Paths.get("./table"), 2).iterator();
            
            while(iter.hasNext()){
                String name = iter.next().toFile().getName();
                if(name.endsWith(".csv")) {
                    tableList.add(name.substring(0,name.length()-4));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create table from string. This method is used for table creation. The string
     * is of form " tableName ( tableName ) "
     * 
     * @param str1 - table name
     */
    private void CreateTable(String str1) throws Exception {
        int index1 = str1.indexOf("(");
        int index2 = str1.indexOf(")");
        // create_table()
        String tableName = str1.substring(index1 + 1, index2);

        // Add a new table to the tableList.
        if (tableList.contains(tableName)) {
            throw new Exception(String.format("Table already exists %s", tableName));
        } else {
            tableList.add(tableName);
            TableHandler.CreateInstance(TableNameToFileName(tableName)).CreateTable(); // no buffer use
        }
    }

    /**
     * Loads and initializes table
     * 
     * @param str1 - String containing table's name
     */
    private void LoadTable(String str1) throws Exception {
        int index1 = str1.indexOf("=");
        String shortName = str1.substring(0, index1 - 1);
        String tableName = str1.substring(index1 + 13, str1.length() - 1);

        if (tableList.contains(tableName)) {
            tableVar.put(shortName, tableName);
            shortNameMapTableFileHandler.put(shortName, TableHandler.CreateInstance(TableNameToFileName(tableName)));
            tableNameToShortName.put(tableName, shortName);
        } else {
            throw new Exception(String.format("Loading non exsisting table %s", tableName));
        }
    }

    /**
     * Saves the table and removes it's intermediate name from the map. This is called when the user saves the table
     * 
     * @param str1 - string containing the table
     */
    private void SaveTable(String str1) {
        // save_table(t1)
        int index1 = str1.indexOf("(");
        int index2 = str1.indexOf(")");
        String shortName = str1.substring(index1 + 1, index2);
        shortNameMapTableFileHandler.get(shortName).Flush();
        tableNameToShortName.remove(tableVar.get(shortName));
        tableVar.remove(shortName);
        shortNameMapTableFileHandler.remove(shortName);
    }

    /**
     * Fetch content from file. This method is used to fetch content from file. The
     * format of the file is : <tableName> <star>
     * 
     * @param str1 - string with table name
     */
    private ArrayList<ArrayList<String>> FetchContent(String str1) throws Exception {
        String remPart = str1.substring(6);
        int index1 = str1.indexOf(" ");
        String tableName = remPart.substring(0, index1);
        String star = remPart.substring(index1 + 1).trim();

        if (star.equals("*")) {
            return TableHandler.CreateInstance(TableNameToFileName(tableName))
                    .GetNRowContentFromFile(-1);
        } else {
            return TableHandler.CreateInstance(TableNameToFileName(tableName))
                    .GetNRowContentFromFile(Integer.parseInt(star));
        }
    }

    /**
     * Converts a table name to a file name. This is used to create CSV file name for
     * the table that will be written to disk.
     * 
     * @param tableName - The name of the table
     * 
     * @return The name of the file to be written to disk
     */
    private String TableNameToFileName(String tableName) {
        return String.format("./table/%s.csv", tableName);
    }

    /**
     * Adds attributes to the table.
     * 
     * @param tableName - Name of the table to which the attributes are to be added
     * @param colNames  - List of column names to be added to the table
     * @param colTypes  - List of column types to be added to the table
     */
    private void AddAttributes(String tableName, ArrayList<String> colNames, ArrayList<String> colTypes) {
        try {
            shortNameMapTableFileHandler.get(tableNameToShortName.get(tableName)).AddColumnDataInBuffer(colNames,
                    colTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluates the commands in the file. This is a utility method to be used by
     * the program that is executed.
     * 
     * @param fileName - Name of the file that contains the commands to execute
     */
    public void Evaluate(String fileName) throws IOException {
        ArrayList<String> commands = (ArrayList<String>) Files.readAllLines(Paths.get(fileName));
        int cindex = 0;

        while (cindex < commands.size()) {
            String command = commands.get(cindex);
            if (command.startsWith("save_table")) {
                System.out.printf("Command :[%s] [%s]\n", command, "save_table");
                SaveTable(command);
                cindex++;
            } else if (command.indexOf("load_table") != -1) {
                try {
                    System.out.printf("Command :[%s] [%s]\n", command, "load_table");
                    LoadTable(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cindex++;
            } else if (command.startsWith("create_table")) {
                try {
                    System.out.printf("Command :[%s] [%s]\n", command, "create_table");
                    CreateTable(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cindex++;
            } else if (command.startsWith("fetch")) {
                System.out.printf("Command :[%s] [%s]\n", command, "fetch");
                try {
                    System.out.printf("Fetch Content [%s]\n", FetchContent(command));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cindex++;
            } else if (command.startsWith("insert_into")) {
                System.out.printf("Command :[%s] [%s]\n", command, "insert_info");
                String remPart = command.substring(12);
                int index1 = remPart.indexOf("\"");
                int index2 = remPart.lastIndexOf("\"");
                String shortName = remPart.substring(0, index1 - 1);
                String values = remPart.substring(index1 + 2, index2 - 1);

                shortNameMapTableFileHandler.get(shortName).AddRowContentInBuffer(values);
                cindex++;
            } else {
                ArrayList<String> colNames = new ArrayList<>();
                ArrayList<String> colTypes = new ArrayList<>();
                String tabName = new String();
                while (true) {
                    if (command.startsWith("add_attribute")) {
                        System.out.printf("Command :[%s] [%s]\n", command, "add_attribute");
                        String remPart = command.substring(14);
                        String tableName = remPart.substring(0, remPart.indexOf(" "));
                        String colType = remPart.substring(tableName.length() + 1, remPart.lastIndexOf(" "));
                        String colName = remPart.substring(remPart.lastIndexOf(" ") + 1);
                        tabName = tableName;

                        colNames.add(colName);
                        colTypes.add(colType);
                        cindex++;
                        command = commands.get(cindex);

                    } else {
                        break;
                    }
                }
                AddAttributes(tabName, colNames, colTypes);
            }
        }
    }

    /**
     * Creates a new instance of the DbEngine.
     * 
     * @return a new instance of the DbEngine ( never null ).
     */
    public static DbEngine CreateInstance() {
        return new DbEngine();
    }

}
