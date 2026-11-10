package SignUI_Front.client;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.text.MessageFormat;

/** Cliente Jersey para UserFacadeREST [entities.user]. */
public class UserRESTClient {

    private final WebTarget webTarget;
    private final Client client;
    private static final String BASE_URI = "http://localhost:8080/SignUI_Back/webresources";

    public UserRESTClient() {
        client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("entities.user");
    }

    public String countREST() throws ClientErrorException {
        return webTarget.path("count").request(MediaType.TEXT_PLAIN).get(String.class);
    }

    public void edit_XML(Object requestEntity, String id) throws WebApplicationException {
        Response response = webTarget.path(MessageFormat.format("{0}", new Object[]{id}))
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(requestEntity, MediaType.APPLICATION_XML));
        checkSuccess(response);
    }

    public void edit_JSON(Object requestEntity, String id) throws ClientErrorException {
        webTarget.path(MessageFormat.format("{0}", new Object[]{id}))
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
    }

    public <T> T find_XML(Class<T> responseType, String id) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("{0}", new Object[]{id}))
                .request(MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T find_JSON(Class<T> responseType, String id) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("{0}", new Object[]{id}))
                .request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T findRange_XML(Class<T> responseType, String from, String to) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("{0}/{1}", new Object[]{from, to}))
                .request(MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T findRange_JSON(Class<T> responseType, String from, String to) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("{0}/{1}", new Object[]{from, to}))
                .request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public void create_XML(Object requestEntity) throws WebApplicationException {
        Response response = webTarget.request(MediaType.APPLICATION_XML)
                .post(Entity.entity(requestEntity, MediaType.APPLICATION_XML));
        checkSuccess(response);
    }

    public void create_JSON(Object requestEntity) throws ClientErrorException {
        webTarget.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(requestEntity, MediaType.APPLICATION_JSON));
    }

    public <T> T findByEmail_XML(Class<T> responseType, String email) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("email/{0}", new Object[]{email}))
                .request(MediaType.APPLICATION_XML).get(responseType);
    }

    /** Versión para colecciones: el endpoint email/{email} devuelve <users>. */
    public <T> T findByEmail_XML(GenericType<T> responseType, String email) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("email/{0}", new Object[]{email}))
                .request(MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T findByEmail_JSON(Class<T> responseType, String email) throws ClientErrorException {
        return webTarget.path(MessageFormat.format("email/{0}", new Object[]{email}))
                .request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T findAll_XML(Class<T> responseType) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T findAll_XML(GenericType<T> responseType) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T findAll_JSON(Class<T> responseType) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T findAll_JSON(GenericType<T> responseType) throws ClientErrorException {
        return webTarget.request(MediaType.APPLICATION_JSON).get(responseType);
    }
    
    /** Lanza WebApplicationException si el servidor no respondió 2xx. */
    private void checkSuccess(Response response) {
        int status = response.getStatus();
        response.close();
        if (status < 200 || status >= 300) {
            throw new WebApplicationException("HTTP " + status, status);
        }
    }
    
    public void remove(String id) throws ClientErrorException {
        webTarget.path(MessageFormat.format("{0}", new Object[]{id})).request().delete();
    }

    public void close() {
        client.close();
    }
}