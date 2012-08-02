package com.sindice.linker.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrLookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
/**
 * Configures the servlet based upon configuration in sindice.home/{appname}
 * folder, or if that folder is not defined then ~/sindice/{appname}. Creates
 * the folder if needed using default logging and configuraiton provided
 * with the web application. This context listener should be moved
 * into a shared sindice package.
 * @author robful
 *
 */
public class ServletConfigurationContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletConfigurationContextListener.class);
    private static final int BUFFER_SIZE_1024 = 1024;
    private static final String DEFAULTLOGGING = "default-logback.xml";

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
		LOGGER.info("invoke BASE contextDestroyed");
  }


@Override
  public void contextInitialized(ServletContextEvent sce) {
	LOGGER.info("invoke BASE contextInitialized");

	final ServletContext context = sce.getServletContext();
    String contextPath = context.getContextPath().replaceFirst("^/", ""); 
    String servletContextName = context.getServletContextName();
    addEnvToContext(context);
    String sindiceHome = (String) context.getAttribute("sindice.home");
    if(sindiceHome == null || "".equals(sindiceHome.trim())){
      String userHome = (String) context.getAttribute("user.home");
      sindiceHome = (userHome==null?"":userHome)+"/sindice";
      LOGGER.warn("sindice.home is not defined, assuming {}",sindiceHome);
      context.setAttribute("sindice.home",sindiceHome);
    }
    context.setAttribute("app.name", contextPath);  
    File configFolder = new File(sindiceHome+"/"+contextPath);
    if(!(configFolder.exists()&&configFolder.isDirectory())){
      LOGGER.warn("Missing configuration folder {}",configFolder);
      if(configFolder.mkdirs()){
        LOGGER.warn("Creating default configuration at "+configFolder);

      }else{
        // set logging level to INFO
        configureLogging(context,configFolder);
        return;
      }
    }
    // does a specific folder exist for this servlet context?
    File specificFolder = new File(configFolder,servletContextName);
    if(specificFolder.exists() && specificFolder.isDirectory()){
      configFolder = specificFolder;
    }
    LOGGER.info("loading configuration from folder {}",configFolder);
    configureLogging(context,configFolder);
    final XMLConfiguration config = createXMLConfiguration(context);
    File applicationConfigFile = new File(configFolder,"config.xml");
    if(!applicationConfigFile.exists()){
      LOGGER.warn("missing application config file {}",applicationConfigFile);
      loadDefaultConfiguration(config,applicationConfigFile);
    }else{
      try {
        config.load(applicationConfigFile);
        LOGGER.info("parsed {}",applicationConfigFile);
      } catch (ConfigurationException e) {
        LOGGER.error("Could not load configuration from {}",applicationConfigFile,e);
        loadDefaultConfiguration(config,null);
      }
    }
    context.setAttribute("config",config);
    LOGGER.info("config now availabe via the following line of code\n" +
    "    XMLConfiguration appConfig = (XMLConfiguration) servletContext.getAttribute(\"config\");");

    LOGGER.info("Starting up {}",servletContextName);
  }



private XMLConfiguration createXMLConfiguration(final ServletContext context) {
    final XMLConfiguration config = new XMLConfiguration();
    ConfigurationInterpolator interpolator = config.getInterpolator();
    final StrLookup defaultLookup = interpolator.getDefaultLookup();
    interpolator.setDefaultLookup(new StrLookup(){
      @Override
      public String lookup(String key) {
        if(context.getAttribute(key) != null){
          return context.getAttribute(key).toString();
        }
        if(context.getInitParameter(key)!= null){
          return context.getInitParameter(key);
        }
        return defaultLookup.lookup(key);
      }});
    return config;
  }

  private void loadDefaultConfiguration(final XMLConfiguration config, File saveAs) {
    LOGGER.info("loading default configuration");
    InputStream in = ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream("default-config.xml");
    if(in == null){
      LOGGER.error("application is missing default-config.xml from classpath");
    }else{
      try{
        config.load(in);
        if(saveAs != null){
          try{
            saveAs.getParentFile().mkdirs();
            config.save(saveAs);
            LOGGER.info("wrote default configuration to {}",saveAs);
          }catch(ConfigurationException e){
            LOGGER.warn("Could not write configuration to {}",saveAs,e);
          }
        }
      }catch(ConfigurationException e){
        LOGGER.error("could not load default-config.xml",e);
      }finally{
        try {
          in.close();
        } catch (IOException e) {
          LOGGER.warn("problem closing stream",e);
        }
      }
    }
  }

  private void addEnvToContext(ServletContext context) {
    addToContext(context,System.getenv());
    addToContext(context,System.getProperties());
  }

  private void addToContext(ServletContext context, Map map) {
    for(Object  key : map.keySet()){
      if(context.getAttribute(key.toString())== null){
        context.setAttribute(key.toString(), map.get(key));
      }
    }
  }

  private void configureLogging(ServletContext context, final File configFolder) {
    InputStream in = openLoggingConfig(configFolder);
    if(in == null){ 
      setDefaultLogging();
      return;
    }
    try{
    final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    final JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    lc.reset();
    try {
      Enumeration<String> attributeNames = context.getAttributeNames();
      while(attributeNames.hasMoreElements()){
        String key =  attributeNames.nextElement();
        Object value = context.getAttribute(key);
        if(value != null){
          configurator.getContext().putProperty(key,value.toString());
        }
      }
      configurator.doConfigure(in);
    } catch (JoranException e) {
      lc.reset();
      setDefaultLogging();
      LOGGER.error("logging configuration failed", e);
      LOGGER.warn("using servlet container logging configuration");
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }finally{
      try {
        in.close();
      } catch (IOException e) {
        LOGGER.warn("problem closing stream",e);
      }
    }
  }
  /**
   * Opens the logging configuration file logback.xml. If that doesn't exist
   * try to create it by copying default-logback.xml which should be provided
   * in the classes folder with the application.
   * @param configFolder
   * @return
   */
  private InputStream openLoggingConfig(File configFolder) {
    String loggerConfigFileName = configFolder+"/logback.xml";
    File logConfigFile = new File(loggerConfigFileName);
    if(!logConfigFile.exists()){
      LOGGER.warn("Missing logging configuration file "+loggerConfigFileName);
      createLogConfigFile(logConfigFile);
    }
    if(logConfigFile.exists()){
      try{
        return new FileInputStream(logConfigFile);
      }catch(Exception e){
        LOGGER.warn("problem reading log file",e);
      }
    }
    return ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream(DEFAULTLOGGING);
  }

  private void createLogConfigFile(File logConfigFile){
    try{
      InputStream in = ServletConfigurationContextListener.class.getClassLoader().getResourceAsStream(DEFAULTLOGGING);
      if(in == null){
        LOGGER.warn("missing default-logging.xml from classpath");
      }else{
        // try to write the application logging file so admin can change it.
        try{
          logConfigFile.getParentFile().mkdirs();
          FileOutputStream out = new FileOutputStream(logConfigFile);
          try{
            byte[] buff = new byte[BUFFER_SIZE_1024];
            int read = 0;
            while((read = in.read(buff))>0){
              out.write(buff,0,read);
            }
          }finally{
            out.close();
          }
        }finally{
          in.close();
        }
      }
    }catch(IOException e){
      LOGGER.warn("couldn't write logConfigFile {}",logConfigFile,e);
    }
  }

  private void setDefaultLogging() {
    final Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    if (rootLogger instanceof ch.qos.logback.classic.Logger) {
      ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.INFO);
    }
    LOGGER.warn("Log level set to INFO, create the logging configuration file to change this.");
  }
  
  
  
  
	

	public static String getParameterWithLogging(XMLConfiguration config,
			String name, String defalt) {
		String value = config.getString(name);
		if (value == null) {
			LOGGER.info("missing init parameter " + name
					+ ", using default value");
			value = defalt;
		}
		LOGGER.info("using " + name + "=[" + value + "]");
		return value;
	}

	
	protected String[]  getParametersWithLogging(XMLConfiguration config,
			String name, String[] defalt) {
		String[] value = config.getStringArray(name);
		if (value.length == 0) {
			LOGGER.info("missing init parameter " + name
					+ ", using default value");
			value = defalt;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : value) {
			if (sb.length() > 0){
				sb.append(',');
			}
			sb.append(s);
		}
		LOGGER.info("using " + name + "=[" + sb + "]");
		return value;
	}
}
