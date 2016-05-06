package ua.nure.doiun.file_manager.model;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Mykyta_Doiun
 */
public class FileNode {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private String fileName;
    private Object fileSize;
    private String fileDate;
    private String filePath;
    private boolean isDirectory;
    private List<FileNode> subNodes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Object getFileSize() {
        return fileSize;
    }

    public void setFileSize(Object fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public List<FileNode> getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(List<FileNode> subNodes) {
        this.subNodes = subNodes;
    }

}
