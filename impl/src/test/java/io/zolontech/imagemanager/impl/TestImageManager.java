package io.zolontech.imagemanager.impl;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.zolontech.imagemanager.DomainEntityInstantiator;
import io.zolontech.imagemanager.User;

@Test
public class TestImageManager {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestImageManager.class);

    @BeforeTest
    public void setup() throws Exception {
    }

    @AfterTest
    public void close() throws Exception {
    }

    @Test(alwaysRun = true)
    public void testInitialize() throws Exception {
        long start = System.currentTimeMillis();

        ImageManager manager = new ImageManager();
        manager.initialize(null);
        
        elapsedTime("testInitialize", start);
    }

    @Test(alwaysRun = true)
    public void testStart() throws Exception {
        long start = System.currentTimeMillis();

        ImageManager manager = new ImageManager();
        manager.start(null);
        
        elapsedTime("testStart", start);
    }

    @Test(alwaysRun = true)
    public void testStop() throws Exception {
        long start = System.currentTimeMillis();

        ImageManager manager = new ImageManager();
        manager.stop(null);
        
        elapsedTime("testStop", start);
    }

    @Test(alwaysRun = true)
    public void testAddUser() throws Exception {
        long start = System.currentTimeMillis();

        User user = DomainEntityInstantiator.getInstance().newInstance(User.class);
        String userCode = UUID.randomUUID().toString();
        long dob = System.currentTimeMillis();
        user.setUserCode(userCode);
        user.setAddress("7901 Stoneridge Drive");
        user.setCreatedAt(System.currentTimeMillis());
        user.setName("Test User");
        user.setEmailId("macaw@macaw.io");
        user.setDob(dob);
        ImageManager manager = new ImageManager();
        manager.addUser(user);
        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        
        elapsedTime("testStop", start);
    }

    private void elapsedTime(String methodName, long start) {
        long end = System.currentTimeMillis();
        logger.info(methodName + " execution total time" + " in seconds ==> " + (end - start) / 1000 + " seconds");
    }

}
