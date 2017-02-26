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


class CalendarView {
  constructor(model) {
    // Get DOM
    this.titleElement = document.getElementById('calendar-title');
    this.monthElement = document.getElementById('calendar-month');

    // Init calendar
    this.init(model);

    // Subscribe events
    model.onAdd.attach(this.present)
  }

  init(calendar) {
    let year = calendar.today.getFullYear();
    let month = calendar.today.getMonth();

    let monthStart = new Date(year, month).getDay();
    let monthEnd = new Date(year, month + 1).getDay();

    let startDate = new Date(year, month, -monthStart + calendar.weekStart + 1);
    let endDate = new Date(year, month + 1, 8 - monthEnd + calendar.weekStart);

    let monthHTML = '';
    for (let date = new Date(startDate), weekDay = 0; date < endDate; date.setDate(date.getDate() + 1), weekDay++) {
      //if (weekDay % 7 === 0) monthHTML += '      <tr>';
      let dayClass = 'calendar-day' + (date.getMonth() === month ? '' : ' muted');
      monthHTML += `
        <div class="${dayClass}">
          <div class="calendar-day-title">${date.getDate()}</div>
        </div>`;
      //if (weekDay % 7 === 6) monthHTML += '\n      </tr>\n';
    }
    console.log(monthHTML);

    // Add HTML
    this.monthElement.innerHTML = monthHTML;
    this.titleElement.innerHTML = monthNames[month] + ' ' + year;
  }

  present(model, event) {}
}
