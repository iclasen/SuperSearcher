package me.iclasen.supersearcher.Searcher.Data;

import me.iclasen.supersearcher.Database.ConnectionHandler;
import me.iclasen.supersearcher.Database.DatabaseWriter;
import org.apache.commons.lang3.StringUtils;

// Immutable container for the search criteria
public class SearchCriteria {
    private final String term;
    private final Integer termId;
    private final Integer searchId;
    private final boolean isRegex;
    private final boolean ignoreCase;

    // Getters
    public String getTerm() {
        return term;
    }
    public Integer getTermId() {
        return termId;
    }
    public Integer getSearchId() {
        return searchId;
    }
    public boolean isRegex() {
        return isRegex;
    }
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    // public constructor
    public SearchCriteria(String term, Integer searchId, boolean isRegex, boolean ignoreCase) {
        this.term = term;
        this.isRegex = isRegex;
        this.ignoreCase = ignoreCase;
        this.searchId = searchId;

        // writes this search criteria to the database upon creation
        // TODO: I don't know if this is the best time to do this, look into a better way
        ConnectionHandler database = ConnectionHandler.getInstance();

        // the termId is pulled from a sequence on the database
        this.termId = DatabaseWriter.writeTermRecord(database.getConnection(), term, searchId, isRegex, ignoreCase);
    }

    // Determines whether the given string matches this specific criteria
    public boolean hasCriteriaMatch(String input) {
        return (isRegex() && input.matches(getTerm()))
                || (!isRegex() && !isIgnoreCase() && input.contains(getTerm()))
                || (!isRegex() && isIgnoreCase() && StringUtils.containsIgnoreCase(input, getTerm()));
    }

}
