package com.github.fertkir.moneytransfer;

import com.github.fertkir.moneytransfer.dao.DaoModule;
import com.github.fertkir.moneytransfer.service.ServiceModule;
import com.github.fertkir.moneytransfer.servlet.AppServletModule;
import com.google.inject.Guice;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;

import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class ApplicationMain {
    public static void main(String[] args) throws Exception {

        Guice.createInjector(
                new DaoModule(),
                new ServiceModule(),
                new AppServletModule());

        Server server = new Server(8080);
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", SESSIONS);
        servletContextHandler.addFilter(GuiceFilter.class, "/*", allOf(DispatcherType.class));
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        server.start();
    }
}
