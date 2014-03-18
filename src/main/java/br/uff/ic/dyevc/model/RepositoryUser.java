package br.uff.ic.dyevc.model;

import java.io.Serializable;

/**
 * Models a user of the repository
 *
 * @author cristiano
 */
public class RepositoryUser implements Serializable {
    private static final long serialVersionUID = 5011296979976191201L;

    /**
     * User id.
     */
    private Long id;
    
    /**
     * User login.
     */
    private String login;
    
    /**
     * User password.
     */
    private String password;
    
    /**
     * If true, then use anonymous user.
     */
    private boolean anonymous = false;
    private MonitoredRepository repository;

    public RepositoryUser(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public RepositoryUser(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public RepositoryUser() {
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User: " + getLogin() + " , Repository: " + repository;
    }

    /**
     * @return the anonymous
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * @param anonymous the anonymous to set
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
