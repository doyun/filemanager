package ua.nure.doiun.file_manager.services.explorer.impl.ftp;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import ua.nure.doiun.file_manager.model.FileNode;
import ua.nure.doiun.file_manager.services.explorer.FileTreeExplorer;
import ua.nure.doiun.file_manager.util.Constants;
import ua.nure.doiun.file_manager.util.FileNodeComparator;
import ua.nure.doiun.file_manager.util.SessionUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mykyta_Doiun
 */
@Path("/explorer/ftp/tree")
public class FileTreeExplorerImpl implements FileTreeExplorer {

    private static final Logger LOG = Logger.getLogger(FileTreeExplorerImpl.class);

    @Override
    @GET
    @Produces("application/json")
    public Response getFileNode(@DefaultValue("/") @QueryParam("path") String path) {
        FileNode fileNode = new FileNode();
        path = StringEscapeUtils.escapeJava(path);
        LOG.info(String.format("Getting file node  by path %s", path));
        FTPClient ftpClient = SessionUtil.getFtpClient();
        if(ftpClient != null){
            if(StringUtils.isEmpty(path)){
                path = Constants.FILE_PATH_SEPARATOR;
            }
            try {
                fileNode = getFileNodeFromPath(path, ftpClient);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                return Response.serverError().build();
            }
        }
        return Response.ok(fileNode).build();
    }

    private FileNode getFileNodeFromPath(String path, FTPClient ftpClient) throws IOException {
        FileNode fileNode;
        FTPFile file = ftpClient.mlistFile(path);
        if (file != null) {
            fileNode = ftpFileToFileNode(file, null);
            fileNode.setFilePath(file.getName());
            FTPFile[] ftpFiles = ftpClient.listFiles(path);
            List<FileNode> subNodes = Stream.of(ftpFiles).map(ftpFile -> ftpFileToFileNode(ftpFile, fileNode.getFileName())).sorted(new FileNodeComparator())
                    .collect(Collectors.toList());
            subNodes.add(0, getRootFileNode(path, ftpClient));
            fileNode.setSubNodes(subNodes);
        } else {
            fileNode = new FileNode();
        }
        return fileNode;
    }

    private FileNode getRootFileNode(String path, FTPClient ftpClient) throws IOException {
        FileNode fileNode = new FileNode();
        fileNode.setDirectory(true);
        fileNode.setFileName("..");
        fileNode.setFilePath(path.substring(0, path.lastIndexOf("/")));
        return fileNode;
    }

    private FileNode ftpFileToFileNode(FTPFile ftpFile, String rootDir) {
        FileNode fileNode = new FileNode();
        fileNode.setDirectory(ftpFile.isDirectory());
        fileNode.setFileName(ftpFile.getName());
        fileNode.setFileSize(ftpFile.getSize());
        if (rootDir != null) {
            fileNode.setFilePath(String.format("%s%s%s", rootDir, Constants.FILE_PATH_SEPARATOR, ftpFile.getName()));
        }
        fileNode.setFileDate(FileNode.DATE_FORMAT.format(new Date(ftpFile.getTimestamp().getTimeInMillis())));
        return fileNode;
    }
}
