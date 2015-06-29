package jalse.entities.functions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import jalse.attributes.AttributeContainer;
import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.NewEntity;
import jalse.entities.methods.NewEntityMethod;

public class NewEntityFunctionTest {

    interface TestEntity extends Entity {}

    interface TestInvalidEntity extends Entity {

	Entity newTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@NewEntity
	void newTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@EntityID
	@NewEntity
	Entity newTest(UUID id);
    }

    interface TestInvalidEntity4 extends Entity {

	@NewEntity
	Entity newTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@NewEntity
	default Entity newTest() {
	    return null;
	}
    }

    interface TestInvalidEntity6 extends Entity {

	@NewEntity
	Entity newTest(UUID id, Class<? extends Entity> type, AttributeContainer container);
    }

    interface TestValidEntity extends Entity {

	@NewEntity
	Entity newTest();
    }

    interface TestValidEntity2 extends Entity {

	@NewEntity
	Entity newTest(UUID id);
    }

    interface TestValidEntity3 extends Entity {

	@EntityID
	@NewEntity
	Entity newTest();
    }

    interface TestValidEntity4 extends Entity {

	@NewEntity
	Entity newTest(UUID id, AttributeContainer container);
    }

    interface TestValidEntity5 extends Entity {

	@NewEntity
	TestEntity newTest();
    }

    interface TestValidEntity6 extends Entity {

	@NewEntity
	Entity newTest(AttributeContainer container);
    }

    interface TestValidEntity7 extends Entity {

	@EntityID
	@NewEntity
	Entity newTest(AttributeContainer container);
    }

    NewEntityFunction function = null;

    @After
    public void after() {
	function = null;
    }

    public Method newTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("newTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity.class);

	final NewEntityMethod nem = function.apply(m);
	assertNull(nem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity3.class, UUID.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid6() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestInvalidEntity6.class, UUID.class, Class.class, AttributeContainer.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertFalse(nem.requiresContainerParam());
	assertFalse(nem.requiresIDParam());
    }

    @Test
    public void testValid2() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity2.class, UUID.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertFalse(nem.requiresContainerParam());
	assertTrue(nem.requiresIDParam());
    }

    @Test
    public void testValid3() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity3.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertFalse(nem.requiresContainerParam());
	assertFalse(nem.requiresIDParam());
    }

    @Test
    public void testValid4() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity4.class, UUID.class, AttributeContainer.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertTrue(nem.requiresContainerParam());
	assertTrue(nem.requiresIDParam());
    }

    @Test
    public void testValid5() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity5.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertFalse(nem.requiresContainerParam());
	assertFalse(nem.requiresIDParam());
    }

    @Test
    public void testValid6() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity6.class, AttributeContainer.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertTrue(nem.requiresContainerParam());
	assertFalse(nem.requiresIDParam());
    }

    @Test
    public void testValid7() {
	function = new NewEntityFunction();

	final Method m = newTestMethod(TestValidEntity7.class, AttributeContainer.class);

	final NewEntityMethod nem = function.apply(m);
	assertNotNull(nem);

	assertTrue(nem.requiresContainerParam());
	assertFalse(nem.requiresIDParam());
    }
}
