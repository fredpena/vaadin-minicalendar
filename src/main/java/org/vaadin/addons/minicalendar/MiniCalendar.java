package org.vaadin.addons.minicalendar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A small calendar component that can be used to let users select {@link LocalDate} values.
 * The component also supports read-only and disabled states and can be customized in various ways.
 *
 * @author Manfred Huber
 */
@CssImport("minicalendar.css")
public class MiniCalendar extends CustomField<LocalDate> implements HasThemeVariant<MiniCalendarVariant>, LocaleChangeObserver {

    private static final String CSS_BASE = "minicalendar";
    private static final String CSS_WEEKDAY = "weekday";
    private static final String CSS_DAY = "day";
    private static final String CSS_SELECTED = "selected";
    private static final String CSS_READONLY = "readonly";
    private static final String CSS_DISABLED = "disabled";
    private final VerticalLayout content = new VerticalLayout();
    private final HashMap<LocalDate, Component> dayToComponentMapping = new HashMap<>(31);
    private final List<MiniCalendarVariant> appliedVariants = new ArrayList<>(MiniCalendarVariant.values().length);
    private final YearMonthHolder yearMonthHolder = new YearMonthHolder();
    private DayOfWeek firstDayOfWeek = getFirstDayOfWeekByLocale(getLocale());
    private Span selectedComponent = null;
    private Button previousMonthButton = null;
    private Button nextMonthButton = null;
    private Span title = null;

    /* External Handlers */
    private SerializablePredicate<LocalDate> dayEnabledProvider = null;


    /* Constructors */

    public MiniCalendar() {
        this(LocalDate.now());
    }

    public MiniCalendar(LocalDate localDate) {
        this(YearMonth.from(localDate));
    }

    public MiniCalendar(YearMonth yearMonth) {

        this.yearMonthHolder.setValue(yearMonth);
        yearMonthHolder.addValueChangeListener(e -> redraw());

        content.addClassName(CSS_BASE);
        content.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        content.setPadding(false);
        content.setMargin(false);
        add(content);

        renderComponent();
    }


    /* Overrides */

    @Override
    public void setValue(LocalDate newValue) {
        final var redrawRequired = !Objects.equals(getValue(), newValue);
        super.setValue(newValue);
        if (newValue != null) {
            yearMonthHolder.setValue(YearMonth.from(newValue));
        }
        if (redrawRequired) {
            redraw();
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        for (Map.Entry<LocalDate, Component> entry : dayToComponentMapping.entrySet()) {
            final var dayComponent = entry.getValue();
            if (dayComponent instanceof HasStyle) {
                final var styledComponent = (HasStyle) dayComponent;
                toggleStyle(styledComponent, CSS_READONLY);
            }
        }
        toggleStyle(title, CSS_READONLY);
        previousMonthButton.setVisible(!readOnly);
        nextMonthButton.setVisible(!readOnly);
    }

    @Override
    public void addThemeVariants(MiniCalendarVariant... variants) {
        HasThemeVariant.super.addThemeVariants(variants);
        appliedVariants.addAll(Set.of(variants));
        redraw();
    }

    @Override
    public void removeThemeVariants(MiniCalendarVariant... variants) {
        HasThemeVariant.super.removeThemeVariants(variants);
        appliedVariants.removeAll(Set.of(variants));
        redraw();
    }

    @Override
    public void localeChange(LocaleChangeEvent localeChangeEvent) {
        redraw();
    }

    @Override
    protected LocalDate generateModelValue() {
        return getValue();
    }

    @Override
    protected void setPresentationValue(LocalDate localDate) {
        setModelValue(localDate, false);
    }


    /* Public API */

    public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        redraw();
    }
    public void setYearMonth(YearMonth yearMonth) {
        yearMonthHolder.setValue(yearMonth);
    }
    public Registration addYearMonthChangeListener(ValueChangeListener<ValueChangeEvent<YearMonth>> listener) {
        return yearMonthHolder.addValueChangeListener(listener);
    }

    public Registration setDayEnabledProvider(SerializablePredicate<LocalDate> dayEnabledProvider) {
        this.dayEnabledProvider = dayEnabledProvider;
        redraw();
        return () -> {
            this.dayEnabledProvider = null;
            redraw();
        };
    }

    /* Internal API */

    private void redraw() {
        resetComponent();
        renderComponent();
    }

    private void resetComponent() {
        content.removeAll();
        dayToComponentMapping.clear();
        selectedComponent = null;
    }

    private void renderComponent() {
        renderTitle();
        renderHeaderRow();
        renderDayRows();
    }

    private void renderTitle() {

        previousMonthButton = new Button(LumoIcon.ANGLE_LEFT.create(), e -> navigateToPreviousMonth());
        previousMonthButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        previousMonthButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previousMonthButton.setVisible(!isReadOnly());

        nextMonthButton = new Button(LumoIcon.ANGLE_RIGHT.create(), e -> navigateToNextMonth());
        nextMonthButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextMonthButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextMonthButton.setVisible(!isReadOnly());

        title = new Span(yearMonthHolder.getValue().getMonth().getDisplayName(TextStyle.FULL, getLocale()) + " " + yearMonthHolder.getValue().getYear());
        title.addClassName("title");
        title.addClickListener(e -> showYearSelection());

        if (isReadOnly()) {
            title.addClassName(CSS_READONLY);
        }

        var titleLayout = new HorizontalLayout(previousMonthButton, title, nextMonthButton);

        titleLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        titleLayout.setWidthFull();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setSpacing(true);
        titleLayout.setHeight(30, Unit.PIXELS);
        titleLayout.expand(title);

        content.add(titleLayout);
    }

    private void renderHeaderRow() {

        var weekDays = new ArrayList<Span>(7);
        var _firstDayOfWeek = firstDayOfWeek;

        do {
            Span weekDay = span(_firstDayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, getLocale()));
            weekDay.addClassName(CSS_WEEKDAY);
            weekDays.add(weekDay);
            _firstDayOfWeek = _firstDayOfWeek.plus(1);
        } while (_firstDayOfWeek != firstDayOfWeek);

        addRow(weekDays);
    }

    private void renderDayRows() {

        var dayComponents = new ArrayList<Component>(7);
        var dayOfWeekOfFirstDayInMonth = yearMonthHolder.getValue().atDay(1).getDayOfWeek();
        var dayIterator = firstDayOfWeek;

        // Fill empty days before first day of month
        while (dayIterator != dayOfWeekOfFirstDayInMonth) {
            dayComponents.add(emptySpan());
            dayIterator = dayIterator.plus(1);
        }

        // Add actual days to the calendar view
        for (int dayOfMonth = 1; dayOfMonth <= getLastDayOfMonth(yearMonthHolder.getValue()); dayOfMonth++) {

            if (dayComponents.size() == 7) {
                addRow(dayComponents);
                dayComponents.clear();
            }

            var day = yearMonthHolder.getValue().atDay(dayOfMonth);
            var dayComponent = createDayComponent(day);
            dayToComponentMapping.put(day, dayComponent);
            dayComponents.add(dayComponent);
        }

        // Fill empty days after last day of month
        while (dayComponents.size() < 7) {
            dayComponents.add(emptySpan());
        }

        addRow(dayComponents);
    }

    private void showYearSelection() {

        if (isInteractionDisabled()) {
            return;
        }

        var yearSelect = new Select<Year>();
        yearSelect.addThemeVariants(SelectVariant.LUMO_SMALL);
        yearSelect.setItems(evaluateEligibleYears());
        yearSelect.setValue(Year.of(yearMonthHolder.getValue().getYear()));

        var selectionDialog = new Dialog(yearSelect);
        selectionDialog.open();

        yearSelect.addValueChangeListener(event -> {
            yearMonthHolder.setValueFromClient(event.getValue().atMonth(yearMonthHolder.getValue().getMonth()));
            selectionDialog.close();
        });
    }

    private void navigateToPreviousMonth() {
        yearMonthHolder.setValueFromClient(yearMonthHolder.getValue().minusMonths(1));
    }

    private void navigateToNextMonth() {
        yearMonthHolder.setValueFromClient(yearMonthHolder.getValue().plusMonths(1));
    }


    /* Utilities */

    private List<Year> evaluateEligibleYears() {

        var minYear = yearMonthHolder.getValue().getYear() - 100;
        var maxYear = yearMonthHolder.getValue().getYear() + 100;
        var eligibleYears = new ArrayList<Year>(201);

        for (int year = minYear; year < maxYear; year++) {
            eligibleYears.add(Year.of(year));
        }

        return Collections.unmodifiableList(eligibleYears);
    }

    private Component createDayComponent(LocalDate forDay) {
        var component = span(String.valueOf(forDay.getDayOfMonth()));
        component.addClickListener(event -> {

            if (isInteractionDisabled()) {
                return;
            }

            if (selectedComponent != null) {
                selectedComponent.removeClassName(CSS_SELECTED);
            }

            selectedComponent = event.getSource();
            selectedComponent.addClassName(CSS_SELECTED);

            setModelValue(forDay, true);
        });

        if (Objects.equals(getValue(), forDay)) {
            component.addClassName(CSS_SELECTED);
            selectedComponent = component;
        }

        if (isWeekend(forDay) && hasVariant(MiniCalendarVariant.HIGHLIGHT_WEEKEND)) {
            component.addClassName(MiniCalendarVariant.HIGHLIGHT_WEEKEND.getVariantName());
        }

        component.addClassName(CSS_DAY);

        if (hasVariant(MiniCalendarVariant.ROUNDED)) {
            component.addClassName(MiniCalendarVariant.ROUNDED.getVariantName());
        }

        if (hasVariant(MiniCalendarVariant.HOVER_DAYS)) {
            component.addClassName(MiniCalendarVariant.HOVER_DAYS.getVariantName());
        }

        if (isReadOnly()) {
            component.addClassName(CSS_READONLY);
        }

        if (dayEnabledProvider != null) {
            var dayEnabled = dayEnabledProvider.test(forDay);
            component.setEnabled(dayEnabled);
            if (!dayEnabled) {
                component.addClassName(CSS_DISABLED);
            }
        }

        return component;
    }

    private void addRow(List<? extends Component> columns) {

        var rowLayout = new FlexLayout();
        rowLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        rowLayout.setFlexWrap(FlexLayout.FlexWrap.NOWRAP);
        rowLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        rowLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        for (Component column : columns) {
            rowLayout.add(column);
        }

        content.add(rowLayout);
    }

    private boolean hasVariant(MiniCalendarVariant variant) {
        return appliedVariants.contains(variant);
    }

    private boolean isInteractionDisabled() {
        return isReadOnly() || !isEnabled();
    }

    private static void toggleStyle(HasStyle hasStyle, String className) {
        if (hasStyle.hasClassName(className)) {
            hasStyle.removeClassName(className);
        } else {
            hasStyle.addClassName(className);
        }
    }

    private static Span emptySpan() {
        return span("");
    }

    private static Span span(String text) {
        var span = new Span(text);
        span.setHeight(30, Unit.PIXELS);
        span.setWidth(30, Unit.PIXELS);
        span.getStyle().set("margin", "1px");
        return span;
    }

    private static DayOfWeek getFirstDayOfWeekByLocale(Locale locale) {
        return WeekFields.of(locale).getFirstDayOfWeek();
    }

    private static int getLastDayOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth().getDayOfMonth();
    }

    private static boolean isWeekend(LocalDate localDate) {
        return localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY;
    }


    private static final class YearMonthHolder implements HasValue<ValueChangeEvent<YearMonth>, YearMonth> {

        private final HasValue<?, YearMonth> instance = this;
        private final List<ValueChangeListener<? super ValueChangeEvent<YearMonth>>> valueChangeListeners = new ArrayList<>();
        private YearMonth value;

        void setValueFromClient(YearMonth value) {
            final var oldValue = this.value;
            this.value = value;
            fireYearMonthValueChangeEvent(oldValue, value, true);
        }

        @Override
        public void setValue(YearMonth value) {
            final var oldValue = this.value;
            this.value = value;
            fireYearMonthValueChangeEvent(oldValue, value, false);
        }

        @Override
        public YearMonth getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<YearMonth>> valueChangeListener) {
            valueChangeListeners.add(valueChangeListener);
            return () -> valueChangeListeners.remove(valueChangeListener);
        }

        private void fireYearMonthValueChangeEvent(YearMonth oldValue, YearMonth newValue, boolean fromClient) {

            if (Objects.equals(oldValue, newValue)) {
                return;
            }

            final var event = new ValueChangeEvent<YearMonth>() {
                @Override
                public HasValue<?, YearMonth> getHasValue() {
                    return instance;
                }

                @Override
                public boolean isFromClient() {
                    return fromClient;
                }

                @Override
                public YearMonth getOldValue() {
                    return oldValue;
                }

                @Override
                public YearMonth getValue() {
                    return newValue;
                }
            };

            valueChangeListeners.forEach(listener -> listener.valueChanged(event));
        }

        @Override
        public void setReadOnly(boolean b) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public void setRequiredIndicatorVisible(boolean b) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
