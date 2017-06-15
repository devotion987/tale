package com.tale.init;

import com.blade.kit.ClassKit;
import com.tale.constants.TaleConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Sqlite 数据库操作
 * <p>
 * Created by biezhi on 2017/3/4.
 */
public final class SqliteJdbc {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteJdbc.class);

    private SqliteJdbc() {
    }

    private static final String DB_NAME = "tale.db";
    public static String DB_PATH = TaleConst.CLASSPATH + DB_NAME;
    public static String DB_SRC = "jdbc:sqlite://" + DB_PATH;

    static {
        ClassKit.loadClass("org.sqlite.JDBC");
    }

    /**
     * 测试连接并导入数据库
     */
    @SuppressWarnings("resource")
    public static void importSql(boolean devMode) {
        try {
            if (devMode) {
                DB_PATH = System.getProperty("user.dir") + "/" + DB_NAME;
                DB_SRC = "jdbc:sqlite://" + DB_PATH;
            }
            Connection con = DriverManager.getConnection(DB_SRC);
            Statement statement = con.createStatement();
            ResultSet rs = statement
                    .executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='t_options'");
            int count = rs.getInt(1);
            if (count == 0) {
                String cp = TaleConst.CLASSPATH;
                InputStreamReader isr = new InputStreamReader(new FileInputStream(cp + "schema.sql"), "UTF-8");
                String sql = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
                statement.executeUpdate(sql);
                LOGGER.info("initialize import database.");
            }
            rs.close();
            statement.close();
            con.close();
            LOGGER.info("database path is: {}", DB_PATH);
        } catch (Exception e) {
            LOGGER.error("initialize database fail", e);
        }
    }

}
