package me.iclasen.supersearcher.Searcher.DirectorySearcher;

import java.io.File;
import java.util.ArrayList;

public record SearchRestrictor(ArrayList<String> allowNameList, ArrayList<String> blockNameList,
                               ArrayList<String> allowExtensionList, ArrayList<String> blockExtensionList) {

    // Determines if the given file matches the restrictions of the SearchRestrictor.
    public boolean fileIsAllowed(File file) {
        String fileName = file.getName();

        return !((file.isFile() && !((allowExtensionList.size() == 0 || sharesExtension(allowExtensionList, fileName)) && (blockExtensionList.size() == 0 || !sharesExtension(blockExtensionList, fileName))))
                || !((allowNameList.size() == 0 || allowNameList.contains(fileName)) && (blockNameList.size() == 0 || !blockNameList.contains(fileName))));
    }

    // Checks to see if the given fileName's extension matches any found in the given extensionList.
    // This is needed to handle extensions like .tar.gz or any other similar cases that the extension has multiple periods
    private boolean sharesExtension(ArrayList<String> extensionList, String fileName) {
        boolean result = false;
        for (String extension : extensionList) {

            if (fileName.endsWith(extension)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
