class Calendar {
  constructor(url) {
    // Model events
    this.onLoad = new Event(this);
    this.onSelectionChange = new Event(this);
    this.onEventAdd = new Event(this);
    this.onEventRemove = new Event(this);


    // Events in calendar
    this.events = [];
    this.selectedDate = new Date();

    // Calendar start day
    this.weekStart = 1;

    // Load events from server
    this.loadEvents(url);
  }


  get selectedDate() {
    return this.selected;
  }

  set selectedDate(date) {
    date = new Date(date);
    this.selected = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    this.onSelectionChange.notify(this.selected);
    return this.selected;
  }


  loadEvents(url) {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', url);

    let calendar = this;
    xhr.onload = function () {
      let eventObjects = JSON.parse(this.responseText);
      let events = [];
      for (let event of eventObjects) {
        events.push(new CalendarEvent(calendar, event.name, new Date(event.date), event.members, event.description));
      }

      calendar.events = calendar.events.concat(events);
      calendar.onLoad.notify(events);
    };

    xhr.onerror = function () {
      console.error('Error connection with server');
      setTimeout(() => { this.loadEvents(url); }, 5000);
    };

    xhr.send();
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

    this.onEventAdd.notify(event);
  }


  removeEvent(event) {
    let eventIndex = this.events.indexOf(event);
    if (eventIndex > -1) {
      let removed = this.events.splice(eventIndex, 1);
      this.onEventRemove.notify(removed);
    }
  }


  filterEvents(startDate, endDate) {
    let filtered = [];
    for (let event of this.events) {
      if (event.date > startDate && event.date < endDate) {
        filtered.push(event);
      }
    }
    return filtered;
  }
}


class CalendarEvent {
  constructor(calendar, name, date, members=[], description='') {
    this.parent = calendar;
    this.name = name;
    this.date = date;
    this.members = members;
    this.description = description;

    this.onCreate = new Event(this);
    this.onUpdate = new Event(this);
    this.onRemove = new Event(this);

    this.onCreate.notify();
  }

  update(params) {
    for (let param in params) {
      if (typeof this[param] !== 'undefined') {
        this[param] = params[param];
      }
    }
    this.onUpdate.notify();
  }

  remove() {
    this.parent.removeEvent(this);
    this.onRemove.notify();
  }
}
