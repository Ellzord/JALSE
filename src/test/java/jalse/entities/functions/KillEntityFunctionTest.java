package jalse.entities.functions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import jalse.entities.Entity;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.KillEntity;
import jalse.entities.methods.KillEntityMethod;

public class KillEntityFunctionTest {

    interface TestInvalidEntity extends Entity {

	boolean killTest(UUID id);
    }

    interface TestInvalidEntity2 extends Entity {

	@KillEntity
	boolean killTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@EntityID
	@KillEntity
	void killTest(UUID id);
    }

    interface TestInvalidEntity4 extends Entity {

	@KillEntity
	void killTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@KillEntity
	default boolean killTest(final UUID id) {
	    return false;
	}
    }

    interface TestInvalidEntity6 extends Entity {

	@KillEntity
	boolean killTest(UUID id, String str);
    }

    interface TestInvalidEntity7 extends Entity {

	@KillEntity
	String killTest(UUID id);
    }

    interface TestValidEntity extends Entity {

	@KillEntity
	boolean killTest(UUID id);
    }

    interface TestValidEntity2 extends Entity {

	@EntityID
	@KillEntity
	boolean killTest();
    }

    interface TestValidEntity3 extends Entity {

	@KillEntity
	void killTest(UUID id);
    }

    interface TestValidEntity4 extends Entity {

	@EntityID
	@KillEntity
	void killTest();
    }

    KillEntityFunction function;

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
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity.class, UUID.class);

	final KillEntityMethod gem = function.apply(m);
	assertNull(gem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity3.class, UUID.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity5.class, UUID.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid6() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity6.class, UUID.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid7() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestInvalidEntity7.class, UUID.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestValidEntity.class, UUID.class);

	final KillEntityMethod kem = function.apply(m);
	assertNotNull(kem);

	assertNull(kem.getIDSupplier());
    }

    @Test
    public void testValid2() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestValidEntity2.class);

	final KillEntityMethod kem = function.apply(m);
	assertNotNull(kem);

	assertNotNull(kem.getIDSupplier());
    }

    @Test
    public void testValid3() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestValidEntity3.class, UUID.class);

	final KillEntityMethod kem = function.apply(m);
	assertNotNull(kem);

	assertNull(kem.getIDSupplier());
    }

    @Test
    public void testValid4() {
	function = new KillEntityFunction();

	final Method m = killTestMethod(TestValidEntity4.class);

	final KillEntityMethod kem = function.apply(m);
	assertNotNull(kem);

	assertNotNull(kem.getIDSupplier());
    }
}
