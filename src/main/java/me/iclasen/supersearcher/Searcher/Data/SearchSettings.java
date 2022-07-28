package me.iclasen.supersearcher.Searcher.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import me.iclasen.supersearcher.Searcher.DirectorySearcher.DirectorySearcher;

// Immutable container for search terms
final public class SearchSettings {
    // TODO: look into making an object that contains the list of search criteria, rather storing them as an arraylist
    private final ArrayList<SearchCriteria> searchCriteria;
    private final DirectorySearcher directorySearcher;
    private final Integer maxThreads;

    // Getter functions
    public ArrayList<SearchCriteria> getSearchCriteria() {
        return searchCriteria;
    }
    public Integer getMaxThreads() {
        return maxThreads;
    }

    public DirectorySearcher getDirectorySearcher() {
        return directorySearcher;
    }

    // Generates the SearchSettings
    public SearchSettings(ArrayList<SearchCriteria> searchCriteria, DirectorySearcher directorySearcher, Integer maxThreads) {
        Objects.requireNonNull(searchCriteria);
        Objects.requireNonNull(directorySearcher);
        this.directorySearcher = directorySearcher;
        this.searchCriteria = searchCriteria;
        this.maxThreads = maxThreads;
    }

    // Searches through the given string to determine if any of the search terms appear in it. Returns any results it finds
    public ArrayList<SearchCriteria> getMatchingTerms(String searchString) {
        ArrayList<SearchCriteria> matches = new ArrayList<>();

        // TODO: I believe there is a way you can pre-compile information related to regexes.
        // If that is the case then each search term upon creation and be compiled.
        for (SearchCriteria criteria: searchCriteria) {
            if(criteria.hasCriteriaMatch(searchString)) {
                matches.add(criteria);
            }
        }

        return matches;
    }

    public synchronized File getNextFile() {
        return directorySearcher.getNextFile();
    }

    public synchronized boolean hasMoreFiles() {
        return directorySearcher.hasMoreFiles();
    }

    public int getTotalFilesSearched() {
        return directorySearcher.getTotalFilesSearched();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = true;

        if(o == this) {
            result = true;
        } else if (!(o instanceof SearchSettings)){
            result = false;
        } else {
            SearchSettings compared = (SearchSettings) o;

            if (this.searchCriteria.size() != compared.getSearchCriteria().size()) {
                result = false;
            } else {
                for (SearchCriteria criteria: this.searchCriteria) {
                    if(!compared.getSearchCriteria().contains(criteria)) {
                        result = false;
                        break;
                    }
                }
            }

            result = result && this.directorySearcher.equals(compared.getDirectorySearcher())
                    && this.getMaxThreads().equals(compared.getMaxThreads());
        }
        return result;
    }


}
