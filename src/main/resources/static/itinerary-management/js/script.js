const storedData = localStorage.getItem('leftBox2Data');

// 데이터를 가져왔다면 처리
if (storedData) {
  const data = JSON.parse(storedData);

  // 모든 con 요소를 초기 상태에서 숨기기
  const allConElements = document.querySelectorAll('#dataContainer .con');
  allConElements.forEach(con => {
    con.style.display = 'none'; // 기본적으로 숨김 처리
  });

  // 데이터 분류 및 해당 div에 추가
  data.forEach(item => {
    const contentBox = document.createElement('div');
    contentBox.classList.add('content-box');

    // conX에 맞는 박스를 찾아서 추가
    const targetDiv = document.querySelector(`#dataContainer .con${item.contenttypeid}`);

    if (targetDiv) {
      // 숨겨진 con 요소 표시
      targetDiv.style.display = 'block';

      // box1 생성
      const box1 = document.createElement('div');
      box1.classList.add('box1');

      // 제목
      const title = document.createElement('h3');
      title.textContent = item.title;
      box1.appendChild(title);
      title.classList.add('title');

      // 정보
      const info = document.createElement('p');
      info.innerHTML = item.info;
      box1.appendChild(info);
      info.classList.add('info');

      // 카테고리
      const category = document.createElement('p');
      category.textContent = item.category; // 카테고리 표시
      box1.appendChild(category);
      category.classList.add('category');

      // box2 생성
      const box2 = document.createElement('div');
      box2.classList.add('box2');

      // 이미지 URL 표시
      const imageElement = document.createElement('img');
      imageElement.src = item.url; // 이미지 URL
      imageElement.alt = '이미지'; // 이미지 설명
      box2.appendChild(imageElement);

      // contentBox에 box1과 box2 추가
      contentBox.appendChild(box1);
      contentBox.appendChild(box2);

      // contentBox를 해당 conX 박스에 추가
      targetDiv.appendChild(contentBox);

      // 카테고리 색상 분류
      switch (item.category) {
        case '음식':
          category.style.color = '#FFC107';
          break;
        case '숙박':
          category.style.color = '#FF4081';
          break;
        case '활동':
          category.style.color = '#2196F3';
          break;
        default:
          category.style.color = '#E0E0E0';
          break;
      }
    }
    
    
  });
  
  
} 

else {
  console.log('로컬 스토리지에 데이터가 없습니다.');
}

function saveItinerary (){
  const accessToken =  localStorage.getItem('accessToken');

  if(!accessToken){
    // 현재 요청 정보를 localStorage에 저장
    const itineraryData = JSON.parse(storedData) || []; // POST 요청의 body 데이터
    localStorage.setItem('pendingRequest', JSON.stringify({
      url: '/save',
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: itineraryData,
    }));

    // 로그인 화면으로 이동 시 원래 요청 URL 추가
    const currentURL = encodeURIComponent('/');
    window.location.href = `/member/login?redirect=${currentURL}`;
  } else {
    const itineraryData = JSON.parse(storedData) || []; // POST 요청의 body 데이터
    fetch('/save', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`, // Authorization 헤더에 토큰 추가
      },
      body: JSON.stringify(itineraryData),
    })
        .then(response => response.json())
        .then(data => {
          alert('일정이 저장되었습니다.')
        })
        .catch(error => {
          console.log('Error:', error);
        });
  }
}

document.getElementById('saveButton').addEventListener("click", saveItinerary);
