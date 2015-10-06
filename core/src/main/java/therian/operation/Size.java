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

import java.util.Objects;

import therian.BindTypeVariable;
import therian.Operation;
import therian.position.Position;

/**
 * Size {@link Operation}.
 */
public class Size<T> extends Operation<Integer> {

	private final Position.Readable<T> position;

	private Size(Position.Readable<T> position) {
		super();
		this.position = position;
	}

	@BindTypeVariable
	public Position.Readable<T> getPosition() {
		return position;
	}

	public void setResult(int result) {
		setResult(Integer.valueOf(result));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!obj.getClass().equals(getClass())) {
			return false;
		}
		Size<?> other = (Size<?>) obj;
		return Objects.equals(other.getPosition(), getPosition());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), getPosition());
	}

	@Override
	public String toString() {
		return String.format("Size of %s", getPosition());
	}

	public static <T> Size<T> of(Position.Readable<T> position) {
		return new Size<T>(position);
	}

}
