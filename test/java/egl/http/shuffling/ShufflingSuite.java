
package egl.http.shuffling;

import egl.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestShuffling.class,
        TestAutomatedShuffling.class
})

public class ShufflingSuite extends AbstractHttpApiSuite { }
