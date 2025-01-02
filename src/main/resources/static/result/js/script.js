let map, bounds, infoWindow; // 전역 변수 선언
let markersArray = []; // 모든 마커를 관리하기 위한 배열

document.addEventListener(`DOMContentLoaded`, function () {
  document.querySelector('.open').addEventListener('click', () => {
    const leftBox2 = document.querySelector('.left-box2');
    const mapElement = document.querySelector('#map');

    // 'on' 클래스 토글
    leftBox2.classList.toggle('on');

    // 'on' 클래스에 따라 #map의 width 값을 변경
    if (leftBox2.classList.contains('on')) {
      mapElement.style.width = 'calc(100% - 1000px)'; // left-box2가 표시될 때
    } else {
      mapElement.style.width = '100%'; // left-box2가 숨겨질 때
    }
  });

  // tabtab 
  const buttons = document.querySelectorAll(`.btn li`);

  // 버튼의 active 추가 제거
  for (const btn of buttons) {
    btn.addEventListener(`click`, function () {
      this.classList.add(`active`);
      for (const siblings of buttons) {
        if (siblings !== this) {
          siblings.classList.remove(`active`);
        }
      }

      // 탭메뉴 연결
      const tab = this.getAttribute(`data-tab`);
      const tabBox = document.querySelectorAll(`.tabs div`);

      // .tabs div 의 active 전부 제거
      for (const tabContent of tabBox) {
        tabContent.classList.remove(`active`);
      }

      // data-tab 에 저장되어있는 아이디명만 active 추가
      const changeTab = document.querySelector(`#${tab}`);
      changeTab.classList.add(`active`);
    });
  }
});

// 데이터 받아와서 들어오는 첫번째 데이터 기준으로 줌
window.initMap = function (itineraryData) {
  // 초기 중심 좌표 설정
  const initialCenter = itineraryData && itineraryData.length > 0  // `itineraryData` 대신 `itinerary` 사용
      ? itineraryData[0]  // 첫 번째 데이터
      : { lat: 37.5400456, lng: 126.9921017 };              // 기본 좌표

  map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: initialCenter.lat, lng: initialCenter.lng }, // 초기 중심 좌표
    zoom: 10, // 지도 확대 수준
    scrollwheel: true,
  });

  bounds = new google.maps.LatLngBounds();
  infoWindow = new google.maps.InfoWindow();

  // 초기 데이터로 마커 로드 (모든 마커를 보여주기 위해 null 전달)
  filterMarkersByType(null);
};



function getMarkerIcon(contenttypeid) {
  // Marker 색상을 contenttypeid에 따라 반환하는 함수
  switch (contenttypeid) {
    case '1':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/red-dot.png" // 타입 12에 대한 빨간색
      };
    case '2':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/blue-dot.png" // 타입 14에 대한 파란색
      };
    case '3':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/green-dot.png" // 타입 15에 대한 초록색
      };
    case '4':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png" // 타입 25에 대한 노란색
      };
    case '5':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/purple-dot.png" // 타입 28에 대한 보라색
      };
    case '6':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/orange-dot.png" // 타입 32에 대한 주황색
      };
    case '7':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/pink-dot.png" // 타입 38에 대한 분홍색
      };
    case '8':
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/gray-dot.png" // 타입 39에 대한 회색
      };
    default:
      return {
        url: "http://maps.google.com/mapfiles/ms/icons/black-dot.png" // 기본 검은색 마커
      };
  }
}
function filterMarkersByType(contenttypeid) {

  const itinerary = JSON.parse(localStorage.getItem('itinerary')) || []; // 로컬 스토리지에서 가져오기

  // contenttypeid가 null이면 전체 데이터를 필터링 없이 사용
  const filteredData = contenttypeid
      ? itinerary.filter(item => item.contenttypeid === contenttypeid.toString())
      : itinerary;

  // 이전에 추가된 마커 제거 및 새로운 마커 추가
  clearMarkers(); // 기존 마커를 제거하는 함수

  filteredData.forEach(({ label, name, lat, lng, contenttypeid }) => {
    const marker = new google.maps.Marker({
      position: { lat, lng },
      label,
      icon: getMarkerIcon(contenttypeid), // 색상에 따른 아이콘 설정
      map
    });

    marker.addListener("click", () => {
      map.panTo(marker.position);
      infoWindow.setContent(name);
      infoWindow.open({
        anchor: marker,
        map
      });
    });

    markersArray.push(marker); // 새로 추가된 마커를 배열에 저장
  });
}

// 각 탭에 클릭 이벤트 추가
document.querySelector('li[data-tab="tab1"]').addEventListener('click', () => filterMarkersByType(null));
document.querySelector('li[data-tab="tab2"]').addEventListener('click', () => filterMarkersByType(1));
document.querySelector('li[data-tab="tab3"]').addEventListener('click', () => filterMarkersByType(2));
document.querySelector('li[data-tab="tab4"]').addEventListener('click', () => filterMarkersByType(3));
document.querySelector('li[data-tab="tab5"]').addEventListener('click', () => filterMarkersByType(4));
document.querySelector('li[data-tab="tab6"]').addEventListener('click', () => filterMarkersByType(5));
document.querySelector('li[data-tab="tab7"]').addEventListener('click', () => filterMarkersByType(6));
document.querySelector('li[data-tab="tab8"]').addEventListener('click', () => filterMarkersByType(7));
document.querySelector('li[data-tab="tab9"]').addEventListener('click', () => filterMarkersByType(8));

function clearMarkers() {
  markersArray.forEach(marker => marker.setMap(null)); // 모든 마커를 지도에서 제거
  markersArray = []; // 배열을 초기화
}








