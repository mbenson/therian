package therian.operation;

import java.lang.reflect.Type;

import org.apache.commons.lang3.ObjectUtils;

import therian.Operation;
import therian.Typed;

public class GetElementType<T> extends Operation<Type> {
    private final Typed<T> typeHost;

    private Type result;

    private GetElementType(Typed<T> typeHost) {
        this.typeHost = typeHost;
    }

    public void setResult(Type result) {
        this.result = result;
    }

    @Override
    protected Type provideResult() {
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        GetElementType<?> other = (GetElementType<?>) obj;
        return ObjectUtils.equals(other.getTypeHost(), getTypeHost());
    }

    @Override
    public int hashCode() {
        int result = 53 << 4;
        result |= getClass().hashCode();
        result <<= 4;
        result |= ObjectUtils.hashCode(getTypeHost());
        return result;
    }

    @Override
    public String toString() {
        return String.format("Get element type of %s", getTypeHost());
    }

    public Typed<T> getTypeHost() {
        return typeHost;
    }

    public static <T> GetElementType<T> of(Typed<T> typeHost) {
        return new GetElementType<T>(typeHost);
    }
}
