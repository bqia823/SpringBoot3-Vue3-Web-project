package com.example.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Interface for quick DTO to VO conversion implementation
 * simply extend the DTO class with this interface to use
 */
public interface BaseData {

    /**
     * Creates a specified VO class and directly copies all member variable
     * values from the current DTO object to the VO object
     * @param clazz the specified VO type
     * @param consumer additional processing using Lambda before returning the VO object
     * @return the specified VO object
     * @param <V> the specified VO type
     */
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    /**
     * Creates a specified VO class and directly copies all member
     * variable values from the current DTO object to the VO object
     * @param clazz the specified VO type
     * @return the specified VO object
     * @param <V> the specified VO type
     */
    default <V> V asViewObject(Class<V> clazz) {
        try {
            Field[] fields = clazz.getDeclaredFields();
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            Arrays.asList(fields).forEach(field -> convert(field, v));
            return v;
        } catch (ReflectiveOperationException exception) {
            Logger logger = LoggerFactory.getLogger(BaseData.class);
            logger.error("An error occurred during the conversion between VO and DTO", exception);
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * For internal use, quickly copies the value of a target object's
     * field with the same name from the current class to the target object's field
     * @param field the target object field
     * @param target the target object
     */
    private void convert(Field field, Object target){
        try {
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(target, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
    }
}
