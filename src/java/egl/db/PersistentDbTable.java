
package egl.db;

public abstract class PersistentDbTable<T> extends EntityDbTable<T> {

    protected PersistentDbTable(String table, DbKey.Factory<T> dbKeyFactory) {
        super(table, dbKeyFactory, false, null);
    }

    protected PersistentDbTable(String table, DbKey.Factory<T> dbKeyFactory, String fullTextSearchColumns) {
        super(table, dbKeyFactory, false, fullTextSearchColumns);
    }

    PersistentDbTable(String table, DbKey.Factory<T> dbKeyFactory, boolean multiversion, String fullTextSearchColumns) {
        super(table, dbKeyFactory, multiversion, fullTextSearchColumns);
    }

    @Override
    public void rollback(int height) {
    }

    @Override
    public final void truncate() {
    }

    @Override
    public final boolean isPersistent() {
        return true;
    }

}
