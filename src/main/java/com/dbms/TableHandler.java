package com.dbms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
 * MyTemp is a auxilary class to store the column names and column types to load in file
 * Pair in javafx is not used becoz i don't want to use any fx related libs
 */
class MyTemp {
    public ArrayList<String> colNames;
    public ArrayList<String> colTypes;

    // Sets the list of column names and column types.
    public MyTemp(ArrayList<String> colNames, ArrayList<String> colTypes) {
        this.colNames = colNames;
        this.colTypes = colTypes;
    }
}

public class TableHandler {

    private File tableFileHandle;
    private String tableFileName;
    private ArrayList<String> internalBuffer;

    private TableHandler() {
        internalBuffer = new ArrayList<>();
    }

    /**
     * Creates instance of TableHandler and sets file name. This method is used to
     * create table handler. The fileName is used to store the table file name
     * 
     * @param fileName - name of the file to store the table
     * 
     * @return new instance of TableHandler and set file name and file handle to the
     *         file that contains the table file
     */
    public static TableHandler CreateInstance(String fileName) {
        TableHandler myRes = new TableHandler();
        File temp = new File(fileName);
        myRes.tableFileName = fileName;
        myRes.tableFileHandle = temp;
        return myRes;
    }

    /**
     * Creates the table if it doesn't exist. Throws an exception if the table
     * already exists in the /table folder
     * in working directory
     */
    public void CreateTable() throws Exception {
        if (this.tableFileHandle.exists()) {
            throw new Exception(String.format("File [%s] already exsists", tableFileName));
        } else {
            this.tableFileHandle.createNewFile();
        }
    }

    /**
     * Adds data to the buffer. This method can be used to add data to the buffer
     * for a given set of column names and types in
     * {@link #tableFileName}
     * 
     * @param colNames - List of column names to add to the buffer
     * @param colTypes - List of column types to add to the buffer
     */
    public void AddColumnDataInBuffer(ArrayList<String> colNames, ArrayList<String> colTypes) throws Exception {
        System.out.printf("[AddColumnDataInBuffer] %s content added to buffer\n", tableFileName);
        _addContentToBuffer(_convertToCsvFormat(_joinNameType(colNames, colTypes)));
    }

    /**
     * Join name and type. This is used to create row content for table file . For
     * example , int type and a name will be saved as a:int
     * 
     * @implNote length of column name and column type must be same
     *
     * @param colNames - List of column names.
     * @param colType  - List of column types.
     * 
     * @return Returns the row content for the column names and column types
     */
    private ArrayList<String> _joinNameType(ArrayList<String> colNames, ArrayList<String> colType) {
        ArrayList<String> myRes = new ArrayList<>();

        // Add the column names and type to the res.
        for (int i = 0; i < colNames.size(); i++) {
            myRes.add(String.format("%s:%s", colNames.get(i), colType.get(i)));
        }

        return myRes;
    }

    /**
     * Adds row content to buffer. Buffer is stored in variable
     * {@link #internalBuffer}
     * 
     * @param rowValue - value to add to buffer
     */
    public void AddRowContentInBuffer(String rowValue) {
        System.out.printf("[AddRowContentInBuffer] %s row content added to buffer\n", tableFileName);
        _addContentToBuffer(rowValue);
    }

    /**
     * Read a N row contents from the table.
     * 
     * @param len - number of lines to read from file. - 1 means all
     *
     * @return returns lines from the table
     */
    public ArrayList<ArrayList<String>> GetNRowContentFromFile(int len) throws Exception {
        ArrayList<ArrayList<String>> myRes = new ArrayList<>();
        System.out.printf("[GetNRowContentFromFile] called on %s with len %d\n", tableFileName, len);
        try {
            ArrayList<String> myres1;
            if (len == -1) {
                myres1 = (ArrayList<String>) Files.readAllLines(Paths.get(tableFileName));
                myres1.remove(0);
            } else {
                ArrayList<String> myTemp = (ArrayList<String>) Files.readAllLines(Paths.get(tableFileName));
                if (myTemp.size() < len) {
                    throw new Exception(String.format("Index out of bound on [%s] len %d requested %d", tableFileName,
                            myTemp.size(), len));
                } else {
                    List<String> myTemp2 = myTemp.subList(1, len + 1);
                    ArrayList<String> myReq = new ArrayList<>();
                    for (String s1 : myTemp2) {
                        myReq.add(s1);
                    }

                    myres1 = myReq;
                }
            }
            for (String r1 : myres1) {
                myRes.add(_splitString(r1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myRes;
    }

    /**
     * Splits a string into an array. This is a helper method to split a string row
     * content into individual value
     * 
     * @param str - String to be split
     *
     * @return returns the list of values that produced by splitting the parameter
     *         passed
     */
    private ArrayList<String> _splitString(String str) {
        ArrayList<String> mystr = new ArrayList<>();
        String[] mystr1 = str.split(",", 0);
        // Add all the characters in the string to the string.
        for (int i = 0; i < mystr1.length; i++) {
            mystr.add(mystr1[i]);
        }

        return mystr;
    }

    /**
     * Flush the content to table file and empty the buffers
     */
    public void Flush() {
        System.out.printf("[Flush] %s flush content\n", tableFileName);
        for (String str1 : internalBuffer) {
            _addContentToFile(str1 + "\n");
        }

        internalBuffer = new ArrayList<>();// don't know how to empty it
    }

    /**
     * Converts ArrayList of Strings to CSV format. This method is used to convert
     * ArrayList of Strings to CSV format
     * 
     * @param values - ArrayList of Strings to produce csv for
     * 
     * @return String of comma separated values for CSV appened Ex. (values
     *         [0],values [1])
     */
    private String _convertToCsvFormat(ArrayList<String> values) {
        String myRes = new String();
        for (String mystr : values) {
            myRes = myRes + mystr + ",";
        }
        return myRes.substring(0, myRes.length() - 1);
    }

    /**
     * Adds content to file. This method is used to add content to file. If file
     * already exists it will be appended
     * 
     * @param str - String to be added
     */
    private void _addContentToFile(String str) {
        try {
            FileWriter fileWriter = new FileWriter(tableFileHandle, true);
            fileWriter.write(str);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds content to buffer.
     * 
     * @param value - the value to add to the buffer
     */
    private void _addContentToBuffer(String value) {
        internalBuffer.add(value);
    }
}
