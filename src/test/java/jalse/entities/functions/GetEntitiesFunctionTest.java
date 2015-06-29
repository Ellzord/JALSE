package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.GetEntities;
import jalse.entities.methods.GetEntitiesMethod;

public class GetEntitiesFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	Set<Entity> getTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@GetEntities
	Boolean getTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@GetEntities
	Set<Boolean> getTest();
    }

    interface TestInvalidEntity4 extends Entity {

	@GetEntities
	Set<Entity> getTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@GetEntities
	default Set<Entity> getTest() {
	    return null;
	}
    }

    interface TestValidEntity extends Entity {

	@GetEntities
	Set<Entity> getTest();
    }

    interface TestValidEntity2 extends Entity {

	@GetEntities
	Set<TestEntity> getTest();
    }

    interface TestValidEntity3 extends Entity {

	@GetEntities(ofType = false)
	Set<TestEntity> getTest();
    }

    interface TestValidEntity4 extends Entity {

	@EntityID
	@GetEntities
	Set<Entity> getTest();
    }

    interface TestValidEntity5 extends Entity {

	@EntityID
	@GetEntities
	Set<TestEntity> getTest();
    }

    GetEntitiesFunction function = null;

    @After
    public void after() {
	function = null;
    }

    public Method getTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("getTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestInvalidEntity.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNull(gem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestInvalidEntity3.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestValidEntity.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNotNull(gem);

	assertTrue(gem.getIDSuppliers().size() == 0);
	assertTrue(gem.isOfType());
	assertEquals(Entity.class, gem.getType());
    }

    @Test
    public void testValid2() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestValidEntity2.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNotNull(gem);

	assertTrue(gem.getIDSuppliers().size() == 0);
	assertTrue(gem.isOfType());
	assertEquals(TestEntity.class, gem.getType());
    }

    @Test
    public void testValid3() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestValidEntity3.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNotNull(gem);

	assertTrue(gem.getIDSuppliers().size() == 0);
	assertFalse(gem.isOfType());
	assertEquals(TestEntity.class, gem.getType());
    }

    @Test
    public void testValid4() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestValidEntity4.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNotNull(gem);

	assertTrue(gem.getIDSuppliers().size() == 1);
	assertTrue(gem.isOfType());
	assertEquals(Entity.class, gem.getType());
    }

    @Test
    public void testValid5() {
	function = new GetEntitiesFunction();

	final Method m = getTestMethod(TestValidEntity5.class);

	final GetEntitiesMethod gem = function.apply(m);
	assertNotNull(gem);

	assertTrue(gem.getIDSuppliers().size() == 1);
	assertTrue(gem.isOfType());
	assertEquals(TestEntity.class, gem.getType());
    }
}
