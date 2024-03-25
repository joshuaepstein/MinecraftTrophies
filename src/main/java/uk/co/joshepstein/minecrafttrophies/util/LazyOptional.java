package uk.co.joshepstein.minecrafttrophies.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.neoforge.common.util.NonNullConsumer;
import net.neoforged.neoforge.common.util.NonNullFunction;
import net.neoforged.neoforge.common.util.NonNullPredicate;
import net.neoforged.neoforge.common.util.NonNullSupplier;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LazyOptional<T> {
	private final NonNullSupplier<T> supplier;
	private final Object lock = new Object();
	private Mutable<T> resolved;
	private Set<NonNullConsumer<LazyOptional<T>>> listeners = new HashSet();
	private boolean isValid = true;
	@Nonnull
	private static final LazyOptional<Void> EMPTY = new LazyOptional((NonNullSupplier)null);
	private static final Logger LOGGER = LogManager.getLogger();

	public static <T> LazyOptional<T> of(@Nullable NonNullSupplier<T> instanceSupplier) {
		return instanceSupplier == null ? empty() : new LazyOptional(instanceSupplier);
	}

	public static <T> LazyOptional<T> empty() {
		return (LazyOptional<T>) EMPTY.cast();
	}

	public LazyOptional<T> cast() {
		return this;
	}

	private LazyOptional(@Nullable NonNullSupplier<T> instanceSupplier) {
		this.supplier = instanceSupplier;
	}

	@Nullable
	private T getValue() {
		if (this.isValid && this.supplier != null) {
			if (this.resolved == null) {
				synchronized(this.lock) {
					if (this.resolved == null) {
						T temp = this.supplier.get();
						if (temp == null) {
							LOGGER.catching(Level.WARN, new NullPointerException("Supplier should not return null value"));
						}

						this.resolved = new MutableObject(temp);
					}
				}
			}

			return this.resolved.getValue();
		} else {
			return null;
		}
	}

	private T getValueUnsafe() {
		T ret = this.getValue();
		if (ret == null) {
			throw new IllegalStateException("LazyOptional is empty or otherwise returned null from getValue() unexpectedly");
		} else {
			return ret;
		}
	}

	public boolean isPresent() {
		return this.supplier != null && this.isValid;
	}

	public void ifPresent(NonNullConsumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
		T val = this.getValue();
		if (this.isValid && val != null) {
			consumer.accept(val);
		}

	}

	public <U> LazyOptional<U> lazyMap(NonNullFunction<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		return this.isPresent() ? of(() -> {
			return mapper.apply(this.getValueUnsafe());
		}) : empty();
	}

	public <U> Optional<U> map(NonNullFunction<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		return this.isPresent() ? Optional.of(mapper.apply(this.getValueUnsafe())) : Optional.empty();
	}

	public Optional<T> filter(NonNullPredicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		T value = this.getValue();
		return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
	}

	public Optional<T> resolve() {
		return this.isPresent() ? Optional.of(this.getValueUnsafe()) : Optional.empty();
	}

	public T orElse(T other) {
		T val = this.getValue();
		return val != null ? val : other;
	}

	public T orElseGet(NonNullSupplier<? extends T> other) {
		T val = this.getValue();
		return val != null ? val : other.get();
	}

	public <X extends Throwable> T orElseThrow(NonNullSupplier<? extends X> exceptionSupplier) throws X {
		T val = this.getValue();
		if (val != null) {
			return val;
		} else {
			throw exceptionSupplier.get();
		}
	}

	public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
		if (this.isPresent()) {
			this.listeners.add(listener);
		} else {
			listener.accept(this);
		}

	}

	public void invalidate() {
		if (this.isValid) {
			this.isValid = false;
			this.listeners.forEach((e) -> {
				e.accept(this);
			});
		}

	}
}
