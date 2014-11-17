package org.jalse.listeners;

import org.jalse.attributes.Attribute;
import org.jalse.wrappers.AgentWrapper;

public abstract class SharedAttributeAdapter<T extends Attribute> implements SharedAttributeListener<T> {

    @Override
    public void attributeAdded(final AgentWrapper agent, final T attr) {

    }

    @Override
    public void attributeChanged(final AgentWrapper agent, final T attr) {

    }

    @Override
    public void attributeRemoved(final AgentWrapper agent, final T attr) {

    }
}
