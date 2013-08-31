package br.uff.ic.dyevc.services;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.CommitInfo;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import br.uff.ic.dyevc.persistence.MongoLabServiceParms;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ObjectMapper;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * This class provides access to services hosted in Mongo Labs
 *
 * @author Cristiano
 */
public class MongoLabProvider {
    /** Field description */
    public static final String COLLECTION_REPOSITORIES = "/collections/repositories";

    /** Field description */
    public static final String        COLLECTION_COMMITS = "/collections/commits";
    private static final String       API_KEY            = "dgOZbb9cNfzHSfuANRekokGrWCYWYCEs";
    private static final String       BASE_URL           = "https://api.mongolab.com/api/1/databases/dyevc";
    private static final ObjectMapper mapper             = new ObjectMapper();

    // <editor-fold defaultstate="collapsed" desc="repositories">

    /**
     * Retrieves a list of repositories from the database
     *
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @return The list of repositories retrieved from the database
     *
     * @throws ServiceException
     */
    public static ArrayList<RepositoryInfo> getRepositories(MongoLabServiceParms params) throws ServiceException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("getRepositories -> Entry");
        ArrayList<RepositoryInfo>                 result = null;
        ClientRequest                             req;
        ClientResponse<ArrayList<RepositoryInfo>> res;
        try {
            req = prepareRequest(COLLECTION_REPOSITORIES, params);
            res = req.get(new GenericType<ArrayList<RepositoryInfo>>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(COLLECTION_REPOSITORIES, res.getStatus(), params);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error getting repositories.", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("getRepositories -> Exit");

        return result;
    }

    /**
     * Upserts a repository to the database, this is, if the specified id does not exist,
     * inserts it, otherwise, updates it
     *
     * @param body The repository to be upserted
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static Object upsertRepository(RepositoryInfo body) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("upsertRepository -> Entry");
        Object         result = null;
        ClientRequest  req;
        ClientResponse res;
        try {
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            String serviceName = COLLECTION_REPOSITORIES + "/" + body.getId();
            req = prepareRequest(serviceName, null, new String(mapper.writeValueAsBytes(body)));
            res = req.put(new GenericType<RepositoryInfo>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(serviceName, res.getStatus(), null);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error updating repository <" + body.getId() + ">",
                                    ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("upsertRepository -> Exit");

        return result;
    }

    /**
     * Deletes a repository from the database. The application should first check
     * if the repository is not referenced anywhere, otherwise there will be inconsistency
     * in the database
     *
     * @param id The id of the repository to be deleted
     * @return The result of the service invocation
     *
     * @throws ServiceException
     */
    public static Object deleteRepository(String id) throws ServiceException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("deleteRepository -> Entry");
        Object         result = null;
        ClientRequest  req;
        ClientResponse res;
        try {
            String serviceName = COLLECTION_REPOSITORIES + "/" + id;
            req = prepareRequest(serviceName, null);
            res = req.delete(new GenericType<RepositoryInfo>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(serviceName, res.getStatus(), null);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error deleting repository <" + id + ">", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("deleteRepository -> Exit");

        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="commits">

    /**
     * Retrieves a list of commits from the database
     *
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @return The list of commits retrieved from the database
     *
     * @throws ServiceException
     */
    public static Set<CommitInfo> getCommits(MongoLabServiceParms params) throws ServiceException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("getCommits -> Entry");
        Set<CommitInfo>                 result = null;
        ClientRequest                   req;
        ClientResponse<Set<CommitInfo>> res;
        try {
            req = prepareRequest(COLLECTION_COMMITS, params);
            res = req.get(new GenericType<Set<CommitInfo>>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(COLLECTION_COMMITS, res.getStatus(), params);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error getting commits.", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("getCommits -> Exit");

        return result;
    }

    /**
     * Inserts a collection of commits in the database
     *
     * @param commits The collection of commits to be inserted
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static Object insertCommits(Collection<CommitInfo> commits) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("insertCommits -> Entry");
        Object         result = null;
        ClientRequest  req;
        ClientResponse res;
        try {
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            String serviceName = COLLECTION_COMMITS;
            req = prepareRequest(serviceName, null, new String(mapper.writeValueAsBytes(commits)));
            res = req.post(new GenericType<Object>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(serviceName, res.getStatus(), null);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error inserting commits.", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("insertCommits -> Exit");

        return result;
    }

    /**
     * Updates a list of commits, according to query embedded in <code>parms</code>, with values specified in <code>updateCmd</code>
     * @param parms The parameters to be used in the update
     * @param updateCmd The update value
     *
     * @return
     *
     * @throws ServiceException
     */
    public static Object updateCommits(MongoLabServiceParms parms, String updateCmd) throws ServiceException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("updateCommits -> Entry");
        Object         result = null;
        ClientRequest  req;
        ClientResponse res;
        try {
            String serviceName = COLLECTION_COMMITS;
            req = prepareRequest(serviceName, parms, updateCmd);
            res = req.put(new GenericType<Object>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(serviceName, res.getStatus(), null);
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error updating commits with command: <" + updateCmd
                                    + ">", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("updateCommits -> Exit");

        return result;
    }

    /**
     * Deletes all commits, according to query embedded in <code>parms</code></code>
     * @param parms The parameters to be used during the deletion
     *
     * @return
     *
     * @throws ServiceException
     */
    public static Object deleteCommits(MongoLabServiceParms parms) throws ServiceException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("deleteCommits -> Entry");
        Object         result = null;
        ClientRequest  req;
        ClientResponse res;
        try {
            String serviceName = COLLECTION_COMMITS;
            req = prepareRequest(serviceName, parms);
            req.body("application/json", "[ ]");
            res = req.put(new GenericType<Object>() {}
            );

            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(serviceName, res.getStatus(), null);
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error deleting commits.", ex);

            throw new ServiceException(ex);
        }

        LoggerFactory.getLogger(MongoLabProvider.class).trace("deleteCommits -> Exit");

        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="request prepare">
    private static ClientRequest prepareRequest(String service, HashMap<String, Object> params) {
        ClientRequest req;
        req = new ClientRequest(BASE_URL + service);
        req.queryParameter("apiKey", API_KEY);

        if (params != null) {
            for (String key : params.keySet()) {
                req.queryParameter(key, params.get(key));
            }
        }

        return req;
    }

    private static ClientRequest prepareRequest(String service, HashMap<String, Object> params, String body) {
        ClientRequest req;
        req = prepareRequest(service, params);

        if (body != null) {
            req.body("application/json", body);
        }

        return req;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="error handling">
    private static void throwErrorMessage(String service, int status, HashMap<String, Object> params)
            throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params);
        LoggerFactory.getLogger(MongoLabProvider.class).error(message.toString());

        throw new ServiceException(message.toString());
    }

    private static void throwErrorMessage(String service, int status, HashMap<String, Object> params, String body)
            throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params, body);
        LoggerFactory.getLogger(MongoLabProvider.class).error(message.toString());

        throw new ServiceException(message.toString());
    }

    private static StringBuilder createErrorMessage(String service, int status, HashMap<String, Object> params)
            throws ServiceException {
        StringBuilder message = new StringBuilder("Error invoking service: ").append(service).append(
                                    ". Return code received on service invocation: ").append(status);
        if ((params != null) &&!params.isEmpty()) {
            message.append(IConstants.LINE_SEPARATOR).append("\tParams used on invocation: ").append(
                IConstants.LINE_SEPARATOR);

            for (String key : params.keySet()) {
                message.append("\t\t").append(key).append(": ").append(params.get(key)).append(
                    IConstants.LINE_SEPARATOR);
            }
        }

        return message;
    }

    private static StringBuilder createErrorMessage(String service, int status, HashMap<String, Object> params,
            String body)
            throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params);
        if ((body != null) &&!"".equals(body)) {
            message.append(IConstants.LINE_SEPARATOR).append("\tMessage body sent on invocation: ").append(
                IConstants.LINE_SEPARATOR);
            message.append("\t\t").append(body);
        }

        return message;
    }

    // </editor-fold>
}
