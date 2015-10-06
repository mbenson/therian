/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian.operation;

import java.lang.reflect.Type;
import java.util.Objects;

import org.apache.commons.lang3.reflect.Typed;

import therian.BindTypeVariable;
import therian.Operation;

/**
 * Operation to get the element type of some "container" type.
 *
 * @param <T>
 */
public class GetElementType<T> extends Operation<Type> {

	private final Typed<T> typedItem;

	private GetElementType(Typed<T> typedItem) {
		this.typedItem = typedItem;
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
		return Objects.equals(other.typedItem, typedItem);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), typedItem);
	}

	@Override
	public String toString() {
		return String.format("Get element type of %s", typedItem);
	}

	@BindTypeVariable
	public Typed<T> getTypedItem() {
		return typedItem;
	}

	public static <T> GetElementType<T> of(Typed<T> typedItem) {
		return new GetElementType<T>(typedItem);
	}
}
