package ua.nure.doiun.file_manager.services;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ua.nure.doiun.file_manager.model.UserDetailsImpl;
import ua.nure.doiun.file_manager.util.Constants;


/**
 * @author Mykyta_Doiun
 */
public class UserDetailServiceImpl implements UserDetailsService {

    private static final Logger LOG = Logger.getLogger(UserDetailServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Configuration config;
        UserDetails userDetails = null;
        try {
            config = new PropertiesConfiguration(Constants.AUTHENTICATION_PROPERTIES);
            userDetails = new UserDetailsImpl(config.getString(Constants.LOGIN), config.getString(Constants.PASSWORD));
        } catch (ConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }
        return userDetails;
    }


}
