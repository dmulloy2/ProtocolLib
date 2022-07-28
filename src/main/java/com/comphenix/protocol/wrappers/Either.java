package com.comphenix.protocol.wrappers;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a datatype where either left or right is present. The values are available with a xor semantic. So at
 * most and at least one value will be available.
 * 
 * @param <L> left data type
 * @param <R> right data type
 */
public abstract class Either<L, R> {

    public static class Left<L, R> extends Either<L, R> {

        private final L value;

        protected Left(L value) {
            this.value = value;
        }

        @Override
        public <T> T map(Function<L, T> leftConsumer, Function<R, T> rightConsumer) {
            return leftConsumer.apply(value);
        }

        @Override
        public Optional<L> left() {
            return Optional.ofNullable(value);
        }

        @Override
        public Optional<R> right() {
            return Optional.empty();
        }
    }

    public static class Right<L, R> extends Either<L, R> {

        private final R value;

        protected Right(R value) {
            this.value = value;
        }

        @Override
        public <T> T map(Function<L, T> leftConsumer, Function<R, T> rightConsumer) {
            return rightConsumer.apply(value);
        }

        @Override
        public Optional<L> left() {
            return Optional.empty();
        }

        @Override
        public Optional<R> right() {
            return Optional.ofNullable(value);
        }
    }

    /**
     * @param leftConsumer transformer if the left value is present
     * @param rightConsumer transformer if the right value is present
     * @return result of applying the given functions to the left or right side
     * @param <T> result data type of both transformers
     */
    public abstract <T> T map(Function<L, T> leftConsumer, Function<R, T> rightConsumer);

    /**
     * @return left value if present
     */
    public abstract Optional<L> left();

    /**
     * @return right value if present
     */
    public abstract Optional<R> right();

    /**
     * @param value containing value
     * @return either containing a left value
     * @param <L> data type of the containing value
     * @param <R> right data type
     */
    public static <L, R>  Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * @param value containing value
     * @return either containing a right value
     * @param <L> left data type
     * @param <R> data type of the containing value
     */
    public static <L, R>  Either<L, R> right(R value) {
        return new Right<>(value);
    }
}
