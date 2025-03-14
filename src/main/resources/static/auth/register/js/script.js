document.addEventListener("DOMContentLoaded", function () {


  const opaButton = document.querySelector('.opa')
  const openButton = document.querySelector('.emailbutton')

  openButton.addEventListener('click', function () {
    opaButton.classList.toggle('active');




  });

  let error = /*[[${error}]]*/ null;
  if (error && error === 'mid') {
    alert("동일한 MID를 가진 계정이 존재합니다.");
  }

});
