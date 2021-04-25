package com.engineersbox.httpproxy.connection.handler;

public abstract class BaseTrafficHandler implements Runnable {

    public abstract void task() throws Exception;

    public abstract void after();

    @Override
    public void run() {
        try {
            task();
        } catch (final Exception e) {
            after();
        }
    }

}
