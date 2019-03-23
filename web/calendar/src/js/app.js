let calendar = new Calendar('../events.json');
let calendarView = new CalendarView(calendar);
let calendarControler = new CalendarController(calendar, calendarView);
