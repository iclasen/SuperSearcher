package me.iclasen.supersearcher.Database;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// Handles reading from a database. The exact source doesn't matter since the connection is always passed in. It does
// require the structure to match the definition in resources/setup.sql
// TODO: look into whether indexing is worth it.
public class DatabaseReader {
    // private records types for each table
    private record SearchTermRecord(int termId, int valueId, String term, boolean isRegex, boolean ignoresCase) {

    }
    private record FileRecord(int fileId, String fileName, String filePath) {

    }
    private record FileLineRecord(int lineId, int fileId, String lineText, int lineNumber) {

    }
    private record ResultRecord(int resultId, int termId, int fileId, int lineId) {

    }

    // Constants that control the sorting order
    public static int SORT_ORDER_FILE_LINE_TERM = 1;
    public static int SORT_ORDER_TERM_FILE_LINE = 2;

    // Returns a list of strings, format will be like the following
    // "File name: [FILE_NAME] Line: [LINE_NUMBER] Term: [TERM] Text: [TEXT]"
    // Defaults the sorting to file, line number, term
    public static ArrayList<String> returnSearchMatchesAsStrings(Connection connection) {
        return returnSearchMatchesAsStrings(connection, SORT_ORDER_FILE_LINE_TERM);
    }

    // Returns a list of strings, format will be like the following
    // "File name: [FILE_NAME] Line: [LINE_NUMBER] Term: [TERM] Text: [TEXT]"
    public static ArrayList<String> returnSearchMatchesAsStrings(Connection connection, int sortOrder) {
        ArrayList<String> result = new ArrayList<>();
        String orderBy = "";
        
        if(sortOrder == SORT_ORDER_FILE_LINE_TERM) {
            orderBy = "ORDER BY fi.file_id, fl.line_number, sc.term_id";
        } else if (sortOrder == SORT_ORDER_TERM_FILE_LINE) {
            orderBy = "ORDER BY sc.term_id, fi.file_id, fl.line_number";
        }

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT 'File name: ' || fi.file_name || ' Line: ' || fl.line_number || ' Term: '" +
                            " || sc.term || ' Text: ' || fl.line_text " +
                        "FROM search_term sc, " +
                            "result rs, " +
                            "file fi, " +
                            "file_line fl " +
                        "WHERE sc.term_id = rs.term_id " +
                        "AND rs.file_id = fi.file_id " +
                        "AND rs.line_id = fl.line_id " +
                            orderBy);
            ResultSet rs;

            rs = statement.executeQuery();

            while(rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    //Outputs the results of the search grouped by terms
    public static void outputTermGroupedResults(Connection connection, OutputStreamWriter output) throws IOException {
        String newLineCharacter =  System.getProperty("line.separator");

        for (SearchTermRecord termRecord : getAllTerms(connection)) {
            // loop through all the terms used in the search and write them
            output.write("Term: " + termRecord.term + newLineCharacter);
            for (FileRecord fileRecord : getFilesByTermId(connection, termRecord.termId)) {
                // for each term, loop through all the files that term was found in and write them
                output.write("    File: " + fileRecord.filePath + newLineCharacter);
                for (FileLineRecord lineRecord : getFileLinesByFileIdAndTermId(connection, fileRecord.fileId, termRecord.termId)) {
                    // for each file, loop through all the lines the term was found in and write them
                    output.write("        Line Number: " + lineRecord.lineNumber + " Text: " + lineRecord.lineText + newLineCharacter);
                }
            }
        }
    }

    // retrieves all the terms used in the search
    private static ArrayList<SearchTermRecord> getAllTerms(Connection connection) {
        ArrayList<SearchTermRecord> result = new ArrayList<>();

        try {
            ResultSet resultSet;
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT st.term_id, " +
                            "   st.search_id, " +
                            "   st.term, " +
                            "   st.is_regex, " +
                            "   st.ignores_case " +
                            "FROM search_term st " +
                            "ORDER BY st.term");

            resultSet = statement.executeQuery();

            while(resultSet.next()) {
                result.add(new SearchTermRecord(resultSet.getInt(1),
                        resultSet.getInt(2),
                        resultSet.getString(3),
                        resultSet.getString(4).equalsIgnoreCase("true"),
                        resultSet.getString(5).equalsIgnoreCase("true")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    // Gets file records by giving it a termId and finding all the files that have that termId
    private static ArrayList<FileRecord> getFilesByTermId(Connection connection, int termId) {
        ArrayList<FileRecord> result = new ArrayList<>();

        try {
            ResultSet resultSet;
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT fi.file_id, " +
                        "   fi.file_name, " +
                        "   fi.file_path " +
                        "FROM file fi, " +
                        "   result rs " +
                        "WHERE fi.file_id = rs.file_id " +
                        "AND rs.term_id = ? " +
                        "ORDER BY fi.file_path");

            statement.setInt(1, termId);
            resultSet = statement.executeQuery();

            while(resultSet.next()) {
                result.add(new FileRecord(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    // Gets FileLineRecords by giving it a fileId and finding all the file lines that have that fileId
    private static ArrayList<FileLineRecord> getFileLinesByFileIdAndTermId(Connection connection, int fileId, int termId) {
        ArrayList<FileLineRecord> result = new ArrayList<>();
        try {
            ResultSet resultSet;
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT fl.line_id, " +
                            "   fl.file_id, " +
                            "   fl.line_text, " +
                            "   fl.line_number " +
                            "FROM file_line fl, " +
                            "   result rs " +
                            "WHERE fl.line_id = rs.line_id " +
                            "AND rs.file_id = ? " +
                            "AND rs.term_id = ? " +
                            "ORDER BY fl.line_number");

            statement.setInt(1, fileId);
            statement.setInt(2, termId);

            resultSet = statement.executeQuery();

            while(resultSet.next()) {
                result.add(new FileLineRecord(resultSet.getInt(1),
                        resultSet.getInt(2),
                        resultSet.getString(3),
                        resultSet.getInt(4)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Makes the DatabaseReader fully static
    private DatabaseReader() {

    }
}
