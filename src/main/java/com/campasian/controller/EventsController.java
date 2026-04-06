package com.campasian.controller;

import com.campasian.model.CampusEvent;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class EventsController implements Initializable {

    @FXML private Button addBtn;
    @FXML private Button allBtn;
    @FXML private Button upcomingBtn;
    @FXML private Button pastBtn;
    @FXML private Button clubsBtn;
    @FXML private VBox eventsVBox;

    private final List<CampusEvent> allEvents = new ArrayList<>();
    private String currentFilter;
    private static final String FILTER_ACTIVE = "marketplace-filter-btn-active";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFilter = null;
        updateFilterButtons();
        loadEvents();
    }

    @FXML protected void onFilterAll() { setFilter(null); }
    @FXML protected void onFilterUpcoming() { setFilter("upcoming"); }
    @FXML protected void onFilterPast() { setFilter("past"); }
    @FXML protected void onFilterClubs() { setFilter("clubs"); }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        renderEvents();
    }

    private void updateFilterButtons() {
        if (allBtn != null) allBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (upcomingBtn != null) upcomingBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (pastBtn != null) pastBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (clubsBtn != null) clubsBtn.getStyleClass().remove(FILTER_ACTIVE);

        if (currentFilter == null && allBtn != null) allBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("upcoming".equals(currentFilter) && upcomingBtn != null) upcomingBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("past".equals(currentFilter) && pastBtn != null) pastBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("clubs".equals(currentFilter) && clubsBtn != null) clubsBtn.getStyleClass().add(FILTER_ACTIVE);
    }

    @FXML
    protected void onAddClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event-add-modal.fxml"));
            Parent root = loader.load();
            EventAddModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Create Event");
            s.setScene(new javafx.scene.Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadEvents);
            s.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        if (eventsVBox == null) return;
        eventsVBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<CampusEvent> list = ApiService.getInstance().getCampusEvents();
                Platform.runLater(() -> {
                    allEvents.clear();
                    allEvents.addAll(list);
                    renderEvents();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (eventsVBox != null) {
                        Label err = new Label("Unable to load events.");
                        err.getStyleClass().add("profile-label");
                        eventsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private void renderEvents() {
        if (eventsVBox == null) return;
        eventsVBox.getChildren().clear();

        List<CampusEvent> filtered = allEvents.stream()
                .filter(this::matchesCurrentFilter)
                .toList();

        for (CampusEvent event : filtered) {
            eventsVBox.getChildren().add(buildCard(event));
        }

        if (filtered.isEmpty()) {
            Label empty = new Label(emptyMessage());
            empty.getStyleClass().add("profile-label");
            eventsVBox.getChildren().add(empty);
        }
    }

    private boolean matchesCurrentFilter(CampusEvent event) {
        if (currentFilter == null) return true;
        return switch (currentFilter) {
            case "upcoming" -> isUpcoming(event);
            case "past" -> isPast(event);
            case "clubs" -> looksLikeClubEvent(event);
            default -> true;
        };
    }

    private String emptyMessage() {
        return switch (currentFilter != null ? currentFilter : "") {
            case "upcoming" -> "No upcoming events found.";
            case "past" -> "No past events found.";
            case "clubs" -> "No club activities found.";
            default -> "No events yet.";
        };
    }

    private VBox buildCard(CampusEvent event) {
        Label categoryChip = buildCategoryChip(event);
        Label title = new Label(event.getTitle() != null ? event.getTitle() : "Untitled");
        title.getStyleClass().add("events-title");
        title.setWrapText(true);

        Label dateChip = new Label(event.getEventDate() != null ? event.getEventDate() : "TBA");
        dateChip.getStyleClass().addAll("events-chip", "events-chip-date");
        Label venueChip = new Label(event.getVenue() != null ? event.getVenue() : "Venue TBA");
        venueChip.getStyleClass().addAll("events-chip", "events-chip-location");
        FlowPane meta = new FlowPane(8, 8);
        meta.getStyleClass().add("events-meta");
        if (categoryChip != null) meta.getChildren().add(categoryChip);
        meta.getChildren().addAll(dateChip, venueChip);

        Label desc = new Label(event.getDescription() != null ? event.getDescription() : "");
        desc.getStyleClass().add("events-description");
        desc.setWrapText(true);

        Button interestedBtn = new Button(event.isUserInterested() ? "Interested" : "Join Interest");
        interestedBtn.getStyleClass().add("btn-interested");
        if (event.isUserInterested()) interestedBtn.getStyleClass().add("btn-interested-active");
        interestedBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().toggleEventInterest(event.getId());
                loadEvents();
            } catch (ApiException ignored) {
            }
        });

        Label countLbl = new Label(event.getInterestedCount() + " interested");
        countLbl.getStyleClass().add("events-count");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(12);
        footer.getStyleClass().add("events-footer");
        footer.getChildren().addAll(countLbl, spacer, interestedBtn);

        VBox card = new VBox(12);
        card.getStyleClass().add("events-card");
        card.getChildren().addAll(title, meta, desc, footer);
        return card;
    }

    private Label buildCategoryChip(CampusEvent event) {
        String category = inferCategory(event);
        if (category == null) return null;

        Label chip = new Label(category);
        chip.getStyleClass().addAll("events-chip", "events-chip-category");
        return chip;
    }

    private String inferCategory(CampusEvent event) {
        String text = ((event.getTitle() != null ? event.getTitle() : "") + " " +
                (event.getDescription() != null ? event.getDescription() : "")).toLowerCase(Locale.ENGLISH);

        if (text.contains("workshop")) return "Workshop";
        if (text.contains("seminar")) return "Seminar";
        if (text.contains("fest") || text.contains("festival")) return "Fest";
        if (looksLikeClubEvent(event)) return "Club";
        return null;
    }

    private boolean looksLikeClubEvent(CampusEvent event) {
        String text = ((event.getTitle() != null ? event.getTitle() : "") + " " +
                (event.getDescription() != null ? event.getDescription() : "") + " " +
                (event.getVenue() != null ? event.getVenue() : "")).toLowerCase(Locale.ENGLISH);
        return text.contains("club") || text.contains("society") || text.contains("association")
                || text.contains("chapter") || text.contains("community");
    }

    private boolean isUpcoming(CampusEvent event) {
        LocalDate date = parseEventDate(event.getEventDate());
        return date != null && !date.isBefore(LocalDate.now());
    }

    private boolean isPast(CampusEvent event) {
        LocalDate date = parseEventDate(event.getEventDate());
        return date != null && date.isBefore(LocalDate.now());
    }

    private LocalDate parseEventDate(String raw) {
        if (raw == null || raw.isBlank()) return null;

        try {
            return OffsetDateTime.parse(raw).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }
}
