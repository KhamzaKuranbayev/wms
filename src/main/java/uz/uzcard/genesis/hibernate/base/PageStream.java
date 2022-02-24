package uz.uzcard.genesis.hibernate.base;

import java.util.stream.Stream;

public class PageStream<T extends _Entity> {
    private final Stream<T> stream;
    private final int size;

    public PageStream(Stream<T> stream, int size) {
        this.stream = stream;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public Stream<T> stream() {
        return stream;
    }
}