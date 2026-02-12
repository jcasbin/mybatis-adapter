package org.casbin.adapter;

import org.apache.ibatis.session.SqlSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConditionsHelper provides utility methods for converting query conditions into MyBatis queries.
 */
public class ConditionsHelper {

    /**
     * ConditionsToMyBatisQuery is a function that converts multiple query conditions into a MyBatis query statement.
     * You can use the GetAllowedObjectConditions() API of Casbin to get conditions,
     * and choose the way of combining conditions through combineType.
     *
     * @param sqlSession the MyBatis SQL session
     * @param conditions the list of query conditions (e.g., ["category_id = 1", "author = 'alice'"])
     * @param combineType the way to combine conditions (OR or AND)
     * @return the list of CasbinRule matching the conditions
     */
    public static List<CasbinRule> conditionsToMyBatisQuery(SqlSession sqlSession, List<String> conditions, CombineType combineType) {
        if (conditions == null || conditions.isEmpty()) {
            return new ArrayList<>();
        }

        CasbinRuleDao casbinRuleDao = sqlSession.getMapper(CasbinRuleDao.class);
        
        Map<String, Object> params = new HashMap<>();
        params.put("conditions", conditions);
        params.put("combineType", combineType.name());
        
        return casbinRuleDao.selectByConditions(params);
    }
}
