package org.example.core.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputeResourceTest {

    @Test
    @DisplayName("Конструктор без аргументов задаёт нули")
    void defaultConstructor_setsZeros() {
        ComputeResource cr = new ComputeResource();

        assertAll(
                () -> assertEquals(0, cr.getCpuCores()),
                () -> assertEquals(0, cr.getDiskSpace()),
                () -> assertEquals(0, cr.getRam())
        );
    }

    @Test
    @DisplayName("Конструктор с параметрами корректно проставляет поля")
    void constructorWithArgs_setsGivenValues() {
        ComputeResource cr = new ComputeResource(2, 50, 4096);

        assertAll(
                () -> assertEquals(2, cr.getCpuCores()),
                () -> assertEquals(50, cr.getDiskSpace()),
                () -> assertEquals(4096, cr.getRam())
        );
    }

    @Test
    @DisplayName("Конструктор копирования создаёт независимую копию")
    void copyConstructor_createsIndependentCopy() {
        ComputeResource original = new ComputeResource(1, 10, 1024);
        ComputeResource copy     = new ComputeResource(original);

        // меняем копию ‑ оригинал не меняется
        copy.setCpuCores(8);

        assertEquals(1, original.getCpuCores());
        assertEquals(8, copy.getCpuCores());
    }

    @Test
    @DisplayName("add увеличивает ресурсы текущего объекта и возвращает this")
    void add_addsValuesAndReturnsSelf() {
        ComputeResource a = new ComputeResource(1, 10, 1000);
        ComputeResource b = new ComputeResource(2, 5,  500);

        ComputeResource returned = a.add(b);

        assertSame(a, returned);
        assertAll(
                () -> assertEquals(3, a.getCpuCores()),
                () -> assertEquals(15, a.getDiskSpace()),
                () -> assertEquals(1500, a.getRam())
        );
    }

    @Test
    @DisplayName("subtract уменьшает ресурсы текущего объекта и возвращает this")
    void subtract_subtractsValues() {
        ComputeResource a = new ComputeResource(4, 40, 4000);
        ComputeResource b = new ComputeResource(1, 10,  500);

        a.subtract(b);

        assertAll(
                () -> assertEquals(3, a.getCpuCores()),
                () -> assertEquals(30, a.getDiskSpace()),
                () -> assertEquals(3500, a.getRam())
        );
    }

    @Test
    @DisplayName("negative меняет знак всех полей и возвращает this")
    void negative_invertsAllFields() {
        ComputeResource a = new ComputeResource(2, 30, 2048);

        a.negative();

        assertAll(
                () -> assertEquals(-2, a.getCpuCores()),
                () -> assertEquals(-30, a.getDiskSpace()),
                () -> assertEquals(-2048, a.getRam())
        );
    }

    @Test
    @DisplayName("max возвращает новый объект с максимумами по каждому полю, не изменяя исходные")
    void max_returnsPerFieldMaximums() {
        ComputeResource a = new ComputeResource(1, 50, 1024);
        ComputeResource b = new ComputeResource(2, 30, 2048);

        ComputeResource c = ComputeResource.max(a, b);

        assertAll(
                () -> assertEquals(2, c.getCpuCores()),
                () -> assertEquals(50, c.getDiskSpace()),
                () -> assertEquals(2048, c.getRam())
        );

        assertEquals(1, a.getCpuCores());
        assertEquals(30, b.getDiskSpace());
    }
}
