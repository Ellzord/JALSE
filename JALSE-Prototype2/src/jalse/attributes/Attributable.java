package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Optional;
import java.util.Set;

public interface Attributable {

    <T extends Attribute> boolean addListener(Class<T> attr, AttributeListener<T> listener);

    <T extends Attribute> Optional<T> associate(T attr);

    <T extends Attribute> Optional<T> disassociate(Class<T> attr);

    <T extends Attribute> boolean fireAttributeChanged(Class<T> attr);

    <T extends Attribute> Optional<T> getAttribute(Class<T> attr);

    <T extends Attribute> Set<AttributeListener<T>> getListeners(Class<T> attr);

    <T extends Attribute> boolean removeListener(Class<T> attr, AttributeListener<T> listener);
}
