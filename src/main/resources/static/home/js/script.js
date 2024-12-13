document.addEventListener('DOMContentLoaded', function () {
  // section4 부분 
  const box = document.querySelectorAll(`.box`);

  for (const boxSelect of box) {
    boxSelect.addEventListener(`click`, function () {
      this.classList.add(`on`);

      for (const siblings of box) {
        if (siblings !== this) {
          siblings.classList.remove(`on`);
        }
      }
    });
  }
  // 1000px 이하에서 항상 on클래스 붙어있게 작성
  function toggleOnClass() {
    const windowWidth = window.innerWidth;
    const boxes = document.querySelectorAll('.box');

    boxes.forEach(box => {
      if (windowWidth <= 1000) {
        box.classList.add('on');
      } else {
        if (!box.classList.contains('on')) {
          box.classList.remove('on');
        }
      }
    });
  }
  // 초기 호출
  toggleOnClass();

  // 윈도우가 리사이즈 될 때마다 호출
  window.addEventListener('resize', toggleOnClass);

  // 모달2
  const mapLinks = document.querySelectorAll("a[data-alt]"); // data-alt 속성이 있는 a 태그 선택

  mapLinks.forEach(link => {
    link.addEventListener("mouseenter", function () {
      const altId = this.getAttribute("data-alt");
      const altElement = document.getElementById(altId); // altId에 해당하는 요소 선택
      if (altElement) {
        altElement.style.display = "block"; // 요소 보이기
      }
    });

    link.addEventListener("mouseleave", function () {
      const altId = this.getAttribute("data-alt");
      const altElement = document.getElementById(altId);
      if (altElement) {
        altElement.style.display = "none"; // 요소 숨기기
      }
    });
  });




  // 모달1
  const openModalButtons = document.querySelectorAll(".content");
  const closeButtons = document.querySelectorAll(".close");

  // 모달 열기 버튼 클릭 이벤트 추가
  openModalButtons.forEach(button => {
    button.addEventListener("click", function () {
      const modalId = this.getAttribute("data-modal");
      const modal = document.getElementById(modalId);
      modal.style.display = "block";
    });
  });

  // 모달 닫기 버튼 클릭 이벤트 추가
  closeButtons.forEach(button => {
    button.addEventListener("click", function () {
      const modal = this.closest(".modal");
      modal.style.display = "none";
    });
  });

  // 모달 외부 클릭 시 모달 닫기
  window.addEventListener("click", function (event) {
    if (event.target.classList.contains("modal")) {
      event.target.style.display = "none";
    }
  });

  // aos 링크연결
  AOS.init();
  function initAOS() {
    const windowWidth = window.innerWidth;

    if (windowWidth > 1000) {
      AOS.init({
        duration: 400,
        easing: 'ease-in-out',
      });
    } else {
      AOS.refresh();
    }
  }

  // 초기화 함수 호출
  initAOS();

  // 윈도우 리사이즈 이벤트 리스너 추가
  window.addEventListener('resize', initAOS);

  // swiper banner
  var swiper = new Swiper(".mySwiper", {
    loop: true, // 슬라이드를 루프 시키는 옵션
    pagination: {
      el: ".swiper-pagination",
    },
    navigation: {
      nextEl: ".swiper-button-next",
      prevEl: ".swiper-button-prev",
    },
  });


  // section4 스와이퍼
  let swiper2 = undefined;
  let swiper3 = undefined;

  function initSwiper2() {
    const windowWidth = window.innerWidth;

    // 화면 너비가 1000px 이하일 때 Swiper2가 없으면 Swiper2를 초기화
    if (windowWidth <= 1000 && swiper2 == undefined) {
      swiper2 = new Swiper(".mySwiper2", {
        pagination: {
          el: ".swiper-pagination2",
        },
        navigation: {
          nextEl: ".swiper-button-next",
          prevEl: ".swiper-button-prev",
        },
        slidesPerView: 1,
        spaceBetween: 10,
        slidesPerGroup: 1,
      });
    }
    // 화면 너비가 1000px 이상일 때 Swiper2가 있으면 Swiper2를 제거
    else if (windowWidth > 1000 && swiper2 != undefined) {
      swiper2.destroy();
      swiper2 = undefined;
    }
  }

  function initSwiper3() {
    // 윈도우 너비값을 변수에 저장
    const windowWidth = window.innerWidth;

    if (windowWidth <= 1000 && swiper3 == undefined) {
      swiper3 = new Swiper(".mySwiper3", {
        pagination: {
          el: ".swiper-pagination3",
        },
        navigation: {
          nextEl: ".swiper-button-next",
          prevEl: ".swiper-button-prev",
        },
        slidesPerView: 1,
        spaceBetween: 10,
        slidesPerGroup: 1,
      });
    }
    else if (windowWidth > 1000 && swiper3 != undefined) {
      swiper3.destroy();
      swiper3 = undefined;
    }
  }

  initSwiper2();
  initSwiper3();


  window.addEventListener('resize', () => {
    initSwiper2();
    initSwiper3();
  });





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


// window.location.origin을 사용해 현재 도메인에 맞는 baseUrl 설정
var baseUrl = window.location.origin;  // http://localhost:8080 또는 https://yourdomain.com

// 모든 .subpage 요소에 대해 href 값 동적으로 설정
document.querySelectorAll('.subpage').forEach(el => {
  el.href = `${baseUrl}/travel-planner/travel-planner.html`;
});
