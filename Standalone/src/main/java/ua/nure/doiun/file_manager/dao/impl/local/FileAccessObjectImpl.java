package ua.nure.doiun.file_manager.dao.impl.local;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.log4j.Logger;
import ua.nure.doiun.file_manager.dao.FileAccessObject;

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
@Path("/dao/lfao")
public class FileAccessObjectImpl implements FileAccessObject {

    private static final Logger LOG = Logger.getLogger(FileAccessObjectImpl.class);

    @Override
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("filepath") String filePath) {
        String escapedFilePath = StringEscapeUtils.escapeJava(filePath);
        File file = new File(escapedFilePath);

        if (!file.exists()) {
            LOG.warn(String.format("File %s does not exist, can not download.", escapedFilePath));
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else if (file.isDirectory()) {
            LOG.warn(String.format("File %s is directory, can not download.", escapedFilePath));
            throw new WebApplicationException("Can not upload directory", Response.Status.BAD_REQUEST);
        }

        return Response.ok(file).header("Content-Disposition", "attachment; filename=" + file.getName()).build();
    }

    @Override
    @DELETE
    public Response deleteFile(@QueryParam("filepath") String filePath) {
        String escapedFilePath = StringEscapeUtils.escapeJava(filePath);
        File file = new File(escapedFilePath);

        if (!file.exists()) {
            LOG.warn(String.format("File %s does not exist, can not delete.", escapedFilePath));
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        boolean wasDeleted = false;
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
                wasDeleted = true;
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            wasDeleted = file.delete();
        }

        return wasDeleted ? Response.ok().build() : Response.status(Response.Status.FORBIDDEN).build();
    }

    @Override
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Multipart("filepath") @NotNull String filePath,
                               @Multipart("file") @NotNull Attachment attachment) {
        DataHandler handler = attachment.getDataHandler();
        InputStream in = null;
        OutputStream out = null;
        try {
            MultivaluedMap<String, String> map = attachment.getHeaders();
            File file = new File(StringEscapeUtils.escapeJava(filePath) + "/" + getFileName(map));
            in = handler.getInputStream();
            out = new FileOutputStream(file);
            IOUtils.copy(in, out);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
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
}
