package ua.nure.doiun.file_manager.dao.impl.local;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import ua.nure.doiun.file_manager.dao.FileAccessObject;

import javax.activation.DataHandler;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Mykyta_Doiun
 */
@Path("/dao/lfao")
public class LocalFileAccessObject implements FileAccessObject {

    @Override
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("filepath") String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else if (file.isDirectory()) {
            throw new WebApplicationException("Can not upload directory", Response.Status.BAD_REQUEST);
        }

        return Response.ok(file).header("Content-Disposition", "attachment; filename=" + file.getName()).build();
    }

    @Override
    @DELETE
    public Response deleteFile(@QueryParam("filepath") String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        boolean wasDeleted = false;
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
                wasDeleted = true;
            } catch (IOException e) {
                e.printStackTrace();
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
            File file = new File(filePath + "/" + getFileName(map));
            in = handler.getInputStream();
            out = new FileOutputStream(file);
            IOUtils.copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
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
