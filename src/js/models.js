class Calendar {
  constructor() {
    // Events in calendar
    this.events = [];

    // Calendar start day
    this.weekStart = 1;

    // Events
    this.onAdd = new Event(this);
  }


  get today() {
    return new Date();
  }


  addEvent(eventParameters) {
    // Default data
    let name = 'Безимянный';
    let date = new Date();
    let members = [];
    let description = '';

    // Extract data
    if (typeof eventParameters['name'] !== 'undefined')
      name = eventParameters.name;
    if (typeof eventParameters['date'] !== 'undefined')
      date = eventParameters.date;
    if (typeof eventParameters['members'] !== 'undefined')
      members = eventParameters.members;
    if (typeof eventParameters['description'] !== 'undefined')
      description = eventParameters.description;

    // Create event
    let event = new CalendarEvent(name, date, members, description);
    this.events.push(event);

    this.onAdd.notify(event);
  }

  filterEvents(startDate, endDate) {}
}

class CalendarEvent {
  constructor(name, date, members=[], description='') {
    this.name = name;
    this.date = date;
    this.members = members;
    this.description = description;
  }
}
