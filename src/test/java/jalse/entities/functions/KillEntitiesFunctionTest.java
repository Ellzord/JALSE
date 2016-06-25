package jalse.entities.functions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.KillEntities;
import jalse.entities.methods.KillEntitiesMethod;

public class KillEntitiesFunctionTest {

    interface TestInvalidEntity extends Entity {

	void killTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@KillEntities
	String killTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@KillEntities
	void killTest(String str);
    }

    interface TestInvalidEntity4 extends Entity {

	@KillEntities
	default void killTest() {}
    }

    interface TestValidEntity extends Entity {

	@KillEntities
	void killTest();
    }

    interface TestValidEntity2 extends Entity {

	@EntityID
	@KillEntities
	void killTest();
    }

    KillEntitiesFunction function;

    @After
    public void after() {
	function = null;
    }

    public Method killTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("killTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestInvalidEntity.class);

	final KillEntitiesMethod kem = function.apply(m);
	assertNull(kem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestInvalidEntity3.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestInvalidEntity4.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestValidEntity.class);

	final KillEntitiesMethod kem = function.apply(m);
	assertNotNull(kem);

	assertTrue(kem.getIDSuppliers().size() == 0);
    }

    @Test
    public void testValid2() {
	function = new KillEntitiesFunction();

	final Method m = killTestMethod(TestValidEntity2.class);

	final KillEntitiesMethod kem = function.apply(m);
	assertNotNull(kem);

	assertTrue(kem.getIDSuppliers().size() == 1);
    }
}
