
package egl.http.accountControl;

import egl.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PhasingOnlyTest.class
})

public class AccountControlSuite extends AbstractHttpApiSuite { }
