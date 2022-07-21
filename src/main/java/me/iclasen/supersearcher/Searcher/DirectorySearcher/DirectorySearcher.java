package me.iclasen.supersearcher.Searcher.DirectorySearcher;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

// Handles searching through directories to get the next file to perform a search on
public class DirectorySearcher {
    private final Queue<File> currentFiles = new LinkedList<>();
    // I haven't done the math, but I believe doing a depth first search will generally keep this amount of memory used
    // by this lower. Very wide or very deep folder structures will definitely affect this.
    private final Stack<File> currentDirectories = new Stack<>();
    // TODO: This can be used to do get the total number of files that will be checked before actually running the search
    // I also may want to use this for when multiple roots are passed in to verify that none of the roots are a folder
    // within one of the other roots.
    private final ArrayList<String> rootDirectories;
    private final Integer maxFilesToPrefetch;
    private final SearchRestrictor restrictor;
    private int totalFilesSearched;

    // Getters
    public int getTotalFilesSearched() {
        return totalFilesSearched;
    }

    // Returns the next file to search.
    public synchronized File getNextFile() {
        File returnFile;

        if(currentFiles.size() > 0) {
            // If there are still files in the queue, return the next one
            returnFile = currentFiles.remove();
            totalFilesSearched++;
        } else {
            // If there are no files in the queue, attempt to continue searching the un-searched directories and push
            // files into the queue from there.
            searchNextDirectory();
            if(currentFiles.size() > 0) {
                // More files were found
                returnFile = currentFiles.remove();
                totalFilesSearched++;
            } else {
                // No more files were found and there are no remaining un-searched directories
                returnFile = null;
            }
        }

        return returnFile;
    }

    // Determine if there are more files to be searched
    public synchronized boolean hasMoreFiles() {
        boolean result;

        if(currentFiles.size() > 0) {
            // If there are files there are more files to search. (Obviously)
            result = true;
        } else if (currentDirectories.size() > 0) {
            // If there are not any files currently loaded but there are more directories there might be more files
            // we have to check after the new search runs, if it still lists no files then all directories and
            // files have been searched
            searchNextDirectory();
            result = currentFiles.size() > 0;
        } else {
            // There are no files or directories remaining
            result = false;
        }

        return result;
    }

    // Searches the next directory and adds the files and folders in it to the list of locations to search
    public synchronized void searchNextDirectory() {
        // Go through directories until there are at least enough files found for the max number of
        // threads this is to prevent issues with loading too many files into memory. this generally
        // shouldn't be an issue but this should handle extreme cases.
        while (currentFiles.size() <= maxFilesToPrefetch && currentDirectories.size() > 0) {
            // Get the next directory to search and add all of its files to the list of files and
            // the subdirectories to the list of directories to search. With the current implementation
            // this is a breadth first search
            for (File file : currentDirectories.pop().listFiles()) {
                // Checks to see if the file either appears on any allow or block lists
                if (restrictor.fileIsAllowed(file)) {
                    if(file.isDirectory()) {
                        currentDirectories.push(file);
                    } else {
                        currentFiles.add(file);
                    }
                }
            }
        }
    }

    // Constructor that takes multiple root directories
    public DirectorySearcher(ArrayList<String> rootDirectories, Integer maxFilesToPrefetch, SearchRestrictor restrictor) {
        this.rootDirectories = rootDirectories;
        for (String directory:rootDirectories) {
            currentDirectories.add(new File(directory));
        }
        this.maxFilesToPrefetch = maxFilesToPrefetch;
        this.restrictor = restrictor;
        this.totalFilesSearched = 0;
    }

}
