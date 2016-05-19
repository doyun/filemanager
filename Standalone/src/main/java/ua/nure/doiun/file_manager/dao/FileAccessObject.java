package ua.nure.doiun.file_manager.dao;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

/**
 * @author Mykyta_Doiun
 */
public interface FileAccessObject {

    Response getFile(String filePath);

    Response deleteFile(String filePath);

    Response uploadFile(String filePath, Attachment attachment);
}
