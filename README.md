# mybatis-adapter

[![codebeat badge](https://codebeat.co/badges/998c8e12-ffdd-4196-b2a2-8979d7f1ee8a)](https://codebeat.co/projects/github-com-jcasbin-mybatis-adapter-master)
[![build](https://github.com/jcasbin/mybatis-adapter/actions/workflows/ci.yml/badge.svg)](https://github.com/jcasbin/mybatis-adapter/actions)
[![codecov](https://codecov.io/github/jcasbin/mybatis-adapter/branch/master/graph/badge.svg?token=4YRFEQY7VK)](https://codecov.io/github/jcasbin/mybatis-adapter)
[![javadoc](https://javadoc.io/badge2/org.casbin/mybatis-adapter/javadoc.svg)](https://javadoc.io/doc/org.casbin/mybatis-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/mybatis-adapter.svg)](https://mvnrepository.com/artifact/org.casbin/mybatis-adapter/latest)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/casbin/lobby)

Mybatis Adapter is the Mybatis adapter for jCasbin, which provides interfaces for loading policies from Mybatis and saving policies to it.

## Installation

    <dependency>
        <groupId>org.casbin</groupId>
        <artifactId>mybatis-adapter</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    
## Example

    package com.company.example;
    
    import org.casbin.jcasbin.main.Enforcer;
    import org.casbin.jcasbin.util.Util;
    import org.casbin.adapter.MybatisAdapter;
    
    public class Example {
        public void test() {
            Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        
            String driver = "com.mysql.jdbc.Driver";
            String url = "jdbc:mysql://localhost:3306/tbl";
            String username = "YourUsername";
            String password = "YourPassword";
            
            MybatisAdapter a = new MybatisAdapter(driver, url, username, password, true);
        
            // Save policy to DB
            a.savePolicy(e.getModel());
        
            // Load policy from DB
            a.loadPolicy(e.getModel());
        }
    }
    
## Getting Help

- [jCasbin](https://github.com/casbin/jcasbin)

## License

This project is under Apache 2.0 License. See the [LICENSE](LICENSE) file for the full license text.