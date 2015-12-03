package com.homes.popmovies.utilities;

import org.pcollections.ConsPStack;

import java.util.Iterator;

import rx.functions.Func0;

public class Pusher {
    static public Pusher start() {
        return new Pusher();
    }

    private ConsPStack<String> mList = ConsPStack.empty();

    private Pusher() {
    }

    public Pusher push(final String s) {
        mList = mList.plus(s);
        return this;
    }

    public String join(final String separator) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<String> iterator = mList.iterator();

        // Java 8 Lambdas don't do recursion
        // y combinator to the rescue ;-)

        Combinator.y0((Func0<Boolean> f) -> () -> {

            if (!iterator.hasNext()) {
                return false;
            }

            String s = iterator.next();

            if (f.call()) {
                stringBuilder.append(separator);
            }

            stringBuilder.append(s);
            return true;
        }).call();

        return stringBuilder.toString();
    }
}
