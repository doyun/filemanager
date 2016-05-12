package ua.nure.doiun.file_manager.services;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Mykyta_Doiun
 */
@Path("/authentication")
public class AuthenticationService {

    private static final Logger LOG = Logger.getLogger(AuthenticationService.class);

    @POST
    @Path("/ftp")
    public Response loginToFtp(@FormParam("host") String host, @FormParam("port") int port,
                               @FormParam("login") String login, @FormParam("password") String password){
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            logFtpServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                LOG.warn(String.format("Failed to connect  to ftp, Server reply code: %d", replyCode));
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            boolean success = ftpClient.login(login, password);
            logFtpServerReply(ftpClient);
            if (!success) {
                LOG.warn("Could not login to the server");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                LOG.info("LOGGED IN SERVER");
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return Response.ok().build();
    }

    private void logFtpServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                LOG.info(String.format("SERVER: %s", aReply));
            }
        }
    }
}
