package jalse.entities.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.MutableActionContext;
import jalse.entities.Entity;
import jalse.entities.annotations.ScheduleForActor;
import jalse.entities.methods.ScheduleForActorMethod;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

public class ScheduleForActorFunctionTest {

    public static class TestAction implements Action<Entity> {

	@Override
	public void perform(final ActionContext<Entity> context) throws InterruptedException {}
    }

    interface TestInvalidEntity extends Entity {

	MutableActionContext<Entity> scheduleTest();
    }

    interface TestInvalidEntity2 extends Entity {

	@ScheduleForActor(action = TestAction.class)
	Boolean scheduleTest();
    }

    interface TestInvalidEntity3 extends Entity {

	@ScheduleForActor(action = TestAction.class)
	MutableActionContext<Boolean> scheduleTest();
    }

    interface TestInvalidEntity4 extends Entity {

	@ScheduleForActor(action = TestAction.class)
	void scheduleTest(String str);
    }

    interface TestInvalidEntity5 extends Entity {

	@ScheduleForActor(action = TestAction.class, initialDelay = -1L, period = -1L)
	void scheduleTest();
    }

    interface TestValidEntity extends Entity {

	@ScheduleForActor(action = TestAction.class)
	MutableActionContext<Entity> scheduleTest();
    }

    interface TestValidEntity2 extends Entity {

	@ScheduleForActor(action = TestAction.class)
	void scheduleTest();
    }

    interface TestValidEntity3 extends Entity {

	@ScheduleForActor(action = TestAction.class, initialDelay = 50, unit = TimeUnit.MILLISECONDS)
	void scheduleTest();
    }

    interface TestValidEntity4 extends Entity {

	@ScheduleForActor(action = TestAction.class, initialDelay = 50, period = 200, unit = TimeUnit.MILLISECONDS)
	void scheduleTest();
    }

    ScheduleForActorFunction function = null;

    @After
    public void after() {
	function = null;
    }

    public Method scheduleTestMethod(final Class<?> clazz, final Class<?>... params) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("scheduleTest", params);
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testInvalid() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestInvalidEntity.class);

	final ScheduleForActorMethod sfam = function.apply(m);
	assertNull(sfam);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestInvalidEntity2.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid3() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestInvalidEntity3.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid4() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestInvalidEntity4.class, String.class);

	function.apply(m);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid5() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestInvalidEntity5.class);

	function.apply(m);
    }

    @Test
    public void testValid() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestValidEntity.class);

	final ScheduleForActorMethod sfam = function.apply(m);
	assertNotNull(sfam);

	assertTrue(sfam.getInitialDelay() == 0L);
	assertTrue(sfam.getPeriod() == 0L);
	assertEquals(sfam.getUnit(), TimeUnit.NANOSECONDS);
    }

    @Test
    public void testValid2() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestValidEntity2.class);

	final ScheduleForActorMethod sfam = function.apply(m);
	assertNotNull(sfam);

	assertTrue(sfam.getInitialDelay() == 0L);
	assertTrue(sfam.getPeriod() == 0L);
	assertEquals(sfam.getUnit(), TimeUnit.NANOSECONDS);
    }

    @Test
    public void testValid3() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestValidEntity3.class);

	final ScheduleForActorMethod sfam = function.apply(m);
	assertNotNull(sfam);

	assertTrue(sfam.getInitialDelay() == 50L);
	assertTrue(sfam.getPeriod() == 0L);
	assertEquals(sfam.getUnit(), TimeUnit.MILLISECONDS);
    }

    @Test
    public void testValid4() {
	function = new ScheduleForActorFunction();

	final Method m = scheduleTestMethod(TestValidEntity4.class);

	final ScheduleForActorMethod sfam = function.apply(m);
	assertNotNull(sfam);

	assertTrue(sfam.getInitialDelay() == 50L);
	assertTrue(sfam.getPeriod() == 200L);
	assertEquals(sfam.getUnit(), TimeUnit.MILLISECONDS);
    }
}
