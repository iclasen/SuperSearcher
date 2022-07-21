package me.iclasen.supersearcher.Searcher.FileSearcher;

import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;

import java.util.ArrayList;

public record LineResult(ArrayList<SearchCriteria> searchCriteria, String lineText, Integer lineNumber) {

}
