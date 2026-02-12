package org.casbin.adapter;

/**
 * CombineType represents different types of condition combining strategies
 */
public enum CombineType {
    /**
     * Combine conditions with OR operator
     */
    OR,
    
    /**
     * Combine conditions with AND operator
     */
    AND
}
