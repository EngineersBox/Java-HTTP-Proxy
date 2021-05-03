package com.engineersbox.httpproxy.servlet;

/**
 * Abstract base class for a servlet
 */
public interface AbstractServlet {

    /**
     * Perform servlet initialisation
     */
    void init();

    /**
     * Start serving the servlet via connections
     */
    void serve();

}
