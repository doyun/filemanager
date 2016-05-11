package ua.nure.doiun.file_manager.services.explorer.impl.local;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import ua.nure.doiun.file_manager.model.FileNode;
import ua.nure.doiun.file_manager.services.explorer.FileTreeExplorer;
import ua.nure.doiun.file_manager.util.Constants;
import ua.nure.doiun.file_manager.util.FileNodeComparator;

import javax.swing.filechooser.FileSystemView;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.File;
import java.util.Arrays;
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

    private static final Logger LOG = Logger.getLogger(FileTreeExplorerImpl.class);

    @Override
    @GET
    @Produces("application/json")
    public FileNode getFileNode(@QueryParam("path") String path) {
        FileNode fileNode = new FileNode();
        LOG.info(String.format("Getting file node  by path %s", path));
        if (!StringUtils.isEmpty(path)) {
            File file = new File(StringEscapeUtils.escapeJava(path));
            if (file.exists()) {
                fileNode = getFileNodeFromFile(file, true);
            }
        } else {
            fileNode = getLocalDisks();
        }
        return fileNode;
    }

    private List<FileNode> listFilesInDir(File directory) {
        List<FileNode> fileNodes;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            Arrays.sort(files);
            fileNodes = Stream.of(files)
                    .map(file -> getFileNodeFromFile(file, false)).sorted(new FileNodeComparator())
                    .collect(Collectors.toList());
            //set back folder
            fileNodes.add(0, getFileNodeFromFile(directory.getParentFile(), false));
            fileNodes.get(0).setFileName("..");
        } else {
            fileNodes = Collections.emptyList();
        }
        return fileNodes;
    }

    private FileNode getFileNodeFromFile(File file, boolean withSubNodes) {
        FileNode fileNode = new FileNode();
        if (file != null) {
            fileNode.setFileName(file.getName());
            fileNode.setFileDate(FileNode.DATE_FORMAT.format(new Date(file.lastModified())));
            fileNode.setFilePath(file.getPath());
            fileNode.setFileSize(getFileLength(file));
            fileNode.setDirectory(file.isDirectory());
            if (withSubNodes) {
                fileNode.setSubNodes(listFilesInDir(file));
            }
        } else {
            fileNode.setDirectory(true);
        }
        return fileNode;
    }

    private long getFileLength(File file) {
        /*if(file.isDirectory()){
            return FileUtils.sizeOfDirectory(file);
        }*/
        return file.length();
    }

    private FileNode getLocalDisks() {
        List<File> root = Arrays.asList(File.listRoots());
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        List<FileNode> fileNodes = root.stream().filter(file ->
                Constants.LOCAL_DISK_TYPE.equals(fileSystemView.getSystemTypeDescription(file))
        ).map(file -> getFileNodeFromFile(file, false)).collect(Collectors.toList());
        FileNode fileNode = new FileNode();
        fileNode.setFileName("root");
        fileNode.setDirectory(true);
        fileNode.setSubNodes(fileNodes);
        return fileNode;
    }
}
