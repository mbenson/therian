package net.morph.position;

import java.lang.reflect.Type;

import net.morph.MorphContext;

public class Property<P, T, POSITION_TYPE extends Position.Readable<T> & Position.Writable<T>> extends
    RelativePositionFactory<P, T, POSITION_TYPE> {
    private final String propertyName;

    @SuppressWarnings("unchecked")
    private Property(String propertyName) {
        super(new RelativePosition.GetType<P, T>() {

            public Type getType(Position<? extends P> parentPosition) {
                // TODO Auto-generated method stub
                final MorphContext context = MorphContext.getInstance();

                return null;
            }

        }, new RelativePosition.GetValue<P, T>() {

            public T getValue(Position<? extends P> parentPosition) {
                // TODO Auto-generated method stub
                return null;
            }

        }, new RelativePosition.SetValue<P, T>() {

            public void setValue(Position<? extends P> parentValue, T value) {
                // TODO Auto-generated method stub

            }

        });
        this.propertyName = propertyName;
    }

    public static <P, T, POSITION_TYPE extends Position.Readable<T> & Position.Writable<T>> Property<P, T, POSITION_TYPE> at(
        String propertyName) {
        return new Property<P, T, POSITION_TYPE>(propertyName);
    }
}
