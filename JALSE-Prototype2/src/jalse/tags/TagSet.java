package jalse.tags;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TagSet extends AbstractSet<Tag> implements Serializable {

    private static final long serialVersionUID = 4251919034814631329L;

    private final Map<Class<?>, Tag> tags;

    public TagSet() {

	tags = new ConcurrentHashMap<>();
    }

    @Override
    public Iterator<Tag> iterator() {

	return tags.values().iterator();
    }

    @Override
    public int size() {

	return tags.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends Tag> T get(final Class<T> clazz) {

	return (T) tags.get(clazz);
    }

    @Override
    public boolean isEmpty() {

	return tags.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {

	return tags.containsKey(o);
    }

    @Override
    public boolean add(final Tag e) {

	return tags.put(e.getClass(), e) == null;
    }

    @Override
    public boolean remove(final Object o) {

	return tags.remove(o.getClass()) != null;
    }

    @Override
    public void clear() {

	tags.clear();
    }
}
