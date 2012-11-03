/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.tools.vcs;

import br.uff.ic.dyevc.model.git.TrackedBranch;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
     * Test of getRemoteNames method, of class GitConnector.
     */
    @Test
    public void testGetRemotes() {
        System.out.println("getRemotes");
        GitConnector instance = null;
        Set<String> expResult = null;
        Set<String> result = instance.getRemoteNames();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTrackedBranches method, of class GitConnector.
     */
    @Test
    public void testGetBranches() {
        System.out.println("getBranches");
        GitConnector instance = null;
        List<TrackedBranch> expResult = null;
        List<TrackedBranch> result = instance.getTrackedBranches();
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
        PullResult expResult = null;
        PullResult result = instance.pull();
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
        String id = null;
        GitConnector instance = null;
        GitConnector expResult = null;
        GitConnector result = instance.cloneRepository(source, target, id);
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
        String id = null;
        GitConnector instance = null;
        GitConnector expResult = null;
        GitConnector result = instance.cloneRepository(source, target, id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
