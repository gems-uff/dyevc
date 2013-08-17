package br.uff.ic.dyevc.persistence;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents an object Id in mongo lab
 * @author Cristiano
 */
public class Oid {
    private String oid;

    public Oid() {
        
    }
    
    public Oid(String oid) {
        this.oid = oid;
    }
    
    @JsonProperty(value = "$oid")
    public String getOid() {
        return oid;
    }

    @JsonProperty(value = "$oid")
    public void setOid(String oid) {
        this.oid = oid;
    }
    
}
