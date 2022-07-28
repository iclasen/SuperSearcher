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

    @Override
    public boolean equals(Object o) {
        boolean result = true;

        if(o == this) {
            result = true;
        } else if (!(o instanceof SearchRestrictor)){
            result = false;
        } else {
            SearchRestrictor compared = (SearchRestrictor) o;

            if(this.allowNameList.size() != compared.allowNameList.size()
                    || this.blockNameList.size() != compared.blockNameList.size()
                    || this.allowExtensionList.size() != compared.allowExtensionList.size()
                    || this.blockExtensionList.size() != compared.blockExtensionList.size()) {
                result = false;
            } else {
                for (String directory : this.allowNameList) {
                    if(!compared.allowNameList.contains(directory)) {
                        result = false;
                        break;
                    }
                }
                for (String directory : this.blockNameList) {
                    if(!compared.blockNameList.contains(directory)) {
                        result = false;
                        break;
                    }
                }
                for (String directory : this.allowExtensionList) {
                    if(!compared.allowExtensionList.contains(directory)) {
                        result = false;
                        break;
                    }
                }
                for (String directory : this.blockExtensionList) {
                    if(!compared.blockExtensionList.contains(directory)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

}
