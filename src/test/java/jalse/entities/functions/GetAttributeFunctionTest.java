package jalse.entities.functions;

import static jalse.attributes.Attributes.BOOLEAN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import jalse.entities.Entity;
import jalse.entities.annotations.GetAttribute;
import jalse.entities.methods.GetAttributeMethod;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

public class GetAttributeFunctionTest {

    interface TestInvalidEntity extends Entity {

	void getTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@GetAttribute(name = "test")
	void getTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@GetAttribute
	Boolean getTest(String str);
    }

    interface TestInvalidEntity4 extends Entity {

	@GetAttribute
	default Boolean getTest() {
	    return false;
	}
    }

    interface TestValidOptEntity extends Entity {

	@GetAttribute(name = "test")
	Optional<Boolean> getTest();
    }

    interface TestValidWithNameEntity extends Entity {

	@GetAttribute(name = "test")
	Boolean getTest();
    }

    interface TestValidWithoutNameEntity extends Entity {

	@GetAttribute
	Boolean getTest();
    }

    GetAttributeFunction function = null;

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
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestInvalidEntity.class);

	final GetAttributeMethod gam = function.apply(m);
	assertNull(gam);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestInvalidEntity3.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestInvalidEntity4.class);

	function.apply(m);
    }

    @Test
    public void testValidOpt() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestValidOptEntity.class);

	final GetAttributeMethod gam = function.apply(m);
	assertNotNull(gam);

	assertEquals(gam.getName(), "test");
	assertEquals(gam.getType(), BOOLEAN_TYPE);
    }

    @Test
    public void testValidWithName() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestValidWithNameEntity.class);

	final GetAttributeMethod gam = function.apply(m);
	assertNotNull(gam);

	assertEquals(gam.getName(), "test");
	assertEquals(gam.getType(), BOOLEAN_TYPE);
    }

    @Test
    public void testValidWithoutName() {
	function = new GetAttributeFunction();

	final Method m = getTestMethod(TestValidWithoutNameEntity.class);

	final GetAttributeMethod gam = function.apply(m);
	assertNotNull(gam);

	assertEquals(gam.getName(), "test");
	assertEquals(gam.getType(), BOOLEAN_TYPE);
    }
}
