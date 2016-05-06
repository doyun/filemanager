package ua.nure.doiun.file_manager.util;

import ua.nure.doiun.file_manager.model.FileNode;

import java.util.Comparator;

/**
 * @author Mykyta_Doiun
 */
public class FileNodeComparator implements Comparator<FileNode> {

    @Override
    public int compare(FileNode o1, FileNode o2) {
        if(o1.isDirectory() && !o2.isDirectory()){
            return -1;
        }
        if(!o1.isDirectory() && o2.isDirectory()) {
            return 1;
        }
        return 0;
    }
}
