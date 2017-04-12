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

    // save key
    data.sort = key || data.sort;

    // look for rigth column
    var col = Object.keys(data.headers).indexOf(data.sort);
    if (col === -1) return;

    // sort
    (data.search ? data.search : data.rows).sort(function (a, b) {
      return a[col].localeCompare(b[col]) * dir;
    });
  }

  function search(q) {
    // reset page
    data.page = 0;

    // process query
    if (!q) {
      data.search = false;
      if (data.sort) sort(data.sort);
    } else {
      data.search = {q: q, rows: []};
      data.rows.forEach(function (e) {
        if (e.join('\n').indexOf(q) !== -1) data.search.rows.push(e);
      });
    }

    show();
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
    var i, html = '';

    // search
    html += '<div class="search">';
    var searchQuery = (data.search ? data.search.q : '');
    html += '<input placeholder="Search..." value="' + searchQuery +'">';
    html += '</div>';

    // table
    html += '<table>';
    for (i in data.headers) html +=
      '<th data-sort="' + i + '">' + data.headers[i] + '</th>';
    var rows = (data.search ? data.search.rows : data.rows);
    for (i = data.page * pageLen; i < (data.page + 1) * pageLen
         && i < rows.length; ++i) {
      html += '<tr>';
      rows[i].forEach(function (e) {
        html += '<td>' + e + '</td>';
      });
      html += '</tr>';
    }
    html += '</table>';

    // pagination
    html += '<div class="pagination">';
    for (i = 0; i < Math.ceil(rows.length / pageLen); ++i) {
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
    element.querySelector('.search input').onkeyup = function () {
      search(this.value);
      element.querySelector('.search input').focus();
    };
  }

  init();
}

var appElement = document.getElementById('app');
component(appElement);

document.getElementById('add-component').onclick = function () {
  component(appElement);
};
