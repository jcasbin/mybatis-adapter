package org.casbin.adapter;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CasbinRuleDao {
    @Select("select * from casbin_rule")
    List<CasbinRule> loadAll();

    @Update("CREATE DATABASE IF NOT EXISTS ${databaseName}")
    void createDatabase(@Param("databaseName") String databaseName);

    @Update("CREATE TABLE IF NOT EXISTS ${tableName} " +
            "(ptype VARCHAR(100) not NULL, " +
            " v0 VARCHAR(100), " +
            " v1 VARCHAR(100), " +
            " v2 VARCHAR(100), " +
            " v3 VARCHAR(100), " +
            " v4 VARCHAR(100), " +
            " v5 VARCHAR(100))")
    void createMysqlTable(@Param("tableName") String tableName);


    @Update("declare " +
            "nCount NUMBER;" +
            "v_sql LONG;" +
            "begin " +
            "SELECT count(*) into nCount FROM USER_TABLES where table_name = '${tableName}';" +
            "IF(nCount <= 0) " +
            "THEN " +
            "v_sql:='" +
            "CREATE TABLE ${tableName} " +
            "                    (ptype VARCHAR(100) not NULL, " +
            "                     v0 VARCHAR(100), " +
            "                     v1 VARCHAR(100), " +
            "                     v2 VARCHAR(100), " +
            "                     v3 VARCHAR(100)," +
            "                     v4 VARCHAR(100)," +
            "                     v5 VARCHAR(100))';" +
            "execute immediate v_sql;" +
            "END IF;" +
            "end;")
    void createOracleTable(@Param("tableName") String tableName);

    @Update("DROP TABLE IF EXISTS ${tableName}")
    void dropMysqlTable(@Param("tableName") String tableName);



    @Update("declare " +
            "nCount NUMBER;" +
            "v_sql LONG;" +
            "begin " +
            "SELECT count(*) into nCount FROM dba_tables where table_name = '${tableName}';" +
            "IF(nCount >= 1) " +
            "THEN " +
            "v_sql:='drop table ${tableName}';" +
            "execute immediate v_sql;" +
            "END IF;" +
            "end;")
    void dropOracleTable(@Param("tableName") String tableName);


//    @Insert("<script>"  +
//            "INSERT INTO casbin_rule (ptype,v0,v1,v2,v3,v4,v5) VALUES" +
//            "<foreach collection=\"list\" item=\"rule\" index=\"index\"  separator=\",\">" +
//            "(#{rule.ptype},#{rule.v0},#{rule.v1},#{rule.v2},#{rule.v3},#{rule.v4},#{rule.v5})" +
//            "</foreach>" +
//            "</script>")
//    void insertData(@Param("list")List<CasbinRule> rules);


    @Insert("INSERT INTO casbin_rule (ptype,v0,v1,v2,v3,v4,v5) VALUES (#{ptype},#{v0},#{v1},#{v2},#{v3},#{v4},#{v5})")
    void insertData(CasbinRule line);

    @Insert("<script>"  +
            "DELETE FROM casbin_rule WHERE ptype = #{ptype}" +
            "<foreach collection=\"list\" item=\"item1\" index=\"index\"  separator=\" \">" +
            " AND v#{index} = #{item1}" +
            "</foreach>" +
            "</script>")
    void deleteData(@Param("ptype") String ptype, @Param("list") List<String> rules);

}
