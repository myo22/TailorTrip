let startDate = null; // 전역 변수로 선언
let endDate = null;
// submitRegionData.js
// 지역 데이터 전송 시작
// 다음 섹션으로 스크롤하는 함수
function scrollToNextSection(sectionId) {
  const section = document.getElementById(sectionId);
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' });
  }
}

// submitRegionData 함수
function submitRegionData() {
  const region = window.selectedRegion; // 전역 변수에서 선택된 지역 가져오기
  if (region) {
    scrollToNextSection('section3'); // 다음 섹션으로 스크롤 이동

    fetch('http://localhost:8080/submit-region', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ region }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('데이터 전송 완료: ' + data.message);
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('지역을 선택해 주세요.');
  }
}

// 버튼 클릭 시 데이터 전송
document.getElementById('submit-btn').addEventListener('click', submitRegionData);
// 데이터를 서버로 전송하는 함수 달력 시작
function submitData() {
  const region = window.selectedRegion; // 이 변수는 전역 변수로 정의되어 있어야 합니다.
  const dates = {
    region: region,
    startDate: startDate ? startDate.toISOString().split('T')[0] : null, // YYYY-MM-DD 형식으로 변환
    endDate: endDate ? endDate.toISOString().split('T')[0] : null
  };

  if (dates.startDate && dates.endDate) {
    scrollToNextSection('section4');
    fetch('http://localhost:8080/submit-dates', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(dates),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('여행 기간 데이터 전송 완료: ' + data.message);
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('여행 기간을 선택해 주세요.'); // 경고 메시지 추가
  }
}
// 버튼 클릭 시 데이터 전송
document.getElementById('submit-btn1').addEventListener('click', submitData);
// 달력 코드 끝
// 데이터를 서버로 전송하는 함수 section4
// submitInterestData.js
function submitInterestData() {
  const selectedInterests = [];
  const selectedElements = document.querySelectorAll('.img-box1.on'); // 선택된 클래스가 'on'인 요소들

  selectedElements.forEach(element => {
    const interest = element.querySelector('p').textContent; // 선택된 관심사의 텍스트 가져오기
    selectedInterests.push(interest);
  });

  if (selectedInterests.length > 0) {
    scrollToNextSection('section5');
    fetch('http://localhost:8080/submit-interests', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ interests: selectedInterests }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('관심사 데이터 전송 완료: ' + data.message);
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('관심사를 선택해 주세요.');
  }
}

// '다음' 버튼 클릭 시 데이터 전송
document.getElementById('submit-interests-btn').addEventListener('click', submitInterestData);

// section5 방식
// submitActivityData.js section5
function submitActivityData() {
  const selectedActivities = [];
  const selectedActivitiesElements = document.querySelectorAll('.sec-5-box.on'); // 선택된 활동
  const selectedPetPreference = document.querySelector('.img-box2.on p')?.textContent || null; // 선택된 동물 동반 여부

  // 선택된 활동들을 배열에 추가
  selectedActivitiesElements.forEach(element => {
    const activity = element.querySelector('p').textContent; // 선택된 활동의 텍스트 가져오기
    selectedActivities.push(activity);
  });

  // 데이터 전송
  if (selectedActivities.length > 0 && selectedPetPreference) {
    scrollToNextSection('section6');
    fetch('http://localhost:8080/submit-activities', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ activities: selectedActivities, petPreference: selectedPetPreference }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('활동 데이터 전송 완료: ' + data.message);
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('활동과 동물 동반 여부를 선택해 주세요.');
  }
}

// '다음' 버튼 클릭 시 데이터 전송
document.getElementById('submit-activities-btn').addEventListener('click', submitActivityData);


// section6 -------------------------------------------------
// submitFoodAndTravelStyleData.js

// 음식 종류와 여행 스타일을 전송하는 함수
function submitFoodAndTravelStyleData() {
  const selectedFoodPreferences = [];
  const selectedTravelStyleElement = document.querySelector('.sec-6-box2.on'); // 선택된 여행 스타일 요소
  const selectedTravelStyle = selectedTravelStyleElement ? selectedTravelStyleElement.querySelector('p').textContent : null; // 선택된 여행 스타일 텍스트

  // 선택된 음식 종류 요소를 가져옵니다
  const selectedFoodElements = document.querySelectorAll('.sec-6-box1.on');
  selectedFoodElements.forEach(element => {
    const foodType = element.querySelector('p').textContent; // 선택된 음식 종류의 텍스트 가져오기
    selectedFoodPreferences.push(foodType); // 배열에 추가
  });

  // 데이터 전송
  if (selectedFoodPreferences.length > 0 && selectedTravelStyle) {
    scrollToNextSection('section7');
    fetch('http://localhost:8080/submit-food-and-travel-style', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        foodPreferences: selectedFoodPreferences,
        travelStyle: selectedTravelStyle
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
        alert('음식 종류와 여행 스타일 데이터 전송 완료: ' + data.message);
        scrollToNextSection('section7'); // 다음 섹션으로 스크롤 이동
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('음식 종류와 여행 스타일을 선택해 주세요.');
  }
}

// 버튼 클릭 시 데이터 전송
document.getElementById('submit-food-and-travel-style-btn').addEventListener('click', submitFoodAndTravelStyleData);


// section 777777777777
function submitAccommodationData(event) {
  event.preventDefault(); // 기본 링크 동작 방지

  const selectedAccommodation = document.querySelector('.sec-7-box.on'); // 선택된 숙소 박스
  let accommodation;

  if (selectedAccommodation) {
    accommodation = selectedAccommodation.getAttribute('data-accommodation'); // 숙소 정보 가져오기

    // POST 요청 전송
    fetch('http://localhost:8080/submit-accommodation', { // 실제 서버 URL로 변경
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ accommodation }), // 선택된 숙소 정보 전송
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        console.log('Success:', data);
        alert('숙소 데이터 전송 완료: ' + data.message);
        // 성공적으로 데이터 전송 후 다음 섹션으로 이동
        window.location.href = '#nextSection'; // 실제 이동할 섹션의 ID로 변경
      })
      .catch(error => {
        console.error('Error:', error);
        alert('데이터 전송 중 오류가 발생했습니다.');
      });
  } else {
    alert('숙소를 선택해 주세요.');
  }
}

// 숙소 박스를 클릭했을 때 on 클래스 토글
document.querySelectorAll('.sec-7-box').forEach(box => {
  box.addEventListener('click', function () {
    document.querySelectorAll('.sec-7-box').forEach(b => b.classList.remove('on')); // 기존 선택 해제
    this.classList.add('on'); // 클릭한 박스 선택
  });
});

// 모든 선호도를 서버로 제출하고 일정을 생성하는 함수
function submitAllPreferences() {
  const sessionId = getSessionId(); // 사용자 세션 ID를 가져오는 로직 구현 필요

  fetch('http://localhost:8080/submit-all', { // 실제 서버 URL로 변경
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ sessionId }), // 필요한 경우 세션 ID 포함
  })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text(); // HTML 응답일 경우
      })
      .then(data => {
        console.log('Success:', data);
        // recommendResult.html 페이지로 이동하거나, AJAX로 데이터를 처리
        window.location.href = '/recommendResult'; // 실제 경로로 변경
      })
      .catch(error => {
        console.error('Error:', error);
        alert('일정을 생성하는 중 오류가 발생했습니다.');
      });
}

// '모든 선호도 제출' 버튼 클릭 시 호출
document.getElementById('submit-all-btn').addEventListener('click', submitAllPreferences);


