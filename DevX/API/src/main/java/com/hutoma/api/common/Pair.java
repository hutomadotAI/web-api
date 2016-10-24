package com.hutoma.api.common;


import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by pedrotei on 06/10/16.
 */
public class Pair<A, B> {
    /**
     * The first part.
     */
    private final A a;
    /**
     * The second part.
     */
    private final B b;

    /**
     * Ctor.
     * @param partA the first part
     * @param partB the second part
     */
    public Pair(A partA, B partB) {
        this.a = partA;
        this.b = partB;
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
     * Checks for equality.
     * @param o1 the first object
     * @param o2 the second object
     * @return whether the objects are equal or not
     */
    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
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
     * Gets whether the pair are equal or not.
     * @param other the other pair to compare to
     * @return whether the pair are equal or not
     */
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?, ?> castedOther = (Pair<?, ?>) other;
        return eq(this.a, castedOther.getA()) && eq(this.b, castedOther.getB());
    }
}
