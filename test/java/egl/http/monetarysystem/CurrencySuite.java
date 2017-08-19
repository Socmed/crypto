
package egl.http.monetarysystem;

import egl.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestCurrencyIssuance.class,
        TestCurrencyExchange.class,
        TestCurrencyReserveAndClaim.class,
        TestCurrencyMint.class,
        egl.TestMintCalculations.class,
        DeleteCurrencyTest.class,
})

public class CurrencySuite extends AbstractHttpApiSuite { }
