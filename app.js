function component(parentElement) {
  // elements
  var element = document.createElement('div');
  parentElement.appendChild(element);

  // data
  var data = {};

  // options
  var pageLen = 50;


  function load(url) {
    element.innerHTML = 'Loading...';

    var xhr = new XMLHttpRequest();
    xhr.onload = function () {
      if (xhr.status === 200 || xhr.status === 304) {
        // invalid json
        if (xhr.response !== null) {
          data.headers = xhr.response.splice(0, 1)[0];
          data.rows = xhr.response;
          data.page = 0;
          show();
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

  function sort(key) {
    // check sort direction
    var dir = 1;
    if (data.sort === key) dir = -1;

    // look for rigth column
    var col = Object.keys(data.headers).indexOf(key);
    console.log(col);
    if (col === -1) return;

    // sort
    data.rows.sort(function (a, b) {
      return a[col].localeCompare(b[col]) * dir;
    });
  }

  function paginate(i) {
    data.page = i;
    show();
  }

  function init() {
    element.innerHTML = ' ... selector... ';
    element.querySelectorAll('.select').forEach(function (e) {
      e.onchange = function () {
        load(this.value);
      };
    });

    load('data/large.json');
  }

  function show() {
    var i;
    console.log(data);

    // table
    var html = '<table>';
    for (i in data.headers) html +=
      '<th data-sort="' + i + '">' + data.headers[i] + '</th>';
    for (i = data.page * pageLen; i < (data.page + 1) * pageLen
         && i < data.rows.length; ++i) {
      html += '<tr>';
      data.rows[i].forEach(function (e) {
        html += '<td>' + e + '</td>';
      });
      html += '</tr>';
    }
    html += '</table>';

    // pagination
    html += '<div class="pagination">';
    for (i = 0; i < Math.ceil(data.rows.length / pageLen); ++i) {
      html += '<a href="#' + (i + 1) + '">' + (i + 1) + '</a>';
    }
    html += '</div>';

    element.innerHTML = html;
    element.querySelectorAll('th').forEach(function (e) {
      e.onclick = function () {
        sort(this.getAttribute('data-sort'));
      };
    });
    element.querySelectorAll('.pagination a').forEach(function (e) {
      e.onclick = function (event) {
        event.preventDefault();
        paginate(Number.parseInt(this.hash.substr(1)));
      };
    });
  }

  init();
}

var appElement = document.getElementById('app');
component(appElement);

document.getElementById('add-component').onclick = function () {
  component(appElement);
};
