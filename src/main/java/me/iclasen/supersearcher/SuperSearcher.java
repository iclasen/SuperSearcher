package me.iclasen.supersearcher;

import me.iclasen.supersearcher.Database.ConnectionHandler;
import me.iclasen.supersearcher.Database.DatabaseReader;
import me.iclasen.supersearcher.FileIO.SearchSettingsHandler;
import me.iclasen.supersearcher.Searcher.Data.SearchSettings;
import me.iclasen.supersearcher.Searcher.SearcherNexus;

import java.io.*;
import java.time.Duration;
import java.time.Instant;

public class SuperSearcher {

    public static void main(String[] args) {
        String inputFile;

        // TODO: Expand on this, allow flags (only store the line number a result came on, report on all lines/files that
        // have multiple search terms
        if(args.length == 1) {
            inputFile = args[0];
        } else {
            inputFile = "Searches/default.xml";
        }

        performThreadedSearch(SearchSettingsHandler.getSearchSettings(inputFile));
    }

    // Creates a FileReaderNexus and kicks off a search
    private static void performThreadedSearch(SearchSettings searchSettings) {
        SearcherNexus nexus = new SearcherNexus(searchSettings);
        Instant start = Instant.now();


        nexus.performSearch();
        writeResults(nexus.getRunTime(), nexus.getNumberOfFilesSearched());
    }

    // Writes the results
    private static void writeResults(Duration runTime, int totalFilesSearched) {
        ConnectionHandler database = ConnectionHandler.getInstance();

        try {
            FileWriter writer = new FileWriter("output.txt");

            writer.write("Total Runtime: " + runTime.toString() + System.getProperty("line.separator"));
            writer.write("Total Files Searched: " + totalFilesSearched + System.getProperty("line.separator") + System.getProperty("line.separator"));
            DatabaseReader.outputTermGroupedResults(database.getConnection(), writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
