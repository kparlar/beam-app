package com.kparlar.beam.util;

import com.kparlar.beam.Configuration;
import com.kparlar.beam.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import org.apache.commons.dbcp2.BasicDataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

@Slf4j
public class DBConnectionUtil implements Serializable {

    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    private static transient BasicDataSource basicDataSource;
    private static transient SqlSessionFactory sqlSessionFactory;

    public static synchronized void init(Configuration configuration) {
        DBConnectionUtil.dbUrl = Objects.requireNonNull(configuration.getDbUrl());
        DBConnectionUtil.dbUser = Objects.requireNonNull(configuration.getDbUser());
        DBConnectionUtil.dbPass = configuration.getDbPassword();
    }

    private static BasicDataSource createNewPooledDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPass);
        ds.setDefaultAutoCommit(false);
        ds.setMaxTotal(16);
        ds.setMaxIdle(16);
        ds.setMaxConnLifetimeMillis(3_600_000); // 60 minutes
        ds.setMaxWaitMillis(60_000); // 1 minute
        ds.setLogExpiredConnections(true);
        return ds;
    }
    private static synchronized BasicDataSource getDatabaseConnectionPool() {
        if (basicDataSource == null) {
            log.info("Creating new database connection pool");
            basicDataSource = createNewPooledDataSource();
        } else {
            log.debug("Re-using database connection pool");
        }
        return basicDataSource;
    }
    private static synchronized SqlSessionFactory getSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            log.info("Creating new SQL session factory");
            sqlSessionFactory = createNewSqlConnectionFactoryWithRepositories();
        } else {
            log.debug("Re-using SQL session factory");
        }
        return sqlSessionFactory;
    }
    private static SqlSessionFactory createNewSqlConnectionFactoryWithRepositories() {
        return createNewSqlConnectionFactory(
                FileRepository.class);
    }

    private static SqlSessionFactory createNewSqlConnectionFactory(Class... daoClasses) {
        Environment environment = new Environment("default", new JdbcTransactionFactory(), getDatabaseConnectionPool());
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration(environment);

        for (Class daoClass : daoClasses) {
            config.addMapper(daoClass);
        }

        return new SqlSessionFactoryBuilder().build(config);
    }
    /**
     * Opens a connection and creates a new sql session that can be used to interact with the database
     * within the instance of {@link RunInTransactionWithNumericResult}.
     * <p>
     * If the instance of {@link RunInTransactionWithNumericResult} throws an exception then the connection is closed without committing,
     * otherwise the session is committed before the connection is closed.
     * <p>
     * Doing this programmatically (instead of using annotations) because this is a dataflow application.
     * Meaning that Spring like integration options cannot be used.
     *
     * @return the number of rows that were updated/affected
     */
    public static int runInTransactionWithNumericResult(RunInTransactionWithNumericResult runInTransaction) throws SQLException {
        try (Connection conn = DBConnectionUtil.getConnection()) {
            SqlSession sqlSession = getSqlSessionFactory().openSession(conn);
            final int result = runInTransaction.run(sqlSession);
            sqlSession.commit();
            sqlSession.close();
            return result;
        }
    }


    private static Connection getConnection() throws SQLException {
        try {
            final BasicDataSource dataSource = getDatabaseConnectionPool();

            final StopWatch stopWatch = StopWatch.createStarted();

            final Connection connection = dataSource.getConnection();

            stopWatch.stop();
            final long waitTime = stopWatch.getTime();
            if (waitTime > 20_000) {
                log.warn("It took {}ms to get a DB connection (active={}/{}, idle={}/{})",
                        waitTime, dataSource.getNumActive(), dataSource.getMaxTotal(), dataSource.getNumIdle(), dataSource.getMaxIdle());
                // Uncomment this to get a more fine grained insight in the database connection performance
                //} else if (waitTime > 5_000) {
                //    log.info("It took {}ms to get a DB connection (active={}/{}, idle={}/{})",
                //            waitTime, dataSource.getNumActive(), dataSource.getMaxTotal(), dataSource.getNumIdle(), dataSource.getMaxIdle()
                //    );
                //} else if (waitTime < 1000){
                //    log.info("It took {}ms to get a DB connection (active={}/{}, idle={}/{})",
                //            waitTime, dataSource.getNumActive(), dataSource.getMaxTotal(), dataSource.getNumIdle(), dataSource.getMaxIdle()
                //    );
            }

            return connection;
        } catch (Exception ex) {
            log.error("Error while trying to get connection from pool. Falling back on DriverManager approach.", ex);
            final Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            return connection;
        }
    }

    /**
     * Implementing classes are expected to throw their error
     * so errors can be detected allowing for the sql session to be rolled back
     */
    public interface RunInTransactionWithNumericResult {
        int run(SqlSession sqlSession) throws SQLException;
    }


}
