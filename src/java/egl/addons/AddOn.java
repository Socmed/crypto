
package egl.addons;

import egl.http.APIServlet;

public interface AddOn {

    default void init() {}

    default void shutdown() {}

    default APIServlet.APIRequestHandler getAPIRequestHandler() {
        return null;
    }

    default String getAPIRequestType() {
        return null;
    }

}
