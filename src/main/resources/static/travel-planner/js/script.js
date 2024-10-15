

function scrollToNextSection(nextSectionId) {
    const nextSection = document.getElementById(nextSectionId);
    nextSection.scrollIntoView({ behavior: 'smooth' });
}

document.addEventListener(`DOMContentLoaded`, function () {
    // 달력
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

    function renderCalendar() {
        const firstDay = new Date(selectedYear, selectedMonth, 1);
        const lastDay = new Date(selectedYear, selectedMonth + 1, 0);

        monthElement.textContent = `${monthNames[selectedMonth]} ${selectedYear}`;
        daysElement.innerHTML = '';

        for (let i = 0; i < firstDay.getDay(); i++) {
            daysElement.innerHTML += `<div></div>`;
        }

        for (let day = 1; day <= lastDay.getDate(); day++) {
            const dayElement = document.createElement('div');
            dayElement.textContent = day;
            const dayDate = new Date(selectedYear, selectedMonth, day);

            if (startDate && dayDate.getTime() === startDate.getTime()) {
                dayElement.classList.add('selected');
            }
            if (endDate && dayDate.getTime() === endDate.getTime()) {
                dayElement.classList.add('selected');
            }
            if (startDate && endDate && dayDate > startDate && dayDate < endDate) {
                dayElement.classList.add('range');
            }

            dayElement.addEventListener('click', () => handleDateClick(dayDate));
            daysElement.appendChild(dayElement);
        }
    }

    function handleDateClick(dayDate) {
        if (!startDate || (startDate && endDate)) {
            startDate = dayDate;
            endDate = null;
        } else if (dayDate < startDate) {
            startDate = dayDate;
        } else {
            const differenceInDays = (dayDate - startDate) / (1000 * 60 * 60 * 24) + 1;
            if (differenceInDays <= 10) {
                endDate = dayDate;
            } else {
                alert('최대 10일까지만 선택할 수 있습니다.');
                return;
            }
        }
        renderCalendar();
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
        scrollBar: true,
        // normalScrollElements: '.sec4, .footer',
        // 높이값이 풀페이지가 아닌 경우 풀페이지 상단으로 올라가는 것 막아주기
        fitToSection: false,
        responsiveWidth: 1000,
        scrollingSpeed: 600,

    });
    // script.js
    let selectedRegion = null; // 선택된 지역을 저장하는 전역 변수

    // 지역 선택 시 처리하는 함수
    const buttons = document.querySelectorAll('.con');
    buttons.forEach(button => {
        button.addEventListener('click', function () {
            selectedRegion = this.getAttribute('data-region'); // 선택된 지역 저장
            buttons.forEach(btn => {
                btn.classList.remove('active'); // 모든 버튼에서 active 제거
            });
            this.classList.add('active'); // 클릭한 버튼에 active 추가
        });
    });
    // 선택된 지역을 외부에서 접근 가능하도록 내보내기
    window.selectedRegion = () => selectedRegion;

    // section4 
    const bttn1 = document.querySelectorAll(`.img-box1`);
    let selectedCount = 0;

    for (let button of bttn1) {
        button.addEventListener(`click`, function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                selectedCount--;
            } else if (selectedCount < 2) {
                this.classList.add('on');
                selectedCount++;
            }
        });
    }
    // section5 bottom-box
    const bttn2 = document.querySelectorAll(`.img-box2`);
    for (let button of bttn2) {
        button.addEventListener(`click`, function () {
            this.classList.add(`on`);
            for (let btn of bttn2) {
                if (btn !== this) {
                    btn.classList.remove(`on`);
                }
            }
        });
    }
    // section5 top-box
    const bttn4 = document.querySelectorAll(`.sec-5-box`);
    let selectedItems = 0;  // 카운트 변수명 변경

    for (let button of bttn4) {
        button.addEventListener(`click`, function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                selectedItems--;  // 선택 해제 시 카운트 감소
            } else if (selectedItems < 2) {
                this.classList.add('on');
                selectedItems++;  // 새로 선택할 때 카운트 증가
            }
        });
    }
    // section6 box1
    const bttn3 = document.querySelectorAll(`.sec-6-box1`);
    let selectedcount1 = 0;  // 카운트 변수명 변경

    for (let button of bttn3) {
        button.addEventListener(`click`, function () {
            if (this.classList.contains('on')) {
                this.classList.remove('on');
                selectedcount1--;  // 선택 해제 시 카운트 감소
            } else if (selectedcount1 < 2) {
                this.classList.add('on');
                selectedcount1++;  // 새로 선택할 때 카운트 증가
            }
        });
    }
    // section6 box2
    const bttn5 = document.querySelectorAll(`.sec-6-box2`);
    for (let button of bttn5) {
        button.addEventListener(`click`, function () {
            this.classList.add(`on`);
            for (let btn of bttn5) {
                if (btn !== this) {
                    btn.classList.remove(`on`);
                }
            }
        });
    }
    // sectuin7 
    const bttn6 = document.querySelectorAll(`.sec-7-box`);
    for (let button of bttn6) {
        button.addEventListener(`click`, function () {
            this.classList.add(`on`);
            for (let btn of bttn6) {
                if (btn !== this) {
                    btn.classList.remove(`on`);
                }
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