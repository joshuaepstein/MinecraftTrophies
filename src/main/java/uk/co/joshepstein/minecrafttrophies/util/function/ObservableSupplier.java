/*
 * Copyright (c) 2023. Joshua Epstein
 * All rights reserved.
 */

package uk.co.joshepstein.minecrafttrophies.util.function;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObservableSupplier<T>
		implements Supplier<T> {
	private static final ObservableSupplier<?> EMPTY = new ObservableSupplier<Object>(() -> null, (o1, o2) -> true);
	protected final Supplier<T> supplier;
	protected final BiPredicate<T, T> equivalenceTest;
	protected T previousValue;

	public static <T> ObservableSupplier<T> empty() {
		return new ObservableSupplier<>(() -> null, (o1, o2) -> true);
	}

	protected ObservableSupplier(Supplier<T> supplier, BiPredicate<T, T> equivalenceTest) {
		this.supplier = supplier;
		this.equivalenceTest = equivalenceTest;
	}

	public static <T> ObservableSupplier<T> of(Supplier<T> supplier, BiPredicate<T, T> equivalenceFunction) {
		return new ObservableSupplier<T>(supplier, equivalenceFunction);
	}

	public boolean hasChanged() {
		return this.hasChanged(this.get());
	}

	protected boolean hasChanged(T currentValue) {
		if (this.previousValue == currentValue || this.previousValue != null && this.equivalenceTest.test(this.previousValue, currentValue)) {
			return false;
		}
		this.previousValue = currentValue;
		return true;
	}

	public T get() {
		return this.supplier.get();
	}

	public void ifChanged(Consumer<? super T> action) {
		T value = this.get();
		if (this.hasChanged(value)) {
			action.accept(value);
		}
	}

	public void ifChangedOrElse(Consumer<? super T> action, Consumer<? super T> unchangedAction) {
		T value = this.get();
		boolean hasChanged = this.hasChanged(value);
		if (hasChanged) {
			action.accept(value);
		} else {
			unchangedAction.accept(value);
		}
	}
}