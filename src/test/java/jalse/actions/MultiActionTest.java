package jalse.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jalse.actions.MultiAction.OperationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class MultiActionTest {

    public static class Builder {

	MultiAction<String> multi;

	@After
	public void after() {
	    multi = null;
	}

	@Test
	public void testBuilder1() {
	    multi = new MultiAction.Builder<String>().addPerform(new TestAction()).build();
	    assertNotNull(multi);
	}

	@Test
	public void testBuilder2() {
	    final List<Action<String>> lst = new ArrayList<>();
	    lst.add(new TestAction());
	    multi = new MultiAction.Builder<String>().addPerform(lst).build();
	    assertNotNull(multi);
	}

	@Test
	public void testBuilder3() {
	    multi = new MultiAction.Builder<String>().addSchedule(new TestAction()).build();
	    assertNotNull(multi);
	}

	@Test
	public void testBuilder4() {
	    final List<Action<String>> lst = new ArrayList<>();
	    lst.add(new TestAction());
	    multi = new MultiAction.Builder<String>().addSchedule(lst).build();
	    assertNotNull(multi);
	}

	@Test
	public void testBuilder5() {
	    multi = new MultiAction.Builder<String>().addScheduleAndAwait(new TestAction()).build();
	    assertNotNull(multi);
	}

	@Test
	public void testBuilder6() {
	    final List<Action<String>> lst = new ArrayList<>();
	    lst.add(new TestAction());
	    multi = new MultiAction.Builder<String>().addScheduleAndAwait(lst).build();
	    assertNotNull(multi);
	}
    }

    static class TestAction implements Action<String> {

	@Override
	public void perform(final ActionContext<String> context) throws InterruptedException {}
    }
    
    MultiAction<String> multi;

    @After
    public void after() {
	multi = null;
    }
    
    @Test
    public void testAddOperation() {
	multi = new MultiAction<String>();

	final MultiAction.ActionOperation<String> op = new MultiAction.ActionOperation<String>(OperationType.PERFORM,
		new TestAction());
	multi.add(op);
    }

    @Test
    public void testBuildChainActionOfSArray() {
	multi = MultiAction.buildChain(new TestAction());
	assertFalse(multi.isEmpty());
    }

    @Test
    public void testBuildChainListOfQextendsActionOfS() {
	final List<Action<String>> lst = new ArrayList<>();
	lst.add(new TestAction());
	multi = MultiAction.buildChain(lst);
	assertFalse(multi.isEmpty());
    }

    @Test
    public void testHasOperations() {
	multi = new MultiAction<String>();

	assertTrue(multi.isEmpty());

	final MultiAction.ActionOperation<String> op = new MultiAction.ActionOperation<String>(OperationType.PERFORM,
		new TestAction());
	multi.add(op);

	assertFalse(multi.isEmpty());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testActionOperationConstructorWithEmptyCollection() {
	Collection<TestAction> emptyList = Collections.emptyList();
	new MultiAction.ActionOperation<String>(OperationType.PERFORM, emptyList);
    }
    
    @Test
    public void testPerform() throws InterruptedException {
	multi = new MultiAction<String>();
	final MultiAction.ActionOperation<String> op = new MultiAction.ActionOperation<String>(OperationType.PERFORM,
		new TestAction());
	multi.add(op);
	
	UnschedulableDelegateActionContext<String> actionContext = new UnschedulableDelegateActionContext<>(null);
	multi.perform(actionContext);
    }
}
