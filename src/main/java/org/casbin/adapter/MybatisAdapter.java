package org.casbin.adapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.casbin.jcasbin.model.Assertion;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.Helper;

import javax.sql.DataSource;
import java.util.*;


/**
 * MybatisAdapter is the Mybatis adapter for jCasbin.
 * It can load policy from Mybatis supported database or save policy to it.
 */
public class MybatisAdapter implements Adapter {
    private String driver;
    private String url;
    private String username;
    private String password;
    private boolean dbSpecified;
    private SqlSessionFactory factory;

    /**
     * MybatisAdapter is the constructor for MybatisAdapter.
     *
     * @param driver the database driver, like "com.mysql.cj.jdbc.Driver".
     * @param url the database URL, like "jdbc:mysql://localhost:3306/casbin".
     * @param username the username of the database.
     * @param password the password of the database.
     */
    public MybatisAdapter(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbSpecified = false;

        open();
    }

    /**
     * MybatisAdapter is the constructor for MybatisAdapter.
     *
     * @param driver the database driver, like "com.mysql.cj.jdbc.Driver".
     * @param url the database URL, like "jdbc:mysql://localhost:3306/casbin".
     * @param username the username of the database.
     * @param password the password of the database.
     * @param dbSpecified whether you have specified an existing DB in url.
     * If dbSpecified == true, you need to make sure the DB in url exists.
     * If dbSpecified == false, the adapter will automatically create a DB named "casbin".
     */
    public MybatisAdapter(String driver, String url, String username, String password, boolean dbSpecified) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbSpecified = dbSpecified;

        open();
    }


    private DataSource getDataSource(String driver, String url, String username, String password){
        PooledDataSource dataSource = new PooledDataSource(driver, url, username, password);
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }



    private SqlSessionFactory initSqlSessionFactory(DataSource dataSource){

        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        //Create a Configuration object
        Configuration configuration = new Configuration(environment);
        //Register a MyBatis context alias
        configuration.getTypeAliasRegistry().registerAlias("admin", CasbinRule.class);
        //Add a mapper
        configuration.addMapper(CasbinRuleDao.class);
        //Building SqlSessionFactory with SqlSessionFactoryBuilder
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        return sqlSessionFactory;
    }


    private String getUrl(String url){
        return url + "?characterEncoding=utf-8&&serverTimezone=UTC&&useSSL=false";
    }



    private void createDatabase(){
        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);
        casbinRuleDao.createDatabase("casbin");
        sqlSession.close();
    }

    private void createTable(){
        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);

        switch (driver){
            case "oracle.jdbc.OracleDriver":
                casbinRuleDao.createOracleTable("casbin_rule");
                break;
            case "com.mysql.cj.jdbc.Driver":
                casbinRuleDao.createMysqlTable("casbin_rule");
                break;
        }

        sqlSession.close();
    }


    private void dropTable(){
        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);

        switch (driver){
            case "oracle.jdbc.OracleDriver":
                casbinRuleDao.dropOracleTable("casbin_rule");
                break;
            case "com.mysql.cj.jdbc.Driver":
                casbinRuleDao.dropMysqlTable("casbin_rule");
                break;
        }

        sqlSession.close();
    }

    private void open(){
        this.factory = initSqlSessionFactory(getDataSource(driver,getUrl(url),username,password));

        if (!dbSpecified){
            createDatabase();
            this.factory = initSqlSessionFactory(getDataSource(driver, getUrl(url + "casbin"), username, password));
        }

        createTable();
    }



    /**
     * loadPolicy loads all policy rules from the storage.
     */
    @Override
    public void loadPolicy(Model model) {
        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);
        List<CasbinRule> casbinRules = casbinRuleDao.loadAll();
        for (CasbinRule line : casbinRules) {
            loadPolicyLine(line, model);
        }
        sqlSession.close();
    }

    private static void loadPolicyLine(CasbinRule line, Model model){
        String lineText = line.getPtype();
        if (line.getV0() != null) {
            lineText += ", " + line.getV0();
        }
        if (line.getV1() != null) {
            lineText += ", " + line.getV1();
        }
        if (line.getV2() != null) {
            lineText += ", " + line.getV2();
        }
        if (line.getV3() != null) {
            lineText += ", " + line.getV3();
        }
        if (line.getV4() != null) {
            lineText += ", " + line.getV4();
        }
        if (line.getV5() != null) {
            lineText += ", " + line.getV5();
        }
        Helper.loadPolicyLine(lineText,model);
    }


    /**
     * savePolicy saves all policy rules to the storage.
     */
    @Override
    public void savePolicy(Model model) {
        dropTable();
        createTable();


        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);

        for (Map.Entry<String, Assertion> entry : model.model.get("p").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();

            for (List<String> rule : ast.policy) {
                CasbinRule line = savePolicyLine(ptype, rule);
                casbinRuleDao.insertData(line);
            }
        }

        for (Map.Entry<String, Assertion> entry : model.model.get("g").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();

            for (List<String> rule : ast.policy) {
                CasbinRule line = savePolicyLine(ptype, rule);
                casbinRuleDao.insertData(line);
            }
        }

        sqlSession.close();
    }

    private CasbinRule savePolicyLine(String ptype, List<String> rule) {
        CasbinRule line = new CasbinRule();

        line.setPtype(ptype);
        if (rule.size() > 0) {
            line.setV0(rule.get(0));
        }
        if (rule.size() > 1) {
            line.setV1(rule.get(1));
        }
        if (rule.size() > 2) {
            line.setV2(rule.get(2));
        }
        if (rule.size() > 3) {
            line.setV3(rule.get(3));
        }
        if (rule.size() > 4) {
            line.setV4(rule.get(4));
        }
        if (rule.size() > 5) {
            line.setV5(rule.get(5));
        }

        return line;
    }


    /**
     * addPolicy adds a policy rule to the storage.
     */
    @Override
    public void addPolicy(String sec, String ptype, List<String> rule) {
        if(CollectionUtils.isEmpty(rule)) return;
        CasbinRule line = savePolicyLine(ptype, rule);

        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);
        casbinRuleDao.insertData(line);
        sqlSession.close();

    }


    /**
     * removePolicy removes a policy rule from the storage.
     */
    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        if(CollectionUtils.isEmpty(rule)) return;
        removeFilteredPolicy(sec, ptype, 0, rule.toArray(new String[0]));
    }

    /**
     * removeFilteredPolicy removes policy rules that match the filter from the storage.
     */
    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        List<String> values = Optional.ofNullable(Arrays.asList(fieldValues)).orElse(new ArrayList<>());
        if(CollectionUtils.isEmpty(values)) return;

        SqlSession sqlSession = factory.openSession(true);
        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);
        casbinRuleDao.deleteData(ptype, values);
        sqlSession.close();
    }
}
