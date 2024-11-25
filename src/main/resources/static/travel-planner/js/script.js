

document.addEventListener(`DOMContentLoaded`, function () {
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

    renderCalendar();

    //   달력끝
    // 풀페이지
    new fullpage('#wrap', {
        anchors: ['anchor1', 'anchor2', 'anchor3', 'anchor4'],
        scrollingSpeed: 600,
        onLeave: (origin, destination, direction) => {
            // 2번째(1)부터 7번째(6) 섹션에서 스크롤 차단
            if (destination.index >= 1 && destination.index <= 6) {
                fullpage_api.setAllowScrolling(false); // 스크롤 비활성화
            } else {
                fullpage_api.setAllowScrolling(true);  // 스크롤 활성화
            }
        }

    });
    // script.js
    const buttons = document.querySelectorAll('.con');
    buttons.forEach(button => {
        button.addEventListener('click', function () {
            const selectedRegion = this.getAttribute('data-region'); // 선택된 지역 저장
            localStorage.setItem('selectedRegion', selectedRegion); // localStorage에 저장
            buttons.forEach(btn => {
                btn.classList.remove('active'); // 모든 버튼에서 active 제거
            });
            this.classList.add('active'); // 클릭한 버튼에 active 추가
        });
    });

    // section4
    for (let button of bttn1) {
        button.addEventListener(`click`, function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                selectedInterests = selectedInterests.filter(interest => interest !== this.querySelector('.box-txt p').textContent); // 선택 해제 시 관심사 배열에서 제거
            } else if (selectedInterests.length < 5) {
                this.classList.add('on');
                selectedInterests.push(this.querySelector('.box-txt p').textContent); // 선택된 관심사 배열에 추가
            }
            // else {
            //     alert(`최대 ${maxSelection}개까지만 선택할 수 있습니다.`); // 최대 선택 수 초과 시 경고
            // }
        });
    }

    // section5
    // 전역 변수 선언
    window.selectedActivities = [];  // 활동 선택 목록
    window.selectedPetPreference = null;  // 애완동물 동반 여부

    // 활동 선택 처리
    const bttn4 = document.querySelectorAll('.sec-5-box'); // 활동 선택 박스들

    bttn4.forEach(button => {
        button.addEventListener('click', function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                window.selectedActivities = window.selectedActivities.filter(activity => activity !== this.querySelector('p').textContent);
            } else if (window.selectedActivities.length < 4) {
                this.classList.add('on');
                window.selectedActivities.push(this.querySelector('p').textContent);
            }
        });
    });

    // 애완동물 동반 여부 선택 처리
    const bttn2 = document.querySelectorAll('.img-box2'); // 애완동물 선택 박스들

    bttn2.forEach(button => {
        button.addEventListener('click', function () {
            bttn2.forEach(btn => btn.classList.remove('on')); // 모든 버튼에서 on 클래스 제거
            this.classList.add('on');
            window.selectedPetPreference = this.querySelector('p').textContent; // 선택된 텍스트 저장
        });
    });
    // section6 box1
    // 전역 변수 선언
    window.selectedFoodPreferences = [];  // 음식 선택 목록
    window.selectedTravelStyle = null;  // 여행 스타일 선택

    // 음식 종류 선택 처리
    const bttn3 = document.querySelectorAll('.sec-6-box1');
    let selectedFoodCount = 0;  // 선택된 음식 개수

    bttn3.forEach(button => {
        button.addEventListener('click', function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                window.selectedFoodPreferences = window.selectedFoodPreferences.filter(food => food !== this.querySelector('p').textContent);
                selectedFoodCount--;  // 선택 해제 시 카운트 감소
            } else if (selectedFoodCount < 5) {
                this.classList.add('on');
                window.selectedFoodPreferences.push(this.querySelector('p').textContent);
                selectedFoodCount++;  // 새로 선택할 때 카운트 증가
            }
        });
    });

    // 여행 스타일 선택 처리
    const bttn5 = document.querySelectorAll('.sec-6-box2');

    bttn5.forEach(button => {
        button.addEventListener('click', function () {
            bttn5.forEach(btn => btn.classList.remove('on'));  // 다른 스타일 선택 시 'on' 클래스 제거
            this.classList.add('on');
            window.selectedTravelStyle = this.querySelector('p').textContent;  // 선택된 스타일 저장
        });
    });
    // sectuin7 
    const bttn6 = document.querySelectorAll('.sec-7-box');

    // 숙소 선택을 위한 변수
    window.selectedAccommodation = []; // 배열로 설정하여 여러 숙소를 저장

    // 숙소 선택 이벤트 리스너 추가
    for (let button of bttn6) {
        button.addEventListener('click', function () {
            // 'on' 클래스가 이미 있다면 제거하고, 선택 목록에서 해당 숙소를 제거
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                window.selectedAccommodation = window.selectedAccommodation.filter(accommodation => accommodation !== this.getAttribute('data-accommodation'));
            } else {
                // 'on' 클래스 추가하고, 선택 목록에 숙소 추가
                this.classList.add('on');
                window.selectedAccommodation.push(this.getAttribute('data-accommodation'));
            }
        });
    }








    // topbtn
    const topBtn = document.querySelector(`.top-btn`);

    window.addEventListener(`scroll`, function () {
        const sct = window.scrollY;

        console.log(sct);

        if (sct >= 300) {
            topBtn.classList.add(`on`);
        } else {
            topBtn.classList.remove(`on`);
        }
    });

    topBtn.addEventListener(`click`, () => {
        window.scrollTo({
            top: 0,
            behavior: `smooth`
        });
    });

});