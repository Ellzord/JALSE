package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.UnmarkAsType;
import jalse.entities.methods.UnmarkAsTypeMethod;

public class UnmarkAsTypeFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	void unmarkTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@UnmarkAsType(Entity.class)
	boolean unmarkTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@UnmarkAsType(TestEntity.class)
	void unmarkTest(String str);
    }

    interface TestInvalidEntity4 extends Entity {

	@UnmarkAsType(TestEntity.class)
	default void unmarkTest() {}
    }

    interface TestInvalidEntity5 extends Entity {

	@UnmarkAsType(TestEntity.class)
	String unmarkTest();
    }

    interface TestValidEntity extends Entity {

	@UnmarkAsType(TestEntity.class)
	boolean unmarkTest();
    }

    interface TestValidEntity2 extends Entity {

	@UnmarkAsType(TestEntity.class)
	void unmarkTest();
    }

    UnmarkAsTypeFunction function;

    @After
    public void after() {
	function = null;
    }

    @Test
    public void testInvalid() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestInvalidEntity.class);

	final UnmarkAsTypeMethod kem = function.apply(m);
	assertNull(kem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestInvalidEntity3.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestInvalidEntity4.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestValidEntity.class);

	final UnmarkAsTypeMethod kem = function.apply(m);
	assertNotNull(kem);

	assertEquals(kem.getType(), TestEntity.class);
    }

    @Test
    public void testValid2() {
	function = new UnmarkAsTypeFunction();

	final Method m = unmarkTestMethod(TestValidEntity2.class);

	final UnmarkAsTypeMethod kem = function.apply(m);
	assertNotNull(kem);

	assertEquals(kem.getType(), TestEntity.class);
    }

    public Method unmarkTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("unmarkTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }
}
