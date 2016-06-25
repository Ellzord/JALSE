package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.MarkAsType;
import jalse.entities.methods.MarkAsTypeMethod;

public class MarkAsTypeFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	void markTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@MarkAsType(Entity.class)
	boolean markTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@MarkAsType(TestEntity.class)
	void markTest(String str);
    }

    interface TestInvalidEntity4 extends Entity {

	@MarkAsType(TestEntity.class)
	default void markTest() {}
    }

    interface TestInvalidEntity5 extends Entity {

	@MarkAsType(TestEntity.class)
	String markTest();
    }

    interface TestValidEntity extends Entity {

	@MarkAsType(TestEntity.class)
	boolean markTest();
    }

    interface TestValidEntity2 extends Entity {

	@MarkAsType(TestEntity.class)
	void markTest();
    }

    MarkAsTypeFunction function;

    @After
    public void after() {
	function = null;
    }

    public Method markTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("markTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestInvalidEntity.class);

	final MarkAsTypeMethod kem = function.apply(m);
	assertNull(kem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestInvalidEntity3.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestInvalidEntity4.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestValidEntity.class);

	final MarkAsTypeMethod kem = function.apply(m);
	assertNotNull(kem);

	assertEquals(kem.getType(), TestEntity.class);
    }

    @Test
    public void testValid2() {
	function = new MarkAsTypeFunction();

	final Method m = markTestMethod(TestValidEntity2.class);

	final MarkAsTypeMethod kem = function.apply(m);
	assertNotNull(kem);

	assertEquals(kem.getType(), TestEntity.class);
    }
}
