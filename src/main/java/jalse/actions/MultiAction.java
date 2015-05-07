package jalse.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class MultiAction<T> implements Action<T> {

    private class ActionsAndOperation {

	private final Collection<? extends Action<T>> actions;
	private final MultiActionOperation operation;

	private ActionsAndOperation(final Collection<? extends Action<T>> actions, final MultiActionOperation operation) {
	    this.actions = actions;
	    this.operation = operation;
	}
    }

    enum MultiActionOperation {

	PERFORM, SCHEDULE, SCHEDULE_AWAIT
    }

    private final List<ActionsAndOperation> operations;

    MultiAction() {
	operations = new ArrayList<>();
    }

    public void addOperation(final Action<T> action, final MultiActionOperation operation) {
	operations.add(new ActionsAndOperation(Collections.singletonList(action), operation));
    }

    public void addOperation(final Collection<? extends Action<T>> actions, final MultiActionOperation operation) {
	operations.add(new ActionsAndOperation(actions, operation));
    }

    public boolean hasOperations() {
	return !operations.isEmpty();
    }

    @Override
    public void perform(final ActionContext<T> context) throws InterruptedException {
	for (final ActionsAndOperation aao : operations) {
	    switch (aao.operation) {
	    case PERFORM:
		performAll(context, aao.actions);
		break;
	    case SCHEDULE:
		scheduleAll(context, aao.actions);
		break;
	    case SCHEDULE_AWAIT:
		scheduleAwaitAll(context, aao.actions);
		break;
	    }
	}
    }

    private void performAll(final ActionContext<T> context, final Collection<? extends Action<T>> actions)
	    throws InterruptedException {
	for (final Action<T> action : actions) {
	    action.perform(context); // Execute action
	}
    }

    private Collection<MutableActionContext<T>> scheduleAll(final ActionContext<T> context,
	    final Collection<? extends Action<T>> actions) {
	final Collection<MutableActionContext<T>> newContexts = new ArrayList<>();

	final ActionEngine engine = context.getEngine();
	final T actor = context.getActor();

	for (final Action<T> action : actions) {
	    final MutableActionContext<T> newContext = engine.newContext(action);
	    newContext.setActor(actor); // Same actor
	    newContext.putAll(context.toMap()); // Copy bindings (current)
	    newContext.schedule();

	    newContexts.add(newContext);
	}

	return newContexts;
    }

    private void scheduleAwaitAll(final ActionContext<T> context, final Collection<? extends Action<T>> actions)
	    throws InterruptedException {
	for (final MutableActionContext<T> newContext : scheduleAll(context, actions)) {
	    if (!newContext.isDone()) { // Stops us for waiting forever
		newContext.await();
	    }
	}
    }
}
