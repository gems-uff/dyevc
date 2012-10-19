package br.uff.ic.dyevc.model;

public class RepositoryRelationship {

    private int ahead;
    private int behind;
    private MonitoredRepository repository;

    public int getAhead() {
        return ahead;
    }

    public void setAhead(int ahead) {
        this.ahead = ahead;
    }

    public int getBehind() {
        return behind;
    }

    public void setBehind(int behind) {
        this.behind = behind;
    }

    public MonitoredRepository getRepository() {
        return repository;
    }

    public void setRepository(MonitoredRepository repository) {
        this.repository = repository;
    }
}
