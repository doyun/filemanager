package ua.nure.doiun.file_manager.dao.impl.ftp;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.log4j.Logger;
import ua.nure.doiun.file_manager.dao.FileAccessObject;
import ua.nure.doiun.file_manager.util.Constants;
import ua.nure.doiun.file_manager.util.SessionUtil;

import javax.activation.DataHandler;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.UUID;

/**
 * @author Mykyta_Doiun
 */
@Path("/dao/ftpfao")
public class FileAccessObjectImpl implements FileAccessObject {
    private static final Logger LOG = Logger.getLogger(ua.nure.doiun.file_manager.dao.impl.local.FileAccessObjectImpl.class);

    @Override
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("filepath") String filePath) {
        String escapedFilePath = StringEscapeUtils.escapeJava(filePath);
        FTPClient ftpClient = SessionUtil.getFtpClient();
        File file = null;
        try {
            FTPFile ftpFile = ftpClient.mlistFile(escapedFilePath);
            checkIfFileExists(ftpFile, escapedFilePath);
            checkIfNotDirectory(ftpFile, escapedFilePath);
            file = new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getName(filePath));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            ftpClient.retrieveFile(filePath, outputStream);
            IOUtils.closeQuietly(outputStream);
            file.deleteOnExit();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok(file).header("Content-Disposition", "attachment; filename=" + file.getName()).build();
    }

    @Override
    @DELETE
    public Response deleteFile(@QueryParam("filepath") String filePath) {
        String escapedFilePath = StringEscapeUtils.escapeJava(filePath);
        FTPClient ftpClient = SessionUtil.getFtpClient();

        boolean wasDeleted;
        try {
            FTPFile ftpFile = ftpClient.mlistFile(escapedFilePath);
            checkIfFileExists(ftpFile, escapedFilePath);

            if (ftpFile.isDirectory()) {
                wasDeleted = removeDirectory(ftpClient, escapedFilePath);
            } else {
                wasDeleted = ftpClient.deleteFile(escapedFilePath);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new WebApplicationException("Can not remove, servers interaction failure.", Response.Status.INTERNAL_SERVER_ERROR);
        }

        return wasDeleted ? Response.ok().build() : Response.status(Response.Status.FORBIDDEN).build();
    }

    @Override
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Multipart("filepath") @NotNull String filePath,
                               @Multipart("file") @NotNull Attachment attachment) {
        DataHandler handler = attachment.getDataHandler();
        FTPClient ftpClient = SessionUtil.getFtpClient();
        InputStream in = null;
        try {
            MultivaluedMap<String, String> map = attachment.getHeaders();
            in = handler.getInputStream();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.storeFile(StringEscapeUtils.escapeJava(filePath) + Constants.FILE_PATH_SEPARATOR + getFileName(map), in);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return Response.ok("file uploaded").build();
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String exactFileName = name[1].trim().replaceAll("\"", "");
                return exactFileName;
            }
        }
        return UUID.randomUUID().toString();
    }

    private boolean removeDirectory(FTPClient ftpClient, String filePath){
        boolean wasRemoved;
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles(filePath);
            for (FTPFile ftpFile : ftpFiles) {
                if(ftpFile.isDirectory()){
                    if(!removeDirectory(ftpClient, filePath + "/" + ftpFile.getName())){
                        return false;
                    }
                }
                if(!ftpClient.deleteFile(filePath + "/" + ftpFile.getName())){
                    return false;
                }
            }
            wasRemoved = ftpClient.removeDirectory(filePath);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            wasRemoved = false;
        }
        return wasRemoved;
    }

    private void checkIfFileExists(FTPFile ftpFile, String filePath) {
        if (ftpFile == null || ftpFile.getName() == null) {
            LOG.warn(String.format("File %s does not exist.", filePath));
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private void checkIfNotDirectory(FTPFile ftpFile, String filePath) {
        if (ftpFile.isDirectory()) {
            LOG.warn(String.format("File %s is directory, can not download.", filePath));
            throw new WebApplicationException("Can not download directory", Response.Status.BAD_REQUEST);
        }

    }

}
