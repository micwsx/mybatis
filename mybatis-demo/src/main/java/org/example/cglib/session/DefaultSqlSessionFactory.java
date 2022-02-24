package org.example.cglib.session;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Properties dbProperties;

    public DefaultSqlSessionFactory(Properties dbProperties) {
        this.dbProperties = dbProperties;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(ExecutorType.SIMPLE, null, false);
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(ExecutorType.SIMPLE, null, autoCommit);
    }

    @Override
    public SqlSession openSession(Connection connection) {
        return openSessionFromConnection(ExecutorType.SIMPLE, connection);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(ExecutorType.SIMPLE, level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        return openSessionFromDataSource(execType, null, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return openSessionFromDataSource(execType, null, autoCommit);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return openSessionFromDataSource(execType, level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        return openSessionFromConnection(execType, connection);
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
           /* <environment id="development">
                <transactionManager type="JDBC"/>
                <dataSource type="POOLED">
                    <property name="driver" value="${driver}"/>
                    <property name="url" value="${url}"/>
                    <property name="username" value="${username}"/>
                    <property name="password" value="${password}"/>
                </dataSource>
            </environment>*/
            // JDBC ->  JdbcTransactionFactory.class
            // POOLED -> PooledDataSourceFactory.class

            final TransactionFactory txFactory = JdbcTransactionFactory.class.getDeclaredConstructor().newInstance();
            final DataSourceFactory dataSourceFactory = PooledDataSourceFactory.class.getConstructor().newInstance();
            dataSourceFactory.setProperties(this.dbProperties);
            final DataSource dataSource = dataSourceFactory.getDataSource();
            Environment.Builder environmentBuilder = new Environment.Builder("development")
                    .transactionFactory(txFactory)
                    .dataSource(dataSource);
            final Environment environment = environmentBuilder.build();
            tx = txFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            Executor executor=null;
            if (execType==ExecutorType.SIMPLE){
                // TODO
//                executor = new SimpleExecutor(tx);
            }
            return new DefaultSqlSession(autoCommit,executor);
        } catch (Exception e) {
            closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {

        }
    }

    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
        try {
            boolean autoCommit;
            try {
                autoCommit = connection.getAutoCommit();
            } catch (SQLException e) {
                // Failover to true, as most poor drivers
                // or databases won't support transactions
                autoCommit = true;
            }
            final TransactionFactory txFactory = JdbcTransactionFactory.class.getDeclaredConstructor().newInstance();
            final DataSourceFactory dataSourceFactory = PooledDataSourceFactory.class.getConstructor().newInstance();
            dataSourceFactory.setProperties(this.dbProperties);
            final DataSource dataSource = dataSourceFactory.getDataSource();
            final Transaction tx=txFactory.newTransaction(connection);
            Executor executor=null;
            if (execType==ExecutorType.SIMPLE){
                // TODO
//                executor = new SimpleExecutor(tx);
            }
            return new DefaultSqlSession(autoCommit,executor);
        }catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
//            ErrorContext.instance().reset();
        }
    }

    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
}
