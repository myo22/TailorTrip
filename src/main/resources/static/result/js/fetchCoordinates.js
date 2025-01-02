let itineraryData = []; // 전역 변수로 선언

document.addEventListener('DOMContentLoaded', () => {
  itineraryData = JSON.parse(localStorage.getItem('itinerary')) || []; // 로컬 스토리지에서 데이터 불러오기
  if (itineraryData.length > 0) {
    updateContentBox(itineraryData); // 불러온 데이터를 사용하여 업데이트
  } else {
    console.log('로컬 스토리지에 일정 데이터가 없습니다.');
  }
});

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

    // 이미지 URL 박스 생성
    const imageUrlBox = document.createElement('div'); // 새로운 div 박스 생성
    imageUrlBox.className = 'image-url-box'; // 클래스 이름 지정
    const imageElement = document.createElement('img'); // 이미지 요소 생성
    imageElement.src = item.url; // URL 값 설정 (item.url을 사용하는 가정)
    imageElement.alt = '이미지'; // 이미지 설명 추가
    imageUrlBox.appendChild(imageElement); // 이미지 박스에 이미지 추가

    // box 부분 생성
    const box = document.createElement('div');
    box.className = 'box';

    // h3와 p를 감싸는 div 박스 생성
    const textBox = document.createElement('div');
    textBox.className = 'text-box'; // 새로운 클래스 이름 지정

    const h3Element = document.createElement('h3');
    h3Element.textContent = item.name;

    const pElement = document.createElement('p');
    pElement.innerHTML = item.address;

    // 새로운 p 요소 생성 및 추가
    const pElementCategory = document.createElement('p');
    pElementCategory.className = 'category';  // 기존 class 이름 유지
    pElementCategory.textContent = item.category;

    switch (item.category) {
      case '음식':
        pElementCategory.style.color = '#FFC107'; // 예: 음식 카테고리는 연한 핑크색
        break;
      case '숙박':
        pElementCategory.style.color = '#FF4081'; // 숙박은 연한 초록색
        break;
      case '활동':
        pElementCategory.style.color = '#2196F3'; // 관광은 연한 파란색
        break;
      case '':
        pElementCategory.style.color = '#FFEB3B'; // 문화는 노란색
        break;
        // 추가적인 카테고리 색을 필요에 따라 추가할 수 있음
      default:
        pElementCategory.style.color = '#E0E0E0'; // 기본 색상 (회색)
        break;
    }


    // textBox에 h3, p 추가
    textBox.appendChild(h3Element);
    textBox.appendChild(pElement);
    textBox.appendChild(pElementCategory);

    // contentBox에 추가
    box.appendChild(textBox); // 추가된 textBox를 box에 넣음
    box.appendChild(imageUrlBox); // 이미지 URL 박스를 box에 추가
    contentBox.appendChild(box);

    // 이동 버튼 생성
    const moveButton = document.createElement('button');
    moveButton.className = 'move-button';
    moveButton.innerHTML = '<img src="./image/none_select.svg" alt="이동하기"> ';

    // 삭제 버튼 생성
    const deleteButton = document.createElement('button');
    deleteButton.className = 'delete-button';
    deleteButton.innerHTML = '<img src="./image/times_bold.svg" alt="삭제"> ';
    deleteButton.style.display = 'none'; // 초기에는 숨김

    // 수정1
    let isMoved = false;  // 이미 이동했는지 여부를 확인하는 변수

    moveButton.addEventListener('click', function () {
      const img = moveButton.querySelector('img');

      // isMoved가 false일 때만 이미지 변경
      if (isMoved == false) {
        img.src = './image/res_select.svg';  // 새로운 이미지로 변경
        isMoved = true;  // 상태 변경
      } else {
        img.src = './image/res_select.svg';
      }
    });

    contentBox.appendChild(moveButton);
    contentBox.appendChild(deleteButton); // 삭제 버튼 추가

    tab1Container.appendChild(contentBox); // tab1에 항상 추가


    // contenttypeid에 따라 적절한 탭에 contentBox 복사본 추가

    // 마우스가 contentBox에 진입할 때 infoWindow 표시
    contentBox.addEventListener('mouseenter', () => {
      const maxLength = 300; // 제한할 글자 수
      const infotext = item.infotext;

      // 글자 수 제한 후 '...' 추가
      const truncatedText = infotext.length > maxLength
          ? infotext.slice(0, maxLength) + "..."
          : infotext;

      // InfoWindow 콘텐츠 설정
      infoWindow.setContent(`
        <div>
            <h1>${item.name}</h1>
            <h3>${item.address}</h3>
            <p class="infotext">${truncatedText}</p> <!-- 제한된 텍스트 사용 -->
        </div>
      `);
      infoWindow.setPosition({ lat: item.lat + 0.0011, lng: item.lng }); // item의 좌표를 사용
      infoWindow.open(map); // 지도에 infoWindow 표시
    });


    // 마우스가 contentBox에서 나갈 때 infoWindow 숨기기
    contentBox.addEventListener('mouseleave', () => {
      infoWindow.close();
    });

    // 수정1

    // let newContentBox = contentBox.cloneNode(true); // contentBox의 복사본 생성
    const newContentBox = contentBox.cloneNode(true);
    newContentBox.classList.add('content');

    // 복사본에 infoWindow 이벤트 설정
    newContentBox.addEventListener('mouseenter', () => {
      infoWindow.setContent(`
    <div>
        <h1>${item.name}</h1>
        <h3>${item.address}</h3>
        <p class="infotext">${item.infotext}</p>
    </div>
  `);
      infoWindow.setPosition({ lat: item.lat + 0.0011, lng: item.lng });
      infoWindow.open(map);
    });
    newContentBox.addEventListener('mouseleave', () => {
      infoWindow.close();
    });
    // click




    // 각 탭에 맞는 복사본 추가
    switch (item.contenttypeid) {
      case "1":
        tab2Container.appendChild(newContentBox);
        break;
      case "2":
        tab3Container.appendChild(newContentBox);
        break;
      case "3":
        tab4Container.appendChild(newContentBox);
        break;
      case "4":
        tab5Container.appendChild(newContentBox);
        break;
      case "5":
        tab6Container.appendChild(newContentBox);
        break;
      case "6":
        tab7Container.appendChild(newContentBox);
        break;
      case "7":
        tab8Container.appendChild(newContentBox);
        break;
      default:
        tab1Container.appendChild(newContentBox);
        break;
    }

    // 복사본 이동을 위한 이벤트 리스너 설정 복사본 클릭하면 left-box2로 이동
    const clonedMoveButton = newContentBox.querySelector('.move-button');
    clonedMoveButton.addEventListener('click', () => {
      let targetSelector = '';
      switch (item.contenttypeid) {
        case "1":
          targetSelector = '.left-box2 .con1';
          break;
        case "2":
          targetSelector = '.left-box2 .con2';
          break;
        case "3":
          targetSelector = '.left-box2 .con3';
          break;
        case "4":
          targetSelector = '.left-box2 .con4';
          break;
        case "5":
          targetSelector = '.left-box2 .con5';
          break;
        case "6":
          targetSelector = '.left-box2 .con6';
          break;
        case "7":
          targetSelector = '.left-box2 .con7';
          break;
        case "8":
          targetSelector = '.left-box2 .con8';
          break;
        default:
          targetSelector = '.left-box2 .con1';
          break;
      }
      // 타겟셀렉트 = 전체는 타겟셀렉트에 포함되지 않음 타겟셀렉트는 구분하는거

      const targetContainer = document.querySelector(targetSelector);

      const existingContent = Array.from(targetContainer.querySelectorAll('.content'))
          .some(existingBox => existingBox.querySelector('h3').textContent === box.querySelector('h3').textContent);

      if (existingContent) {
        // 겹치는 내용이 있을 때 경고 메시지 표시
        alert('해당 내용이 이미 존재합니다! 이동할 수 없습니다.');
        return; // 이동을 막음
      }
      // 이게 이동을 하는거임 수정

      if (targetContainer) {
        // 새로운 이동용 복사본 생성
        const additionalCopy = newContentBox.cloneNode(true);

        additionalCopy.classList.add('moved-content');

        // 추가 복사본의 삭제 버튼 설정
        const deleteButton = additionalCopy.querySelector('.delete-button');
        deleteButton.style.display = 'inline';
        deleteButton.onclick = () => {
          console.log("Deleting copy and resetting state");

          additionalCopy.remove();  // 복사본 삭제

          const img = moveButton.querySelector('img');
          img.src = './image/none_select.svg';  // 이미지 복원

          isMoved = false;  // 상태 초기화
          console.log("isMoved after deletion:", isMoved);  // 상태 확인
        };

        // 이동 버튼 숨기기
        additionalCopy.querySelector('.move-button').style.display = 'none';

        // 추가 복사본을 타겟 컨테이너에 이동
        targetContainer.appendChild(additionalCopy);
      }
    });

    // contenttypeid와 대응되는 tab의 class 이름 (탭 숨기기)
    // 기존 tabClass 숨김 코드 유지
    const contentTypeTabMap = [
      { contentTypeId: "7", tabClass: '.tab8' },
      { contentTypeId: "6", tabClass: '.tab7' },
      { contentTypeId: "5", tabClass: '.tab6' },
      { contentTypeId: "4", tabClass: '.tab5' },
      { contentTypeId: "3", tabClass: '.tab4' },
      { contentTypeId: "2", tabClass: '.tab3' }
    ];


    // 반복문을 사용하여 각 contenttypeid에 대해 처리
    contentTypeTabMap.forEach(({ contentTypeId, tabClass }) => {
      const isContentTypePresent = itineraryData.some(item => item.contenttypeid === contentTypeId);
      if (!isContentTypePresent) {
        const tabElement = document.querySelector(tabClass);
        if (tabElement) {
          tabElement.style.display = 'none'; // 해당 tab 숨기기
        }
      }
    });

    // 추가된 conClass 숨김 코드
    const contentTypeConMap = [
      { contentTypeId: "7", conClass: '.con7' },
      { contentTypeId: "6", conClass: '.con6' },
      { contentTypeId: "5", conClass: '.con5' },
      { contentTypeId: "4", conClass: '.con4' },
      { contentTypeId: "3", conClass: '.con3' },
      { contentTypeId: "2", conClass: '.con2' }
    ];

    // 반복문을 사용하여 각 contenttypeid에 대해 처리
    contentTypeConMap.forEach(({ contentTypeId, conClass }) => {
      const isContentTypePresent = itineraryData.some(item => item.contenttypeid === contentTypeId);
      if (!isContentTypePresent) {
        const conElement = document.querySelector(conClass);
        if (conElement) {
          conElement.style.display = 'none'; // 해당 con 요소 숨기기
        }
      }
    });

    // 수정항목 끝




    // 원본 박스 이동을 위한 버튼 리스너 설정
      setupMoveButton(contentBox, 'main', item.contenttypeid);
  });

  // 이동 버튼 이벤트 리스너 설정하는 함수
  function setupMoveButton(box, source, contentTypeId) {
    const moveButton = box.querySelector('.move-button');

    // 이동 버튼 클릭 시 새로운 복사본을 생성하여 이동 처리
    moveButton.addEventListener('click', () => {
      let targetSelector = '';
      switch (contentTypeId) {
        case "1":
          targetSelector = '.left-box2 .con1';
          break;
        case "2":
          targetSelector = '.left-box2 .con2';
          break;
        case "3":
          targetSelector = '.left-box2 .con3';
          break;
        case "4":
          targetSelector = '.left-box2 .con4';
          break;
        case "5":
          targetSelector = '.left-box2 .con5';
          break;
        case "6":
          targetSelector = '.left-box2 .con6';
          break;
        case "7":
          targetSelector = '.left-box2 .con7';
          break;
        case "8":
          targetSelector = '.left-box2 .con8';
          break;
        default:
          targetSelector = '.left-box2 .con1';
          break;
      }

      const targetContainer = document.querySelector(targetSelector);

      const existingContent = Array.from(targetContainer.querySelectorAll('.content'))
          .some(existingBox => existingBox.querySelector('h3').textContent === box.querySelector('h3').textContent);

      if (existingContent) {
        // 겹치는 내용이 있을 때 경고 메시지 표시
        alert('해당 내용이 이미 존재합니다! 이동할 수 없습니다.');
        return; // 이동을 막음
      }

      if (targetContainer) {
        // 추가 복사본 생성
        const additionalCopy = box.cloneNode(true);

        additionalCopy.classList.add('moved-content');

        // 추가 복사본의 이동 버튼 숨기기
        additionalCopy.querySelector('.move-button').style.display = 'none';

        // 추가 복사본의 삭제 버튼 설정
        const deleteButton = additionalCopy.querySelector('.delete-button');
        deleteButton.style.display = 'inline';
        deleteButton.onclick = () => {
          console.log("Deleting copy and resetting state");

          additionalCopy.remove();  // 복사본 삭제

          const img = moveButton.querySelector('img');
          img.src = './image/none_select.svg';  // 이미지 복원

          isMoved = false;  // 상태 초기화
          console.log("isMoved after deletion:", isMoved);  // 상태 확인
        };

        // 타겟 컨테이너로 추가 복사본 이동
        targetContainer.appendChild(additionalCopy);


      }
    });
  }

}

function initMapWithData() {
  const itineraryData = JSON.parse(localStorage.getItem('itinerary')) || []; // 로컬 스토리지에서 데이터 불러오기

  if (itineraryData.length === 0) {
    console.warn("Itinerary data is empty or not found in local storage.");
    return;
  }

  window.initMap(itineraryData); // 데이터와 함께 initMap 호출
}

// 로컬스토리지
document.getElementById('moveButton').addEventListener('click', () => {
  // left-box2에서 데이터 수집
  const leftBox2Contents = document.querySelectorAll('.left-box2 .content');
  const dataToSend = [];

  // 각 콘텐츠 박스의 정보를 수집하기
  leftBox2Contents.forEach(contentBox => {
    const title = contentBox.querySelector('h3').textContent; // 제목 가져오기
    const info = contentBox.querySelector('p').innerHTML; // 정보 가져오기
    const category = contentBox.querySelector('.category').textContent.trim().replace(/\d+/g, '');

    // category 가져오기
    const url = contentBox.querySelector('.image-url-box img').src; // 이미지 URL 가져오기
    const contenttypeid = contentBox.querySelector('.contenttypeid').textContent; // contenttypeid 가져오기 (숨겨진 요소)

    // 데이터를 dataToSend 배열에 추가
    dataToSend.push({ title, info, category, url, contenttypeid });
  });

  // 로컬 스토리지에 데이터 저장
  localStorage.setItem('leftBox2Data', JSON.stringify(dataToSend));

  // 페이지 이동
  window.location.href = '/itinerary-management/itinerary-management.html'; // 다음 페이지로 이동
});





