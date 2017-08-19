
package egl;

import egl.db.BasicDb;
import egl.db.TransactionalDb;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "egl.testDb" : "egl.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(Eagle.getIntProperty("egl.dbCacheKB"))
            .dbUrl(Eagle.getStringProperty(PREFIX + "Url"))
            .dbType(Eagle.getStringProperty(PREFIX + "Type"))
            .dbDir(Eagle.getStringProperty(PREFIX + "Dir"))
            .dbParams(Eagle.getStringProperty(PREFIX + "Params"))
            .dbUsername(Eagle.getStringProperty(PREFIX + "Username"))
            .dbPassword(Eagle.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(Eagle.getIntProperty("egl.maxDbConnections"))
            .loginTimeout(Eagle.getIntProperty("egl.dbLoginTimeout"))
            .defaultLockTimeout(Eagle.getIntProperty("egl.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(Eagle.getIntProperty("egl.dbMaxMemoryRows"))
    );

    public static void init() {
        db.init(new EagleDbVersion());
    }

    static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
