
package egl.db;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Create Statement and PrepareStatement for use with FilteredConnection
 */
public interface FilteredFactory {

    /**
     * Create a FilteredStatement for the supplied Statement
     *
     * @param   stmt                Statement
     * @return                      Wrapped statement
     */
    Statement createStatement(Statement stmt);

    /**
     * Create a FilteredPreparedStatement for the supplied PreparedStatement
     *
     * @param   stmt                Prepared statement
     * @param   sql                 SQL statement
     * @return                      Wrapped prepared statement
     */
    PreparedStatement createPreparedStatement(PreparedStatement stmt, String sql);
}
