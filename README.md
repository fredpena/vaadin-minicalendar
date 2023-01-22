# MiniCalendar

`MiniCalendar` is a [server-only](https://github.com/vaadin/addon-starter-flow) Vaadin component for displaying and
selecting `LocalDate` values.

|                                              The *sunny* side 🌞                                              |                                          The *dark* side (of the moon 🌒)                                          |
|:-------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_standard.png" alt="Default Standard" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_standard.png" alt="Default Standard" width="300" style="border-radius: 10px"/> |

## Fundamentals
The internals are built on the [Java Time API](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
, the displayed values are localized with the locale that is set for the current UI.

The component implements the `LocaleChangeObserver`. It listens for locale changes and will redraw itself when the
locale has changed.

It is highly customizable, offers a lot of configuration- and interaction possibilities. You can either use the built-in
`MiniCalendarVariant` or provide custom CSS classes using the /* TODO StyleProvider */.

## Features

### Single Value Selection
The Component is designed to have a single value selected. It implements the `HasValue`
interface and can therefore be used with a `Binder` like any other default Vaadin field.

You can listen to value changes as well as `YearMonth` changes which will be triggered when
the user navigates through the months *or* the component gets a new value set which differs
from the previous `YearMonth` value.

```java
val miniCalendar = new MiniCalendar();

miniCalendar.addValueChangeListener(event -> {
    Notification.show("Value changed to " + event.getValue());
});

miniCalendar.addYearMonthChangeListener(event -> {
    Notification.show("Value changed to " + event.getValue());
});
```

When adding a listener you'll get an instance of `Registration` back that which can be used
to remove said listener again.

```java
var registration = miniCalendar.addYearMonthChangeListener(...);

registration.remove();
```

## Appearance

### Theming
The component is based on the [Lumo Theme](https://vaadin.com/docs/latest/styling/lumo), and it's appearance can easily
be changed by using the built-in [Theme Variants](https://vaadin.com/docs/latest/styling/lumo/variants).

To apply a Theme Variant you simply call the `addThemeVariants()` method.

```java
miniCalendar.addThemeVariants(MiniCalendarVariant.ROUNDED);
miniCalendar.addThemeVariants(MiniCalendarVariant.HIGHLIGHT_WEEKEND);
```
To remove an already applied Theme Variant simply call the `removeThemeVariants()` method.

```java
miniCalendar.removeThemeVariants(MiniCalendarVariant.HOVER_DAYS);
```

You can combine multiple Theme Variants to change the component's appearance.

<details>
    <summary>Show examples</summary>

#### Highlight weekends

|                                            Light Mode                                            |                                               Dark Mode                                               |
|:------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_highlight_weekends.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_highlight_weekends.png" width="300" style="border-radius: 10px"/> |

#### Shifted beginning of the week

|                                               Light Mode                                                |                                                  Dark Mode                                                   |
|:-------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> |


#### Shifted beginning of the week

|                                               Light Mode                                                |                                                  Dark Mode                                                   |
|:-------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> |


#### Hover days

|                                        Light Mode                                        |                                           Dark Mode                                           |
|:----------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_hover_days.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_hover_days.png" width="300" style="border-radius: 10px"/> |


#### Rounded

|                                      Light Mode                                       |                                         Dark Mode                                          |
|:-------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_rounded.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_rounded.png" width="300" style="border-radius: 10px"/> |


#### Rounded, Highlight weekends

|                                                Light Mode                                                |                                                   Dark Mode                                                   |
|:--------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/default_rounded_highlight_weekends.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/dark_default_rounded_highlight_weekends.png" width="300" style="border-radius: 10px"/> |

</details>


### Component State
The component's state implicitly affects the appearance of the component. For instance a *disabled* component will look
gray-ish to indicate that the user cannot interact with it. A component in *read only* state won't change the cursor
when hovering over interaction parts and hide the navigation buttons.

Check out these examples of the component in different states.

<details>
    <summary>Read Only</summary>

#### Highlight weekends

|                                                Light Mode                                                 |                                                   Dark Mode                                                    |
|:---------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_highlight_weekends.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_highlight_weekends.png" width="300" style="border-radius: 10px"/> |

#### Shifted beginning of the week

|                                                    Light Mode                                                    |                                                       Dark Mode                                                       |
|:----------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> |


#### Shifted beginning of the week

|                                                    Light Mode                                                    |                                                       Dark Mode                                                       |
|:----------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_shifted_beginning_of_week.png" width="300" style="border-radius: 10px"/> |


#### Hover days

|                                            Light Mode                                             |                                               Dark Mode                                                |
|:-------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_hover_days.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_hover_days.png" width="300" style="border-radius: 10px"/> |


#### Rounded

|                                           Light Mode                                           |                                              Dark Mode                                              |
|:----------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_rounded.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_rounded.png" width="300" style="border-radius: 10px"/> |


#### Rounded, Highlight weekends

|                                                    Light Mode                                                     |                                                       Dark Mode                                                        |
|:-----------------------------------------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------------------:|
| <img src="docs/screens/readonly_default_rounded_highlight_weekends.png" width="300" style="border-radius: 10px"/> | <img src="docs/screens/readonly_dark_default_rounded_highlight_weekends.png" width="300" style="border-radius: 10px"/> |

</details>



