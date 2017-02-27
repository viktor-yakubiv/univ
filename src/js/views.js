const monthNames = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сенябрь',
  'Октябрь',
  'Нябрь',
  'Декабрь'
];

const dayNames = [
  'Воскресенье',
  'Понедельник',
  'Вторник',
  'Среда',
  'Четверг',
  'Пятница',
  'Суббота'
];


// Hashes date to string
function dateKey(date) {
  return '' + date.getMonth() + date.getDate();
}


class CalendarView {
  constructor(model) {
    this.model = model;

    // Get DOM
    this.titleElement = document.getElementById('calendar-title');
    this.monthElement = document.getElementById('calendar-month');

    // Create events
    this.onEventViewAdd = new Event(this);
    this.onMonthChange = new Event(this);
    this.onSelectDate = new Event(this);

    // Subscribe events
    model.onLoad.attach(this.init.bind(this));
    model.onSelectionChange.attach(this.changeSelection.bind(this));

    // Setup actions
    document.getElementById('calendar-button-next').addEventListener('click', () => {
      this.onMonthChange.notify('next');
    });
    document.getElementById('calendar-button-prev').addEventListener('click', () => {
      this.onMonthChange.notify('prev');
    });

    // Init calendar
    this.dayElements; // object of hashed days
    this.init(model);
  }

  init() {
    let calendar = this.model;

    let today = new Date();
    today = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    let year = calendar.selectedDate.getFullYear();
    let month = calendar.selectedDate.getMonth();

    let monthStart = new Date(year, month).getDay();
    let monthEnd = new Date(year, month + 1).getDay();

    let startDate = new Date(year, month, -monthStart + calendar.weekStart + 1);
    let endDate = new Date(year, month + 1, 8 - monthEnd + calendar.weekStart);

    // Refresh data
    this.dayElements = {};

    // Refresh HTML
    this.monthElement.innerHTML = '';
    this.titleElement.innerHTML = monthNames[month] + ' ' + year;

    // Add elements
    let rowElement;
    for (let date = new Date(startDate), weekDay = 0;
         date < endDate;
         date.setDate(date.getDate() + 1), weekDay++)
    {
      // Create new row every week
      if (weekDay % 7 === 0) {
        rowElement = this.monthElement.insertRow();
        this.monthElement.appendChild(rowElement);
      }

      // Generate day element
      let dayElement = rowElement.insertCell();
      dayElement.className = 'calendar-day' +
        (date.getMonth() === month ? '' : ' muted') +
        (date.getDate() === calendar.selectedDate.getDate() ? ' selected' : '') +
        (date.getDate() == today.getDate() ? ' today' : '');
      dayElement.innerHTML = `<div class="calendar-day-title">${date.getDate()}</div>`;
      let eventDate = new Date(date);
      dayElement.addEventListener('click', () => {
        this.onSelectDate.notify(eventDate);
      });

      // Generate day events
      let dayEventsElement = document.createElement('ul');
      dayEventsElement.className = 'calendar-day-events';
      let nextDate = new Date(new Date(date).setDate(date.getDate() + 1));
      for (let event of calendar.filterEvents(date, nextDate)) {
        let eventView = new CalendarEventView(event, dayEventsElement);
        this.onEventViewAdd.notify({model: event, view: eventView });
      }
      dayElement.appendChild(dayEventsElement);

      this.dayElements[dateKey(date)] = dayElement;
    }
    this.selectedDateCache = new Date(calendar.selectedDate);
  }

  changeSelection() {
    if (Math.abs(this.selectedDateCache.getMonth() - this.model.selectedDate.getMonth()) > 0) {
      this.init(this.model);
    } else {
      this.dayElements[dateKey(this.selectedDateCache)].classList.remove('selected');
      this.selectedDateCache = new Date(this.model.selectedDate);
      this.dayElements[dateKey(this.selectedDateCache)].className += ' selected';
    }
  }
}


class CalendarEventView {
  constructor(model, parentElement) {
    this.model = model;

    // Events
    this.onRemove = new Event(this);
    this.onUpdate = new Event(this);

    // Initialize element
    this.element = document.createElement('li');
    this.element.innerHTML = `<div>${model.name}</div>
      <div>
        <button class="button inline transparent">
          <span class="icon-delete"></span>
          <span class="sr-only">Редактировать</span>
        </button>
        <button class="button inline transparent">
          <span class="icon-delete"></span>
          <span class="sr-only">Удалить</span>
        </button>
      </div>`;
    parentElement.appendChild(this.element);

    // Element to update
    this.nameElement = this.element.children[0];

    // Event initialization
    let controlElements = this.element.children[1].children;
    let self = this;
    controlElements[1].onclick = function () {
      self.onRemove.notify(self.model);
    };

    // Subscribe model events
    this.model.onRemove.attach(this.remove.bind(this));
    this.model.onUpdate.attach(this.update.bind(this));
  }

  remove() {
    this.element.remove();
  }

  update() {
    this.nameElement.innerHTML = this.model.name;
  }
}
