package ua.nure.doiun.file_manager.explorer.impl.local;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.common.util.StringUtils;
import ua.nure.doiun.file_manager.explorer.FileTreeExplorer;
import ua.nure.doiun.file_manager.model.FileNode;

import javax.ws.rs.*;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mykyta_Doiun
 */
@Path("/explorer/local/tree")
public class FileTreeExplorerImpl implements FileTreeExplorer {

    @Override
    @GET
    @Produces("application/json")
    public FileNode getFileNode(@QueryParam("path") String path) {
        FileNode fileNode = new FileNode();
        if (!StringUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                fileNode = getFileNodeFromFile(file, true);
            }
        }
        return fileNode;
    }

    private List<FileNode> listFilesInDir(File directory) {
        List<FileNode> fileNodes;
        if (directory.isDirectory()) {
            fileNodes = Stream.of(directory.listFiles())
                    .map(file -> getFileNodeFromFile(file, false))
                    .collect(Collectors.toList());
        } else {
            fileNodes = Collections.emptyList();
        }
        return fileNodes;
    }

    private FileNode getFileNodeFromFile(File file, boolean withSubNodes) {
        FileNode fileNode = new FileNode();
        fileNode.setFileName(file.getName());
        fileNode.setFileDate(new Date(file.lastModified()));
        fileNode.setFilePath(file.getPath());
        fileNode.setFileSize(getFileLendth(file));
        fileNode.setDirectory(file.isDirectory());
        if (withSubNodes) {
            fileNode.setSubNodes(listFilesInDir(file));
        }
        return fileNode;
    }

    private long getFileLendth(File file){
        /*if(file.isDirectory()){
            return FileUtils.sizeOfDirectory(file);
        }*/
        return file.length();
    }
}
