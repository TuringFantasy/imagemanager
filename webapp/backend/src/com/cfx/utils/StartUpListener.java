package com.cfx.utils;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartUpListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	//ServletContext sc = sce.getServletContext();
    	//InputStream inStream = sc.getResourceAsStream("/WEB-INF/ui-backend.properties");
        //load properties at start up
        try {
			ServiceConfig.getInstance().loadMacawProperties();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    	
    }

}
