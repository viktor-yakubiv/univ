/* Event
 * Stores list of listeners and notify them about an event when it happens.
 *
 * Implementation of Observer pattern
 */
class Event {
  constructor(sender) {
    this.sender = sender;
    this.listeners = [];
  }

  attach(listener) {
    this.listeners.push(listener);
  }

  notify(argument) {
    for (let listener of this.listeners) listener(this.sender, argument);
  }
}
