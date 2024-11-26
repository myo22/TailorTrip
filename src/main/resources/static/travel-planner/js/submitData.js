let startDate = null; // 전역 변수로 선언
let endDate = null;
let selectedRegion = null; // 선택된 지역을 저장하는 전역 변수

let userPreferences = {
  region: null,
  tripDuration: null,
  interests: [],
  activities: [],
  foodPreferences: [],
  petPreference: false,
  travelStyle: null,
  accommodation: null
};

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

// ---------------------------------------------------------지역 선택-------------------------------------------
// submitRegionData 함수
function submitRegionData() {
  const activeRegion = document.querySelector('.con.active'); // active 클래스가 있는 지역 확인
  if (activeRegion) {
    const region = localStorage.getItem('selectedRegion'); // localStorage에서 선택된 지역 가져오기
    if (region) {
      userPreferences.region = region; // userPreferences 객체에 저장
      // alert(`지역 선택 완료: ${region}`);  데이터 전송 수정
      scrollToNextSection('section3'); // 다음 섹션으로 스크롤
    } else {
      alert('지역을 선택해 주세요.');
    }
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
    const startDateString = startDate.toISOString().split('T')[0];
    const endDateString = endDate.toISOString().split('T')[0];

    // 여행 기간 계산
    const durationInDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1; // 날짜 객체에서 직접 계산
    userPreferences.tripDuration = durationInDays; // 여행 기간 추가

    // alert(`날짜 선택 완료: ${startDateString} ~ ${endDateString}`);
    scrollToNextSection('section4'); // 다음 섹션으로 스크롤
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

// ----------------------------------관심사 선택-------------------------------------------
// 데이터를 서버로 전송하는 함수 section4
const bttn1 = document.querySelectorAll(`.img-box1`);
let selectedInterests = []; // 선택된 관심사를 저장하는 배열
const maxSelection = 2; // 최대 선택 가능 수
// 다음 버튼 클릭 시 데이터 전송
document.getElementById('submit-interests-btn').addEventListener('click', submitInterestsData);

// 선택된 관심사를 서버에 전송하는 함수
function submitInterestsData() {
  if (selectedInterests.length > 0) {
    userPreferences.interests = selectedInterests; // userPreferences 객체에 저장
    // alert(`관심사 선택 완료: ${selectedInterests.join(', ')}`);
    scrollToNextSection('section5'); // 다음 섹션으로 스크롤
  } else {
    alert('관심사를 선택해 주세요.');
  }
}

// ----------------------------------관심사 선택 끝---------------------------------------------------------------------------------------

// ----------------------------------활동 및 애완동물 동반 여부 선택-------------------------------------------
function submitActivityData() {
  const activities = window.selectedActivities;
  const petPreference = window.selectedPetPreference === "애견동반해요";

  if (activities.length > 0) {
    userPreferences.activities = activities; // userPreferences 객체에 저장
    userPreferences.petPreference = petPreference;

    // alert('활동 및 애완동물 동반 여부 선택 완료');
    scrollToNextSection('section6'); // 다음 섹션으로 이동
  } else {
    alert('활동과 애완동물 동반 여부를 선택해 주세요.');
  }
}
// "다음" 버튼 클릭 시 POST 요청 실행
document.getElementById('submit-activities-btn').addEventListener('click', submitActivityData);
// ----------------------------------활동 및 애완동물 동반 여부 선택 끝---------------------------------------------------------------------------------------

// ----------------------------------음식 종류 및 여행 스타일 선택-------------------------------------------
// 음식과 여행 스타일 데이터를 서버로 전송하는 함수
function submitFoodAndTravelStyleData() {
  if (window.selectedFoodPreferences.length > 0 && window.selectedTravelStyle) {
    userPreferences.foodPreferences = window.selectedFoodPreferences; // userPreferences 객체에 저장
    userPreferences.travelStyle = window.selectedTravelStyle;

    // alert('음식 종류 및 여행 스타일 선택 완료');
    scrollToNextSection('section7'); // 다음 섹션으로 이동
  } else {
    alert('음식 종류와 여행 스타일을 선택해 주세요.');
  }
}
// "다음" 버튼 클릭 시 데이터 전송
document.getElementById('submit-food-and-travel-style-btn').addEventListener('click', submitFoodAndTravelStyleData);
// ----------------------------------음식 종류 및 여행 스타일 선택 끝---------------------------------------------------------------------------------------

// ----------------------------------숙소 선택-------------------------------------------
function submitAccommodationData(event) {
  event.preventDefault(); // 기본 버튼 동작 방지

  if (window.selectedAccommodation) {
    userPreferences.accommodation = window.selectedAccommodation; // userPreferences 객체에 저장
    // alert('숙소 선택 완료');

    // 모든 데이터가 수집된 후, 최종 데이터 전송 함수 호출
    submitAllUserPreferences();
  } else {
    alert('숙소를 선택해 주세요.');
  }
}
// "완료" 버튼 클릭 시 데이터 전송
document.getElementById('submit-accommodation-btn').addEventListener('click', submitAccommodationData);
// ----------------------------------숙소 선택 끝---------------------------------------------------------------------------------------

// ----------------------------------최종 데이터 전송-------------------------------------------
function submitAllUserPreferences() {
  console.log('Submitting preferences:', userPreferences); // 전송할 데이터 출력
  // 서버로 POST 요청 보내기
  fetch('/submit-preferences', { // 실제 서버 URL로 변경
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userPreferences), // userPreferences 객체를 JSON 형식으로 전송
  })
      .then(response => {
        console.log('Response status:', response.status); // 상태 코드 출력
        console.log('Response body:', response); // 응답 객체 출력
        if (!response.ok) {
          throw new Error(`Network response was not ok: ${response.status}`);
        }
        return response.json();
      })
      .then(itinerary => {
        // 여기서 itinerary 데이터를 사용하여 페이지를 업데이트하거나 리디렉션
        localStorage.setItem('itinerary', JSON.stringify(itinerary)); // 일정 데이터를 Local Storage에 저장
        window.location.href = '/result/result.html'; // 페이지 리디렉션
      })
      .catch(error => {
        console.error('Error:', error);
        alert('모든 질문을 선택하지 않았습니다.');
      });
}

// 섹션으로 부드럽게 스크롤하는 함수
function scrollToNextSection(sectionId) {
  const section = document.getElementById(sectionId);
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' });
  }
}

// previous 버튼 클릭 시 각 섹션으로 이동하는 함수
for (let i = 1; i <= 5; i++) {
  document.querySelector(`.previous${i}`).addEventListener('click', function() {
    scrollToNextSection(`section${i + 1}`); // 예: previous1 -> section2, previous2 -> section3
  });
}

// ----------------------------------최종 데이터 전송 끝---------------------------------------------------------------------------------------
