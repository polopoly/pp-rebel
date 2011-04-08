package com.polopoly.javarebel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.zeroturnaround.javarebel.LoggerFactory;

import com.polopoly.javarebel.cfg.Configuration;
import com.polopoly.javarebel.cfg.Configuration.Item;

public class PolopolyFSProvider implements FSProvider {

    boolean exists = true;
    long lastChange = -1;
    File configFile;
    Configuration config;
    
    public PolopolyFSProvider()
    {
        String polopoly = FSUtil.getPolopoly();
        if (polopoly == null) {
            LoggerFactory.getInstance().echo("WARNING: No PP_HOME environment defined, Polopoly Content Rebel Plugin will be disabled");
            return;
        }
        configFile = new File(polopoly + "/pp-rebel.xml");
    }
    
    public FS getFS(String externalid)
    {
        Configuration config = getConfiguration();
        if (config == null) {
            return null;
        }
        List<Configuration.Item> item = config.get(externalid);
        if (item == null) {
            return null;
        }
        return createFS(item);
    }

    private FS createFS(List<Item> item)
    {
        return new PolopolyFS(item);
    }

    private synchronized Configuration getConfiguration()
    {
        if (configFile == null) {
            return null;
        }
        if ((!configFile.exists() || configFile.isDirectory()) && exists) {
            LoggerFactory.getInstance().echo("Error, " + configFile.getAbsolutePath() + " does not exist");
            exists = false;
        }
        if (!exists) {
            return config;
        }
        long currentLastModified = configFile.lastModified();
        if (currentLastModified > lastChange) {
            try {
                config = Configuration.parse(new FileReader(configFile));
                lastChange = currentLastModified;
            } catch (FileNotFoundException e) {
                LoggerFactory.getInstance().error(e);
            } catch (JAXBException e) {
                LoggerFactory.getInstance().error(e);
                lastChange = currentLastModified;
            }
        }
        return config;
    }
}
