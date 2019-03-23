function priceComponent(parentElement) {
  // elements
  var element = document.createElement('div');
  element.className = 'price';
  parentElement.appendChild(element);

  // init bootstrap subcomponent
  loaderComponent(element);
}

function loaderComponent(parentElement) {
  // data

  let message = '';


  // methods

  function load(url) {
    parentElement.innerHTML = 'Loading...';

    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
      if (xhr.status === 200 || xhr.status === 304) {
        if (xhr.response !== null) {
          tableComponent(parentElement, xhr.response);
        } else {
          alert('Wrong JSON');
        }
      } else {
        alert('Have no internet connection?');
      }
    };
    xhr.open('GET', url);
    xhr.responseType = 'json';
    xhr.send();
  }

  function parse(str) {
    var obj = JSON.parse(str);
    console.log(obj);
  }

  function show() {
    var html;
    if (message) {
      html =
        '<div class="container">' +
          '<div class="message">' + message + '</div>' +
        '</div>';
    } else {
      html =
        '<div class="container">' +
          '<div class="select-container">' +
            '<label>' +
              '<input type="radio" name="data" value="data/large.json"> Large' +
            '</label>' +
            '<label>' +
              '<input type="radio" name="data" value="data/small.json"> Small' +
            '</label>' +
            '<label>' +
              '<input class="custom-control" type="radio" name="data"> Custom' +
            '</label>' +
          '</div>' +
          '<div class="custom-input hidden">' +
            '<textarea placeholder="Put your JSON here..."></textarea>' +
            '<button>Process</button>' +
          '</div>' +
        '</div>';
    }
    parentElement.innerHTML = html;


    // events

    parentElement.querySelectorAll('input').forEach(function (e) {
      e.onchange = function () {
        load(this.value);
      };
    });

    parentElement.querySelector('.custom-control').onchange = function () {
      parentElement.querySelector('.custom-input').className = 'custom-input';
    };

    parentElement.querySelector('.custom-input button').onclick = function () {
      parse(document.querySelector('.custom-input textarea'));
    };
  }

  show();
}

function tableComponent(parentElement, data) {
  // options

  var pageLen = 50;


  // data

  var table = {};
  table.head = data.splice(0, 1)[0];
  table.rows = data;

  table.page = 0;
  table.sort = false;
  table.filter = false;
  table.selection = [];

  var components = {};


  // methods

  function init() {
    parentElement.innerHTML = '';

    new filterComponent(parentElement, filter);

    // table
    var tableElement = document.createElement('table');
    parentElement.appendChild(tableElement);
    components.tableHead = new headComponent(tableElement, table.head, sort);
    components.tableRows = new rowsComponent(tableElement, select);

    components.pagination = new paginationComponent(parentElement, paginate);
    components.selection = new selectionComponent(parentElement, table.head);

    show();
  }

  function sort(key) {
    if (!table.sort) table.sort = {
      key: '',
      dir: 1
    };

    // change direction
    if (table.sort.key === key) {
      table.sort.dir *= -1;
    }

    // save key
    table.sort.key = key || table.sort.key;

    // look for rigth column
    var col = Object.keys(table.head).indexOf(table.sort.key);
    if (col === -1) return;

    // sort
    (table.filter ? table.filter.rows : table.rows).sort(function (a, b) {
      // TODO: Fix numbers
      return String(a[col]).localeCompare(String(b[col]),
        {numeric: true}) * table.sort.dir;
    });

    show();
  }

  function filter(q) {
    // reset page
    table.page = 0;

    // process query
    if (!q) {
      table.filter = false;
      if (table.sort) sort(table.sort.key);
    } else {
      table.filter = {q: q, rows: []};
      table.rows.forEach(function (e) {
        if (e.join('\n').indexOf(q) !== -1) table.filter.rows.push(e);
      });
    }

    show();
  }

  function select(index) {
    index = pageLen * table.page + index;
    var row = (table.filter ? table.filter.rows : table.rows)[index];
    // TODO: Fix existence of rows in selection
    table.selection.push(row);
    components.selection.show(table.selection);
  }

  function paginate(i) {
    table.page = i;
    show();
  }

  function show() {
    var rows = table.filter ? table.filter.rows : table.rows;
    components.tableHead.show(table.sort);
    components.tableRows.show(
      rows.slice(table.page * pageLen, (table.page + 1) * pageLen));
    components.pagination.show(Math.ceil(rows.length / pageLen));
    components.selection.show(table.selection);
  }


  // subcomponents

  function filterComponent(parentElement, callback) {
    var element = document.createElement('div');
    element.className = 'container filter';
    element.innerHTML = '<input placeholder="Filter...">';
    parentElement.appendChild(element);

    element.querySelector('input').oninput = function () {
      callback(this.value);
    };

    return this;
  }

  function headComponent(parentElement, head, callback) {
    var element = document.createElement('thead');
    parentElement.appendChild(element);

    this.show = function(sort) {
      var html = '';
      for (var key in head) {
        var classAttr = (sort && sort.key === key
          ? ' class="' + (sort.dir > 0 ? 'sort-asc' : 'sort-desc') + '"'
          : '');
        html +=
          '<th' + classAttr + ' data-sort="' + key + '">' +
            head[key] +
          '</th>';
      }
      element.innerHTML = html;

      element.querySelectorAll('th').forEach(function (e) {
        e.onclick = function () {
          callback(this.getAttribute('data-sort'));
        };
      });
    };

    return this;
  }

  function rowsComponent(parentElement, callback) {
    var element = document.createElement('tbody');
    parentElement.appendChild(element);

    this.show = function (rows) {
      var html = '';
      for (var row of rows) {
        html += '<tr>';
        for (var cell of row) html += '<td>' + cell + '</td>';
        html += '</tr>';
      }
      element.innerHTML = html;

      element.querySelectorAll('tr').forEach(function (e, i) {
        e.onclick = function () {
          callback(i);
        };
      });
    };

    return this;
  }

  function selectionComponent(parentElement, head) {
    var element = document.createElement('table');
    parentElement.appendChild(element);

    this.show = function (rows) {
      var html = '';

      if (rows && rows.length > 0) {
        html += '<thead><tr>';
        for (var key in head) {
          html += '<th>' + head[key] + '</th>';
        }
        html += '</tr></thead>';

        html += '<tbody>';
        for (var row of rows) {
          html += '<tr>';
          for (var cell of row) {
            html += '<td>' + cell + '</td>';
          }
          html += '</tr>';
        }
        html += '</tbody>';
      }

      element.innerHTML = html;
    };

    return this;
  }

  function paginationComponent(parentElement, callback) {
    var element = document.createElement('div');
    element.className = 'container pagination';
    parentElement.appendChild(element);

    this.show = function (pageCount) {
      if (pageCount < 2) {
        element.innerHTML = '';
        return;
      }

      var html = '';
      for (var i = 0; i < pageCount; ++i) {
        html += '<a href="#' + (i + 1) + '">' + (i + 1) + '</a>';
      }
      element.innerHTML = html;

      element.querySelectorAll('a').forEach(function (e) {
        e.onclick = function (event) {
          event.preventDefault();
          callback(Number.parseInt(this.hash.substr(1)));
        };
      });
    };

    return this;
  }

  init();
}


var appElement = document.getElementById('app');
priceComponent(appElement);

document.getElementById('add-component').onclick = function () {
  priceComponent(appElement);
};
