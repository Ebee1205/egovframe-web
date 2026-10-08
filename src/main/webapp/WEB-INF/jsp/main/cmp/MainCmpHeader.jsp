<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!-- Header -->
<header class="header | header-sticky">
  <div class="container-fluid | justify-content-between">
    <a class="header-brand" href="#">Header</a>

    <div class="position-relative" id="headerUserSwitcher">
      <button type="button" class="avatar | bg-primary | text-white | border-0" id="headerAvatar" aria-haspopup="true" aria-expanded="false">U</button>
      <ul class="dropdown-menu dropdown-menu-end" id="headerUserMenu" style="max-height: 320px; overflow-y: auto;">
        <li><h6 class="dropdown-header">사용자 변경</h6></li>
      </ul>
    </div>
  </div>
</header>
<script>
  (function () {
    var avatar = document.getElementById('headerAvatar');
    var menu = document.getElementById('headerUserMenu');
    var wrapper = document.getElementById('headerUserSwitcher');
    var loaded = false;
    var STORAGE_KEY = '_user_info';
    var me = null;

    function readMe() {
      try {
        var parsed = JSON.parse(localStorage.getItem(STORAGE_KEY));
        return parsed && typeof parsed === 'object' ? parsed : null;
      } catch (e) {
        return null;
      }
    }

    function saveMe(user) {
      me = user && typeof user === 'object' ? user : null;
      try {
        if (me) {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(me));
        } else {
          localStorage.removeItem(STORAGE_KEY);
        }
      } catch (e) { /* 저장소 접근 불가 시 무시 */ }
      document.dispatchEvent(new CustomEvent('current-user-changed'));
    }

    me = readMe();

    function initial(user) {
      return String((user && (user.nickname || user.email)) || 'U').charAt(0).toUpperCase();
    }

    function renderAvatar() {
      avatar.textContent = initial(me);
      avatar.title = me ? (me.nickname || '') + ' (' + (me.email || '') + ')' : '';
    }

    function setOpen(open) {
      menu.classList.toggle('show', open);
      avatar.setAttribute('aria-expanded', open ? 'true' : 'false');
      if (open) {
        menu.style.position = 'absolute';
        menu.style.right = '0';
        menu.style.left = 'auto';
        menu.style.top = '100%';
      }
    }

    function renderMenu(users) {
      var selected = me;
      users.forEach(function (user) {
        var li = document.createElement('li');
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'dropdown-item' + (selected && selected.uid === user.uid ? ' active' : '');
        btn.textContent = user.nickname + ' (' + user.email + ')';
        btn.addEventListener('click', function () {
          fetch('<c:url value="/me/switch.do"/>?uid=' + encodeURIComponent(user.uid), { method: 'POST' })
            .then(function (res) { return res.json(); })
            .then(function (switched) {
              if (!switched) { return; }
              saveMe(switched);
              renderAvatar();
              Array.prototype.forEach.call(menu.querySelectorAll('.dropdown-item'), function (el) {
                el.classList.remove('active');
              });
              btn.classList.add('active');
              setOpen(false);
            });
        });
        li.appendChild(btn);
        menu.appendChild(li);
      });
    }

    function loadUsers() {
      if (loaded) { return; }
      loaded = true;
      fetch('<c:url value="/me/list.do"/>')
        .then(function (res) { return res.json(); })
        .then(renderMenu)
        .catch(function () { loaded = false; });
    }

    avatar.addEventListener('click', function () {
      var open = !menu.classList.contains('show');
      if (open) { loadUsers(); }
      setOpen(open);
    });
    document.addEventListener('click', function (e) {
      if (!wrapper.contains(e.target)) { setOpen(false); }
    });
    renderAvatar();
  })();
</script>
<!--// Header -->