function updateContentBox(data) {
  const tab1Container = document.querySelector('#tab1');
  const tab2Container = document.querySelector('#tab2');
  const tab3Container = document.querySelector('#tab3');
  const tab4Container = document.querySelector('#tab4');
  const tab5Container = document.querySelector('#tab5');
  const tab6Container = document.querySelector('#tab6');
  const tab7Container = document.querySelector('#tab7');
  const tab8Container = document.querySelector('#tab8');

  data.forEach(item => {
    // 새로운 contentBox 생성       
    const contentBox = document.createElement('div');
    contentBox.className = 'content';
    contentBox.dataset.contenttypeid = item.contenttypeid; // contenttypeid를 데이터 속성으로 추가

    const moveButton = document.createElement('button');
    moveButton.innerHTML = '<img src="./image/none_select.svg" alt="이동하기"> ';

    const deleteButton = document.createElement('button');
    deleteButton.innerHTML = '<img src="./image/none_select.svg" alt="삭제"> ';
    deleteButton.style.display = 'none'; // 초기에는 숨김


    const box = document.createElement('div');
    box.className = 'box';

    const h3Element = document.createElement('h3');
    h3Element.textContent = item.name;

    const pElement = document.createElement('p');
    pElement.innerHTML = item.infotext;

    box.appendChild(h3Element);
    box.appendChild(pElement);
    contentBox.appendChild(box);
    contentBox.appendChild(moveButton);
    contentBox.appendChild(deleteButton); // 삭제 버튼 추가

    tab1Container.appendChild(contentBox); // tab1에 항상 추가

    // contenttypeid에 따라 적절한 탭에 contentBox 복사본 추가
    let newContentBox = contentBox.cloneNode(true); // contentBox의 복사본 생성

    // 각 탭에 맞는 버튼 리스너 설정
    switch (item.contenttypeid) {
      case "12":
        tab2Container.appendChild(newContentBox); // tab2에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "14":
        tab3Container.appendChild(newContentBox); // tab3에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "15":
        tab4Container.appendChild(newContentBox); // tab4에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "25":
        tab5Container.appendChild(newContentBox); // tab5에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "28":
        tab6Container.appendChild(newContentBox); // tab6에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "32":
        tab7Container.appendChild(newContentBox); // tab7에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "38":
        tab8Container.appendChild(newContentBox); // tab8에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "39":
        // 추가 처리 (필요 시)
        break;
      default:
        tab1Container.appendChild(contentBox); // 기본적으로 tab1에 원본 추가
        break;
    }

    // 원본 박스 이동을 위한 버튼 리스너 설정
    setupMoveButton(contentBox, 'main', item.contenttypeid);
  });

  // 이동 버튼 이벤트 리스너 설정하는 함수
  function setupMoveButton(box, source, contentTypeId) {
    const moveButton = box.querySelector('button:nth-of-type(1)');
    const deleteButton = box.querySelector('button:nth-of-type(2)'); // 삭제 버튼 가져오기

    moveButton.addEventListener('click', () => {
      let targetSelector = '';
      switch (contentTypeId) {
        case "12":
          targetSelector = '.left-box2 .con1'; // con1으로 이동
          break;
        case "14":
          targetSelector = '.left-box2 .con2'; // con2로 이동
          break;
        case "15":
          targetSelector = '.left-box2 .con3'; // con3으로 이동
          break;
        case "25":
          targetSelector = '.left-box2 .con4'; // con4로 이동
          break;
        case "28":
          targetSelector = '.left-box2 .con5'; // con5으로 이동
          break;
        case "32":
          targetSelector = '.left-box2 .con6'; // con6으로 이동
          break;
        case "38":
          targetSelector = '.left-box2 .con7'; // con7으로 이동
          break;
        case "39":
          targetSelector = '.left-box2 .con8'; // con8으로 이동
          break;
        default:
          targetSelector = '.left-box2 .con1'; // 기본적으로 con1으로 이동
          break;
      }

      const targetContainer = document.querySelector(targetSelector);

      // 겹치는 내용이 있는지 확인 (이름 기준으로)
      const existingContent = Array.from(targetContainer.querySelectorAll('.content'))
        .some(existingBox => existingBox.querySelector('h3').textContent === box.querySelector('h3').textContent);

      if (existingContent) {
        // 겹치는 내용이 있을 때 경고 메시지 표시
        alert('해당 내용이 이미 존재합니다! 이동할 수 없습니다.');
        return; // 이동을 막음
      }

      // 원본과 복사본 이동 처리
      if (source === 'main') {
        targetContainer.appendChild(box); // 원본 이동
        deleteButton.style.display = 'inline'; // 삭제 버튼 보이기

        // 삭제 버튼 클릭 이벤트 리스너 설정
        deleteButton.onclick = () => {
          box.remove(); // 콘텐츠 박스 삭제
        };
      } else {
        const clonedBox = box.cloneNode(true); // 복사본 생성
        targetContainer.appendChild(clonedBox); // 복사본 이동
        box.remove(); // 원본 박스 삭제

        // 복사본에 대한 삭제 버튼 설정
        const clonedDeleteButton = clonedBox.querySelector('button:nth-of-type(2)');
        clonedDeleteButton.style.display = 'inline'; // 삭제 버튼 보이기

        clonedDeleteButton.onclick = () => {
          clonedBox.remove(); // 콘텐츠 박스 삭제
        };

        clonedBox.querySelector('button:nth-of-type(1)').style.display = 'none'; // 이동하기 버튼 숨기기
      }

      moveButton.style.display = 'none'; // 이동 후 원본의 버튼 숨김
    });
  }
}

// 페이지가 로드될 때 tab1을 보이게 호출
document.addEventListener('DOMContentLoaded', () => {
  // Local Storage에서 itinerary 데이터 가져오기
  const itineraryData = localStorage.getItem('itinerary');

  if (itineraryData) {
    const itinerary = JSON.parse(itineraryData);
    // 가져온 itinerary 데이터를 사용하여 페이지 업데이트
    document.getElementById('itineraryContainer').innerText = JSON.stringify(itinerary, null, 2);

    const exampleData = itinerary.days.flatMap(day =>
        day.items.map(item => ({
          contentid: `contentid-${day.dayNumber}-${item.timeOfDay}`, // 각 항목의 고유 ID 생성
          contenttypeid: item.activityType === "식사" ? "12" : "14", // 식사 또는 관광에 따라 타입 결정
          infoname: item.activityType === "식사" ? "식사 장소" : "관광 장소", // 정보 이름
          infotext: `이곳에서 ${item.activityType}를 즐길 수 있습니다.`, // 정보 텍스트
          label: item.activityType === "식사" ? "F" : "T", // 레이블 (임의 설정)
          name: item.place.name, // 장소 이름
          lat: item.place.mapy, // 위도
          lng: item.place.mapx  // 경도
        }))
    );

    // itinerary 데이터를 사용하여 예시 데이터를 업데이트
    updateContentBox(exampleData); // itinerary를 인자로 전달
  } else {
    console.error('Itinerary data not found in Local Storage.');
    // 데이터가 없을 경우 적절한 처리
    document.getElementById('itineraryContainer').innerText = '일정 데이터를 찾을 수 없습니다.';
  }
});






// 예시 데이터 (백엔드에서 받아올 데이터처럼 가정)
const exampleData = [
  {
    contentid: "129194",
    contenttypeid: "12",
    infoname: "이용가능시설",
    infotext: "가나 어린이미술관 / 블루 스페이스 / 옐로우 스페이스 등",
    label: "C",
    name: "코엑스몰",
    lat: 37.5115557,
    lng: 127.0595261
  },
  {
    contentid: "129194",
    contenttypeid: "12",
    infoname: "입장료",
    infotext: "대인∙소인(24개월 이상~성인) : 12,000원 <br> 24개월 미만 영유아 : 무료",
    label: "G",
    name: "고투몰",
    lat: 37.5062379,
    lng: 127.0050378
  },
  {
    contentid: "129194",
    contenttypeid: "12",
    infoname: "화장실",
    infotext: "있음(남녀 구분)",
    label: "D",
    name: "동대문시장",
    lat: 37.566596,
    lng: 127.007702
  },
  {
    contentid: "129194",
    contenttypeid: "14",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "I",
    name: "IFC몰",
    lat: 37.5251644,
    lng: 126.9255491
  },
  {
    contentid: "129195",
    contenttypeid: "12",
    infoname: "외국인 예약안내",
    infotext: "[개인 관람예약] - 사전예약 필수",
    label: "N",
    name: "N서울타워",
    lat: 37.5511694,
    lng: 126.9882266
  },
  {
    contentid: "129196",
    contenttypeid: "15",
    infoname: "단체 예약안내",
    infotext: "[단체관람예약] - 30인 이상 단체 관람 시 사전예약 필수",
    label: "L",
    name: "롯데월드",
    lat: 37.5110745,
    lng: 127.0980205
  },
  {
    contentid: "129197",
    contenttypeid: "25",
    infoname: "문화유산 예약안내",
    infotext: "[개인 관람예약] - 사전예약 필수",
    label: "K",
    name: "경복궁",
    lat: 37.579617,
    lng: 126.977041
  },
  {
    contentid: "129198",
    contenttypeid: "28",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "B",
    name: "북촌한옥마을",
    lat: 37.582604,
    lng: 126.983594
  },
  {
    contentid: "129199",
    contenttypeid: "32",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "H",
    name: "한강공원",
    lat: 37.526028,
    lng: 126.932609
  },
  {
    contentid: "129200",
    contenttypeid: "38",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "P",
    name: "광화문 광장",
    lat: 37.571271,
    lng: 126.976927
  },
  {
    contentid: "129201",
    contenttypeid: "37",
    infoname: "외국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "M",
    name: "명동성당",
    lat: 37.564509,
    lng: 126.987058
  },
  {
    contentid: "129202",
    contenttypeid: "12",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "G",
    name: "강남역",
    lat: 37.497942,
    lng: 127.027621
  },
  {
    contentid: "129203",
    contenttypeid: "14",
    infoname: "내국인 예약안내",
    infotext: "[단체관람예약] - 25인 이상 단체 관람 시 사전예약 필수",
    label: "D",
    name: "동대문 디자인 플라자",
    lat: 37.566492,
    lng: 127.008219
  }





];

// 페이지 로드 시 데이터를 추가
