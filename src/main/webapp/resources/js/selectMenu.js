
const pageList = [
  {
    name: 'overview',
    child: [
      {
        page: 'Dashboard',
        name: '대시보드',
        path: '/main.do',
        icon: 'cil-speedometer',
      },
    ],
  },
  {
    name: '운영',
    child: [
      {
        page: 'UserList',
        name: '사용자',
        path: '/user/list.do',
        icon: 'cil-layers',
      },
      {
        page: 'CodeList',
        name: '공통코드',
        path: '/code/list.do',
        icon: 'cil-speedometer',
      },
      // {
      //   page: 'RegionList',
      //   name: '지역코드',
      //   path: '/region/list.do',
      //   icon: 'cil-speedometer',
      // },
    ],
  },
  {
    name: '콘텐츠',
    child: [
      {
        page: 'EventList',
        name: '이벤트',
        path: '/event/list.do',
        icon: 'cil-layers',
      },
      // {
      //   page: 'StoreList',
      //   name: '사업장',
      //   path: '/store/list.do',
      //   icon: 'cil-speedometer',
      // },
      {
        page: 'MapView',
        name: '지도 보기',
        path: '/content/map.do',
        icon: 'cil-speedometer',
      },
    ],
  },
];

(function renderSidebarMenu() {
  const menuElement = document.getElementById('sidebar-menu');
  if (!menuElement) return;

  const contextPath = menuElement.dataset.contextPath || '';
  const pathname = window.location.pathname;
  const currentPath = contextPath && pathname.startsWith(contextPath)
    ? pathname.slice(contextPath.length) || '/'
    : pathname;

  pageList.forEach((group) => {
    const title = document.createElement('li');
    title.className = 'nav-title';
    title.textContent = group.name;
    menuElement.appendChild(title);

    group.child.forEach((menu) => {
      const item = document.createElement('li');
      item.className = 'nav-item';

      const link = document.createElement('a');
      link.className = 'nav-link';
      link.href = `${contextPath}${menu.path}`;
      link.dataset.page = menu.page;

      const menuPrefix = menu.path.slice(0, menu.path.lastIndexOf('/') + 1);
      const isActive = currentPath === menu.path
        || (menuPrefix !== '/' && currentPath.startsWith(menuPrefix));
      if (isActive) {
        link.classList.add('active');
        link.setAttribute('aria-current', 'page');
      }

      const icon = document.createElement('i');
      icon.classList.add('nav-icon', menu.icon);
      link.append(icon, document.createTextNode(menu.name));
      item.appendChild(link);
      menuElement.appendChild(item);
    });
  });
})();
