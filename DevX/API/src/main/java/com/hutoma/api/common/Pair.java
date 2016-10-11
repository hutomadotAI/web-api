package com.hutoma.api.common;


import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by pedrotei on 06/10/16.
 */
public class Pair<A, B> {
    /** The first part. */
    private final A a;
    /** The second part. */
    private final B b;

    /**
     * Ctor.
     * @param a the first part
     * @param b the second part
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Copy ctor.
     * @param other the other pair
     */
    public Pair(Pair<? extends A, ? extends B> other) {
        this.a = other.getA();
        this.b = other.getB();
    }

    /**
     * Gets whether the pair are equal or not.
     * @param other the other pair to compare to
     * @return whether the pair are equal or not
     */
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?,?> e = (Pair<?,?>)other;
        return eq(a, e.getA()) && eq(b, e.getB());
    }

    /**
     * Gets the first part of the pair.
     * @return the first part of the pair
     */
    public A getA() {
        return this.a;
    }

    /**
     * Gets the second part of the pair.
     * @return the second part of the pair
     */
    public B getB() {
        return this.b;
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.a)
                .append(this.b)
                .toHashCode();
    }

    /**
     * Checks for equality.
     * @param o1 the first object
     * @param o2 the second object
     * @return whether the objects are equal or not
     */
    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
