package ua.nure.doiun.file_manager.services;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ua.nure.doiun.file_manager.util.Constants;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Mykyta_Doiun
 */
@Path("/user")
public class UserManager {

    private static final Logger LOG = Logger.getLogger(UserManager.class);

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response updatePassword(@FormParam("newPassword") String newPassword, @FormParam("currentPassword") String oldPassword) {
        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(Constants.AUTHENTICATION_PROPERTIES);
            MessageDigestPasswordEncoder messageDigestPasswordEncoder = new MessageDigestPasswordEncoder(Constants.PASSHASH_ALGORITHM);
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails.getPassword().equals(messageDigestPasswordEncoder.encodePassword(oldPassword, null))) {
                config.setProperty(Constants.PASSWORD, messageDigestPasswordEncoder.encodePassword(newPassword, null));
                config.save();
            } else {
                LOG.info("Invalid password.");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
            return Response.serverError().build();
        }
        LOG.info("User changed password.");
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
