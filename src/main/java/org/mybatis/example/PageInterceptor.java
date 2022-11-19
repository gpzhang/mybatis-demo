package org.mybatis.example;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 分页拦截器
 * <p>
 * Mybatis是对四大接口进行拦截的，
 * Mybatis的四大接口对象 Executor, StatementHandler, ResultSetHandler, ParameterHandler。
 * Mybatis的插件是采用对四大接口的对象生成动态代理对象的方法来实现的。
 * 通过InterceptorChain的pluginAll()方法
 *
 * @author haishen
 * @date 2019/7/31
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class PageInterceptor implements Interceptor {


    private static final String BOUND_SQL_KEY = "delegate.boundSql.sql";

    private static final String PARAMETER_HANDLER_KEY = "delegate.parameterHandler";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = statementHandler.getBoundSql();
        PageCondition page = getPage(boundSql);
        if (page == null) {
            return invocation.proceed();
        }
        String originSql = boundSql.getSql();
        String pageSql = getPageSql(originSql, page);
        // 反射修改原sql
        metaObject.setValue(BOUND_SQL_KEY, pageSql);
        setPageTotalCount(originSql, metaObject, invocation, page);
        return invocation.proceed();
    }

    /**
     * 设置分页对象的总数
     */
    private void setPageTotalCount(String originSql, MetaObject metaObject, Invocation invocation, PageCondition page) throws Exception {
        PreparedStatement countStatement = null;
        ResultSet resultSet = null;
        try {
            String countSql = getCountSql(originSql);
            // 这个数据库连接不能关，后面的分页sql也要使用这个connection
            Connection connection = (Connection) invocation.getArgs()[0];
            countStatement = connection.prepareStatement(countSql);
            ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue(PARAMETER_HANDLER_KEY);
            parameterHandler.setParameters(countStatement);
            resultSet = countStatement.executeQuery();
            int totalCount = 0;
            if (resultSet.next()) {
                totalCount = resultSet.getInt(1);
            }
            page.setTotalCount(totalCount);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (countStatement != null) {
                countStatement.close();
            }
        }
    }

    /**
     * 这种方式比较简陋，使用时注意sql语句末的分号问题
     */
    private String getPageSql(String originSql, PageCondition page) {
        return new StringBuilder().append(originSql).append(" LIMIT ").
                append(page.getCurrentPage() * page.getPageSize()).append(",").append(page.getPageSize()).toString();
    }

    /**
     * 获取分页对象
     */
    private PageCondition getPage(BoundSql boundSql) {
        PageCondition page = null;
        // 获取执行方法的参数
        Object param = boundSql.getParameterObject();
        if (param instanceof Map) {
            Map paramMap = (Map) param;
            for (Iterator iterator = paramMap.values().iterator(); iterator.hasNext(); ) {
                Object obj = iterator.next();
                if (obj instanceof PageCondition) {
                    page = (PageCondition) obj;
                    break;
                }
            }
        } else if (param instanceof PageCondition) {
            page = (PageCondition) param;
        }
        return page;
    }

    /**
     * 这种方式比较简陋，使用时注意sql语句末的分号问题
     */
    private String getCountSql(String originSql) {
        return new StringBuilder().append("select count(*) from (").append(originSql).append(") t").toString();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 这里可以获取到拦截器配置的properties
    }
}
