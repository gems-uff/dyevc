/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.tools.vcs;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Cristiano
 */
public class GitConnectorTest {
    
    public GitConnectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getRepository method, of class GitConnector.
     */
    @Test
    public void testGetRepository() {
        System.out.println("getRepository");
        GitConnector instance = null;
        Repository expResult = null;
        Repository result = instance.getRepository();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRepository method, of class GitConnector.
     */
    @Test
    public void testSetRepository() {
        System.out.println("setRepository");
        Repository repository = null;
        GitConnector instance = null;
        instance.setRepository(repository);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRemotes method, of class GitConnector.
     */
    @Test
    public void testGetRemotes() {
        System.out.println("getRemotes");
        GitConnector instance = null;
        HashMap expResult = null;
        HashMap result = instance.getRemotes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBranchesFromRemote method, of class GitConnector.
     */
    @Test
    public void testGetBranches() {
        System.out.println("getBranches");
        GitConnector instance = null;
        Set expResult = null;
        Set result = instance.getBranchesFromRemote();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pull method, of class GitConnector.
     */
    @Test
    public void testPull() throws Exception {
        System.out.println("pull");
        GitConnector instance = null;
        boolean expResult = false;
        boolean result = instance.pull();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of push method, of class GitConnector.
     */
    @Test
    public void testPush() throws Exception {
        System.out.println("push");
        GitConnector instance = null;
        instance.push();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commit method, of class GitConnector.
     */
    @Test
    public void testCommit() throws Exception {
        System.out.println("commit");
        String message = "";
        GitConnector instance = null;
        instance.commit(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createRepository method, of class GitConnector.
     */
    @Test
    public void testCreateRepository() throws Exception {
        System.out.println("createRepository");
        String path = "";
        GitConnector instance = null;
        Repository expResult = null;
        Repository result = instance.createRepository(path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cloneRepository method, of class GitConnector.
     */
    @Test
    public void testCloneRepository_String_File() throws Exception {
        System.out.println("cloneRepository");
        String source = "";
        File target = null;
        GitConnector instance = null;
        Repository expResult = null;
        Repository result = instance.cloneRepository(source, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cloneRepository method, of class GitConnector.
     */
    @Test
    public void testCloneRepository_String_String() throws Exception {
        System.out.println("cloneRepository");
        String source = "";
        String target = "";
        GitConnector instance = null;
        Repository expResult = null;
        Repository result = instance.cloneRepository(source, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
