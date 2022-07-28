package me.iclasen.FileIO;

import me.iclasen.supersearcher.FileIO.SearchSettingsHandler;
import me.iclasen.supersearcher.Searcher.Data.SearchCriteria;
import me.iclasen.supersearcher.Searcher.Data.SearchSettings;
import me.iclasen.supersearcher.Searcher.DirectorySearcher.DirectorySearcher;
import me.iclasen.supersearcher.Searcher.DirectorySearcher.SearchRestrictor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSearchSettingsHandler {
    // TODO: verify how relative paths work for this
    private final static String TEST_SEARCH_SETTINGS_FILE = "src/test/resources/test.xml";
    private final static int TEST_NUM_THREADS = 21;

    @Test
    void testGetSearchSettings() {
        // TODO: Implement equals for the SearchSettings object
        assertEquals(getExpectedSearchSettings(), SearchSettingsHandler.getSearchSettings(TEST_SEARCH_SETTINGS_FILE));
    }

    private SearchSettings getExpectedSearchSettings() {
        return new SearchSettings(getExpectedSearchCriteria(), getExpectedDirectorySearcher(), TEST_NUM_THREADS);
    }

    private ArrayList<SearchCriteria> getExpectedSearchCriteria() {
        ArrayList<SearchCriteria> result = new ArrayList<>();

        result.add(new SearchCriteria("floppy", 0, false, true));
        result.add(new SearchCriteria("banana", 1, false, false));
        result.add(new SearchCriteria("spider", 2, false, true));
        result.add(new SearchCriteria("wobbly", 3, false, false));
        result.add(new SearchCriteria("flabbergasted", 4, false, false));
        result.add(new SearchCriteria("gastrointestinal", 5, false, false));
        result.add(new SearchCriteria("^regex", 6, true, false));
        result.add(new SearchCriteria("^Dystopian", 7, true, true));

        return result;
    }

    private DirectorySearcher getExpectedDirectorySearcher() {
        return new DirectorySearcher(new ArrayList<>(Arrays.asList("C:\\NOT\\A\\REAL\\DIRECTORY", "C:\\NOT\\REAL")),
                TEST_NUM_THREADS,
                getExpectedSearchRestrictor());
    }

    private SearchRestrictor getExpectedSearchRestrictor() {
        return new SearchRestrictor(new ArrayList<>(Arrays.asList("ALLOWED_NAME", "ALLOWED.txt")),
                new ArrayList<>(Arrays.asList("BLOCKED_NAME", "BLOCKED.txt")),
                new ArrayList<>(Arrays.asList(".ALLOW", ".ALLOWNOPERIOD")),
                new ArrayList<>(Arrays.asList(".BLOCK", ".BLOCKNOPERIOD")));
    }
}
