let startDate = null; // 전역 변수로 선언
let endDate = null;
let selectedRegion = null; // 선택된 지역을 저장하는 전역 변수
// submitRegionData.js
// 지역 데이터 전송 시작
// 다음 섹션으로 스크롤하는 함수
function scrollToNextSection(sectionId) {
  const section = document.getElementById(sectionId);
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' });
  }
}
window.selectedRegion = () => selectedRegion;
// submitRegionData 함수
// submitRegionData 함수
function submitRegionData() {
  const activeRegion = document.querySelector('.con.active'); // active 클래스가 있는 지역 확인
  if (activeRegion) {
    const region = localStorage.getItem('selectedRegion'); // localStorage에서 선택된 지역 가져오기

    // 서버로 POST 요청 보내기
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ region }), // 선택된 지역 데이터를 JSON 형식으로 전송
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert(`데이터 전송 완료: ${data.region}`);

        // 데이터 전송 성공 후 다음 섹션으로 스크롤
        scrollToNextSection('section3'); // 'section3' 부분은 이동할 섹션의 ID로 수정 가능
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('지역을 선택해 주세요.');
  }
}
// '다음' 버튼 클릭 시 데이터 전송
document.getElementById('submit-btn').addEventListener('click', submitRegionData);

// ---------------------------------------------------------달력-------------------------------------------
// 월 이름 배열
const monthNames = [
  "1월", "2월", "3월", "4월", "5월", "6월",
  "7월", "8월", "9월", "10월", "11월", "12월"
];

let currentDate = new Date();
let selectedMonth = currentDate.getMonth();
let selectedYear = currentDate.getFullYear();
const monthElement = document.getElementById('month-name');
const daysElement = document.getElementById('days');
const prevButton = document.getElementById('prev');
const nextButton = document.getElementById('next');

// 캘린더 렌더링
function renderCalendar() {
  const firstDay = new Date(selectedYear, selectedMonth, 1);
  const lastDay = new Date(selectedYear, selectedMonth + 1, 0);

  monthElement.textContent = `${monthNames[selectedMonth]} ${selectedYear}`;
  daysElement.innerHTML = '';

  // 첫 번째 주의 빈 칸 추가
  for (let i = 0; i < firstDay.getDay(); i++) {
    daysElement.innerHTML += `<div></div>`;
  }

  // 각 날짜 렌더링
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const dayElement = document.createElement('div');
    dayElement.textContent = day;
    const dayDate = new Date(Date.UTC(selectedYear, selectedMonth, day)); // UTC 날짜로 생성

    // 선택된 날짜 하이라이트
    if (startDate && dayDate.getTime() === startDate.getTime()) {
      dayElement.classList.add('selected');
    }
    if (endDate && dayDate.getTime() === endDate.getTime()) {
      dayElement.classList.add('selected');
    }
    if (startDate && endDate && dayDate > startDate && dayDate < endDate) {
      dayElement.classList.add('range');
    }

    // 날짜 클릭 이벤트 처리
    dayElement.addEventListener('click', () => handleDateClick(dayDate));
    daysElement.appendChild(dayElement);
  }
}

// 날짜 클릭 시 처리하는 함수
function handleDateClick(dayDate) {
  // UTC 날짜로 설정
  dayDate.setUTCHours(0, 0, 0, 0);

  if (!startDate || (startDate && endDate)) {
    startDate = dayDate;
    endDate = null; // 종료 날짜 초기화
  } else if (dayDate < startDate) {
    startDate = dayDate; // 시작 날짜 재설정
  } else {
    const differenceInDays = (dayDate - startDate) / (1000 * 60 * 60 * 24) + 1;
    if (differenceInDays <= 7) {
      endDate = dayDate; // 종료 날짜 설정
    } else {
      alert('최대 7일까지만 선택할 수 있습니다.');
      return;
    }
  }
  renderCalendar(); // 캘린더 다시 렌더링
}

// 날짜 데이터 서버로 전송하는 함수
function submitDateData() {
  if (startDate && endDate) {
    const startDateString = startDate.toISOString().split('T')[0]; // YYYY-MM-DD 형식으로 변환
    const endDateString = endDate.toISOString().split('T')[0];     // YYYY-MM-DD 형식으로 변환

    // 서버로 POST 요청 보내기
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        startDate: startDateString,
        endDate: endDateString
      }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert(`데이터 전송 완료: ${startDateString} ~ ${endDateString}`);
        scrollToNextSection('section4');
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('날짜를 선택해 주세요.');
  }
}

// 이전/다음 버튼 클릭 이벤트
prevButton.addEventListener('click', () => {
  selectedMonth = (selectedMonth - 1 + 12) % 12;
  if (selectedMonth === 11) {
    selectedYear--;
  }
  renderCalendar();
});

nextButton.addEventListener('click', () => {
  selectedMonth = (selectedMonth + 1) % 12;
  if (selectedMonth === 0) {
    selectedYear++;
  }
  renderCalendar();
});

// "다음" 버튼 클릭 시 데이터 전송
document.getElementById('submit-btn1').addEventListener('click', function () {
  if (startDate && endDate) {
    submitDateData(); // 버튼 클릭 시에만 데이터 전송
  } else {
    alert('날짜를 선택해 주세요.'); // 날짜 선택이 안된 경우 경고
  }
});
// 캘린더 초기 렌더링
renderCalendar()
// ----------------------------------달력 끝---------------------------------------------------------------------------------------
// 데이터를 서버로 전송하는 함수 section4
// submitInterestData.js
const bttn1 = document.querySelectorAll(`.img-box1`);
let selectedInterests = []; // 선택된 관심사를 저장하는 배열
const maxSelection = 2; // 최대 선택 가능 수
// 다음 버튼 클릭 시 데이터 전송
document.getElementById('submit-interests-btn').addEventListener('click', submitInterestsData);

// 선택된 관심사를 서버에 전송하는 함수
function submitInterestsData() {
  if (selectedInterests.length > 0) {
    // 서버로 POST 요청 보내기
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        interests: selectedInterests // 선택된 관심사 배열을 JSON 형식으로 전송
      }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert(`선택된 관심사 전송 완료: ${selectedInterests.join(', ')}`);
        scrollToNextSection('section5');
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('관심사를 선택해 주세요.');
  }
}
// section5------------------------------------------------
function submitActivityData() {
  // 전역 변수를 통해 데이터 접근
  const activities = window.selectedActivities;
  const petPreference = window.selectedPetPreference;

  if (activities.length > 0 && petPreference) {
    // 서버로 POST 요청 보내기
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        activities: activities,
        petPreference: petPreference
      }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('데이터 전송 완료: ' + JSON.stringify(data));
        scrollToNextSection('section6'); // 다음 섹션으로 이동
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('활동과 애완동물 동반 여부를 선택해 주세요.');
  }
}
// "다음" 버튼 클릭 시 POST 요청 실행
document.getElementById('submit-activities-btn').addEventListener('click', submitActivityData);
// // section6 -------------------------------------------------
// submitFoodAndTravelStyleData.js
// 음식 종류와 여행 스타일을 전송하는 함수
// 음식과 여행 스타일 데이터를 서버로 전송하는 함수
function submitFoodAndTravelStyleData() {
  if (window.selectedFoodPreferences.length > 0 && window.selectedTravelStyle) {
    // 서버로 POST 요청 보내기
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        foodPreferences: window.selectedFoodPreferences,
        travelStyle: window.selectedTravelStyle
      }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('데이터 전송 완료: ' + JSON.stringify(data));
        scrollToNextSection('section7');  // 다음 섹션으로 이동
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('음식 종류와 여행 스타일을 선택해 주세요.');
  }
}
// "다음" 버튼 클릭 시 데이터 전송
document.getElementById('submit-food-and-travel-style-btn').addEventListener('click', submitFoodAndTravelStyleData);
// section 777777777777
function submitAccommodationData(event) {
  event.preventDefault(); // 기본 버튼 동작 방지

  if (window.selectedAccommodation) {
    // POST 요청 전송
    fetch('https://jsonplaceholder.typicode.com/posts', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        accommodation: window.selectedAccommodation // 선택된 숙소 정보 전송
      }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('네트워크 응답이 올바르지 않습니다.');
        }
        return response.json();
      })
      .then(data => {
        console.log('성공:', data);
        alert('숙소 데이터 전송 완료: ' + JSON.stringify(data)); // 디버깅을 위해 전체 데이터 표시

        // 성공적으로 데이터 전송 후 다른 링크로 이동
        window.location.href = 'https://example.com/nextSection'; // 이동할 링크를 입력하세요
      })
      .catch(error => {
        console.error('오류:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('숙소를 선택해 주세요.');
  }
}

// "완료" 버튼 클릭 시 데이터 전송
document.getElementById('submit-accommodation-btn').addEventListener('click', submitAccommodationData);







