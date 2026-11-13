package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionsTest {

    @Test
    void crobsExceptionIsUncheckedAndKeepsMessage() {
        CrobsException e = new CrobsException("base message");
        assertInstanceOf(RuntimeException.class, e);
        assertEquals("base message", e.getMessage());
    }

    @Test
    void invalidInputExceptionIsACrobsException() {
        InvalidInputException e = new InvalidInputException("bad input");
        assertInstanceOf(CrobsException.class, e);
        assertEquals("bad input", e.getMessage());
    }

    @Test
    void notFoundExceptionIsACrobsException() {
        NotFoundException e = new NotFoundException("missing");
        assertInstanceOf(CrobsException.class, e);
        assertEquals("missing", e.getMessage());
    }

    @Test
    void permissionDeniedExceptionIsACrobsException() {
        PermissionDeniedException e = new PermissionDeniedException("denied");
        assertInstanceOf(CrobsException.class, e);
        assertEquals("denied", e.getMessage());
    }

    @Test
    void conflictExceptionIsACrobsException() {
        ConflictException e = new ConflictException("clash");
        assertInstanceOf(CrobsException.class, e);
        assertEquals("clash", e.getMessage());
    }

    @Test
    void subclassesCanBeCaughtAsCrobsException() {
        ConflictException thrown = new ConflictException("caught as base");
        CrobsException caught = assertThrows(CrobsException.class, () -> {
            throw thrown;
        });
        assertSame(thrown, caught);
    }
}
