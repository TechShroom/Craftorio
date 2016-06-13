package com.techshroom.mods.pereltrains.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An extension of ArrayList that makes it act like a deque as well.
 * 
 * @param <E>
 */
public class RandomAccessQueue<E> extends ArrayList<E>
        implements List<E>, Deque<E> {

    private static final long serialVersionUID = 877807731816235657L;

    @Override
    public void addFirst(E e) {
        add(0, e);
    }

    @Override
    public void addLast(E e) {
        add(e);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return pollFirst();
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return pollLast();
    }

    @Override
    public E pollFirst() {
        return remove(0);
    }

    @Override
    public E pollLast() {
        return remove(size() - 1);
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return peekFirst();
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return peekLast();
    }

    @Override
    public E peekFirst() {
        return get(0);
    }

    @Override
    public E peekLast() {
        return get(size() - 1);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        // Uses same algo as ArrayList#remove, but adjusted for last occurrence.
        if (o == null) {
            for (int index = size() - 1; index >= 0; index--)
                if (get(index) == null) {
                    remove(index);
                    return true;
                }
        } else {
            for (int index = size() - 1; index >= 0; index--)
                if (o.equals(get(index))) {
                    remove(index);
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr, reversed.
     */
    private class Itr implements Iterator<E> {

        int cursor = size() - 1; // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = RandomAccessQueue.this.modCount;

        @Override
        public boolean hasNext() {
            return this.cursor != -1;
        }

        @Override
        public E next() {
            checkForComodification();
            int i = this.cursor;
            if (i >= size())
                throw new NoSuchElementException();
            this.cursor = i - 1;
            return get(this.lastRet = i);
        }

        @Override
        public void remove() {
            if (this.lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                RandomAccessQueue.this.remove(this.lastRet);
                this.cursor = this.lastRet;
                this.lastRet = -1;
                this.expectedModCount = RandomAccessQueue.this.modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = size();
            int i = this.cursor;
            if (i >= size) {
                return;
            }
            while (i != -1
                    && RandomAccessQueue.this.modCount == this.expectedModCount) {
                consumer.accept(get(i--));
            }
            // update once at end of iteration to reduce heap write traffic
            this.cursor = i;
            this.lastRet = i + 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (RandomAccessQueue.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

}
