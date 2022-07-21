package me.iclasen.supersearcher.Searcher;

import me.iclasen.supersearcher.Searcher.Data.SearchSettings;
import me.iclasen.supersearcher.Searcher.FileSearcher.FileSearcherThread;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

// Handles the running of the search
public class SearcherNexus {
    final private SearchSettings searchSettings;
    private Duration runTime;

    public Duration getRunTime() {
        return runTime;
    }

    public int getNumberOfFilesSearched() {
        return searchSettings.getTotalFilesSearched();
    }

    // Initiates the search and handles all the threading
    public void performSearch() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(8, searchSettings.getMaxThreads(), 50, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch latch = new CountDownLatch(1);
        Instant start = Instant.now();
        // Continue to process/wait as long as there are file remaining to process or there are threads still running
        while (searchSettings.hasMoreFiles() || executor.getActiveCount() > 0) {
            if (executor.getActiveCount() < searchSettings.getMaxThreads() && searchSettings.hasMoreFiles()) {
                // if there are more files to process, and we are not at the thread limit, kick off another thread.
                executor.submit(new FileSearcherThread(searchSettings.getNextFile(), latch, searchSettings));
            } else {
                // We are either at the thread limit or out of files and waiting for threads to finish.
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // The latch is interrupted
                }
            }
        }
        runTime = Duration.between(start, Instant.now());
        // Clean up the thread pool
        executor.shutdown();
    }

    // FileSearchNexus constructor that takes in all the values needed for the search
    public SearcherNexus(SearchSettings searchSettings) {
        this.searchSettings = searchSettings;
    }
}
