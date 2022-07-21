package me.iclasen.supersearcher.Searcher.FileSearcher;

import me.iclasen.supersearcher.Database.ConnectionHandler;
import me.iclasen.supersearcher.Database.DatabaseWriter;
import me.iclasen.supersearcher.Searcher.Data.SearchSettings;
import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class FileSearcherThread implements Runnable {
    private final File file;
    private final CountDownLatch latch;
    private final SearchSettings searchSettings;
    private final SearchResult result;

    // Constructor for the FileThreadSearcher
    public FileSearcherThread(File file, CountDownLatch latch, SearchSettings searchSettings) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(latch);
        Objects.requireNonNull(searchSettings);
        this.file = file;
        this.latch = latch;
        this.searchSettings = searchSettings;
        result = new SearchResult(file.getName(), file.getPath());
    }

    // Searches through a single file and stores the results
    @Override
    public void run() {
        Integer index = 1;
        ArrayList<SearchCriteria> foundMatches;
        try {
            // TODO: Look into using a FileChannel
            for (String line: Files.readAllLines(file.toPath())) {
                foundMatches = searchSettings.getMatchingTerms(line);
                if(foundMatches.size() > 0) {
                    result.addLineResults(searchSettings.getMatchingTerms(line), line, index);
                }
                index++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(result.hasLineResults()) {
            writeResults();
        }
        latch.countDown();
    }

    // Writes the search results to the database
    private void writeResults() {
        ConnectionHandler database = ConnectionHandler.getInstance();

        DatabaseWriter.writeData(database.getConnection(), result);
    }
}
