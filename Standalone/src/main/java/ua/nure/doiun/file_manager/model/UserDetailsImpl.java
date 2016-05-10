package ua.nure.doiun.file_manager.model;

import org.apache.commons.collections.ListUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Mykyta_Doiun
 */
public class UserDetailsImpl implements UserDetails {

    private static final List<GrantedAuthority> grantedAuthorities = Arrays.asList((GrantedAuthority) () -> "ROLE_USER");

    private String username;
    private String password;

    public UserDetailsImpl(String username, String password) {
        this.password = password;
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ListUtils.unmodifiableList(grantedAuthorities);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
