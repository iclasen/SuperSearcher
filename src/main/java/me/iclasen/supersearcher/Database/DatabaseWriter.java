package me.iclasen.supersearcher.Database;

import me.iclasen.supersearcher.Searcher.FileSearcher.LineResult;
import me.iclasen.supersearcher.Searcher.FileSearcher.SearchResult;
import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseWriter {
    // Writes a search term record
    public static Integer writeTermRecord(Connection connection, String searchTerm, Integer searchId, boolean isRegex, boolean ignoreCase) {
        Integer termId = getNextTermId(connection);
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO search_term (term_id, search_id, term, is_regex, ignores_case) VALUES (?, ?, ?, ?, ?)");

            statement.setInt(1, termId);
            statement.setInt(2, searchId);
            statement.setString(3, searchTerm);
            statement.setString(4, String.valueOf(isRegex));
            statement.setString(5, String.valueOf(ignoreCase));
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return termId;
    }

    // Writes information about a search result
    public static void writeData (Connection connection, SearchResult result) {
        Integer fileId;
        fileId = writeFileData(connection, result.getFileName(), result.getFilePath());
        writeLineResultsData(connection, fileId, result.getLineResults());
    }

    // Writes the file data
    public static Integer writeFileData(Connection connection, String fileName, String filePath) {
        try {
            // Write the
            Integer fileId = getNextFileId(connection);

            PreparedStatement statement = connection.prepareStatement("INSERT INTO file (file_id, file_name, file_path) VALUES (?, ?, ?)");

            statement.setInt(1, fileId);
            statement.setString(2, fileName);
            statement.setString(3, filePath);

            statement.executeUpdate();

            return fileId;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    // Writes the line results
    public static void writeLineResultsData(Connection connection, Integer fileId, ArrayList<LineResult> results) {
        try {
            Integer fileLineId;
            PreparedStatement fileLineStatement = connection.prepareStatement("INSERT INTO file_line (line_id, file_id, line_text, line_number) VALUES (?, ?, ?, ?)");
            PreparedStatement resultStatement = connection.prepareStatement("INSERT INTO result (result_id, term_id, file_id, line_id) VALUES (NEXT VALUE FOR sq_result, ?, ?, ?)");
            // Add all the lines that have results as a batch
            for (LineResult result: results) {
                fileLineId = getNextFileLineId(connection);
                fileLineStatement.setInt(1, fileLineId);
                fileLineStatement.setInt(2, fileId);
                // TODO: figure out a better way to limit the length of the string besides hardcoding
                if(result.lineText().length() > 1000) {
                    fileLineStatement.setString(3, result.lineText().substring(0, 999));
                } else {
                    fileLineStatement.setString(3, result.lineText());

                }
                fileLineStatement.setInt(4, result.lineNumber());

                fileLineStatement.addBatch();

                // Add all of th search terms for a line as a batch
                for (SearchCriteria term: result.searchCriteria()) {
                    resultStatement.setInt(1, term.getTermId());
                    resultStatement.setInt(2, fileId);
                    resultStatement.setInt(3, fileLineId);

                    resultStatement.addBatch();
                }
            }

            fileLineStatement.executeBatch();
            resultStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get the next sequence value for the search_term table
    private static Integer getNextTermId(Connection connection) {
        return getNextSequenceValue(connection, "sq_search_term");
    }

    // Get the next sequence value for the file table
    private static Integer getNextFileId(Connection connection) {
        return getNextSequenceValue(connection, "sq_file");
    }

    // Get the next sequence value for the file_line table
    private static Integer getNextFileLineId(Connection connection) {
        return getNextSequenceValue(connection, "sq_file_line");
    }

    // Gets the next value from the passed in sequence. This is only necessary so the result can be passed easily up the
    // stack to the calling procedures.
    private static Integer getNextSequenceValue(Connection connection, String sequenceName) {
        int result = -1;
        try {
            // I don't like doing dynamic SQL like this, but this should be relatively safe since this not exposed to any public procedures
            PreparedStatement statement = connection.prepareStatement("SELECT NEXT VALUE FOR " + sequenceName + " FROM dual");
            ResultSet rs;

            rs = statement.executeQuery();

            if(rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Makes the DatabaseWriter fully static
    private DatabaseWriter() {

    }
}
