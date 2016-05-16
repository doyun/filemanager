package ua.nure.doiun.file_manager.services.explorer;

import javax.ws.rs.core.Response;

/**
 * @author Mykyta_Doiun
 */

public interface FileTreeExplorer {

    Response getFileNode(String path);
}
