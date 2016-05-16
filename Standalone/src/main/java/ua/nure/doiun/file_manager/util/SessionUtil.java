package ua.nure.doiun.file_manager.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Mykyta_Doiun
 */
public class SessionUtil {

    private static final Logger LOG = Logger.getLogger(SessionUtil.class);

    public static void setFtpClient(FTPClient ftpClient) {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        HttpSession session = request.getSession(true);
        session.setAttribute(Constants.Session.FTP_CLIENT, ftpClient);
    }

    public static FTPClient getFtpClient(){
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        HttpSession session = request.getSession(true);
        return (FTPClient) session.getAttribute(Constants.Session.FTP_CLIENT);
    }
}
