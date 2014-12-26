package jalse.listeners;

import jalse.attributes.Attribute;

public interface AttributeListener<T extends Attribute> {

    void attributeAdded(T attr);

    void attributeChanged(T attr);

    void attributeRemoved(T attr);
}
