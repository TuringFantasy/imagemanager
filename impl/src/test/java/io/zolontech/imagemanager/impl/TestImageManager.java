package io.zolontech.imagemanager.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.zolontech.imagemanager.ApprovalStatus;
import io.zolontech.imagemanager.DomainEntityInstantiator;
import io.zolontech.imagemanager.ImageRecord;
import io.zolontech.imagemanager.User;

@Test
public class TestImageManager {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestImageManager.class);

    ImageManager manager = null;
    
    @BeforeTest
    public void setup() throws Exception {
    }

    @AfterTest
    public void close() throws Exception {
    }

    @Test(alwaysRun = true)
    public void testInitialize() throws Exception {
        long start = System.currentTimeMillis();

        manager = new ImageManager();
        manager.initialize(null);
        
        elapsedTime("testInitialize", start);
    }

    @Test(alwaysRun = true, dependsOnMethods="testInitialize")
    public void testStart() throws Exception {
        long start = System.currentTimeMillis();

        manager.start(null);
        
        elapsedTime("testStart", start);
    }

    @Test(alwaysRun = true)
    public void testStop() throws Exception {
        long start = System.currentTimeMillis();

        manager.stop(null);
        
        elapsedTime("testStop", start);
    }

    @Test(alwaysRun = true, dependsOnMethods="testStart")
    public void testAddUser() throws Exception {
        long start = System.currentTimeMillis();

        User user = DomainEntityInstantiator.getInstance().newInstance(User.class);
        String userCode = UUID.randomUUID().toString();
        long dob = System.currentTimeMillis();
        user.setCode(userCode);
        user.setAddress("7901 Stoneridge Drive");
        user.setName("Test User");
        user.setEmailId("macaw@macaw.io");
        user.setDob(dob);
        manager.addUser(user);
        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        assert (users.size() > 0);

        elapsedTime("testAddUser", start);
        User existing = users.get(0);
        assert(existing.getName().equals("Test User"));
    }

    @Test(alwaysRun = true, dependsOnMethods="testAddUser")
    public void testGetUsers() throws Exception {
        long start = System.currentTimeMillis();

        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        assert (users.size() > 0);

        elapsedTime("testGetUsers", start);
        User existing = users.get(0);
        assert(existing.getName().equals("Test User"));
    }
    
    @Test(alwaysRun = true, dependsOnMethods="testAddUser")
    public void testUploadImage() throws Exception {
        long start = System.currentTimeMillis();
        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        assert (users.size() > 0);
        
        String imageName = "image-1";
        
        String imageId = manager.uploadImage(users.get(0).getCode(), imageName);
        
        assertNotNull(imageId);
        
        ImageRecord record = manager.getImageRecord(imageId);
        
        assertNotNull(record);

        elapsedTime("testUploadImage", start);
        assert(record.getImageName().equals(imageName));
    }
    
    @Test(alwaysRun = true, dependsOnMethods="testUploadImage")
    public void testGetImageRecords() throws Exception {
        long start = System.currentTimeMillis();
        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        assert (users.size() > 0);
        
        List<ImageRecord> records = manager.getImageRecords(users.get(0).getCode());
        
        assertNotNull(records);
        assertFalse(records.isEmpty());

        elapsedTime("testGetImageRecords", start);
    }
    
    @Test(alwaysRun = true, dependsOnMethods="testGetImageRecords")
    public void testUpdateStatus() throws Exception {
        long start = System.currentTimeMillis();
        List<io.zolontech.imagemanager.User> users = manager.getUsers();        
        assert (users.size() > 0);
        
        List<ImageRecord> records = manager.getImageRecords(users.get(0).getCode());
        
        assertNotNull(records);
        assertFalse(records.isEmpty());
        
        ImageRecord record = records.get(0);
        
        manager.updateStatus(record.getId(), ApprovalStatus.APPROVED);
        
        ImageRecord updated = manager.getImageRecord(record.getId());
        
        assertNotNull(updated);
        
        assertEquals(updated.getStatus(), ApprovalStatus.APPROVED);

        elapsedTime("testUpdateStatus", start);
    }

    private void elapsedTime(String methodName, long start) {
        long end = System.currentTimeMillis();
        logger.info(methodName + " execution total time" + " in seconds ==> " + (end - start) / 1000 + " seconds");
    }

}
