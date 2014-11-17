package org.jalse.listeners;

import org.jalse.attributes.Attribute;

public interface AttributeListener<T extends Attribute> {

    void attributeAdded(T attr);

    void attributeChanged(T attr);

    void attributeRemoved(T attr);
}
