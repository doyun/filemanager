package ua.nure.doiun.file_manager.services.explorer;

import ua.nure.doiun.file_manager.model.FileNode;

/**
 * @author Mykyta_Doiun
 */

public interface FileTreeExplorer {

    FileNode getFileNode(String path);
}
