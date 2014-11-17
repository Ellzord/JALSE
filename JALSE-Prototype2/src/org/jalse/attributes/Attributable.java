package org.jalse.attributes;

import java.util.Optional;
import java.util.Set;

import org.jalse.listeners.AttributeListener;

public interface Attributable {

    <T extends Attribute> boolean addListener(Class<T> attr, AttributeListener<T> listener);

    <T extends Attribute> Optional<T> associate(T attr);

    <T extends Attribute> Optional<T> disassociate(Class<T> attr);

    <T extends Attribute> boolean fireAttributeChanged(Class<T> attr);

    <T extends Attribute> Optional<T> getAttribute(Class<T> attr);

    <T extends Attribute> Set<AttributeListener<T>> getListeners(Class<T> attr);

    <T extends Attribute> boolean removeListener(Class<T> attr, AttributeListener<T> listener);
}
