package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.models.dto.Project;
import org.example.core.models.dto.ProjectsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ServerClient serverClient;
    @Mock PreferencesStorage preferencesStorage;   // в сервисе не используется, но нужен конструктору

    ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(serverClient, preferencesStorage);
    }

    @Test
    @DisplayName("getProjects – проксирует вызов к ServerClient и возвращает ответ без изменений")
    void getProjects_success() {
        int page = 2, perPage = 25;

        ProjectsResponse expected = new ProjectsResponse(1,1,1,1,
                List.of(new Project(1, "Demo", "", "", 100),
                        new Project(2, "Test", "", "", 100)));   // конструкторы – примерные

        when(serverClient.getProjects(page, perPage))
                .thenReturn(Mono.just(expected));

        ProjectsResponse actual = service.getProjects(page, perPage);

        assertSame(expected, actual);                       // тот же объект
        verify(serverClient).getProjects(page, perPage);    // ровно один вызов
    }

    @Test
    @DisplayName("getProjectById – фильтрует список по идентификатору")
    void getProjectById_filtersCorrectly() {
        var p1 = new Project(10, "Calc", "", "", 100);
        var p2 = new Project(42, "IDEA", "", "", 100);

        when(serverClient.getProjects())
                .thenReturn(Mono.just(new ProjectsResponse(1,1,1,1, List.of(p1, p2))));

        List<Project> result = service.getProjectById(42);

        assertEquals(1, result.size());
        assertEquals(42, result.get(0).getId());
        verify(serverClient).getProjects();
    }

    @Test
    @DisplayName("getProjectByNamePattern – находит проекты, чьё имя содержит подстроку")
    void getProjectByNamePattern_filtersCorrectly() {
        var p1 = new Project(1, "MyTestProject", "", "", 100);
        var p2 = new Project(2, "Sample", "", "", 100);
        var p3 = new Project(3, "AnotherTest", "", "", 100);

        when(serverClient.getProjects())
                .thenReturn(Mono.just(new ProjectsResponse(1,1,1,1, List.of(p1, p2, p3))));

        List<Project> result = service.getProjectByNamePattern("Test");

        assertIterableEquals(List.of(p1, p3), result);
        verify(serverClient).getProjects();
    }
}
