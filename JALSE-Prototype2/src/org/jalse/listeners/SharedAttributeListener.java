package org.jalse.listeners;

import org.jalse.attributes.Attribute;
import org.jalse.wrappers.AgentWrapper;

public interface SharedAttributeListener<T extends Attribute> {

    static boolean isSharedListener(final AttributeListener<?> listener) {

	final Class<?> clazz = listener.getClass();

	return clazz.isAnonymousClass() && SharedAttributeListener.class.equals(clazz.getEnclosingClass());
    }

    static <T extends Attribute> AttributeListener<T> toAttributeListener(final SharedAttributeListener<T> listener,
	    final AgentWrapper agent) {

	return new AttributeListener<T>() {

	    @Override
	    public void attributeAdded(final T attr) {

		listener.attributeAdded(agent, attr);
	    }

	    @Override
	    public void attributeChanged(final T attr) {

		listener.attributeChanged(agent, attr);
	    }

	    @Override
	    public void attributeRemoved(final T attr) {

		listener.attributeRemoved(agent, attr);
	    }

	    @Override
	    public boolean equals(final Object obj) {

		return obj == listener;
	    }

	    @Override
	    public int hashCode() {

		return listener.hashCode();
	    }
	};
    }

    void attributeAdded(AgentWrapper agent, T attr);

    void attributeChanged(AgentWrapper agent, T attr);

    void attributeRemoved(AgentWrapper agent, T attr);
}
