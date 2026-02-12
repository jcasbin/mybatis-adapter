package org.casbin.adapter;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.util.Util;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MybatisAdapterTest {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "casbin_test";
    private static final String PASSWORD = "TEST_casbin";

    static void testEnforce(Enforcer e, String sub, Object obj, String act, boolean res) {
        assertEquals(res, e.enforce(sub, obj, act));
    }

    private static void testGetPolicy(Enforcer e, List<List<String>> res) {
        List<List<String>> myRes = e.getPolicy();
        Util.logPrint("Policy: " + myRes);

        if (!Util.array2DEquals(res, myRes)) {
            fail("Policy: " + myRes + ", supposed to be " + res);
        }
    }

    @Test
    public void testAdapter() {
        // Because the DB is empty at first,
        // so we need to load the policy from the file adapter (.CSV) first.
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

        MybatisAdapter a = new MybatisAdapter(DRIVER, URL, USERNAME, PASSWORD);
        // This is a trick to save the current policy to the DB.
        // We can't call e.savePolicy() because the adapter in the enforcer is still the file adapter.
        // The current policy means the policy in the jCasbin enforcer (aka in memory).
        a.savePolicy(e.getModel());

        // Clear the current policy.
        e.clearPolicy();
        testGetPolicy(e, asList());

        // Load the policy from DB.
        a.loadPolicy(e.getModel());
        testGetPolicy(e, asList(
                asList("alice", "data1", "read"),
                asList("bob", "data2", "write"),
                asList("data2_admin", "data2", "read"),
                asList("data2_admin", "data2", "write")));

        // Note: you don't need to look at the above code
        // if you already have a working DB with policy inside.

        // Now the DB has policy, so we can provide a normal use case.
        // Create an adapter and an enforcer.
        // new Enforcer() will load the policy automatically.
        a = new MybatisAdapter(DRIVER, URL, USERNAME, PASSWORD);
        e = new Enforcer("examples/rbac_model.conf", a);
        testGetPolicy(e, asList(
                asList("alice", "data1", "read"),
                asList("bob", "data2", "write"),
                asList("data2_admin", "data2", "read"),
                asList("data2_admin", "data2", "write")));


    }

    @Test
    public void testAddAndRemovePolicy() {
        MybatisAdapter a = new MybatisAdapter(DRIVER, URL, USERNAME, PASSWORD);
        Enforcer e = new Enforcer("examples/rbac_model.conf", a);
        testEnforce(e, "cathy", "data1", "read", false);

        // AutoSave is enabled by default.
        // It can be disabled by:
        // e.enableAutoSave(false);

        // Because AutoSave is enabled, the policy change not only affects the policy in Casbin enforcer,
        // but also affects the policy in the storage.
        e.addPolicy("cathy", "data1", "read");
        testEnforce(e, "cathy", "data1", "read", true);

        // Reload the policy from the storage to see the effect.
        e.clearPolicy();
        a.loadPolicy(e.getModel());
        // The policy has a new rule: {"cathy", "data1", "read"}.
        testEnforce(e, "cathy", "data1", "read", true);

        // Remove the added rule.
        e.removePolicy("cathy", "data1", "read");
        testEnforce(e, "cathy", "data1", "read", false);

        // Reload the policy from the storage to see the effect.
        e.clearPolicy();
        a.loadPolicy(e.getModel());
        testEnforce(e, "cathy", "data1", "read", false);
    }

    @Test
    public void testAddAndRemovePolicyBatch() {
        MybatisAdapter a = new MybatisAdapter(DRIVER, URL, USERNAME, PASSWORD);
        Enforcer e = new Enforcer("examples/rbac_model.conf", a);

        // test addPolicies()
        e.clearPolicy();
        e.addPolicies(asList(
                asList("alice", "data1", "read"),
                asList("bob", "data2", "write"),
                asList("data2_admin", "data2", "read"),
                asList("data2_admin", "data2", "write")
        ));
        e.clearPolicy();
        a.loadPolicy(e.getModel());
        testEnforce(e, "alice", "data1", "read", true);
        testEnforce(e, "bob", "data2", "write", true);
        testEnforce(e, "data2_admin", "data2", "read", true);
        testEnforce(e, "data2_admin", "data2", "write", true);

        // test removePolicies()
        e.clearPolicy();
        a.savePolicy(e.getModel());
        e.addPolicies(asList(
                asList("alice", "data1", "read"),
                asList("bob", "data2", "write"),
                asList("data2_admin", "data2", "read"),
                asList("data2_admin", "data2", "write")
        ));
        e.removePolicies(asList(
                asList("alice", "data1", "read"),
                asList("bob", "data2", "write")
        ));
        e.clearPolicy();
        a.loadPolicy(e.getModel());
        testEnforce(e, "alice", "data1", "read", false);
        testEnforce(e, "bob", "data2", "write", false);
        testEnforce(e, "data2_admin", "data2", "read", true);
        testEnforce(e, "data2_admin", "data2", "write", true);
    }

    @Test
    public void testConditionsToMyBatisQuery() {
        MybatisAdapter a = new MybatisAdapter(DRIVER, URL, USERNAME, PASSWORD);
        Enforcer e = new Enforcer("examples/rbac_model.conf", a);

        // Clear existing policies and add test data
        e.clearPolicy();
        a.savePolicy(e.getModel());  // Actually clear the database table
        e.addPolicy("alice", "data1", "read");
        e.addPolicy("bob", "data2", "write");
        e.addPolicy("alice", "data3", "read");
        e.addPolicy("charlie", "data1", "write");

        // Test with OR combineType - should find rules matching either v0='alice' OR v1='data1'
        List<String> conditionsOr = new ArrayList<>();
        conditionsOr.add("v0 = 'alice'");
        conditionsOr.add("v1 = 'data1'");

        List<CasbinRule> resultsOr = a.conditionsToMyBatisQuery(conditionsOr, CombineType.OR);
        assertNotNull(resultsOr);
        assertTrue("Should find at least 3 rules with OR conditions (alice+data1, alice+data3, charlie+data1)", resultsOr.size() >= 3);
        
        // Verify that OR results include alice rules and charlie+data1 rule
        boolean hasAliceData1 = false;
        boolean hasAliceData3 = false;
        boolean hasCharlieData1 = false;
        for (CasbinRule rule : resultsOr) {
            if ("alice".equals(rule.getV0()) && "data1".equals(rule.getV1())) {
                hasAliceData1 = true;
            }
            if ("alice".equals(rule.getV0()) && "data3".equals(rule.getV1())) {
                hasAliceData3 = true;
            }
            if ("charlie".equals(rule.getV0()) && "data1".equals(rule.getV1())) {
                hasCharlieData1 = true;
            }
        }
        assertTrue("OR should include alice+data1", hasAliceData1);
        assertTrue("OR should include alice+data3", hasAliceData3);
        assertTrue("OR should include charlie+data1", hasCharlieData1);

        // Test with AND combineType - should find only rules matching both v0='alice' AND v1='data1'
        List<String> conditionsAnd = new ArrayList<>();
        conditionsAnd.add("v0 = 'alice'");
        conditionsAnd.add("v1 = 'data1'");

        List<CasbinRule> resultsAnd = a.conditionsToMyBatisQuery(conditionsAnd, CombineType.AND);
        assertNotNull(resultsAnd);
        assertEquals("AND should return exactly 1 result (alice+data1)", 1, resultsAnd.size());
        
        // Verify the AND result is the specific alice+data1 rule
        CasbinRule andResult = resultsAnd.get(0);
        assertEquals("AND result should have v0='alice'", "alice", andResult.getV0());
        assertEquals("AND result should have v1='data1'", "data1", andResult.getV1());
        
        // Verify that AND returns fewer results than OR
        assertTrue("AND should return fewer results than OR", resultsAnd.size() < resultsOr.size());

        // Test with empty conditions
        List<String> emptyConditions = new ArrayList<>();
        List<CasbinRule> emptyResults = a.conditionsToMyBatisQuery(emptyConditions, CombineType.OR);
        assertNotNull(emptyResults);
        assertTrue("Empty conditions should return empty results", emptyResults.isEmpty());
    }
}
