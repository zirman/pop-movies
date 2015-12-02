package com.homes.popmovies;

import org.pcollections.TreePVector;

import rx.functions.Func1;
import rx.functions.Func2;

public class Transform<A> {

    static public <T1, R> TreePVector<R> map(
        final TreePVector<T1> vector,
        final Func1<T1, R> fun) {

        TreePVector<R> retVal = TreePVector.empty();

        for (final T1 t1 : vector) {
            retVal = retVal.plus(fun.call(t1));
        }

        return retVal;
    }

    static public <T> TreePVector<T> filter(
        final TreePVector<T> vector,
        final Func1<T, Boolean> fun) {

        TreePVector<T> retVal = TreePVector.empty();

        for (final T t : vector) {

            if (fun.call(t)) {
                retVal = retVal.plus(t);
            }
        }

        return retVal;
    }

    static public <T1, R> R reduce(
        final TreePVector<T1> vector,
        final Func2<R, T1, R> fun,
        R memo) {

        for (final T1 t1 : vector) {
            memo = fun.call(memo, t1);
        }

        return memo;
    }

    static public <T> Transform<T> start(final TreePVector<T> vector) {
        return new Transform<>(vector);
    }

    private final TreePVector<A> mVector;

    private Transform(final TreePVector<A> vector) {
        mVector = vector;
    }

    public Transform<R> map(final Func1<A, R> fun) {
        return new Transform<>(map(mVector, fun));
    }

    public Transform<R> filter(final Func1<A, Boolean> fun) {
        return new Transform(filter(mVector, fun));
    }

    public R reduce(final Func2<R, A, R> fun, R memo) {
        return reduce(mVector, fun, memo);
    }

    public TreePVector<A> result() {
        return mVector;
    }
}
