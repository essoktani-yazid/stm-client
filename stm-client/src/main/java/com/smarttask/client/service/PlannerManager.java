package com.smarttask.client.service;

import com.smarttask.client.util.SessionManager;
import com.smarttask.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlannerManager {

    // DAO SUPPRIMÉS POUR LE CLIENT AUTONOME
    // private final CalendarEventDAO eventDAO;
    // private final TaskDAO taskDAO;

    private final ObservableList<CalendarEvent> events;
    private final ObservableList<Task> tasks;
    private final User currentUser;
    private final GoogleCalendarService googleCalendarService;
    private boolean googleSyncEnabled = true;

    public PlannerManager(User currentUser) {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.googleCalendarService = GoogleCalendarService.getInstance();
        this.events = FXCollections.observableArrayList();
        this.tasks = FXCollections.observableArrayList();
        loadFromMemory();
    }

    private void loadFromMemory() {
        System.out.println("⚠️ Client Mode: Database disconnected.");
        // Ajoutez une tâche de test ici si vous voulez voir quelque chose au démarrage
    }

    public List<PlannerItem> getItemsForDate(LocalDate date) {
        List<PlannerItem> items = new ArrayList<>();
        events.stream().filter(e -> e.occursOn(date)).map(EventPlannerAdapter::new).forEach(items::add);
        tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().toLocalDate().equals(date))
             .map(TaskPlannerAdapter::new).forEach(items::add);
        items.sort(Comparator.comparing(PlannerItem::getStartTime));
        return items;
    }

    public void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
            if (isGoogleSyncEnabled()) googleCalendarService.syncTask(task);
        }
    }

    public void updateTask(Task task) {
        if (isGoogleSyncEnabled()) googleCalendarService.syncTask(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        if (isGoogleSyncEnabled()) googleCalendarService.deleteTask(task);
    }

    public void addEvent(CalendarEvent event) {
        if (event != null) {
            events.add(event);
            if (isGoogleSyncEnabled()) googleCalendarService.syncEvent(event);
        }
    }

    public List<Task> getTasksForUser(String userId) { return new ArrayList<>(tasks); }
    public List<CalendarEvent> getAllEvents() { return new ArrayList<>(events); }
    public void enableGoogleSync() { googleCalendarService.authenticate(currentUser).thenAccept(s -> googleSyncEnabled = s); }
    public boolean isGoogleSyncEnabled() { return googleSyncEnabled && googleCalendarService.isAuthenticated(); }
    public void reloadFromDatabase() { }
}