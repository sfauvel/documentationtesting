package org.sfvl.test_tools;

public interface SupplierWithException<T> {
    T run() throws Exception;
}
