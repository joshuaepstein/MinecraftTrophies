/*
 * Copyright (c) 2023. Joshua Epstein
 * All rights reserved.
 */

package uk.co.joshepstein.minecrafttrophies.util.function;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class Memo {
	public static <T> Supplier<T> of(final Supplier<T> factory) {
		return new Supplier<T>() {
			private T cached;

			public T get() {
				return this.cached == null ? (this.cached = factory.get()) : this.cached;
			}
		};
	}

	@SafeVarargs
	public static <T> Supplier<T> reactive(final Supplier<T> factory, final Supplier<Object>... dependencies) {
		return new Supplier<T>() {
			private Object[] prevDependencies = this.calcDependencies();
			private T cached = factory.get();

			public T get() {
				if (this.dependenciesChanged(this.prevDependencies, dependencies)) {
					this.prevDependencies = this.calcDependencies();
					this.cached = factory.get();
				}
				return this.cached;
			}

			private boolean dependenciesChanged(Object[] currentDependencies, Supplier<Object>[] nextDependencies) {
				if (currentDependencies.length != nextDependencies.length) {
					return true;
				}
				for (int i = 0; i < nextDependencies.length; ++i) {
					Object currentDept = currentDependencies[i];
					Object nextDept = nextDependencies[i].get();
					if (Objects.deepEquals(currentDept, nextDept)) continue;
					return true;
				}
				return false;
			}

			private Object[] calcDependencies() {
				return Arrays.stream(dependencies).map(Supplier::get).toArray();
			}
		};
	}
}