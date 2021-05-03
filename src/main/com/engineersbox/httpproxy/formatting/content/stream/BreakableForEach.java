package com.engineersbox.httpproxy.formatting.content.stream;

import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class BreakableForEach {

    public static class Breaker {
        private boolean shouldBreak = false;

        public void stop() {
            shouldBreak = true;
        }

        boolean get() {
            return shouldBreak;
        }
    }

    public static <T> void forEach(final Stream<T> stream, final BiConsumer<T, Breaker> consumer) {
        final Spliterator<T> spliterator = stream.spliterator();
        boolean hadNext = true;
        final Breaker breaker = new Breaker();

        while (hadNext && !breaker.get()) {
            hadNext = spliterator.tryAdvance(elem -> {
                consumer.accept(elem, breaker);
            });
        }
    }

}
