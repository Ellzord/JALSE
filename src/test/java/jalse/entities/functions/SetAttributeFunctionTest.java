package jalse.entities.functions;

import static jalse.attributes.Attributes.BOOLEAN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import jalse.entities.Entity;
import jalse.entities.annotations.SetAttribute;
import jalse.entities.methods.SetAttributeMethod;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

public class SetAttributeFunctionTest {

    interface TestInvalidEntity extends Entity {

	void setTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@SetAttribute(name = "test")
	void setTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@SetAttribute(name = "test")
	void setTest(boolean test);
    }

    interface TestInvalidEntity4 extends Entity {

	@SetAttribute
	Boolean setTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@SetAttribute
	default void setTest(final Boolean test) {}
    }

    interface TestInvalidOptEntity extends Entity {

	@SetAttribute
	Optional<Boolean> setTest(String test);
    }

    interface TestValidOptEntity extends Entity {

	@SetAttribute(name = "test")
	Optional<Boolean> setTest(Boolean test);
    }

    interface TestValidWithNameEntity extends Entity {

	@SetAttribute(name = "test")
	void setTest(Boolean test);
    }

    interface TestValidWithoutNameEntity extends Entity {

	@SetAttribute
	void setTest(Boolean test);
    }

    SetAttributeFunction function = null;

    @After
    public void after() {
	function = null;
    }

    public Method setTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("setTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidEntity.class);

	final SetAttributeMethod sam = function.apply(m);
	assertNull(sam);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidEntity3.class, boolean.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidEntity5.class, Boolean.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOpt() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestInvalidOptEntity.class, String.class);

	function.apply(m);
    }

    @Test
    public void testValidOpt() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestValidOptEntity.class, Boolean.class);

	final SetAttributeMethod sam = function.apply(m);
	assertNotNull(sam);

	assertEquals(sam.getName(), "test");
	assertEquals(sam.getType(), BOOLEAN_TYPE);
    }

    @Test
    public void testValidWithName() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestValidWithNameEntity.class, Boolean.class);

	final SetAttributeMethod sam = function.apply(m);
	assertNotNull(sam);

	assertEquals(sam.getName(), "test");
	assertEquals(sam.getType(), BOOLEAN_TYPE);
    }

    @Test
    public void testValidWithoutName() {
	function = new SetAttributeFunction();

	final Method m = setTestMethod(TestValidWithoutNameEntity.class, Boolean.class);

	final SetAttributeMethod sam = function.apply(m);
	assertNotNull(sam);

	assertEquals(sam.getName(), "test");
	assertEquals(sam.getType(), BOOLEAN_TYPE);
    }
}
