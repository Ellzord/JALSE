package jalse.actions;

import jalse.misc.Engine.TickInfo;

@SuppressWarnings({ "rawtypes", "unchecked" })
class ActionWithActor {

    private Action action;
    private Object actor;

    ActionWithActor() {
	action = null;
	actor = null;
    }

    public synchronized void perform(final TickInfo tick) {
	if (action != null && actor != null) {
	    action.perform(actor, tick);
	}
    }

    public synchronized void set(final Action action, final Object actor) {
	if (action == null && actor != null || action != null && actor == null) {
	    throw new NullPointerException();
	}

	this.action = action;
	this.actor = actor;
    }
}