package me.iclasen.supersearcher.Searcher.FileSearcher;

import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;

import java.util.ArrayList;

public class SearchResult {
    private final String fileName;
    private final String filePath;
    private ArrayList<LineResult> lineResults = new ArrayList<>();

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public ArrayList<LineResult> getLineResults() {
        return lineResults;
    }

    public boolean hasLineResults() {
        return lineResults.size() > 0;
    }

    public void setLineResults(ArrayList<LineResult> lineResults) {
        this.lineResults = lineResults;
    }

    // Default constructor. Always require the file name and path
    public SearchResult(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    // Adds line results to the search result
    public void addLineResults(ArrayList<SearchCriteria> searchCriteria, String lineText, Integer lineNumber) {
        lineResults.add(new LineResult(searchCriteria, lineText, lineNumber));
    }


}
