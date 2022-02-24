package org.example.practise;

public interface ITest<T> {

    T selectOne(String id);

    <E> E selectMany2(T e);

    <E> T selectMany(E e);
}
