package org.example.cglib.session;

import java.io.*;
import java.util.Properties;

public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            Properties dbProperties = new Properties();
            dbProperties.load(reader);
            return new DefaultSqlSessionFactory(dbProperties);
        } catch (Exception e) {
            throw new RuntimeException("Error building SqlSession", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            //  Reader reader=new InputStreamReader(inputStream);
            Properties dbProperties = new Properties();
            dbProperties.load(inputStream);
            return new DefaultSqlSessionFactory(dbProperties);
        } catch (Exception e) {
            throw new RuntimeException("Error building SqlSession", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
}
