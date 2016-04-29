package ua.nure.doiun.file_manager.model;

import java.util.Date;
import java.util.List;

/**
 * @author Mykyta_Doiun
 */
public class FileNode {

    private String fileName;
    private Object fileSize;
    private Date fileDate;
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

    public Date getFileDate() {
        return fileDate;
    }

    public void setFileDate(Date fileDate) {
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
