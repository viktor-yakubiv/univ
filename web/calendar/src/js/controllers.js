class CalendarController {
  constructor(model, view) {
    this.model = model;
    this.view = view;

    this.view.onEventViewAdd.attach(this.createEventController.bind(this));
    this.view.onMonthChange.attach(this.changeMonth.bind(this));
    this.view.onSelectDate.attach(this.selectCell.bind(this));
  }

  createEventController(view, options) {
    new CalendarEventController(options.model, options.view);
  }

  changeMonth(view, direction) {
    this.model.selectedDate = this.model.selectedDate.setMonth(this.model.selectedDate.getMonth() +
      (direction === 'next' ? 1 : -1));
  }

  selectCell(view, date) {
    this.model.selectedDate = date;
  }
}


class CalendarEventController {
  constructor(model, view) {
    this.model = model;
    this.view = view;

    this.view.onRemove.attach(this.invokeModelRemove.bind(this));
    this.view.onUpdate.attach(this.invokeModelUpdate.bind(this));
  }

  invokeModelUpdate(view, params) {
    this.model.update(params);
  }

  invokeModelRemove(view, event) {
    event.remove();
  }
}
