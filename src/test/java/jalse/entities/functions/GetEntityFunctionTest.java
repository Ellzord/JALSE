package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.GetEntity;
import jalse.entities.methods.GetEntityMethod;

public class GetEntityFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	Entity getTest(UUID id);
    }

    interface TestInvalidEntity2 extends Entity {

	@GetEntity
	Entity getTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@EntityID
	@GetEntity
	Entity getTest(UUID id);
    }

    interface TestInvalidEntity4 extends Entity {

	@GetEntity
	Entity getTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@GetEntity
	default Entity getTest(final UUID id) {
	    return null;
	}
    }

    interface TestInvalidEntity6 extends Entity {

	@GetEntity
	Entity getTest(UUID id, String str);
    }

    interface TestInvalidEntity7 extends Entity {

	@GetEntity
	Optional<Boolean> getTest(UUID id);
    }

    interface TestInvalidEntity8 extends Entity {

	@GetEntity
	Boolean getTest(UUID id);
    }

    interface TestValidEntity extends Entity {

	@GetEntity
	Entity getTest(UUID id);
    }

    interface TestValidEntity2 extends Entity {

	@EntityID
	@GetEntity
	Entity getTest();
    }

    interface TestValidEntity3 extends Entity {

	@GetEntity
	TestEntity getTest(UUID id);
    }

    interface TestValidEntity4 extends Entity {

	@GetEntity
	Optional<Entity> getTest(UUID id);
    }

    interface TestValidEntity5 extends Entity {

	@GetEntity
	Optional<TestEntity> getTest(UUID id);
    }

    GetEntityFunction function;

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
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity.class, UUID.class);

	final GetEntityMethod gem = function.apply(m);
	assertNull(gem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity3.class, UUID.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity5.class, UUID.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid6() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity6.class, UUID.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid7() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestInvalidEntity7.class, UUID.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestValidEntity.class, UUID.class);

	final GetEntityMethod gem = function.apply(m);
	assertNotNull(gem);

	assertNull(gem.getIDSupplier());
	assertFalse(gem.isOptional());
	assertEquals(Entity.class, gem.getType());
    }

    @Test
    public void testValid2() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestValidEntity2.class);

	final GetEntityMethod gem = function.apply(m);
	assertNotNull(gem);

	assertNotNull(gem.getIDSupplier());
	assertFalse(gem.isOptional());
	assertEquals(Entity.class, gem.getType());
    }

    @Test
    public void testValid3() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestValidEntity3.class, UUID.class);

	final GetEntityMethod gem = function.apply(m);
	assertNotNull(gem);

	assertNull(gem.getIDSupplier());
	assertFalse(gem.isOptional());
	assertEquals(TestEntity.class, gem.getType());
    }

    @Test
    public void testValid4() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestValidEntity4.class, UUID.class);

	final GetEntityMethod gem = function.apply(m);
	assertNotNull(gem);

	assertNull(gem.getIDSupplier());
	assertTrue(gem.isOptional());
	assertEquals(Entity.class, gem.getType());
    }

    @Test
    public void testValid5() {
	function = new GetEntityFunction();

	final Method m = getTestMethod(TestValidEntity5.class, UUID.class);

	final GetEntityMethod gem = function.apply(m);
	assertNotNull(gem);

	assertNull(gem.getIDSupplier());
	assertTrue(gem.isOptional());
	assertEquals(TestEntity.class, gem.getType());
    }
}
