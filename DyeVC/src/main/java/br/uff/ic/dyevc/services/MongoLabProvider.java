package br.uff.ic.dyevc.services;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.exception.DyeVCException;
import br.uff.ic.dyevc.exception.ServiceException;
import br.uff.ic.dyevc.model.topology.RepositoryInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to services hosted in Mongo Labs
 *
 * @author Cristiano
 */
public class MongoLabProvider {
    public static final String COLLECTION_REPOSITORIES = "/collections/repositories";

    private static final String API_KEY = "X90TQA2NqU53IpEg5WmRnE_R76EOd4Cj";
    private static final String BASE_URL = "https://api.mongolab.com/api/1/databases/dyevc";

    /**
     * Gets a list of repository information from the database
     *
     * @param service The service to be invoked. All services must begin with
     * slashes ("/")
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static ArrayList<RepositoryInfo> getRepositories(HashMap<String, Object> params) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("getRepositories -> Entry");
        ArrayList<RepositoryInfo> result = null;
        ClientRequest req;
        ClientResponse<ArrayList<RepositoryInfo>> res;
        try {
            req = prepareRequest(COLLECTION_REPOSITORIES, params);
            res = req.get(new GenericType<ArrayList<RepositoryInfo>>() {
            });
            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(COLLECTION_REPOSITORIES, res.getStatus(), params);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            LoggerFactory.getLogger(MongoLabProvider.class).error("Error getting repositories.", ex);
            throw new DyeVCException(ex);
        }
        LoggerFactory.getLogger(MongoLabProvider.class).trace("getRepositories -> Exit");
        return result;
    }


    /**
     * Invokes a GET service in mongolab
     *
     * @param <T> The type returned as a result of the service invocation
     * @param service The service to be invoked. All services must begin with
     * slashes ("/")
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static <T> T getService(String service, HashMap<String, Object> params) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        T result = null;
        ClientRequest req;
        ClientResponse<T> res;
        try {
            req = prepareRequest(service, params);
            res = req.get(new GenericType<T>() {
            });
            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(service, res.getStatus(), params);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            Logger.getLogger(MongoLabProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new DyeVCException(ex);
        }
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        return result;
    }

    /**
     * Invokes a POST service in mongolab
     *
     * @param <T> The type returned as a result of the service invocation
     * @param service The service to be invoked. All services must begin with
     * slashes ("/")
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @param body The body of the POST message
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static <T> T postService(String service, HashMap<String, Object> params, String body) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        T result = null;
        ClientRequest req;
        ClientResponse<T> res;
        try {
            req = prepareRequest(service, params);

            res = req.post(new GenericType<T>() {
            });
            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(service, res.getStatus(), params, body);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            Logger.getLogger(MongoLabProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new DyeVCException(ex);
        }
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        return result;
    }

    /**
     * Invokes a PUT service in mongolab
     *
     * @param <T> The type returned as a result of the service invocation
     * @param service The service to be invoked. All services must begin with
     * slashes ("/")
     * @param params A mapping of parameter names and parameter values to be
     * used in the service invocation
     * @param body The body of the POST message
     * @return The result of the service invocation
     * @throws DyeVCException In case of any exception during the service
     * invocation
     */
    public static <T> T putService(String service, HashMap<String, Object> params, String body) throws DyeVCException {
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        T result = null;
        ClientRequest req;
        ClientResponse<T> res;
        try {
            req = prepareRequest(service, params);

            res = req.put(new GenericType<T>() {
            });
            if (res.getStatus() == 200) {
                result = res.getEntity();
            } else {
                throwErrorMessage(service, res.getStatus(), params, body);
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception ex) {
            Logger.getLogger(MongoLabProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new DyeVCException(ex);
        }
        LoggerFactory.getLogger(MongoLabProvider.class).trace("Constructor -> Entry");
        return result;
    }

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

        if (body != null && !"".equals(body)) {
            req.body("application/json", body);
        }

        return req;
    }

    private static void throwErrorMessage(String service, int status, HashMap<String, Object> params) throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params);
        LoggerFactory.getLogger(MongoLabProvider.class).error(message.toString());
        throw new ServiceException(message.toString());
    }

    private static void throwErrorMessage(String service, int status, HashMap<String, Object> params, String body) throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params, body);
        LoggerFactory.getLogger(MongoLabProvider.class).error(message.toString());
        throw new ServiceException(message.toString());
    }

    private static StringBuilder createErrorMessage(String service, int status, HashMap<String, Object> params) throws ServiceException {
        StringBuilder message = new StringBuilder("Error invoking service: ")
                .append(service)
                .append(". Return code received on service invocation: ")
                .append(status);
        if (!params.isEmpty()) {
            message.append(IConstants.LINE_SEPARATOR)
                    .append("\tParams used on invocation: ").append(IConstants.LINE_SEPARATOR);
            for (String key : params.keySet()) {
                message.append("\t\t")
                        .append(key)
                        .append(": ")
                        .append(params.get(key))
                        .append(IConstants.LINE_SEPARATOR);
            }
        }
        return message;
    }

    private static StringBuilder createErrorMessage(String service, int status, HashMap<String, Object> params, String body) throws ServiceException {
        StringBuilder message = createErrorMessage(service, status, params);

        if (body != null && !"".equals(body)) {
            message.append(IConstants.LINE_SEPARATOR)
                    .append("\tMessage body sent on invocation: ").append(IConstants.LINE_SEPARATOR);
            message.append("\t\t")
                    .append(body);
        }
        return message;
    }
}
