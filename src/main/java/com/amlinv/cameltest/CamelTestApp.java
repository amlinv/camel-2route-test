package com.amlinv.cameltest;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Command-line application startup for the Camel Test using a Spring XML Application startup file by default.
 *
 * Created by art on 5/19/14.
 */
public class CamelTestApp {
    private static final Logger         LOG = LoggerFactory.getLogger(CamelTestApp.class);

    private AbstractApplicationContext  appContext;
    private boolean                     runningInd = true;

    private AtomicBoolean               shutdownCompleteInd = new AtomicBoolean(false);
    private long                        maxParkingLotShutdownTime = 60000;

    /**
     * Main program startup method on the class; creates an instance of the Application object and runs the instance
     * version of this method.
     *
     * @param args - - arguments provided on the command-line.
     */
    public static void  main (String[] args) {
        CamelTestApp mainObj = new CamelTestApp();

        try {
            mainObj.instanceMain(args);
        } catch ( Throwable thrown ) {
            thrown.printStackTrace();
        }
    }

    /**
     * Main program startup method on an instance of the application.
     *
     * @param args - arguments provided on the command-line.
     */
    public void instanceMain (String[] args) {
        if ( args.length == 0 ) {
            throw   new Error(
                    "application.xml file required as a command-line argument; additional XML files may be supplied");
        }

        /**
         * Create the application-context, register the shutdown hook for cleanup, and then apply after-instantiation
         * callbacks.
         */
        this.appContext = new ClassPathXmlApplicationContext(args);
        this.appContext.registerShutdownHook();
        this.appContext.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        this.runningInd = true;

        /**
         * Add a shutdown hook to initiate cleanup at shutdown time.
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run () {
                LOG.info("Camel Test App shutdown hook executed");
                CamelTestApp.this.initiateShutdown();
            }
        });

        this.waitForShutdown();
    }

    public void initiateShutdown () {
        /**
         * Log the shutdown; use a debug-level message to enable a thorough means to determine how shutdown was
         * initiated.
         */
        LOG.info("shutting down the application");
        LOG.debug("shutdown stack trace", new Exception());

        synchronized ( this ) {
            if ( this.runningInd ) {
                this.runningInd = false;
                this.notifyAll();

                Thread shutdownThread = new Thread() {
                    @Override
                    public void run () {
                        CamelTestApp.this.doShutdown();
                    }
                };

                shutdownThread.start();
            }
        }
    }

    /**
     * Wait for shutdown of the Application.
     */
    public void waitForShutdown() {
        synchronized ( this ) {
            while ( this.runningInd ) {
                try {
                    this.wait();
                } catch ( InterruptedException intExc ) {
                    if ( this.runningInd ) {
                        LOG.warn("main loop interrupted while running; ignoring", intExc);
                    } else {
                        LOG.debug("main loop terminating with interrupted thread", intExc);
                    }
                }
            }
        }
    }

    /**
     * Perform the shutdown steps of the application now.
     */
    protected void doShutdown () {
        LOG.info("SHUTDOWN COMMENCING");

        LOG.info("SHUTDOWN FINISHED");
    }
}
