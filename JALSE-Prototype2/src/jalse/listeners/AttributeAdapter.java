package jalse.listeners;

import jalse.attributes.Attribute;

public abstract class AttributeAdapter<T extends Attribute> implements AttributeListener<T> {

    @Override
    public void attributeAdded(final T attr) {

    }

    @Override
    public void attributeChanged(final T attr) {

    }

    @Override
    public void attributeRemoved(final T attr) {

    }
}
