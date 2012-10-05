package br.uff.ic.dyevc.model;

import java.io.Serializable;

/**
 *
 * @author cristiano
 */
public class RepositoryUser implements Serializable {
    private static final long serialVersionUID = 5011296979976191201L;

    private Long id;
    private String login;
    private String password;
    private boolean anonymous = false;
    private Repository repository;

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
