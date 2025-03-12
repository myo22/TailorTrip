console.log(window.innerWidth, window.innerHeight);

function loginData(event) {

    event.preventDefault();

    // 사용자 입력값 수집
    const mid = document.querySelector('input[id="email"]').value;
    const mpw = document.querySelector('input[id="password"]').value;

    console.log(mid);
    console.log(mpw);

    const data = {mid, mpw}; // axios 전송 데이터 설정

    // 현재 URL에서 쿼리 파라미터 추출
    const params = new URLSearchParams(window.location.search);
    const redirectURL = params.get('redirect') || '/travel-planner/index.html'; // 기본값으로 '/' 설정

    // 토큰 요청
    axios.post("http://localhost:8080/generateToken", data)
        .then(res => {
            const accessToken = res.data.accessToken;
            const refreshToken = res.data.refreshToken;

            // 로컬 스토리지에 토큰 저장
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);

            alert("로그인 성공!");

            // 저장된 요청 복원
            const pendingRequest = JSON.parse(localStorage.getItem('pendingRequest'));
            if (pendingRequest) {
                // POST 요청 재실행
                fetch(pendingRequest.url, {
                    method: pendingRequest.method,
                    headers: {
                        ...pendingRequest.headers,
                        Authorization: `Bearer ${accessToken}`,
                    },
                    body: JSON.stringify(pendingRequest.body),
                })
                    .then(response => response.json())
                    .then(data => {
                        alert('일정이 저장되었습니다.');
                    })
                    .catch(error => {
                        console.error('Error:', error);
                    })
                    .finally(() => {
                        // 요청 완료 후 pendingRequest 삭제
                        localStorage.removeItem('pendingRequest');
                    });
            }

            window.location.href = redirectURL; // 원하는 리디렉션 경로
        })
        .catch(error => {
            console.error("로그인 실패:", error);
            alert("로그인 실패! 다시 시도하세요.");
        });
}

document.getElementById("loginForm").addEventListener("submit", loginData)