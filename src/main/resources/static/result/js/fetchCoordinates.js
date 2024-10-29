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
      case "1":
        tab2Container.appendChild(newContentBox); // tab2에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "2":
        tab3Container.appendChild(newContentBox); // tab3에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "3":
        tab4Container.appendChild(newContentBox); // tab4에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "4":
        tab5Container.appendChild(newContentBox); // tab5에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "5":
        tab6Container.appendChild(newContentBox); // tab6에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "6":
        tab7Container.appendChild(newContentBox); // tab7에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "7":
        tab8Container.appendChild(newContentBox); // tab8에 복사본 추가
        setupMoveButton(newContentBox, 'tab1', item.contenttypeid); // tab1에서 이동할 수 있도록 설정
        break;
      case "8":
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
        case "1":
          targetSelector = '.left-box2 .con1'; // con1으로 이동
          break;
        case "2":
          targetSelector = '.left-box2 .con2'; // con2로 이동
          break;
        case "3":
          targetSelector = '.left-box2 .con3'; // con3으로 이동
          break;
        case "4":
          targetSelector = '.left-box2 .con4'; // con4로 이동
          break;
        case "5":
          targetSelector = '.left-box2 .con5'; // con5으로 이동
          break;
        case "6":
          targetSelector = '.left-box2 .con6'; // con6으로 이동
          break;
        case "7":
          targetSelector = '.left-box2 .con7'; // con7으로 이동
          break;
        case "8":
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

// 페이지가 로드될 때 로컬 스토리지에서 데이터 불러오기
document.addEventListener('DOMContentLoaded', () => {
  const itineraryData = JSON.parse(localStorage.getItem('itinerary')); // 로컬 스토리지에서 데이터 불러오기
  if (itineraryData) {
    updateContentBox(itineraryData); // 불러온 데이터를 사용하여 업데이트
  } else {
    console.log('로컬 스토리지에 일정 데이터가 없습니다.');
  }
});






