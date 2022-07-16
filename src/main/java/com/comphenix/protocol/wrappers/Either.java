package com.comphenix.protocol.wrappers;

import java.util.Optional;
import java.util.function.Function;

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
        public Optional<L> getLeft() {
            return Optional.ofNullable(value);
        }

        @Override
        public Optional<R> getRight() {
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
        public Optional<L> getLeft() {
            return Optional.empty();
        }

        @Override
        public Optional<R> getRight() {
            return Optional.ofNullable(value);
        }
    }

    public abstract <T> T map(Function<L, T> leftConsumer, Function<R, T> rightConsumer);

    public abstract Optional<L> getLeft();

    public abstract Optional<R> getRight();

    public static <L, R>  Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R>  Either<L, R> right(R value) {
        return new Right<>(value);
    }
}
