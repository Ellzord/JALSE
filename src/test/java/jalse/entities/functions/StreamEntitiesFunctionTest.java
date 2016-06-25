package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.StreamEntities;
import jalse.entities.methods.StreamEntitiesMethod;

public class StreamEntitiesFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	Stream<Entity> streamTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@StreamEntities
	Boolean streamTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@StreamEntities
	Stream<Boolean> streamTest();
    }

    interface TestInvalidEntity4 extends Entity {

	@StreamEntities
	Stream<Entity> streamTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@StreamEntities
	default Stream<Entity> streamTest() {
	    return null;
	}
    }

    interface TestValidEntity extends Entity {

	@StreamEntities
	Stream<Entity> streamTest();
    }

    interface TestValidEntity2 extends Entity {

	@StreamEntities
	Stream<TestEntity> streamTest();
    }

    interface TestValidEntity3 extends Entity {

	@StreamEntities(ofType = false)
	Stream<TestEntity> streamTest();
    }

    interface TestValidEntity4 extends Entity {

	@EntityID
	@StreamEntities
	Stream<Entity> streamTest();
    }

    interface TestValidEntity5 extends Entity {

	@EntityID
	@StreamEntities
	Stream<TestEntity> streamTest();
    }

    StreamEntitiesFunction function;

    @After
    public void after() {
	function = null;
    }

    public Method newTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("streamTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestInvalidEntity.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNull(sem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestInvalidEntity3.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestValidEntity.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNotNull(sem);

	assertTrue(sem.getIDSuppliers().size() == 0);
	assertTrue(sem.isOfType());
	assertEquals(Entity.class, sem.getType());
    }

    @Test
    public void testValid2() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestValidEntity2.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNotNull(sem);

	assertTrue(sem.getIDSuppliers().size() == 0);
	assertTrue(sem.isOfType());
	assertEquals(TestEntity.class, sem.getType());
    }

    @Test
    public void testValid3() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestValidEntity3.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNotNull(sem);

	assertTrue(sem.getIDSuppliers().size() == 0);
	assertFalse(sem.isOfType());
	assertEquals(TestEntity.class, sem.getType());
    }

    @Test
    public void testValid4() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestValidEntity4.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNotNull(sem);

	assertTrue(sem.getIDSuppliers().size() == 1);
	assertTrue(sem.isOfType());
	assertEquals(Entity.class, sem.getType());
    }

    @Test
    public void testValid5() {
	function = new StreamEntitiesFunction();

	final Method m = newTestMethod(TestValidEntity5.class);

	final StreamEntitiesMethod sem = function.apply(m);
	assertNotNull(sem);

	assertTrue(sem.getIDSuppliers().size() == 1);
	assertTrue(sem.isOfType());
	assertEquals(TestEntity.class, sem.getType());
    }
}
